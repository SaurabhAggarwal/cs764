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

	SIMPLE(3, 2, 4),
	T5_I2_D100K(5, 2, 97048),
	T10_I2_D100K(10, 2, 99916),
	T10_I4_D100K(10, 4, 98395),
	T20_I2_D100K(20, 2, 100000),
	T20_I4_D100K(20, 4, 99996),
	T20_I6_D100K(20, 6, 99942);

	private int avgTxnSize, avgMaxLargeItemsetSize;
	private int numTxns;
	
	private Dataset(int avgTxnSize, int avgMaxLargeItemsetSize, int numTxns)
	{
		this.avgTxnSize = avgTxnSize;
		this.avgMaxLargeItemsetSize = avgMaxLargeItemsetSize;
		this.numTxns = numTxns;
	}
	
	public int getAvgTxnSize()
	{
		return this.avgTxnSize;
	}
	
	public int getAvgMaxLargeItemsetSize()
	{
		return this.avgMaxLargeItemsetSize;
	}
	
	// Number of distinct transactions in this dataset.
	public int getNumTxns() {
		return numTxns;
	}
}