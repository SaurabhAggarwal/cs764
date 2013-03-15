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
	T5_I2_D100K(97048),
	T10_I2_D100K(99916), 
	T10_I4_D100K(98395), 
	T20_I2_D100K(100000), 
	T20_I4_D100K(99996), 
	T20_I6_D100K(99942);
	
	// Number of distinct transactions in this dataset.
	private int numTxns;

	Dataset(int numTxns) {
		this.numTxns = numTxns;
	}

	public int getNumTxns() {
		return numTxns;
	}

}
