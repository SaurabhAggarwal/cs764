package util;

import java.util.List;
import java.util.Map;

import model.HashTreeNode;
import model.ItemSet;
import model.Transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class contains some common frequent itemset mining utility functions.
 * 
 * @author shishir
 */
public class MiningUtils {

	/*
	 * Generates the initial large-1 itemsets i.e. itemsets containing 1 item such that their support
	 * is greater than the minimum specified threshold.
	 * 
	 * @param transactions    - List of retail transactions
	 * @param minSupportCount - Minimum desired support threshold for large itemsets.
	 * 
	 * @returns	List of large-1 itemsets
	 */
	public static List<ItemSet> getInitialLargeItemsets(InputReader reader, int minSupportCount)
	{
		// Usage of tree hashmap would ensure that that keys stay in the sorted order.
		Map<Integer, Integer> itemSetSupportMap = Maps.newTreeMap();
		
		// Generate support for each item in the list of transactions
		while(reader.hasNextTransaction()) {
			Transaction txn = reader.getNextTransaction();
			List<Integer> items = txn.getItems();
			for(Integer item : items) {
				int count = 1;
				if(itemSetSupportMap.containsKey(item)) {
					count = count + itemSetSupportMap.get(item);
				}
				
				itemSetSupportMap.put(item, count);
			}
		}

		// Filter out the itemsets whose support is greater than the minimum desired support
		List<ItemSet> largeItemSets = Lists.newArrayList();
		for(Map.Entry<Integer, Integer> entry : itemSetSupportMap.entrySet()) {
			if(entry.getValue() >= minSupportCount) {
				largeItemSets.add(new ItemSet(Lists.newArrayList(entry.getKey()), entry.getValue()));
			}
		}
		
		return largeItemSets;
	}

	/*
	 * Returns the subset of itemsets from the input set of itemsets contained in this transaction. Uses HashTree
	 * data structure for efficient subset generation.
	 * 
	 * @param itemsets - Input itemsets which have to be searched in a transaction.
	 * @param t        - A retail transaction.
	 * @param itemsetSize - Size of itemsets in the input set.
	 * 
	 * @returns List of itemsets from the input set contained in this transaction.
	 */
	public static List<ItemSet> getSubsetItemsets(List<ItemSet> itemsets, Transaction t, int itemsetSize)
	{
		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(itemsets, itemsetSize);
		return HashTreeUtils.findItemsets(hashTreeRoot, t, 0);
	}
	
	/*
	 * Returns a boolean to indicate if this itemset has the desired minimum support for this
	 * dataset.
	 * 
	 * @param itemset - Itemset whose support needs to be determined
	 * @param minSupport - Minimum support threshold desired for this dataset.
	 * 
	 * @returns A boolean indicating if this itemset has the minimum suppport threshold.
	 */
	public static boolean hasMinSupport(ItemSet itemset, int minSupport)
	{
		return itemset.getSupportCount() >= minSupport ? true : false;
	}
	
	/*
	 * Generates a map of hashcode and the corresponding itemset. Since multiple hashcodes can
	 * have the same hashcode, there would be a list of itemsets for any hashcode.
	 */
	public static Map<Integer, List<ItemSet>> getLargeItemsetMap(List<ItemSet> largeItemsets)
	{
		Map<Integer, List<ItemSet>> largeItemsetMap = Maps.newHashMap();
		
		List<ItemSet> itemsets = null;
		for(ItemSet itemset : largeItemsets) {
			int hashCode = itemset.hashCode();
			if(largeItemsetMap.containsKey(hashCode)) {
				itemsets = largeItemsetMap.get(hashCode);
			}
			else {
				itemsets = Lists.newArrayList();
			}
			
			largeItemsetMap.put(hashCode, itemsets);
		}

		return largeItemsetMap;
	}
}
