package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is used for doing the database operations like staging data etc. required during the
 * intermediate processing of database-centric algorithms like SETM.
 * 
 * @author shishir
 *
 */
public class DBUtils {

	private static Connection dbConn = null;

	private static void init()
	{
		if(dbConn != null) {
			return;
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "db_user", "db_user");
		} catch (Exception e) {
			System.err.println("Failed to initialise db connection. Reason : " + e);
		}
	}
	
	@Override
	protected void finalize() throws Throwable 
	{
		// Close this connection
		dbConn.commit();
		dbConn.close();
	};
	
	/*
	 * Executes a SQL query and returns a boolean to indicate if the execution was successful.
	 */
	public static boolean executeInsertQuery(String query)
	{
		init();
		boolean isQueryDone = false;
		try {
			Statement dbQueryStmt = dbConn.createStatement();
			dbQueryStmt.executeUpdate(query);
			isQueryDone = true;
		} catch (SQLException e) {
			System.err.println("Failed to execute the query. Reason : " + e);
		}

		return isQueryDone;
	}

	/*
	 * Executes a SQL query and returns the resultset, if the execution was successful.
	 */
	public static ResultSet executeSelectQuery(String query)
	{
		init();
		ResultSet resultSet = null;
		try {
			Statement dbQueryStmt = dbConn.createStatement();
			resultSet = dbQueryStmt.executeQuery(query);
		} catch (SQLException e) {
			System.err.println("Failed to execute the query. Reason : " + e);
		}

		return resultSet;
	}
	
	/*
	 * Drop the temporary tables created during intermediate processing
	 */
	public static boolean dropTempTables(int itemsetSize)
	{
		boolean areTablesDropped = true;
		for(int i=1; i <= itemsetSize; i++) {
			try {
				Statement dbQueryStmt = dbConn.createStatement();
				dbQueryStmt.executeUpdate("DROP TABLE mining_datasets.large_bar_" + i);
				if(i >=2) {
					dbQueryStmt.executeUpdate("DROP TABLE mining_datasets.candidate_ext_bar_" + i);
					dbQueryStmt.executeUpdate("DROP TABLE mining_datasets.candidate_support_" + i);
				}
				
				dbQueryStmt.close();
			} catch (SQLException e) {
				System.out.println("Table drop failed . Reason : " + e);
				areTablesDropped = false;
			}
			
		}
		
		return areTablesDropped;
	}
}
