package model;

/**
 * Represents the various datasets for which the frequent itemset mining algorithms have to be tested.
 * The general naming of the dataset is T<?>_I<?>_D<?>K
 * 
 * where 	T -> Average size of transactions
 * 			I -> Average size of the maximally potential large itemsets
 * 			D -> Number of transactions
 * 
 * 			Number of maximally potential large itemsets = 2000
 * 			Number of items = 1000
 * 			Correlation level = 0.5
 * 
 * @author shishir
 *
 */
public enum Dataset {
	REF_TESTDATA(3, 2, 4, "/data/simple", "REF_TESTDATASET"),
	T5_I2_D100K(5, 2, 97048,    "/data/T5.I2.D100K",  "T5_I2_D100K"),
	T10_I2_D100K(10, 2, 99916,  "/data/T10.I2.D100K", "T10_I2_D100K"),
	T10_I4_D100K(10, 4, 98395,  "/data/T10.I4.D100K", "T10_I4_D100K"),
	T20_I2_D100K(20, 2, 100000, "/data/T20.I2.D100K", "T20_I2_D100K"),
	T20_I4_D100K(20, 4, 99996,  "/data/T20.I4.D100K", "T2_I4_D100K"),
	T20_I6_D100K(20, 6, 99942,  "/data/T20.I6.D100K", "T20_I6_D100K");

	private int avgTxnSize, avgMaxLargeItemsetSize;
	// Total number of distinct transactions in this dataset
	private int numTxns;
	
	// The filesystem location where this dataset is stored
	private String dataFileLocation;
	// The database table for this dataset.
	private String datasetDBTable;

	private Dataset(
			int avgTxnSize, int avgMaxLargeItemsetSize, int numTxns, 
			String dataFileLocation, String datasetDBTable)
	{
		this.avgTxnSize = avgTxnSize;
		this.avgMaxLargeItemsetSize = avgMaxLargeItemsetSize;
		this.numTxns = numTxns;
		this.dataFileLocation = dataFileLocation;
		this.datasetDBTable = datasetDBTable;
	}
	
	public int getAvgTxnSize()
	{
		return this.avgTxnSize;
	}
	
	public int getAvgMaxLargeItemsetSize()
	{
		return this.avgMaxLargeItemsetSize;
	}
	
	public int getNumTxns() {
		return numTxns;
	}

	public String getDataFileLocation() {
		return dataFileLocation;
	}

	public String getDatasetDBTable() {
		return "mining_datasets." + datasetDBTable;
	}
}