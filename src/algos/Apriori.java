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
import util.DBReader;
import model.HashTreeNode;
import model.ItemSet;
import model.LargeItemset;
import model.MinSup;
import model.Transaction;
import util.AprioriUtils;
import util.FileReader;
import util.HashTreeUtils;
import util.InputReader;

/**
 * Implements the classical Apriori Algorithm for frequent itemset mining.
 * 
 * @author saurabh
 *
 */
public class Apriori {

	private static int MAX_K;
	
	public static void main(String[] args)
	{
		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_SEVEN_FIVE_PERCENT);
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
		System.out.println("Apriori: " + dataset + ", " + minSup);
		
		long expStartTime = System.currentTimeMillis();
		
		MAX_K = 400 * dataset.getAvgTxnSize();
		
		int minSupportCount = (int)(minSup.getMinSupPercentage() * dataset.getNumTxns())/100;
		
		InputReader reader = getDatasetReader(dataset);
		
		LargeItemset[] largeItemsets = new LargeItemset[MAX_K];
		CandidateItemset[] candidateItemsets = new CandidateItemset[MAX_K];
		
		candidateItemsets[1] = new CandidateItemset(MAX_K);
		largeItemsets[1] = new LargeItemset();
		
		getInitialCandidateItemsets(reader, candidateItemsets[1]);
		getInitialLargeItemsets(candidateItemsets[1], minSupportCount, largeItemsets[1]);
		
		//AprioriUtils.print(largeItemsets[1], candidateItemsets[1].getItemsets());
		
		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
		{
			candidateItemsets[k] = new CandidateItemset(MAX_K);
			candidateItemsets[k].setItemsets(AprioriUtils.apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
			
			largeItemsets[k] = generateLargeItemsets(getDatasetReader(dataset), candidateItemsets[k], minSupportCount, k);
			//print(largeItemsets[k], candidateItemsets[k].getItemsets());

			//AprioriUtils.print(largeItemsets[k], candidateItemsets[k].getItemsets());
		}
		
		long expEndTime = System.currentTimeMillis();
		int timeTaken = (int)((expEndTime - expStartTime) / 1000); 
		System.out.println("Time taken = " + timeTaken + " seconds.\n");
		
		return timeTaken;
	}
	
	private static LargeItemset generateLargeItemsets(InputReader reader, CandidateItemset candidateItemset, int minSupportCount, int currItemsetSize) {
		
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
		return new FileReader(dataset, Algorithm.APRIORI);
	}
}
