package algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import model.Algorithm;
import model.Dataset;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import model.aprioritid.CandidateItemset;
import model.aprioritid.CandidateItemsetBar;
import model.aprioritid.ItemSetBar;
import model.aprioritid.LargeItemset;
import util.FileReader;
import util.InputReader;

public class AprioriTid {
	
	private static int MAX_K;
	
	public static void main(String[] args)
	{
		runExperiment(Dataset.SIMPLE, MinSup.POINT_TWO_FIVE_PERCENT, new FileReader());
	}
	
	/* 
	 * Given C_bar[k-1] and C[k], find C_bar[k].
	 */
	private static CandidateItemsetBar generate_C_bar(CandidateItemsetBar C_k_1_bar, CandidateItemset Ck_1, CandidateItemset C_k)
	{
		//System.out.println("In generate_C_bar().");
		CandidateItemsetBar toReturn = new CandidateItemsetBar();

		int outer = 1;
		//System.out.println("C_k_1_bar.getItemsetbars().size() = " + C_k_1_bar.getItemsetbars().size());
		for(ItemSetBar itemsetbar : C_k_1_bar.getItemsetbars())	//1 transaction
		{
			//System.out.println("Outer: " + outer++);
			//System.out.println(itemsetbar.getCandidateItemsetId());
			ItemSetBar k_itemset_bar = new ItemSetBar();
			k_itemset_bar.setTid(itemsetbar.getTid());
			
			List<Integer> prev_itemset_indices = itemsetbar.getCandidateItemsetId();
			
			int C_k_index = 0;
			int inner = 1;
			for(ItemSet itemset : C_k.getItemsets())
			{
				//System.out.println("Inner: " + inner++);
				if(itemset == null)
					break;
				int found = 0;
				List<Integer> subitemset1 = new ArrayList<Integer>(itemset.getItems());
				subitemset1.remove(subitemset1.size() - 1);
				List<Integer> subitemset2 = new ArrayList<Integer>(itemset.getItems());
				subitemset2.remove(subitemset2.size() - 2);
			
				for(Integer index : prev_itemset_indices)
				{
					if(Ck_1.getItemsets()[index].getItems().equals(subitemset1))
						found++;
					if(Ck_1.getItemsets()[index].getItems().equals(subitemset2))
						found++;
					if(found == 2)
					{
						k_itemset_bar.getCandidateItemsetId().add(C_k_index);
						itemset.incrementSupportCount();
						break;
					}
				}
				C_k_index++;
			}
			
			if(k_itemset_bar.getCandidateItemsetId().size() > 0)
				toReturn.getItemsetbars().add(k_itemset_bar);
		}
		
		return toReturn;
	}

	/*
	 * Run AprioriTid algorithm for the specified experiment parameters
	 */
	private static void runExperiment(Dataset dataset, MinSup minSup, InputReader reader)
	{
		List<Transaction> transactions = reader.getTransactions(dataset, Algorithm.APRIORI_TID);
		int minSupportCount = 2; //(int)(minSup.getMinSupPercentage() * transactions.size())/100;
		MAX_K = 400 * dataset.getAvgTxnSize();
		System.out.println(MAX_K);
		
		LargeItemset[] largeItemsets = new LargeItemset[MAX_K];
		CandidateItemset[] candidateItemsets = new CandidateItemset[MAX_K];
		CandidateItemsetBar[] candidateItemsetBars = new CandidateItemsetBar[MAX_K];
		
		candidateItemsets[1] = new CandidateItemset(MAX_K);
		candidateItemsetBars[1] = new CandidateItemsetBar();
		largeItemsets[1] = new LargeItemset();
		getInitialCandidateItemsets(transactions, candidateItemsets[1], candidateItemsetBars[1]);
		getInitialLargeItemsets(candidateItemsets[1], minSupportCount, largeItemsets[1]);
		//printAll(candidateItemsets[1].getItemsets());
		print(largeItemsets[1], candidateItemsets[1].getItemsets());
		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
		{
			System.out.println("k = " + k);
			candidateItemsets[k] = new CandidateItemset(MAX_K);
			candidateItemsets[k].setItemsets(apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
			//System.out.println(candidateItemsets[k].getItemsets().toString());
			candidateItemsetBars[k] = generate_C_bar(candidateItemsetBars[k-1], candidateItemsets[k-1], candidateItemsets[k]);
			//System.out.println(candidateItemsetBars[k].getItemsetbars().toString());
			largeItemsets[k] = generateLargeItemsets(candidateItemsets[k], minSupportCount);
			print(largeItemsets[k], candidateItemsets[k].getItemsets());
			System.out.println();
		}
		
		System.out.println("The End !");
	}
	
	private static void printAll(ItemSet[] allItemsets)
	{
		for(ItemSet itemset : allItemsets)
		{
			if(itemset == null)
				break;
			for(Integer j : itemset.getItems())
				System.out.print(j + " ");
			System.out.println("- " + itemset.getSupportCount());
		}
	}
	
	private static void print(LargeItemset l, ItemSet[] allItemsets)
	{
		for(Integer i : l.getItemsetIds())
		{
			for(Integer j : allItemsets[i].getItems())
				System.out.print(j + " ");
			System.out.println("- " + allItemsets[i].getSupportCount());
		}
	}
	
	private static LargeItemset generateLargeItemsets(CandidateItemset candidateItemset, int minSupportCount) {
		
		//System.out.println("In generateLargeItemsets().");
		
		LargeItemset largeItemset = new LargeItemset();
		int index = 0;
		for(ItemSet itemset : candidateItemset.getItemsets())
		{
			if(itemset == null)
				break;
			if(itemset.getSupportCount() >= minSupportCount)
				largeItemset.getItemsetIds().add(index);
			index++;
		}
		
		return largeItemset;
	}

	private static void getInitialCandidateItemsets(List<Transaction> transactions, CandidateItemset C, CandidateItemsetBar C_) {
		//System.out.println("In getInitialCandidateItemsets().");
		SortedMap<Integer, Integer> itemset_support = new TreeMap<Integer, Integer>();
		
		//This loop counts support.
		for(Transaction t : transactions)
		{
			for(Integer i : t.getItems())
			{
				if(itemset_support.containsKey(i))
				{
					int support = itemset_support.get(i);
					itemset_support.put(i, support + 1);
				}
				else
				{
					itemset_support.put(i, 1);
				}
			}
		}
		
		//This part sorts and creates candidate itemsets.
		SortedSet<Integer> keys = new TreeSet<Integer>(itemset_support.keySet());
		Map<Integer, Integer> itemset_pos = new HashMap<Integer, Integer>(); 
		int index = 0;
		for(Integer item : keys)
		{
			Integer support = itemset_support.get(item);
			C.getItemsets()[index] = new ItemSet();
			C.getItemsets()[index].getItems().add(item);
			C.getItemsets()[index].setSupportCount(support);
			
			itemset_pos.put(item, index);
			index++;
		}
		
		//This part creates C_.
		for(Transaction t : transactions)
		{
			ItemSetBar itemsetbar = new ItemSetBar();
			itemsetbar.setTid(t.getTid());
			for(Integer i : t.getItems())
			{
				int pos = itemset_pos.get(i);
				itemsetbar.getCandidateItemsetId().add(pos);
			}
			C_.getItemsetbars().add(itemsetbar);
		}
	}

	private static void getInitialLargeItemsets(CandidateItemset C, int minSupportCount, LargeItemset L)
	{
		int index = 0;
		for(ItemSet itemset : C.getItemsets()) {
			if(itemset == null)
				break;
			if(itemset.getSupportCount() >= minSupportCount)
				L.getItemsetIds().add(index);
			index++;
		}
	}
	
	private static boolean prune(ItemSet[] allItemsets, List<Integer> largeItemsets, ItemSet query)
	{
		/*
		 * Prune the generated candidate itemsets by removing all such candidate itemsets whose any (K-1) subset
		 * does not belong to the list of (K-1) large itemsets.
		 */
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
	
	//Given L[k-1], returns C[k]
	public static List<ItemSet> apriori_gen(ItemSet[] allItemsets, List<Integer> largeItemSets, int itemSetSize)
	{
		//System.out.println("In apriori_gen " + itemSetSize);
		//Collections.sort(largeItemSets);
		
		/*
		 * Generate the candidate itemsets by joining the two itemsets in the large itemsets such that except their
		 * last items match. Include all the matching items + the last item of both the itemsets to generate a new
		 * candidate itemset. 
		 */
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
	 * Generate all possible increasing k-1 subsets for this itemset
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
}
