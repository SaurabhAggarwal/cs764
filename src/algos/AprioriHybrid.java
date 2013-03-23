package algos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import model.Algorithm;
import model.CandidateItemset;
import model.Dataset;
import model.HashTreeNode;
import model.ItemSet;
import model.LargeItemset;
import model.MinSup;
import model.Transaction;
import model.aprioritid.CandidateItemsetBar;
import model.aprioritid.ItemSetBar;
import util.AprioriUtils;
import util.DBReader;
import util.FileReader;
import util.HashTreeUtils;
import util.InputReader;

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
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
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
		System.out.println("AprioriHybrid: " + dataset + ", " + minSup);
		
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
		
		//AprioriUtils.print(largeItemsets[1], candidateItemsets[1].getItemsets());
		
		boolean switch_to_aprioritid = false;
		boolean in_transition = false;
		
		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
		{
			candidateItemsets[k] = new CandidateItemset(MAX_K);
			candidateItemsets[k].setItemsets(AprioriUtils.apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
			largeItemsets[k-1] = null; 
			
			long estimateSizeCBar = getEstimateSizeCBar(dataset.getNumTxns(), candidateItemsets[k]);
			long freeMemory = getFreeMemory();
			
			if((!switch_to_aprioritid) && (estimateSizeCBar < freeMemory))
			{
				//if(k == 2)
				//{
					switch_to_aprioritid = true;
					in_transition = true;
					System.out.println("Switching to AprioriTID at pass k = " + k);
					System.out.println("Estimated size of C_bar = " + estimateSizeCBar / 1024 + " KB.");
					System.out.println("Free memory = " + freeMemory / 1024 + " KB.");
				//}
			}
			
			if(!switch_to_aprioritid) //Do Apriori
			{
				largeItemsets[k] = generateLargeItemsets_Apriori(getDatasetReader(dataset), candidateItemsets[k], minSupportCount, k);
			}
			else if(in_transition) //Make a switch
			{
				//System.out.println("Transition 1.");
				candidateItemsetBars[k] = generate_C_bar_transient(getDatasetReader(dataset), candidateItemsets[k], k);
				//System.out.println("Transition 2.");
				largeItemsets[k] = generateLargeItemsets_AprioriTID(candidateItemsets[k], minSupportCount);
				in_transition = false;
				//System.out.println("Transition complete.");
			}
			else //Do AprioriTID
			{
				candidateItemsetBars[k] = generate_C_bar(candidateItemsetBars[k-1], candidateItemsets[k-1].getItemsets(), candidateItemsets[k]);
				largeItemsets[k] = generateLargeItemsets_AprioriTID(candidateItemsets[k], minSupportCount);
			}
			candidateItemsetBars[k-1] = null;
			candidateItemsets[k-1] = null;
			//AprioriUtils.print(largeItemsets[k], candidateItemsets[k].getItemsets());
		}
		
		long expEndTime = System.currentTimeMillis();
		int timeTaken = (int)((expEndTime - expStartTime) / 1000); 
		
		System.out.println("Time taken = " + timeTaken + " seconds.\n");
		
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
				//System.out.println("Itemset in transaction: " + allItemsets[Ck_1_id].getItems());
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
		
		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(C_k.getItemsets(), currItemsetSize);
		
		while(reader.hasNextTransaction())
		{
			Transaction txn = reader.getNextTransaction();
			//System.out.println("In transaction: " + txn.getTid());
			ItemSetBar k_itemset_bar = new ItemSetBar();
			k_itemset_bar.setTid(txn.getTid());
			
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
	
		
	/*
	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
	 */
	private static InputReader getDatasetReader(Dataset dataset)
	{
		return new FileReader(dataset, Algorithm.APRIORI_HYBRID);
	}

}
