package util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import model.Algorithm;
import model.Dataset;
import model.Transaction;

import com.google.common.collect.Lists;

/**
 * Reads input transactions stored in filesystem.
 * 
 * @author shishir
 *
 */
public class FileReader extends InputReader 
{
	private Scanner fileScanner = null;
	
	private long startTime = System.currentTimeMillis();
	private long endTime   = System.currentTimeMillis();

	public FileReader(Dataset dataset, Algorithm algorithm)
	{
		super(dataset, algorithm);
		
		// Get the filesystem location of the dataset file to be read.
		String fileLoc = getAbsoluteFileLocation(dataset.getDataFileLocation());
		if(fileLoc == null) {
			System.out.println("Failed to locate the file for dataset " + dataset.toString());
			System.exit(0);
		}

		// Set the scanner here
		startTime = System.currentTimeMillis();
		try {
			fileScanner = new Scanner(new File(fileLoc));
		}
		catch(Exception e) {
			System.err.println("Failed to read the dataset file . Reason : " + e);
			System.exit(0);
		}
	}
	
	@Override
	public Transaction getNextTransaction()
	{
		Transaction transaction = null;
		if(fileScanner.hasNext()) {
			String currLine = fileScanner.nextLine().trim();
			String[] words = currLine.split("[\\s\\t]+");

			int currTid = Integer.parseInt(words[0].trim());
			List<Integer> currItems = Lists.newArrayList();
			for(int i=1; i < words.length; i++) {
				currItems.add(Integer.parseInt(words[i].trim()));
			}
			
			transaction = new Transaction(currTid, currTid, currItems);
		}
		
		return transaction;
	}

	@Override
	public boolean hasNextTransaction()
	{
		boolean hasMoreTransactions = fileScanner.hasNext();
		if(!hasMoreTransactions) {
			endTime = System.currentTimeMillis();
		}

		return hasMoreTransactions;
	}

	@Override
	public int getDatasetReadTime()
	{
		return (int)(endTime - startTime)/1000;
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
