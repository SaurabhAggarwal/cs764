package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import model.Algorithm;
import model.Dataset;
import model.Transaction;

import com.google.common.collect.Lists;


/**
 * Reads input transactions stored in database
 * 
 * @author shishir
 *
 */
public class DBReader extends InputReader
{
	private int currTransactionId = 1;
	private Connection dbConn = null;
	private PreparedStatement dbStmt = null;
	private ResultSet queryResult = null;
	
	private long startTime = System.currentTimeMillis();
	private long endTime   = System.currentTimeMillis();

	/*
	 * Initialize database connection here
	 */
	public DBReader(Dataset dataset, Algorithm algorithm)
	{
		super(dataset, algorithm);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "db_user", "db_user");
			
			dbStmt = dbConn.prepareStatement(getTxnReadQuery());
		} catch (Exception e) {
			System.err.println("Failed to initialise db connection. Reason : " + e);
		}
		
		startTime = System.currentTimeMillis();
	}

	@Override
	protected void finalize() throws Throwable 
	{
		// Close this connection
		dbConn.close();
	};
	
	@Override
	public Transaction getNextTransaction() {

		List<Integer> items = Lists.newArrayList();
		try {
			dbStmt.setInt(1, currTransactionId);
			queryResult = dbStmt.executeQuery();
			while(queryResult.next()) {
				Integer item = queryResult.getInt("itemID");
				items.add(item);
			}
		} catch (SQLException e) {
			System.err.println("Failed to create db statement. Reason : " + e);
			System.exit(1);
		}

		Transaction txn = new Transaction(currTransactionId, currTransactionId, items);
		++currTransactionId;		

		return txn;
	}

	@Override
	public boolean hasNextTransaction() {
		boolean hasMoreTransactions = (currTransactionId <= getDataset().getNumTxns());
		if(!hasMoreTransactions) {
			endTime = System.currentTimeMillis();
			System.out.println("#DB Time " + getDatasetReadTime());
			try {
				dbConn.close();
			} catch (SQLException e) {
				System.err.println("Failed to close the db connection .." + e);
			}
		}

		return hasMoreTransactions;
	}

	@Override
	public int getDatasetReadTime() {
		return (int)(endTime - startTime)/1000;
	}
	
	/*
	 * Creates the SQL query fired for fetching the current transaction id.
	 */
	private String getTxnReadQuery()
	{
		String tableName = getDataset().getDatasetDBTable();
		String query = 
			" SELECT itemID FROM " + tableName + " USE INDEX (pk_tid) WHERE tid = ?";
		
		return query;
	}
}
