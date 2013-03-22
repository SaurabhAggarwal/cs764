package util;

import java.util.ArrayList;
import java.util.List;

import model.ItemSet;
import model.LargeItemset;

import com.google.common.collect.Lists;

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
	public static List<ItemSet> apriori_gen(ItemSet[] allItemsets, List<Integer> largeItemSets, int itemSetSize)
	{
		List<ItemSet> candidateItemSets = Lists.newArrayList();
		List<Integer> items = null;
		int new_cand_index = 0;
		Integer[] largeItemSetsArray = largeItemSets.toArray(new Integer[largeItemSets.size()]);
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
						
						if(prune(allItemsets, largeItemSets, newitemset))
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
					}
				}
			}
		}
		
		return candidateItemSets;
	}

	/*
	 * Returns false if any of the query candidate itemset's (K-1) subset
	 * does not belong to the list of (K-1) large itemsets.
	 */
	private static boolean prune(ItemSet[] allItemsets, List<Integer> largeItemsets, ItemSet query)
	{
		List<ItemSet> subsets = getSubsets(query);
			
		for(ItemSet s : subsets)
		{
			boolean contains = false;
			for(Integer index : largeItemsets)
			{
				ItemSet itemset = allItemsets[index];
				if(itemset.equals(s))
				{
					contains = true;
					break;
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
