package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.ItemSet;
import model.MinSup;

/*
 * Utility class to save output of algorithm runs to a file. The outputs would contain the 
 * large itemsets generated at each pass. This would help in comparing the outuuts of different
 * frequent itemset mining algorithm on the same dataset for correctness.
 */
public class OutputUtils {

	/*
	 * Writes the output of algorithm to a file in the format :
	 * <Itemset > - SupportCount
	 */
	public static void writeOutputToFile(Algorithm algo, Dataset dataset, 
			MinSup minSup, Map<Integer, List<ItemSet>> largeItemSetsMap) throws IOException
	{
		StringBuilder opFileName = new StringBuilder();
		opFileName.append(algo.toString()).append("_");
		opFileName.append(dataset.toString().replace("_", "."));
		
		String fileLoc = new File(".").getCanonicalPath() + "/output/" + opFileName.toString() + ".txt";
		System.out.println("#File : " + fileLoc);
		File file = new File(fileLoc);
		
		// Since across various minimum supports also, the result of algorithm would be same for 
		// the same dataset, just create a single file for algo-dataset combination.
		if(file.exists()) {
			return;
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileLoc)));
		for(Map.Entry<Integer, List<ItemSet>> entry : largeItemSetsMap.entrySet()) {
			List<ItemSet> largeItemsetsCurrPass = entry.getValue();
			Collections.sort(largeItemsetsCurrPass);
			
			bw.write("Large itemsets of size " + entry.getKey() + " are " + largeItemsetsCurrPass.size());
			bw.newLine();

			for(ItemSet itemset : largeItemsetsCurrPass) {
				if(itemset == null || itemset.getItems() == null || itemset.getItems().isEmpty()) {
					continue;
				}

				bw.write(itemset.toString());
				bw.newLine();
			}
		}
		
		bw.close();
	}
}
