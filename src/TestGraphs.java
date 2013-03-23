import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.MinSup;
import util.GraphUtils;

import com.google.common.collect.Maps;


/**
 * Test class for testing graph related features.
 * @author shishir
 *
 */
public class TestGraphs {

	public static void main(String[] args) {
		Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap = Maps.newHashMap();
		
		Map<MinSup, Integer> minSupRunTimeMap = Maps.newLinkedHashMap();
		minSupRunTimeMap.put(MinSup.POINT_SEVEN_FIVE_PERCENT, 14);
		minSupRunTimeMap.put(MinSup.ONE_PERCENT, 9);
		minSupRunTimeMap.put(MinSup.TWO_PERCENT, 5);
		algoRunTimeMap.put(Algorithm.APRIORI, minSupRunTimeMap);
		
		minSupRunTimeMap = Maps.newHashMap();
		minSupRunTimeMap.put(MinSup.POINT_SEVEN_FIVE_PERCENT, 22);
		minSupRunTimeMap.put(MinSup.ONE_PERCENT, 19);
		minSupRunTimeMap.put(MinSup.TWO_PERCENT, 9);
		algoRunTimeMap.put(Algorithm.AIS, minSupRunTimeMap);

		minSupRunTimeMap = Maps.newHashMap();
		minSupRunTimeMap.put(MinSup.POINT_SEVEN_FIVE_PERCENT, 42);
		minSupRunTimeMap.put(MinSup.ONE_PERCENT, 59);
		minSupRunTimeMap.put(MinSup.TWO_PERCENT, 99);
		algoRunTimeMap.put(Algorithm.SETM, minSupRunTimeMap);

		minSupRunTimeMap = Maps.newHashMap();
		minSupRunTimeMap.put(MinSup.POINT_SEVEN_FIVE_PERCENT, 12);
		minSupRunTimeMap.put(MinSup.ONE_PERCENT, 29);
		minSupRunTimeMap.put(MinSup.TWO_PERCENT, 39);
		algoRunTimeMap.put(Algorithm.APRIORI_TID, minSupRunTimeMap);

		minSupRunTimeMap = Maps.newHashMap();
		minSupRunTimeMap.put(MinSup.POINT_SEVEN_FIVE_PERCENT, 52);
		minSupRunTimeMap.put(MinSup.ONE_PERCENT, 69);
		minSupRunTimeMap.put(MinSup.TWO_PERCENT, 129);
		algoRunTimeMap.put(Algorithm.APRIORI_HYBRID, minSupRunTimeMap);

		GraphUtils.drawGraph(algoRunTimeMap, Dataset.T5_I2_D100K);
	}

}
