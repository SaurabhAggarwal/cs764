package util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Algorithm;
import model.Dataset;
import model.Transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Reads input transactions stored in filesystem.
 * 
 * @author shishir
 *
 */
public class FileReader implements InputReader 
{
	public static Map<Dataset, String> datasetLocMap = Maps.newHashMap();
	static {
		datasetLocMap.put(
				Dataset.T5_I2_D100K, "/data/T5_I2_D100_ASCII_data"
		);
		datasetLocMap.put(
				Dataset.SIMPLE, "/data/simple"
		);
	}
	
	@Override
	public List<Transaction> getTransactions(Dataset dataset, Algorithm algorithm) {
		List<Transaction> transactions = Lists.newArrayList();
		if(dataset == null) {
			System.out.println("Please provide the dataset as an argument");
			return transactions;
		}
		
		String fileLoc = getAbsoluteFileLocation(datasetLocMap.get(dataset));
		if(fileLoc == null) {
			System.out.println("Failed to locate the file for dataset " + dataset.toString());
			return transactions;
		}
		
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(fileLoc));
		}
		catch(Exception e) {
			System.err.println("Failed to read the dataset file . Reason : " + e);
			return transactions;
		}
		
		int prevTid = -1;
		List<Integer> items = Lists.newArrayList();
		Transaction transaction = null;
		
		int count=0;
		while(fileScanner.hasNext()) {
			++count;
			String currLine = fileScanner.nextLine().trim();
			String[] words = currLine.split("[\\s\\t]+");

			/*
			 * Since all the items for any transaction occur together in the dataset, we can read all the items of a
			 * transaction one-by-one and keep appending this transaction to the list of transactions.
			 */
			int currTid = Integer.parseInt(words[0].trim());
			int currItemId = Integer.parseInt(words[2].trim());
			if(prevTid == -1) {
				prevTid = currTid;
			}

			if(currTid != prevTid) {
				transaction = new Transaction(prevTid, prevTid, items, algorithm);
				transactions.add(transaction);
				items = Lists.newArrayList();
			}

			items.add(currItemId);
			prevTid = currTid;
		}

		System.out.println("Total transactions : " + count);
		return transactions;
	}

	/*
	 * Returns the absolute file location of the dataset file.
	 */
	private static String getAbsoluteFileLocation(String fileLoc)
	{
		String absFileLoc = null;
		try {
			absFileLoc = new File(".").getCanonicalPath() + fileLoc;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return absFileLoc;
	}
}
