package util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
			Dataset.T5_I2_D100K, "/home/shishir/DMProject/resources/synthetic_data_generator/datasets/T5_I2_D100_ASCII_data"
			//Dataset.T5_I2_D100K, "/home/shishir/DMProject/resources/synthetic_data_generator/datasets/test.data"
		);
	}
	
	@Override
	public List<Transaction> getTransactions(Dataset dataset) {
		List<Transaction> transactions = Lists.newArrayList();
		if(dataset == null) {
			System.out.println("Please provide the dataset as an argument");
			return transactions;
		}
		
		String fileLoc = datasetLocMap.get(dataset);
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
			
			int currTid = Integer.parseInt(words[0].trim());
			int currItemId = Integer.parseInt(words[2].trim());
			if(prevTid == -1) {
				prevTid = currTid;
			}

			if(currTid != prevTid) {
				transaction = new Transaction(prevTid, prevTid, items);
				transactions.add(transaction);
				items = Lists.newArrayList();
			}

			items.add(currItemId);
			prevTid = currTid;
		}

		System.out.println("Total transactions : " + count);
		return transactions;
	}

}
