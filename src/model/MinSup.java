package model;

/**
 * Represents the minimum support percentages for which the experiments have to be run.
 * 
 * Actual MinSupport = (minSupportPercentage/100)*(Total Transactions)
 * 
 * @author shishir
 */
public enum MinSup {
	REF_TESTDATA_MINSUP(50),
	TWO_PERCENT(2),
	ONE_POINT_FIVE_PERCENT(1.5),
	ONE_PERCENT(1),
	POINT_SEVEN_FIVE_PERCENT(0.75),
	POINT_FIVE_PERCENT(0.50),
	POINT_THREE_THREE_PERCENT(0.33),
	POINT_TWO_FIVE_PERCENT(0.25);

	private double minSupPercentage;
	
	private MinSup(double minSupPercentage) {
		this.minSupPercentage = minSupPercentage;
	}
	
	public double getMinSupPercentage() {
		return this.minSupPercentage;
	}
}
