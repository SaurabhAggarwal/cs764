package algos;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import model.Algorithm;
import model.Dataset;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import util.FileReader;
import util.InputReader;
import util.MiningUtils;
import util.OutputUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * Implements AIS algorithm for frequent itemset mining.
 * 
 * @author shishir
 *
 */
public class AIS {

	public static void main(String[] args)
	{
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_TWO_FIVE_PERCENT);
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
		System.out.println("AIS: " + dataset + ", " + minSup);

		int expRunTime = generateLargeItemSets(dataset, minSup);
		System.out.println("Time taken = " + expRunTime + " seconds.\n");
		
		return expRunTime;
	}

	/*
	 * Generates the large itemsets for each pass usig AIS algorithm and returns the
	 * total time taken for the experiment.
	 * 
	 * @param dataset      - For which dataset, this experiment has to be run.
	 * @param minSup       - Minimum desired support threshold
	 * 
	 * @returns Time taken to generate the large itemsets
	 */
	private static int generateLargeItemSets(Dataset dataset, MinSup minSup)
	{
		long expStartTime = System.currentTimeMillis();
		long fileWriteTime = 0;
		List<Integer> candidateItemsetsCount = Lists.newArrayList();

		File largeItemsetsFile = OutputUtils.getOutputFile("LARGEITEMSETS", Algorithm.AIS, dataset, minSup);
		File candItemsetsCountFile = OutputUtils.getOutputFile("CANDITEMSETSCOUNT", Algorithm.AIS, dataset, minSup);

		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		InputReader reader = getDatasetReader(dataset);
		List<ItemSet> largeItemsets = MiningUtils.getInitialLargeItemsets(reader, minSupportCount);

		int currItemsetSize = 1;
	
		while(!largeItemsets.isEmpty()) {
			// Write large itemsets to file
			try {
				long fileWriteStartTime = System.currentTimeMillis();
				Collections.sort(largeItemsets);
				OutputUtils.writeLargeItemsetsToFile(largeItemsetsFile, currItemsetSize, largeItemsets);
				fileWriteTime += System.currentTimeMillis() - fileWriteStartTime;
			} catch (IOException e) {
				System.err.println("Failed to write to file. Reason : " + e);
			}

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
								candidate.incrementSupportCount();								
							}
						}
						else {
							ItemSet newItemset = new ItemSet(ext.getItems(), 1);
							candidateKItemSetMap.put(hashKey, newItemset);
						}
					}
				}
				
			}
			
			// TODO : The support count for every itemset is coming one less than the actual value.

			// Only retain the candidate sets which have the minimum support
			largeItemsets = Lists.newArrayList();
			for(ItemSet candidate : candidateKItemSetMap.values()) {
				if(MiningUtils.hasMinSupport(candidate, minSupportCount)) {
					largeItemsets.add(candidate);
				}
			}
		
			candidateItemsetsCount.add(candidateKItemSetMap.values().size());
		}

		try {
			long fileWriteStartTime = System.currentTimeMillis();
			OutputUtils.writeCandidateCountToFile(candItemsetsCountFile, candidateItemsetsCount);
			fileWriteTime += System.currentTimeMillis() - fileWriteStartTime;
		} catch (IOException e) {
			System.err.println("Failed to write candidate itemset count to file. Reason : " + e);
		}

		long expEndTime = System.currentTimeMillis();
		int expTime = (int)(expEndTime - expStartTime - fileWriteTime)/1000;

		return expTime;
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
