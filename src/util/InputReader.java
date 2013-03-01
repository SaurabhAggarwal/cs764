package util;

import java.util.List;

import model.Dataset;
import model.Transaction;

/**
 * Interface used for reading transaction data from various sources like file, database etc.
 * 
 * @author shishir
 *
 */
public interface InputReader 
{
	/*
	 * Returns the list of transactions for the requested dataset.
	 */
	List<Transaction> getTransactions(Dataset dataset);
}
