package util;

import java.util.List;

import model.Algorithm;
import model.Dataset;
import model.Transaction;

/**
 * Reads input transactions stored in database
 * 
 * @author shishir
 *
 */
public class DBReader implements InputReader
{

	@Override
	public List<Transaction> getTransactions(Dataset dataset, Algorithm algorithm) {
		return null;
	}

}
