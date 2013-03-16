import algos.Apriori;
import model.Algorithm;
import model.Dataset;
import model.MinSup;
import model.Transaction;
import util.FileReader;
import util.InputReader;

/**
 * Class for ad-hoc tests.
 * 
 * @author shishir
 *
 */
public class TestClass 
{
	public static void main(String[] args) {
		testFileReader();
		testAlgosOnRefDataSet();
	}
	
	/*
	 * Test the frequent itemset mining algo on the reference dataset given in the paper.
	 */
	private static void testAlgosOnRefDataSet()
	{
		Apriori.runExperiment(Dataset.REF_TESTDATA, MinSup.REF_TESTDATA_MINSUP);
	}
	
	private static void testFileReader()
	{
		InputReader reader = new FileReader(Dataset.T5_I2_D100K, Algorithm.APRIORI);
		int count = 0;
		while(reader.hasNextTransaction()) {
			Transaction txn = reader.getNextTransaction();
			System.out.println( txn.toString());
			++count;
		}
		
		System.out.println("##Transactions : " + count);
	}
	
}
