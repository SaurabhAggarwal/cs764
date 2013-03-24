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
import model.LargeItemset;
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
	
	/*
	 * Returns handle to the file object that would be used to store the output large itemsets
	 * for this experiment/ candidate itemset count for each pass.
	 * 
	 * @param opFileType - Type of output file.
	 */
	public static File getOutputFile(String opFileType, Algorithm algo, Dataset dataset, MinSup minSup)
	{
		StringBuilder opFileName = new StringBuilder();
		opFileName.append(algo.toString()).append("_");
		opFileName.append(dataset.toString()).append("_");
		opFileName.append(minSup.getMinSupPercentage());

		StringBuilder fileLoc = new StringBuilder();
		try {
			fileLoc.append(new File(".").getCanonicalPath());
			fileLoc.append("/output/");
			fileLoc.append(opFileName.toString()).append("_");
			fileLoc.append(opFileType);
			fileLoc.append(".txt");
		} catch (IOException e) {
			System.err.println("Failed to generate the file location. Reason : " + e);
		}

		File file = new File(fileLoc.toString());
		if(file.exists()) {
			// Delete the existing file
			file.delete();
			
			// Recreate the file
			file = new File(fileLoc.toString());
		}
		return file;
	}

	/*
	 * Populates output file with the large itemsets generated for each pass for non-apriori algos.
	 */
	public static void writeLargeItemsetsToFile(File file, int pass, List<ItemSet> largeItemsets) throws IOException
	{
		if(largeItemsets == null || largeItemsets.isEmpty()) {
			return;
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		
		String largeItemsetsStats = 
				"# Number of large itemsets in pass " + pass + " are " + largeItemsets.size();
		bw.write(largeItemsetsStats);
		bw.newLine();

		for(ItemSet itemset : largeItemsets) {
			if(itemset == null || itemset.getItems() == null || itemset.getItems().isEmpty()) {
				continue;
			}

			bw.write(itemset.toString());
			bw.newLine();
		}

		bw.close();
	}

	/*
	 * Populates output file with the large itemsets generated for each pass for non-apriori algos.
	 */
	public static void writeLargeItemsetsToFile(File file, int pass, LargeItemset l, ItemSet[] allItemsets) throws IOException
	{
		if(l.getItemsetIds().isEmpty()) {
			return;
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		
		String largeItemsetsStats = 
				"# Number of large itemsets in pass " + pass + " are " + l.getItemsetIds().size();
		bw.write(largeItemsetsStats);
		bw.newLine();

		for(Integer i : l.getItemsetIds())
		{
			StringBuilder printStr = new StringBuilder();
			for(Integer j : allItemsets[i].getItems())
				printStr.append(j).append(" ");
			printStr.append(" - ").append(allItemsets[i].getSupportCount());
			
			bw.write(printStr.toString());
			bw.newLine();
		}

		bw.close();
	}

	/*
	 * Populates output file with the number of candidate itemsets generated for each pass.
	 */
	public static void writeCandidateCountToFile(File file, List<Integer> candidateItemsetsCount) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		int passCounter = 0;
		for(Integer itemsetsCount : candidateItemsetsCount) {
			if(itemsetsCount == 0) {
				continue;
			}

			++passCounter;
			bw.write(passCounter + " - " + itemsetsCount);
			bw.newLine();
		}
		
		bw.close();
	}
}
