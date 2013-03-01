import java.util.List;

import algos.Apriori;

import com.google.common.collect.Lists;

import model.Dataset;
import model.ItemSet;
import model.Transaction;
import util.FileReader;
import util.InputReader;

/**
 * Driver class for the application
 * 
 * @author shishir
 *
 */
public class Driver 
{
	public static void main(String[] args) {
		//InputReader reader = new FileReader();
		//List<Transaction> transactions = reader.getTransactions(Dataset.T5_I2_D100K);
		//System.out.println("Total transactions fetched : " + transactions.size());
		
		//testCandidateGeneration();
		testSubsetFunction();
	}
	
	private static void testCandidateGeneration()
	{
		List<ItemSet> largeSets = Lists.newArrayList();
		ItemSet a = new ItemSet(Lists.newArrayList(1,2,3), 0);
		ItemSet b = new ItemSet(Lists.newArrayList(1,2,4), 0);
		ItemSet c = new ItemSet(Lists.newArrayList(1,3,4), 0);
		ItemSet d = new ItemSet(Lists.newArrayList(1,3,5), 0);
		ItemSet e = new ItemSet(Lists.newArrayList(2,3,4), 0);
		largeSets.add(a);largeSets.add(b);largeSets.add(c);largeSets.add(d);largeSets.add(e);
		
		Apriori elem = new Apriori();
		List<ItemSet> candidateSets = elem.generateCandiateItemsets(largeSets, 3);
		System.out.println("Candidate Sets : " + candidateSets.toString());
		
	}
	
	private static void testSubsetFunction()
	{
		List<ItemSet> largeSets = Lists.newArrayList();
		ItemSet a = new ItemSet(Lists.newArrayList(1,2,3), 0);
		ItemSet b = new ItemSet(Lists.newArrayList(1,2,4), 0);
		ItemSet c = new ItemSet(Lists.newArrayList(1,3,4), 0);
		ItemSet d = new ItemSet(Lists.newArrayList(4,5,6), 0);
		largeSets.add(a);largeSets.add(b);largeSets.add(c);largeSets.add(d);
		
		Transaction t = new Transaction(1,1, Lists.newArrayList(1,2,3,4));
		
		List<ItemSet> subsets = Apriori.subset(largeSets, t);
		System.out.println("Subsets : " + subsets.toString());
		
	}
}
