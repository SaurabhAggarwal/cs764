package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * A simple utility class that pre-processes the input dataset file to group all the 
 * items in a single transaction in a single line.
 * 
 * @author shishir
 *
 */
public class FilePreProcessorUtils {

	public static void main(String[] args) throws IOException{
		String inputFileLoc = args[0]; // Complete source file path as input
		String outputFileLoc = args[1]; // Complete destination file path as input
		
		List<String> txnLines = readTxnLines(inputFileLoc);
		writeTxnLines(txnLines, outputFileLoc);
		System.out.println("File pre-processing completed for dataset " + inputFileLoc);
	}
	
	/*
	 * Reads all the lines in a input file and concatenates items belonging to same transaction as
	 * space separated values.
	 */
	private static List<String> readTxnLines(String inputFileLoc)
	{
		Map<Integer, List<Integer>> txnItemsMap = Maps.newLinkedHashMap();
		List<String> txnLines = Lists.newArrayList();

		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(inputFileLoc));
		}
		catch(Exception e) {
			System.err.println("Failed to read the dataset file . Reason : " + e);
		}
		
		while(fileScanner.hasNext()) {
			String currLine = fileScanner.nextLine().trim();
			String[] words = currLine.split("[\\s\\t]+");

			int currTid = Integer.parseInt(words[0].trim());
			int currItemId = Integer.parseInt(words[2].trim());
			
			List<Integer> items = null;
			if(txnItemsMap.containsKey(currTid)) {
				items = txnItemsMap.get(currTid);
				items.add(currItemId);
			}
			else {
				items = Lists.newArrayList(currItemId);
			}
			
			txnItemsMap.put(currTid, items);
		}
		fileScanner.close();
		
		for(Map.Entry<Integer, List<Integer>> entry : txnItemsMap.entrySet()) {
			StringBuilder builder = new StringBuilder();
			builder.append(entry.getKey().toString()).append(" ");
			for(Integer item : entry.getValue()) {
				builder.append(item.toString()).append(" ");
			}
			
			txnLines.add(builder.toString().trim());
		}
		
		System.out.println("Number of transactions in the dataset : " + txnLines.size());
		return txnLines;
	}
	
	/*
	 * Writes a transaction with its itemset to the console line.
	 */
	private static boolean writeTxnLines(List<String> txnLines, String outputFileLoc) 
			throws IOException
	{
		boolean isWriteDone = false;
		File opFile = new File(outputFileLoc);
		BufferedWriter writer = Files.newWriter(opFile, Charset.defaultCharset());
		for(String line : txnLines) {
			writer.write(line);writer.newLine();
		}
		writer.flush();
		writer.close();
		
		isWriteDone = true;
		
		return isWriteDone;
	}

}
