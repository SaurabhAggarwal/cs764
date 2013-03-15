package util;

import model.Algorithm;
import model.Dataset;
import model.Transaction;

/**
 * Interface used for reading transaction data from various sources like file, database etc.
 * 
 * @author shishir
 *
 */
public abstract class InputReader 
{
	private Dataset dataset; // Dataset being read by this input reader
	private Algorithm algorithm; // Which algorithm is this reader servicing ?
	
	public InputReader(Dataset dataset, Algorithm algorithm)
	{
		this.dataset = dataset;
		this.algorithm = algorithm;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/*
	 * Implements iterator functionality to return the next transaction from the dataset. This is
	 * required to simulate the scanning of dataset record-by-record as done in the frequent itemset
	 * mining algorithms in the paper. We could have just read all the transactions in a single 
	 * call but to simulate the iterative fetching of data, we would initially open a connection
	 * to the data source - file or database, keep it open till we keep reading transactions and 
	 * finally close the connection when we are done.
	 */
	abstract public Transaction getNextTransaction();
	
	/*
	 * Checks if all the transactions in the dataset have been read.
	 */
	abstract public boolean hasNextTransaction();

	/*
	 * Returns the time taken to read the dataset.
	 */
	abstract public int getDatasetReadTime();

	/*
	 * Returns the list of transactions for the requested dataset.
	 */
	//List<Transaction> getTransactions(Dataset dataset, Algorithm algorithm);
}
