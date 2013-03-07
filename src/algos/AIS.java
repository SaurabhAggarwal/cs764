package algos;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Algorithm;
import model.Dataset;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import util.FileReader;
import util.InputReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Implements AIS algorithm for frequent itemset mining.
 * 
 * @author shishir
 *
 */
public class AIS {

	public static void main(String[] args) {
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT, new FileReader());
	}
	
	/*
	 * Run AIS algorithm for the specified experiment parameters
	 */
	private static void runExperiment(Dataset dataset, MinSup minSup, InputReader reader)
	{
		long expStartTime = System.currentTimeMillis();
		
		long datasetReadStart = System.currentTimeMillis();
		List<Transaction> transactions = reader.getTransactions(Dataset.T5_I2_D100K, Algorithm.AIS);
		long datasetReadEnd = System.currentTimeMillis();

		long largeItemSetGenStart = System.currentTimeMillis();
		Map<Integer, List<ItemSet>> largeItemSetsMap = getLargeItemSetsMap(transactions, minSup);
		long largeItemSetGenEnd = System.currentTimeMillis();
		
		System.out.println("Size " + largeItemSetsMap.size());
		for(Map.Entry<Integer, List<ItemSet>> entry : largeItemSetsMap.entrySet()) {
			System.out.println("Itemsets for size " + entry.getKey() + " are : " + Arrays.toString(entry.getValue().toArray()));
		}
		
		long expEndTime = System.currentTimeMillis();
		System.out.println(
				" Time taken for experiment " + dataset.toString() + " with support " + minSup.toString() + 
				" % support is " + (expEndTime - expStartTime)/1000 + " s --> " +
				" {Dataset Read : " + (datasetReadEnd - datasetReadStart)/1000 + " s } , " +
				" {Large itemset generation : " + (largeItemSetGenEnd - largeItemSetGenStart)/1000 + " s } , "
		); 

	}

	/*
	 * Generates the list of large itemsets for each pass for these set of transactions using AIS algorithm.
	 */
	private static Map<Integer, List<ItemSet>> getLargeItemSetsMap(List<Transaction> transactions, MinSup minSup)
	{
		Map<Integer, List<ItemSet>> largeItemSetsMap = Maps.newHashMap();
		
		List<ItemSet> frontierSets = null;
		List<ItemSet> largeItemSets = null;
		List<ItemSet> candidateSets = null;
		Set<ItemSet> expectedToBeSmallSets = null;
		int currentPass=0;
		boolean isFirstPass = true;

		int datasetSize = transactions.size();
		int minSupportCount = (int)(minSup.getMinSupPercentage() * datasetSize)/100;
		int minSupport = minSupportCount/datasetSize;
		
		Map<Integer, Integer> itemFrequencyMap = getItemFrequencyMap(transactions);
		// Keep iterating till the frontier set becomes empty
		do {
			++currentPass;
			candidateSets = Lists.newArrayList();
			largeItemSets = Lists.newArrayList();
			expectedToBeSmallSets = Sets.newHashSet();
			
			// Make pass over all the transactions
			int currTransactionNum = 0;
			for(Transaction t : transactions) {
				++currTransactionNum;
				if(isFirstPass) {
					List<ItemSet> extensionSets = 
						getExtensionItemsets(t, null, itemFrequencyMap, currTransactionNum, datasetSize
					);
					genSupportForCandidateSets(candidateSets, extensionSets);
				}
				else {
					for(ItemSet f : frontierSets) {
						if(isItemSetInTransaction(t, f)) {
							List<ItemSet> extensionSets = 
								getExtensionItemsets(t, f, itemFrequencyMap, currTransactionNum, datasetSize
							);
							// Determine which of these extension sets is supposed to be small
							for(ItemSet i : extensionSets) {
								if(i.getExpectedSupportCount() < minSupport) {
									expectedToBeSmallSets.add(i);
								}
							}

							genSupportForCandidateSets(candidateSets, extensionSets);
							
						}
					}
				}
			}
			
			if(isFirstPass) {
				isFirstPass = false;
			}
			
			// Consolidate
			System.out.println("Found " + candidateSets.size() + " candidate sets for pass " + currentPass);
			frontierSets = Lists.newArrayList();
			for(ItemSet itemset : candidateSets) {
				boolean hasMinSupport = hasMinSupport(itemset, datasetSize, minSupportCount); 
				if(hasMinSupport) {
					largeItemSets.add(itemset);
					
					// If the generated candidate itemset was initially predicted to be small but turned out to 
					// be large, then add it to the frontier set.
					if(expectedToBeSmallSets.contains(itemset)) {
						frontierSets.add(itemset);	
					}
				}
			}
			
			largeItemSetsMap.put(currentPass, largeItemSets);
		} 
		while (!(frontierSets == null || frontierSets.isEmpty()));
		
		return largeItemSetsMap;
	}
	
	/*
	 * Determines if this itemset has the minimum support as required by the frequent itemset mining algorithm.
	 * 
	 * support = (support count for itemset)/ total dataset size
	 */
	private static boolean hasMinSupport(ItemSet itemset, int datasetSize, int minSupport) {
		boolean hasMinSupport = false;
		if((itemset.getSupportCount()/datasetSize) >= (minSupport/datasetSize)) {
			hasMinSupport = true;
		}

		return hasMinSupport;
	}

	/*
	 * Generates the possible (K+1) candidate sets from the frontier sets of pass (K).
	 */
	public static List<ItemSet> getExtensionItemsets(
			Transaction transaction, ItemSet frontierSet, Map<Integer, Integer> itemFrequencyMap,
			int currTransactionCount, int datasetSize)
	{
		List<ItemSet> extItemSets = Lists.newArrayList();
		
		List<Integer> baseItems = Lists.newArrayList(); 
		int maxItemId = -1;
		// Frontier Set would be null for the first pass
		if(frontierSet != null) {
			baseItems = frontierSet.getItems();
			maxItemId = baseItems.get(baseItems.size() - 1);
		}
		
		int[] transactionItems = transaction.getItemVector();
		for(int i=maxItemId+1; i < transactionItems.length; i++) {
			if(transactionItems[i] == 1) {
				List<Integer> newItems = Lists.newArrayList(baseItems);
				int itemId = i;
				newItems.add(itemId);
				
				/*
				 * Expected support for a new candidate itemset is product of relative frequency
				 * of new item with the actual support for base frontier itemset in the remaining
				 * portion of the database.
				 */
				int frontierSupport = 1;
				if(frontierSet != null) {
					frontierSupport = (frontierSet.getSupportCount() - currTransactionCount)/datasetSize;	
				}
				int newItemRelFrequency = itemFrequencyMap.get(itemId)/datasetSize;
				int expectedSupport = frontierSupport*newItemRelFrequency;
				
				ItemSet newItemset = new ItemSet(newItems, 0);
				newItemset.setExpectedSupportCount(expectedSupport);
				extItemSets.add(newItemset);
			}
		}

		return extItemSets;
	}

	/*
	 * Adds the extension sets for the current transaction to candidate sets and generates support for it. 
	 */
	public static void genSupportForCandidateSets(List<ItemSet> candidateSets, List<ItemSet> extensionSets)
	{
		for(ItemSet ext : extensionSets) {
			if(candidateSets.contains(ext)) {
				ext.setSupportCount(ext.getSupportCount() + 1);
			}
			else {
				ext.setSupportCount(1);
				candidateSets.add(ext);
			}
		}
	}
	
	// Checks if the itemset is contained in the transaction
	public static boolean isItemSetInTransaction(Transaction transaction, ItemSet itemset)
	{
		boolean isItemSetInTransaction = true;
		
		List<Integer> items = itemset.getItems();
		int[] itemVector = transaction.getItemVector();
		for(Integer item : items) {
			if(itemVector[item] != 1) {
				isItemSetInTransaction = false;
				break;
			}
		}

		return isItemSetInTransaction;
	}
	
	/*
	 * Generates the map of all items and their counts in the dataset. This is important for calculating
	 * the relative frequency of individual items during frontier set generation. 
	 */
	public static Map<Integer, Integer> getItemFrequencyMap(List<Transaction> transactions)
	{
		Map<Integer, Integer> itemFrequencyMap = Maps.newHashMap();
		for(Transaction t : transactions) {
			List<Integer> items = t.getItems();
			for(Integer item : items) {
				int count=1;
				if(itemFrequencyMap.containsKey(item)) {
					count = count + itemFrequencyMap.get(item);
				}
				itemFrequencyMap.put(item, count);
			}
		}

		return itemFrequencyMap;
	}
}
