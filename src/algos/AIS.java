package algos;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import util.DBReader;
import util.FileReader;
import util.InputReader;
import util.MiningUtils;
import util.OutputUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implements AIS algorithm for frequent itemset mining.
 * 
 * @author shishir
 *
 */
public class AIS {

	public static void main(String[] args)
	{
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
	}

	/*
	 * Run AIS algorithm for the specified experiment parameters
	 * 
	 * @param dataset - Name of the dataset on which the experiment is to be run.
	 * @param minSup - Minimum support threshold to classify an itemset as frequent or large.
	 * 
	 * @return Time taken to finish this experiment.
	 */
	public static int runExperiment(Dataset dataset, MinSup minSup)
	{
		long expStartTime = System.currentTimeMillis();
		
		long largeItemSetGenStart = System.currentTimeMillis();
		Map<Integer, Integer> candidateCountMap = Maps.newTreeMap();
		Map<Integer, List<ItemSet>> largeItemSetsMap = 
				getLargeItemSetsMap(dataset, minSup, candidateCountMap);
		long largeItemSetGenEnd = System.currentTimeMillis();
		
		for(Map.Entry<Integer, List<ItemSet>> entry : largeItemSetsMap.entrySet()) {
			if(entry.getValue().isEmpty()) {
				continue;
			}
		}
		
		long expEndTime = System.currentTimeMillis();
		System.out.println(
				"Time taken for experiment " + Algorithm.AIS.toString() + "/" + dataset.toString() + 
				" with support " + minSup.toString() + " % support is " + 
				(expEndTime - expStartTime)/1000 + " s --> " + " {Large itemset generation : " + 
				(largeItemSetGenEnd - largeItemSetGenStart)/1000 + " s } "
		);
		
		try {
			OutputUtils.writeOutputToFile(Algorithm.AIS, dataset, minSup, largeItemSetsMap);
		} catch (IOException e) {
			System.err.println("Failed to write output to file. Reason : " + e);
		}

		return (int)(expEndTime - expStartTime)/1000;
	}

	/*
	 * Returns a map of large itemsets for each pass for the set of transactions.
	 * 
	 * @param dataset      - For which dataset, this experiment has to be run.
	 * @param minSup       - Minimum desired support threshold
	 * @param candidateCountMap - Map of pass number and the number of candidate sets generated in 
	 * 						this pass.
	 * 
	 * @returns Map of large itemsets for each pass
	 */
	private static Map<Integer, List<ItemSet>> getLargeItemSetsMap(
			Dataset dataset, MinSup minSup, Map<Integer, Integer> candidateCountMap)
	{
		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		
		InputReader reader = getDatasetReader(dataset);
		List<ItemSet> largeItemsets = 
				MiningUtils.getInitialLargeItemsets(reader, minSupportCount);
		int currItemsetSize = 1;
		Map<Integer, List<ItemSet>> largeItemSetsMap = Maps.newTreeMap();
		largeItemSetsMap.put(currItemsetSize, largeItemsets);
	
		while(!largeItemsets.isEmpty()) {
			++currItemsetSize; // Pass number = Size of large items in this pass
			
			/*
			 * We needed a mechanism to quickly find a candidate itemset in the list of candidate
			 * itemsets. The initial attempt of sorting the candidate itemsets and subsequent binary
			 * search didn't yield good performance. So, here I have tried to create a hashmap whose
			 * key is the hashcode of the candidate itemset and the value can be one or more itemsets
			 * which have the same hashcode value. In case of hashcode collision, we need to compare
			 * all the objects in the collision list to determine the actual search object.
			 */
			ListMultimap<Integer, ItemSet> candidateKItemSetMap = ArrayListMultimap.create();

			reader = getDatasetReader(dataset);
			while(reader.hasNextTransaction()) {
				Transaction txn = reader.getNextTransaction();
				// Determine which large items of last pass are present in the current transaction.
				List<ItemSet> largeItemsetsInTxn = 
						MiningUtils.getSubsetItemsets(largeItemsets, txn, currItemsetSize-1);
				
				for(ItemSet largeItemSet : largeItemsetsInTxn) {
					// Generate 1-extension candidate sets from the large itemsets of prev pass
					List<ItemSet> extensionItemsets = getExtensionItemsets(txn, largeItemSet);
					for(ItemSet ext : extensionItemsets) {
						int hashKey = ext.hashCode();
						if(candidateKItemSetMap.containsKey(hashKey)) {
							List<ItemSet> candidateSets = candidateKItemSetMap.get(hashKey);
							ItemSet candidate = null;
							for(ItemSet currItemset : candidateSets) {
								if(currItemset.equals(ext)) {
									candidate = currItemset;
									break;
								}
							}

							/*
							 * This case though rare but still can happen. Consider a scenario, where
							 * an object that came earlier had hashcode h1 and was inserted in the 
							 * map. Now, when we see another object with the same hashcode h1, which
							 * has not been inserted into the map yet, we would end up with 'candidate'
							 * as NULL and thus lead to NPE. In such cases, assume that this keyed
							 * object is not present in the map and just insert this object in the
							 * map and continue. 
							 */
							if(candidate == null) {
								ItemSet newItemset = new ItemSet(ext.getItems(), 1);
								candidateKItemSetMap.put(hashKey, newItemset);
							}
							else {
								candidate.setSupportCount(candidate.getSupportCount()+1);								
							}
						}
						else {
							ItemSet newItemset = new ItemSet(ext.getItems(), 1);
							candidateKItemSetMap.put(hashKey, newItemset);
						}
					}
				}
				
			}
			
			// Only retain the candidate sets which have the minimum support
			largeItemsets = Lists.newArrayList();
			for(ItemSet candidate : candidateKItemSetMap.values()) {
				if(MiningUtils.hasMinSupport(candidate, minSupportCount)) {
					largeItemsets.add(candidate);
				}
			}
			
			largeItemSetsMap.put(currItemsetSize, largeItemsets);
			candidateCountMap.put(currItemsetSize, candidateKItemSetMap.values().size());
		}
		
		return largeItemSetsMap;
	}
	
	/* Returns set of 1-extension itemsets corresponding the input large itemset and
	 * a transaction.
	 * 
	 * @param txn - The transaction which has to be used to generate 1-extension itemsets.
	 * @param largeItemset - Large itemset which would serve as the seed for generating other itemsets.
	 * 
	 * @returns List of 1-extension candidate itemsets for the current pass.
	 */
	private static List<ItemSet> getExtensionItemsets(Transaction txn, ItemSet largeItemset)
	{
		List<ItemSet> extensionItemsets = Lists.newArrayList();
		
		List<Integer> largeItems = largeItemset.getItems();
		List<Integer> txnItems = txn.getItems();
		int itemsetSize = largeItems.size();
		int largestItemId = largeItems.get(itemsetSize-1);
		
		// Generate all the possible 1-extension candidate sets. Since the items in the transaction
		// are lexically ordered, we only need to consider the items greater than the maximum
		// item id in the large itemset.
		for(int i=0; i < txnItems.size(); i++) {
			int currItemId = txnItems.get(i);
			if(currItemId <= largestItemId) {
				continue;
			}
			
			List<Integer> newItems = Lists.newArrayList(largeItems);
			newItems.add(currItemId);
			ItemSet newItemset = new ItemSet(newItems, 0);
			extensionItemsets.add(newItemset);
		}

		return extensionItemsets;
	}

	/*
	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
	 */
	private static InputReader getDatasetReader(Dataset dataset)
	{
		return new FileReader(dataset, Algorithm.AIS);
	}
}
