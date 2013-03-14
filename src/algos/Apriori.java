package algos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.HashTreeNode;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import util.FileReader;
import util.HashTreeUtils;
import util.InputReader;
import util.MiningUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implements the classical Apriori Algorithm for frequent itemset mining.
 * 
 * @author shishir
 *
 */
public class Apriori {

	public static void main(String[] args)
	{
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
	}

	/*
	 * Run Apriori algorithm for the specified experiment parameters
	 */
	private static void runExperiment(Dataset dataset, MinSup minSup)
	{
		long expStartTime = System.currentTimeMillis();
		
		// Store the large itemsets for each level
		Map<Integer, List<ItemSet>> largeItemSetsMap = Maps.newHashMap();
		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		
		long initialLargeSetGenStart = System.currentTimeMillis();
		InputReader reader = getDatasetReader(dataset);
		List<ItemSet> largeItemsets = 
				MiningUtils.getInitialLargeItemsets(reader, minSupportCount);
		long initialLargeSetGenEnd = System.currentTimeMillis();
		
		long largeItemsetGenStart = System.currentTimeMillis();
		int currItemsetSize = 1;
		largeItemSetsMap.put(currItemsetSize, largeItemsets);
		
		/*
		 * Keep iterating till no further large itemsets are being generated
		 */
		long passStart = System.currentTimeMillis();
		long passEnd = System.currentTimeMillis();
		while(!largeItemsets.isEmpty()) {
			passStart = System.currentTimeMillis();
			List<ItemSet> candidateKItemsets = generateCandiateItemsets(largeItemsets, currItemsetSize);
			++currItemsetSize;

			HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(candidateKItemsets, currItemsetSize);
			reader = getDatasetReader(dataset);
			while(reader.hasNextTransaction()) {
				Transaction txn = reader.getNextTransaction();
				List<ItemSet> candidateSetsInTrans = HashTreeUtils.findItemsets(hashTreeRoot, txn, 0);
				for(ItemSet c : candidateSetsInTrans) {
					c.setSupportCount(c.getSupportCount() + 1);
				}
			}

			largeItemsets = Lists.newArrayList();
			for(ItemSet i : candidateKItemsets) {
				if(i.getSupportCount() >= minSupportCount) {
					largeItemsets.add(i);
				}
			}
			
			if(!largeItemsets.isEmpty()) {
				largeItemSetsMap.put(currItemsetSize, largeItemsets);
			}
			
			passEnd = System.currentTimeMillis();
			System.out.println("Time taken for large-itemset generation pass " + currItemsetSize + " is " + 
			                    (passEnd - passStart)/1000 + " s ");
		}
		long largeItemsetGenEnd = System.currentTimeMillis();
		
		for(Map.Entry<Integer, List<ItemSet>> entry : largeItemSetsMap.entrySet()) {
			System.out.println("Itemsets for size " + entry.getKey() + " are : " + Arrays.toString(entry.getValue().toArray()));
		}
		
		long expEndTime = System.currentTimeMillis();
		System.out.println(
				" Time taken for experiment " + dataset.toString() + " with support " + minSup.toString() + 
				" % support is " + (expEndTime - expStartTime)/1000 + " s --> " +
				" {1-Large itemset generation : " + (initialLargeSetGenEnd - initialLargeSetGenStart)/1000 + " s } , " +
				" {Other large Itemset generation : " + (largeItemsetGenEnd - largeItemsetGenStart)/1000 + " ms } "
		); 

	}

	/*
	 * Find which all candidate sets occur in the transaction.
	 */
	public static List<ItemSet> subset(List<ItemSet> candidateItemsets, Transaction txn, Integer itemsetSize)
	{
		List<ItemSet> transactionSets = Lists.newArrayList();
		for(ItemSet c : candidateItemsets) {
			List<Integer> candidateItems = c.getItems();
			List<Integer> transactionItems = txn.getItems();
			
			int i=0;
			int j=0;
			while(i < candidateItems.size() && j < transactionItems.size()) {
				if(candidateItems.get(i).equals(transactionItems.get(j))) {
					++i; ++j;
				}
				else {
					++j;
				}
			}
			
			if(i == candidateItems.size()) {
				transactionSets.add(c);
			}
		}

		return transactionSets;
	}

	/*
	 * Find which all candidate sets occur in the transaction using hash tree.
	 */
	public static List<ItemSet> subsetEfficient(List<ItemSet> candidateItemsets, Transaction txn, Integer itemsetSize)
	{
		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(candidateItemsets, itemsetSize);
		return HashTreeUtils.findItemsets(hashTreeRoot, txn, 0);
	}

	/*
	 * Generates candidate itemsets for next iteration based on the large itemsets of the previous iteration
	 */
	public static List<ItemSet> generateCandiateItemsets(List<ItemSet> largeItemSets, int itemSetSize)
	{
		Collections.sort(largeItemSets);
		
		/*
		 * Generate the candidate itemsets by joining the two itemsets in the large itemsets such that except their
		 * last items match. Include all the matching items + the last item of both the itemsets to generate a new
		 * candidate itemset. 
		 */
		List<ItemSet> candidateItemSets = Lists.newArrayList();
		List<Integer> items = null;
		for(int i=0; i < (largeItemSets.size() -1); i++) {
			for(int j=i+1; j < largeItemSets.size(); j++ ) {
				List<Integer> outerItems = largeItemSets.get(i).getItems();
				List<Integer> innerItems = largeItemSets.get(j).getItems();
		
				if((itemSetSize -1) > 0) {
					boolean isMatch = true;
					for(int k=0; k < (itemSetSize -1); k++) {
						if(!outerItems.get(k).equals(innerItems.get(k))) {
							isMatch = false;
							break;
						}
					}
					if(isMatch) {
						items = Lists.newArrayList();
						items.addAll(outerItems);
						items.add(innerItems.get(itemSetSize-1));
						
						candidateItemSets.add(new ItemSet(items, 0));
					}
				}
				// Handle the base case for generation of candidate itemsets for k-1 = 1.
				else {
					if(outerItems.get(0) < innerItems.get(0)) {
						items = Lists.newArrayList();
						items.add(outerItems.get(0));
						items.add(innerItems.get(0));
						
						candidateItemSets.add(new ItemSet(items, 0));
					}
				}
			}
		}
		
		/*
		 * Prune the generated candidate itemsets by removing all such candidate itemsets whose any (K-1) subset
		 * does not belong to the list of (K-1) large itemsets.
		 */
		List<ItemSet> finalCandidateItemSets = Lists.newArrayList();
		for(ItemSet c : candidateItemSets) {
			List<ItemSet> subsets = getSubsets(c);
			
			boolean isValidCandidate = true;
			for(ItemSet s : subsets) {
				if(!largeItemSets.contains(s)) {
					isValidCandidate = false;
					break;
				}
			}
			
			if(isValidCandidate) {
				finalCandidateItemSets.add(c);
			}
		}

		return finalCandidateItemSets;
	}

	/*
	 * Generate all possible increasing k-1 subsets for this itemset
	 */
	private static List<ItemSet> getSubsets(ItemSet itemset)
	{
		List<ItemSet> subsets = Lists.newArrayList();
		
		List<Integer> items = itemset.getItems();
		for(int i=0; i < (items.size() - 1); i++) {
			List<Integer> currItems = Lists.newArrayList();
			for(int j=0; j < items.size(); j++) {
				if(j == (i+1)) {
					continue;
				}
				currItems.add(items.get(j));
			}
			
			subsets.add(new ItemSet(currItems, 0));
		}

		subsets.add(new ItemSet(items.subList(1, items.size()), 0));
		
		return subsets;
	}

	/*
	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
	 */
	private static InputReader getDatasetReader(Dataset dataset)
	{
		return new FileReader(dataset, Algorithm.APRIORI);
	}

}
