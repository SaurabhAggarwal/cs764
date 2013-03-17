package bootstrap;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * A simple utility class to bootstrap the intial datasets into MySQL database.
 * 
 * @author shishir
 *
 */
public class DBBootstrapUtils {

	private static Connection dbConn = null;

	// Initialise the db connection parameters
	private static void preLoad() 
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "db_user", "db_user");
			System.out.println("Opening connection to db ..");
		} catch (Exception e) {
			System.err.println("Failed to initialise db connection. Reason : " + e);
		}

	}
	
	// Release all the db connection resources
	private static void postLoad() {
		try {
			dbConn.commit();
			dbConn.close();
		} catch (SQLException e) {
			System.out.println("Closing db connection ..");
		}
		
	}

	public static void main(String[] args) {
		preLoad();
		
		String srcFilesRootDir = "/home/shishir/DMProject/resources/synthetic_data_generator/datasets_raw";
		loadIntoDB(srcFilesRootDir + "/T10.I4.D100K.raw", "mining_datasets.T10_I4_D100K");
		loadIntoDB(srcFilesRootDir + "/T20.I2.D100K.raw", "mining_datasets.T20_I2_D100K");
		loadIntoDB(srcFilesRootDir + "/T20.I4.D100K.raw", "mining_datasets.T20_I4_D100K");
		loadIntoDB(srcFilesRootDir + "/T20.I6.D100K.raw", "mining_datasets.T20_I6_D100K");
		
		postLoad();
	}

	/*
	 * Loads the data from this source file into the destination table.
	 */
	private static void loadIntoDB(String srcFilePath, String dbTableName)
	{
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(srcFilePath));
		}
		catch(Exception e) {
			System.out.println("Failed to read the file.");
			System.exit(1);
		}

		int count=0;
		PreparedStatement stmt = null;
		try {
			stmt = dbConn.prepareStatement("INSERT INTO " + dbTableName + " VALUES (?, ?, ?)");
		} catch (SQLException e1) {
			System.err.println("Failed to create prepared statement. Reason : " + e1);
			System.exit(1);
		}
		while(fileScanner.hasNext()) {
			String line = fileScanner.nextLine().trim();
			++count;
			
			String[] words = line.split("[\\s\\t]+");
			int currTid = Integer.parseInt(words[0].trim());
			int currCid = currTid;
			int itemId = Integer.parseInt(words[2].trim());
			
			try {
				stmt.setInt(1, currTid);
				stmt.setInt(2, currCid);
				stmt.setInt(3, itemId);

				stmt.execute();
			} catch (SQLException e) {
				System.out.println("Failed to execute SQL query ..");
			}
		}
		
		try {
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Failed to close the statement ..");
		}
		System.out.println("#Inserted " + count + " rows in db table : " + dbTableName);
	}
	
}
