import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.MinSup;
import util.GraphUtils;
import algos.AIS;
import algos.Apriori;
import algos.AprioriHybrid;
import algos.AprioriTid;
import algos.SETM;

import com.google.common.collect.Maps;

/**
 * Run experiments on all the frequent itemset mining algorithms for various configurations.
 * Save the results and use it to plot graphs for visual analysis of the algorithms.
 * 
 * @author shishir
 *
 */
public class RunAllExperiments {

	public static void main(String[] args) {
		int experimentRunTime = 0;
		Dataset[] datasetsToTest = new Dataset[1];
		datasetsToTest[0] = Dataset.T10_I4_D100K;
		MinSup[] minSupsToTest = new MinSup[1];
		minSupsToTest[0] = MinSup.POINT_SEVEN_FIVE_PERCENT;

		Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap = null;
		for(Dataset dataset : datasetsToTest) {
			algoRunTimeMap = Maps.newTreeMap();
			for(MinSup minSup : minSupsToTest) {
				// AIS
				experimentRunTime = AIS.runExperiment(dataset, minSup);
				insertIntoAlgoRunTimeMap(algoRunTimeMap, Algorithm.AIS, minSup, experimentRunTime);
				
				// Apriori
				experimentRunTime = Apriori.runExperiment(dataset, minSup);
				insertIntoAlgoRunTimeMap(algoRunTimeMap, Algorithm.APRIORI, minSup, experimentRunTime);
				
				// AprioriTID
				experimentRunTime = AprioriTid.runExperiment(dataset, minSup);
				insertIntoAlgoRunTimeMap(algoRunTimeMap, Algorithm.APRIORI_TID, minSup, experimentRunTime);
				
				// AprioriHybrid
				experimentRunTime = AprioriHybrid.runExperiment(dataset, minSup);
				insertIntoAlgoRunTimeMap(algoRunTimeMap, Algorithm.APRIORI_HYBRID, minSup, experimentRunTime);
				
				// SETM
				experimentRunTime = SETM.runExperiment(dataset, minSup);
				insertIntoAlgoRunTimeMap(algoRunTimeMap, Algorithm.SETM, minSup, experimentRunTime);
			}
			
			// Draw the graph for this specific dataset
			System.out.println("Generating chart for dataset " + dataset.toString());
			GraphUtils.drawGraph(algoRunTimeMap, dataset);
		}
	}
	
	/*
	 * Saves the results of each algorithmic experiment to be analyzed via graphs.
	 */
	private static void insertIntoAlgoRunTimeMap(
		Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap, Algorithm algo, MinSup minSup, int runTime)
	{
		Map<MinSup, Integer> minSupRunTimeMap = null;
		if(algoRunTimeMap.containsKey(algo)) {
			minSupRunTimeMap = algoRunTimeMap.get(algo);
		}
		else {
			minSupRunTimeMap = Maps.newHashMap();
		}
		
		minSupRunTimeMap.put(minSup, runTime);
		algoRunTimeMap.put(algo, minSupRunTimeMap);
	}
	
	/*
	 * Prints the details about the frequent itemset mining algorithm experiment.
	 */
	@SuppressWarnings("unused")
	private static void printExperimentStats(
			Algorithm algo, Dataset dataset, MinSup minSup, int runTime)
	{
		System.out.println(
			"{Algorithm: " + algo.toString() + ", Dataset: " + dataset.toString() + ", " +
			"MinSup: " + minSup.toString() + "%, RunTime: " + runTime + " s.}"
		);
	}

}
