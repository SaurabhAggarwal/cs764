package algos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import model.Algorithm;
import model.Dataset;
import model.HashTreeNode;
import model.ItemSet;
import model.MinSup;
import model.Transaction;
import model.aprioritid.CandidateItemset;
import model.aprioritid.CandidateItemsetBar;
import model.aprioritid.ItemSetBar;
import model.aprioritid.LargeItemset;
import util.FileReader;
import util.HashTreeUtils;
import util.InputReader;

import com.google.common.collect.Lists;

import com.javamex.classmexer.MemoryUtil;

/**
 * Implements the AprioriHybrid Algorithm for frequent itemset mining.
 * 
 * @author saurabh
 *
 */
public class AprioriHybrid {

	private static int MAX_K;
	
	public static void main(String[] args)
	{
		runExperiment(Dataset.T20_I6_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
	}
	
	private static long getFreeMemory()
	{
		return Runtime.getRuntime().freeMemory();
	}
	
	private static long getEstimateSizeCBar(long numTransactions, CandidateItemset candidateItemsets)
	{
		long memoryUsage = numTransactions;
		
		int sumSupport = 0;
		
		for(ItemSet itemset : candidateItemsets.getItemsets())
		{
			if(itemset == null)
				break;
			sumSupport += itemset.getSupportCount();
		}
		
		if(sumSupport > 0)
			memoryUsage += (sumSupport * MemoryUtil.deepMemoryUsageOf(candidateItemsets.getItemsets()[0]));
		
		return memoryUsage;
	}

	/*
	 * Run Apriori algorithm for the specified experiment parameters.
	 * 
	 * @param dataset - Dataset on which the experiment has to be run.
	 * @param minSup  - Minimum support desired for this experiment run.
	 * 
	 * @return Time taken to complete this experiment.
	 */
	public static int runExperiment(Dataset dataset, MinSup minSup)
	{
		long expStartTime = System.currentTimeMillis();
		
		MAX_K = 400 * dataset.getAvgTxnSize();
		
		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		
		InputReader reader = getDatasetReader(dataset);
		
		LargeItemset[] largeItemsets = new LargeItemset[MAX_K];
		CandidateItemset[] candidateItemsets = new CandidateItemset[MAX_K];
		CandidateItemsetBar[] candidateItemsetBars = new CandidateItemsetBar[MAX_K];
		
		candidateItemsets[1] = new CandidateItemset(MAX_K);
		candidateItemsetBars[1] = new CandidateItemsetBar();
		largeItemsets[1] = new LargeItemset();
		
		getInitialCandidateItemsets(reader, candidateItemsets[1]);
		getInitialLargeItemsets(candidateItemsets[1], minSupportCount, largeItemsets[1]);
		
		print(largeItemsets[1], candidateItemsets[1].getItemsets());
		
		boolean switch_to_aprioritid = false;
		boolean in_transition = false;
		
		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
		{
			candidateItemsets[k] = new CandidateItemset(MAX_K);
			candidateItemsets[k].setItemsets(apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
			
			long estimateSizeCBar = getEstimateSizeCBar(dataset.getNumTxns(), candidateItemsets[k]);
			long freeMemory = getFreeMemory();
			
			if(switch_to_aprioritid)
			{
				if(in_transition)
					in_transition = false;
			}
			else if(estimateSizeCBar < freeMemory)
			{
				//if(k == 2)
				//{
					switch_to_aprioritid = true;
					in_transition = true;
				//	System.out.println("Switching to AprioriTID.");
				//}
			}
			
			if(!switch_to_aprioritid) //Do Apriori
			{
				largeItemsets[k] = generateLargeItemsets_Apriori(getDatasetReader(dataset), candidateItemsets[k], minSupportCount, k);
			}
			else if(in_transition) //Make a switch
			{
				candidateItemsetBars[k] = generate_C_bar_transient(getDatasetReader(dataset), candidateItemsets[k], k);
				largeItemsets[k] = generateLargeItemsets_AprioriTID(candidateItemsets[k], minSupportCount);
			}
			else //Do AprioriTID
			{
				candidateItemsetBars[k] = generate_C_bar(candidateItemsetBars[k-1], candidateItemsets[k-1].getItemsets(), candidateItemsets[k]);
				largeItemsets[k] = generateLargeItemsets_AprioriTID(candidateItemsets[k], minSupportCount);
			}
			print(largeItemsets[k], candidateItemsets[k].getItemsets());
		}
		
		long expEndTime = System.currentTimeMillis();
		int timeTaken = (int)((expEndTime - expStartTime) / 1000); 
		
		System.out.println("Time taken = " + timeTaken + " seconds.");
		System.out.println("The End !");
		
		return timeTaken;
	}
	
	/* 
	 * Given C_bar[k-1] and all itemsets, find C_bar[k].
	 */
	private static CandidateItemsetBar generate_C_bar(CandidateItemsetBar C_k_1_bar, ItemSet[] allItemsets, CandidateItemset C_k)
	{
		//System.out.println("In generate_C_bar().");
		CandidateItemsetBar toReturn = new CandidateItemsetBar();
		
		for(ItemSetBar itemsetbar : C_k_1_bar.getItemsetbars())	//1 transaction
		{
			//System.out.println("In transaction: " + itemsetbar.getTid());
			ItemSetBar k_itemset_bar = new ItemSetBar();
			k_itemset_bar.setTid(itemsetbar.getTid());
			
			SortedMap<Integer, Integer> kitemset_support = new TreeMap<Integer, Integer>();
			for(Integer Ck_1_id : itemsetbar.getCandidateItemsetId())
			{
				System.out.println("Itemset in transaction: " + allItemsets[Ck_1_id].getItems());
				for(Integer Ck_id : allItemsets[Ck_1_id].getExtensions())
				{
					//System.out.print("Extension = " + C_k.getItemsets()[Ck_id] + "; ");
					if(kitemset_support.containsKey(Ck_id))
					{
						int support = kitemset_support.get(Ck_id);
						kitemset_support.put(Ck_id, support + 1);
						//System.out.println("Support = " + (support + 1));
					}
					else
					{
						kitemset_support.put(Ck_id, 1);
						//System.out.println("Support = " + 1);
					}
				}
				
			}
			
			SortedSet<Integer> keys = new TreeSet<Integer>(kitemset_support.keySet());
			for(Integer key : keys)
			{
				if(kitemset_support.get(key) >= 2)
				{
					k_itemset_bar.getCandidateItemsetId().add(key);
					C_k.getItemsets()[key].incrementSupportCount();
				}
			}
			
			if(k_itemset_bar.getCandidateItemsetId().size() > 0)
				toReturn.getItemsetbars().add(k_itemset_bar);
		}
		
		return toReturn;
	}
	
	private static CandidateItemsetBar generate_C_bar_transient(InputReader reader, CandidateItemset C_k, int currItemsetSize)
	{
		//System.out.println("In generate_C_bar().");
		CandidateItemsetBar toReturn = new CandidateItemsetBar();
		
		while(reader.hasNextTransaction())
		{
			Transaction txn = reader.getNextTransaction();
			ItemSetBar k_itemset_bar = new ItemSetBar();
			k_itemset_bar.setTid(txn.getTid());
			
			HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(C_k.getItemsets(), currItemsetSize);
			
			List<ItemSet> candidateSetsInTrans = HashTreeUtils.findItemsets(hashTreeRoot, txn, 0);
			for(ItemSet c : candidateSetsInTrans) {
				c.setSupportCount(c.getSupportCount() + 1);
				k_itemset_bar.getCandidateItemsetId().add(c.getIndex());
			}
			
			if(k_itemset_bar.getCandidateItemsetId().size() > 0)
				toReturn.getItemsetbars().add(k_itemset_bar);
		}
		
		return toReturn;
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
	
	private static LargeItemset generateLargeItemsets_Apriori(InputReader reader, CandidateItemset candidateItemset, int minSupportCount, int currItemsetSize) {
		
		//System.out.println("In generateLargeItemsets().");
		
		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(candidateItemset.getItemsets(), currItemsetSize);
		
		while(reader.hasNextTransaction()) {
			Transaction txn = reader.getNextTransaction();
			List<ItemSet> candidateSetsInTrans = HashTreeUtils.findItemsets(hashTreeRoot, txn, 0);
			for(ItemSet c : candidateSetsInTrans) {
				c.setSupportCount(c.getSupportCount() + 1);
			}
		}
		
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
	
	private static LargeItemset generateLargeItemsets_AprioriTID(CandidateItemset candidateItemset, int minSupportCount) {
		
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
	
	private static void getInitialCandidateItemsets(InputReader reader, CandidateItemset C) {
		//System.out.println("In getInitialCandidateItemsets().");
		SortedMap<Integer, Integer> itemset_support = new TreeMap<Integer, Integer>();
		
		//This loop counts support.
		while(reader.hasNextTransaction())
		{
			Transaction t = reader.getNextTransaction();
		
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
	
	/*
	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
	 */
	private static InputReader getDatasetReader(Dataset dataset)
	{
		return new FileReader(dataset, Algorithm.APRIORI);
	}

}
