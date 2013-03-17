package util;

import java.util.List;

import model.ItemSet;

/*
 * Utility class for printing the commonly used objects in frequent itemset mining algorithms.
 * This would be useful fo debugging.
 */
public class PrintUtils {

	/*
	 * Prints all the itemsets.
	 */
	public static void printItemsets(List<ItemSet> itemsets)
	{
		if(itemsets == null || itemsets.isEmpty()) {
			System.out.println("No itemsets !!");
			return;
		}

		System.out.println("Total itemsets : " + itemsets.size());
		for(ItemSet i : itemsets) {
			System.out.println(i.toString());
		}
	}
	
	public static void printItemsets(ItemSet[] allItemsets)
	{
		int index = 0;
		for(ItemSet itemset : allItemsets)
		{
			if(itemset == null)
				break;
			System.out.print(index + ": ");
			for(Integer j : itemset.getItems())
				System.out.print(j + " ");
			System.out.println("==> " + itemset.getSupportCount());
			index++;
		}
	}
}
