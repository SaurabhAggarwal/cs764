package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.CandidateItemset;
import model.ItemSet;
import model.LargeItemset;

import com.google.common.collect.Lists;
import com.javamex.classmexer.MemoryUtil;

/**
 * Common functions used by all Apriori-based Algorithms
 * 
 * @author saurabh
 *
 */

public class AprioriUtils {
	
	/*
	 * Given large itemsets of length (k-1), returns candidate itemsets of length (k).
	 */
	public static List<ItemSet> apriori_gen(ItemSet[] allItemsets, List<Integer> largeItemsetIndices, int itemSetSize)
	{
		List<ItemSet> candidateItemSets = Lists.newArrayList();
		List<Integer> items = null;

		List<ItemSet> largeItemsets = getLargeItemsetsFromIndices(allItemsets, largeItemsetIndices);
		Map<Integer, List<ItemSet>> largeItemsetsMap = MiningUtils.getLargeItemsetMap(largeItemsets);

		int new_cand_index = 0;
		Integer[] largeItemSetsArray = largeItemsetIndices.toArray(new Integer[largeItemsetIndices.size()]);
		//System.out.println("#############" + largeItemSetsArray.length);
		int count = 0;
		for(int i = 0; i < largeItemSetsArray.length; i++)
		{
			for(int j = i + 1; j < largeItemSetsArray.length; j++) {
				List<Integer> outerItems = allItemsets[largeItemSetsArray[i]].getItems();
				List<Integer> innerItems = allItemsets[largeItemSetsArray[j]].getItems();
		
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
						
						ItemSet newitemset = new ItemSet(items, 0, largeItemSetsArray[i], largeItemSetsArray[j]);
						
						if(prune(largeItemsetsMap, newitemset))
						{
							newitemset.setIndex(new_cand_index);
							candidateItemSets.add(newitemset);
							allItemsets[largeItemSetsArray[i]].addExtension(new_cand_index);
							allItemsets[largeItemSetsArray[j]].addExtension(new_cand_index);
							new_cand_index++;
						}
					}
				}
				// Handle the base case for generation of candidate itemsets for k-1 = 1.
				else {
					if(outerItems.get(0) < innerItems.get(0)) {
						items = Lists.newArrayList();
						items.add(outerItems.get(0));
						items.add(innerItems.get(0));
						
						ItemSet newitemset = new ItemSet(items, 0, largeItemSetsArray[i], largeItemSetsArray[j]);
						newitemset.setIndex(new_cand_index);
						
						candidateItemSets.add(newitemset);
						allItemsets[largeItemSetsArray[i]].addExtension(new_cand_index);
						allItemsets[largeItemSetsArray[j]].addExtension(new_cand_index);
						new_cand_index++;
						count++;
					}
				}
			}
		}
		//System.out.println("#############" + count);
		
		return candidateItemSets;
	}

	/*
	 * Gets the actual large itemsets from the indices.
	 */
	private static List<ItemSet> getLargeItemsetsFromIndices(
			ItemSet[] allItemsets, List<Integer> largeItemsetIndices)
	{
		List<ItemSet> largeItemsets = Lists.newArrayList();
		for(Integer index : largeItemsetIndices) {
			largeItemsets.add(allItemsets[index]);
		}

		return largeItemsets;
	}

	/*
	 * Returns false if any of the query candidate itemset's (K-1) subset
	 * does not belong to the list of (K-1) large itemsets.
	 */
	private static boolean prune(Map<Integer, List<ItemSet>> largeItemsetsMap, ItemSet query)
	{
		List<ItemSet> subsets = getSubsets(query);
			
		for(ItemSet s : subsets) {
			boolean contains = false;
			int hashCodeToSearch = s.hashCode();
			if(largeItemsetsMap.containsKey(hashCodeToSearch)) {
				List<ItemSet> candidateItemsets = largeItemsetsMap.get(hashCodeToSearch);
				for(ItemSet itemset : candidateItemsets) {
					if(itemset.equals(s)) {
						contains = true;
						break;
					}
				}
			}

			if(!contains)
				return false;
		}

		return true;
	}
	
	/*
	 * Generate all possible k-1 subsets for this itemset (preserves order)
	 */
	private static List<ItemSet> getSubsets(ItemSet itemset)
	{
		List<ItemSet> subsets = new ArrayList<ItemSet>();
		
		List<Integer> items = itemset.getItems();
		for(int i = 0; i < items.size(); i++) {
			List<Integer> currItems = new ArrayList<Integer>(items);
			currItems.remove(items.size() - 1 - i);
			subsets.add(new ItemSet(currItems, 0));
		}

		return subsets;
	}
	
	public static long getEstimateSizeCBar(long numTransactions, CandidateItemset candidateItemsets)
	{
		long memoryUsage = numTransactions;
		
		int sumSupport = 0;
		
		for(ItemSet itemset : candidateItemsets.getItemsets())
		{
			if(itemset == null)
				break;
			sumSupport += itemset.getSupportCount();
		}
		
		//Multiply sumSupport with size of 1 itemset.
		if(sumSupport > 0)
			memoryUsage += (sumSupport * MemoryUtil.deepMemoryUsageOf(candidateItemsets.getItemsets()[0]));
		
		return memoryUsage;
	}
	
	public static void print(LargeItemset l, ItemSet[] allItemsets)
	{
		for(Integer i : l.getItemsetIds())
		{
			for(Integer j : allItemsets[i].getItems())
				System.out.print(j + " ");
			System.out.println("- " + allItemsets[i].getSupportCount());
		}
	}
}
