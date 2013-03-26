import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.MinSup;
import util.GraphUtils;

import com.google.common.collect.Maps;


/**
 * This class generates the following static graphs :
 * 		- Size of large and candidate sets for all experiments
 * 		- Per pass execution times of Apriori and AprioriTID
 * 
 * @author shishir
 *
 */
public class GraphGenerator {

	private static Dataset datasetToTest = Dataset.T10_I4_D100K;
	private static MinSup minSupToTest = MinSup.POINT_SEVEN_FIVE_PERCENT;
	
	public static void main(String[] args) {
		GraphUtils.drawPerPassGraph(
			genPerPassExecutionTimeGraph(), 
			"Apriori vs AprioriTID ( " + datasetToTest.toString() + "_" + minSupToTest.getMinSupPercentage() + " )", 
			"Pass#", "Time (sec)",
			false
		);
		
		GraphUtils.drawPerPassGraph(
			genItemsetsSizeGraph(), 
			"Itemsets Generated ( " + datasetToTest.toString() + "_" + minSupToTest.getMinSupPercentage() + " )", 
			"Pass#", "Number of Itemsets",
			true
		);
	}

	/*
	 * Generates a graph to show the relative sizes of the itemsets generated during the
	 * experiment run for various algorithms.
	 * The values for each pass have been taken from the experiment run for dataset
	 * T10_I4_D100K and minimum support 0.75%.
	 */
	private static Map<String, Map<Integer, Integer>> genItemsetsSizeGraph()
	{
		Map<String, Map<Integer, Integer>> itemsetsSizeMap = Maps.newHashMap();
		
		Map<Integer, Integer> largeItemsetsMap = Maps.newHashMap();
		largeItemsetsMap.put(1, 452);
		largeItemsetsMap.put(2, 98);
		largeItemsetsMap.put(3, 17);
		largeItemsetsMap.put(4, 6);
		largeItemsetsMap.put(5, 0);

		Map<Integer, Integer> candAprioriAndTID = Maps.newHashMap();
		candAprioriAndTID.put(2, 101926);
		candAprioriAndTID.put(3, 24);
		candAprioriAndTID.put(4, 6);
		candAprioriAndTID.put(5, 1);
		candAprioriAndTID.put(6, 0);

		Map<Integer, Integer> candAISSETMMap = Maps.newHashMap();
		candAISSETMMap.put(2, 176764);
		candAISSETMMap.put(3, 22459);
		candAISSETMMap.put(4, 2089);
		candAISSETMMap.put(5, 293);
		candAISSETMMap.put(6, 0);

		Map<Integer, Integer> candBarAprioriTID = Maps.newHashMap();
		candBarAprioriTID.put(2, 3990440);
		candBarAprioriTID.put(3, 17629);
		candBarAprioriTID.put(4, 4456);
		candBarAprioriTID.put(5, 707);
		candBarAprioriTID.put(6, 0);

		Map<Integer, Integer> candBarSETM = Maps.newHashMap();
		candBarSETM.put(2, 5213187);
		candBarSETM.put(3, 274419);
		candBarSETM.put(4, 21011);
		candBarSETM.put(5, 2742);
		candBarSETM.put(6, 0);
		
		itemsetsSizeMap.put("L-k", largeItemsetsMap);
		itemsetsSizeMap.put("C-k(Apriori, AprioriTid)", candAprioriAndTID);
		itemsetsSizeMap.put("C-k(AIS, SETM)", candAISSETMMap);
		itemsetsSizeMap.put("C-k-m (AprioriTid)", candBarAprioriTID);
		itemsetsSizeMap.put("C-k-m (SETM)", candBarSETM);
		
		return itemsetsSizeMap;
	}
	
	/*
	 * Generates a graph to show the per pass execution times of Apriori and AprioriTID algo.
	 * The values for each pass have been taken from the experiment run for dataset
	 * T10_I4_D100K and minimum support 0.75%.
	 */
	private static Map<String, Map<Integer, Integer>> genPerPassExecutionTimeGraph()
	{
		Map<String, Map<Integer, Integer>> algoPerPassRunTimeMap = Maps.newTreeMap();
		
		Map<Integer, Integer> aprioriStats = Maps.newTreeMap();
		aprioriStats.put(1, 14);
		aprioriStats.put(2, 17);
		aprioriStats.put(3, 15);
		aprioriStats.put(4, 14);
		aprioriStats.put(5, 14);
		aprioriStats.put(6, 0);

		Map<Integer, Integer> aprioriTidStats = Maps.newTreeMap();
		aprioriTidStats.put(1, 14);
		aprioriTidStats.put(2, 206);
		aprioriTidStats.put(3, 0);
		aprioriTidStats.put(4, 0);
		aprioriTidStats.put(5, 0);
		aprioriStats.put(6, 0);

		algoPerPassRunTimeMap.put(Algorithm.APRIORI.toString(), aprioriStats);
		algoPerPassRunTimeMap.put(Algorithm.APRIORI_TID.toString(), aprioriTidStats);
		
		return algoPerPassRunTimeMap;
	}

}
