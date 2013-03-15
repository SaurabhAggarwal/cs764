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
	SIMPLE(3, 2),
	T5_I2_D100K(5, 2),
	T10_I2_D100K(10, 2),
	T10_I4_D100K(10, 4),
	T20_I2_D100K(20, 2),
	T20_I4_D100K(20, 4),
	T20_I6_D100K(20, 6);
	
	int I, T;
	
	private Dataset(int T, int I)
	{
		this.T = T;
		this.I = I;
	}
	
	public int getAvgTxnSize()
	{
		return this.T;
	}
	
	public int getAvgMaxLargeItemsetSize()
	{
		return this.I;
	}
}
