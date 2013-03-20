package algos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import model.Dataset;
import model.ItemSet;
import model.MinSup;
import util.DBUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implements SETM (Set-Oriented Mining) algorithm for frequent itemset mining. Used MySQL database
 * to save the intermediate relations and SQL processing for implementing the various components
 * of SETM algorithm like sorting and merge-join operations.
 * 
 * @author shishir
 *
 */
public class SETM {

	public static void main(String[] args)
	{
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
	}

	/*
	 * Run SETM algorithm for the specified experiment parameters
	 * 
	 * @param dataset - Name of the dataset on which the experiment is to be run.
	 * @param minSup - Minimum support threshold to classify an itemset as frequent or large.
	 * 
	 * @return Time taken to finish this experiment.
	 */
	public static int runExperiment(Dataset dataset, MinSup minSup) {
		long expStartTime = System.currentTimeMillis();
		
		long largeItemSetGenStart = System.currentTimeMillis();
		Map<Integer, List<ItemSet>> largeItemSetsMap = getLargeItemSetsMap(dataset, minSup);
		long largeItemSetGenEnd = System.currentTimeMillis();
		
		for(Map.Entry<Integer, List<ItemSet>> entry : largeItemSetsMap.entrySet()) {
			if(entry.getValue().isEmpty()) {
				continue;
			}

			System.out.println("Itemsets for size " + entry.getKey() + " are : " + 
								Arrays.toString(entry.getValue().toArray()));
		}
		
		long expEndTime = System.currentTimeMillis();
		System.out.println(
				"Time taken for experiment " + dataset.toString() + " with support " + minSup.toString() + 
				" % support is " + (expEndTime - expStartTime)/1000 + " s --> " +
				" {Large itemset generation : " + (largeItemSetGenEnd - largeItemSetGenStart)/1000 + " s } "
		);
		
		return (int)(expEndTime - expStartTime)/1000;

	}
	
	/*
	 * Returns a map of large itemsets for each pass for the set of transactions.
	 * 
	 * @param dataset      - For which dataset, this experiment has to be run.
	 * @param minSup       - Minimum desired support threshold
	 * 
	 * @returns Map of large itemsets for each pass
	 */
	private static Map<Integer, List<ItemSet>> getLargeItemSetsMap(Dataset dataset, MinSup minSup)
	{
		Map<Integer, List<ItemSet>> largeItemsetsMap = Maps.newTreeMap();
		List<ItemSet> largeKItemSets = getInitialLargeItemsets(dataset, minSup);

		int itemsetSize = 1;
		largeItemsetsMap.put(itemsetSize, largeKItemSets);

		// Keep iterating till the large itemsets generated in the previous pass is empty.
		while(!(largeKItemSets == null || largeKItemSets.isEmpty())) {
			++itemsetSize;
			largeKItemSets = getLargeKItemSets(dataset, minSup, itemsetSize);
			largeItemsetsMap.put(itemsetSize, largeKItemSets);
		}
		
		// Since we would be using the same tables for staging again, drop them. We can also append
		// a unique identifier at the end of temporary tables to avoid dropping the tables but then
		// the code would need to do extra book-keeping to remember the names of the tables. Decided
		// against it but might revisit if this looks like a potential problem.
		boolean areTempTablesDropped = DBUtils.dropTempTables(itemsetSize);
		System.out.println("Temp tables dropped ? " + areTempTablesDropped);

		return largeItemsetsMap;
	}
	
	/*
	 * Fetches the initial large 1-itemsets. Internally, also creates large_bar_1 table containing
	 * <TID, Itemset> such that itemsets are of length 1. 
	 */
	private static List<ItemSet> getInitialLargeItemsets(Dataset dataset, MinSup minSup)
	{
		DBUtils.executeInsertQuery(getLarge1ItemsetsBarQuery(dataset.getDatasetDBTable()));
		return getLargeItemsets(1);
	}

	/*
	 * Fetches large itemsets of size K from the database. It internally uses SQL sorting and
	 * merge-join operations to generate the intermediate candidate sets and use it to generate
	 * the corresponding large itemsets for the current pass.
	 */
	private static List<ItemSet> getLargeKItemSets(Dataset dataset, MinSup minSup, int itemsetSize)
	{
		String datasetTblName = dataset.getDatasetDBTable();
		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		
		DBUtils.executeInsertQuery(getCandidateItemsetsExtBarQuery(datasetTblName, itemsetSize));
		DBUtils.executeInsertQuery(getCandidateSupportQuery(itemsetSize, minSupportCount));
		DBUtils.executeInsertQuery(getLargeItemsetsBarQuery(itemsetSize));

		return getLargeItemsets(itemsetSize);
	}

	/*
	 * Gets large itemsets of specific itemset size.
	 */
	private static List<ItemSet> getLargeItemsets(int itemsetSize)
	{
		List<ItemSet> largeItemSets = Lists.newArrayList();
		ResultSet results = DBUtils.executeSelectQuery(getLargeItemsetsQuery(itemsetSize));
		try {
			while(results.next()) {
				List<Integer> items = Lists.newArrayList();
				for(int itemIndex=1; itemIndex <= itemsetSize; itemIndex++) {
					items.add(results.getInt(itemIndex));
				}
				
				int supportCount = results.getInt(itemsetSize+1);
				largeItemSets.add(new ItemSet(items, supportCount));
			}
		} 
		catch (SQLException e) {
			System.err.println("Failed to read the SQL query results. Reason : " + e);
		}

		return largeItemSets;
	}
	
	// ------------------------ Intermediate query generators -----------------------------
	/*
	 * Generates dynamic query to create the table that would contain all the 1-extension itemsets
	 * along with their TID's using the large itemsets of previous pass.
	 */
	private static String getCandidateItemsetsExtBarQuery(String datasetTblName, int itemsetSize)
	{
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE mining_datasets.candidate_ext_bar_").append(itemsetSize).append(" ( \n");
		query.append("SELECT p.tid, ").append(getItemsetSelectClause(itemsetSize-1)).append(", q.itemID AS item").append(itemsetSize).append("\n");
		query.append("FROM mining_datasets.large_bar_").append(itemsetSize-1).append(" p JOIN ")
		     .append(datasetTblName).append(" q").append("\n");
		query.append("ON (q.tid = p.tid) WHERE q.itemID > ").append("p.item")
			 .append(itemsetSize-1).append(" ) \n");

		return query.toString();
	}
	
	/*
	 * Generates the support count for all the itemsets in the candidate extension bar itemsets
	 * generated earlier.
	 */
	private static String getCandidateSupportQuery(int itemsetSize, int minSupCount)
	{
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE mining_datasets.candidate_support_").append(itemsetSize).append(" ( \n");
		query.append("SELECT ").append(getItemsetSelectClause(itemsetSize))
			 .append(", COUNT(1) AS count").append("\n");
		query.append("FROM mining_datasets.candidate_ext_bar_").append(itemsetSize).append(" p \n");
		query.append("GROUP BY ").append(getItemsetSelectClause(itemsetSize)).append("\n");
		query.append("HAVING COUNT(1) >= ").append(minSupCount).append(" ) \n");
		
		return query.toString();
		
	}
	
	/*
	 * Generates the large itemsets for the current pass and stores them in <TID, Itemset> format
	 * to be used in subsequent iterations.
	 */
	private static String getLargeItemsetsBarQuery(int itemsetSize)
	{
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE mining_datasets.large_bar_").append(itemsetSize).append(" ( \n");
		query.append("SELECT p.tid, ").append(getItemsetSelectClause(itemsetSize)).append("\n");
		query.append("FROM mining_datasets.candidate_ext_bar_").append(itemsetSize).append(" p, mining_datasets.candidate_support_")
			 .append(itemsetSize).append(" q").append("\n");
		query.append("WHERE ");
		for(int i=1; i <=(itemsetSize-1); i++) {
			query.append("p.item").append(i).append(" = q.item").append(i).append(" AND ");
		}
		query.append("p.item").append(itemsetSize).append(" = q.item").append(itemsetSize).append("\n");
		query.append("ORDER BY p.tid, ").append(getItemsetSelectClause(itemsetSize)).append(" ) \n");

		return query.toString();
	}
	
	/*
	 * Generates the query to generate the L1_bar table from the dataset table. This can be used
	 * to then generate the L1 large itemsets.
	 */
	private static String getLarge1ItemsetsBarQuery(String datasetTblName)
	{
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE mining_datasets.large_bar_1 (").append("\n");
		query.append("SELECT q.tid, p.item1 FROM ").append(datasetTblName)
		     .append(" q JOIN (SELECT itemID as item1, COUNT(1) AS count FROM ")
		     .append(datasetTblName).append(" GROUP BY itemID HAVING COUNT(1) >= 2 ORDER BY itemID) " +
		     		"AS p ON (q.itemID = p.item1) ORDER BY q.tid, p.item1").append(" )\n");

		return query.toString();
	}

	/*
	 * Fetches the large itemsets of specific itemset size.
	 */
	private static String getLargeItemsetsQuery(int itemsetSize)
	{
		StringBuilder query = new StringBuilder();
		query.append("SELECT ").append(getItemsetSelectClause(itemsetSize)).append(", COUNT(1) AS count ").append("\n");
		query.append("FROM mining_datasets.large_bar_").append(itemsetSize).append(" p ").append("\n");
		query.append("GROUP BY ").append(getItemsetSelectClause(itemsetSize)).append("\n");
		query.append("ORDER BY ").append(getItemsetSelectClause(itemsetSize)).append("\n");

		return query.toString();
	}

	/*
	 * Returns select clause for SQL queries above like p.item1, p.item2 ..
	 */
	private static String getItemsetSelectClause(int itemsetSize)
	{
		StringBuilder query = new StringBuilder();
		for(int i=1; i <= (itemsetSize-1); i++) {
			query.append("p.item").append(i).append(", ");
		}
		query.append("p.item").append(itemsetSize);

		return query.toString();
	}
}
