import java.util.List;

import model.Algorithm;
import model.HashTreeNode;
import model.ItemSet;
import model.Transaction;
import util.HashTreeUtils;
import algos.Apriori;

import com.google.common.collect.Lists;

/**
 * Class for ad-hoc tests.
 * 
 * @author shishir
 *
 */
public class TestClass 
{
	public static void main(String[] args) {
		//InputReader reader = new FileReader();
		//List<Transaction> transactions = reader.getTransactions(Dataset.T5_I2_D100K);
		//System.out.println("Total transactions fetched : " + transactions.size());
		
		//testCandidateGeneration();
		//testSubsetFunction();
		//testHashTreeUtils();
		//testAISBasicMethods();
		//testGetExtensionItemsets();
		//testGenSupportForCandidateSets();
		ItemSet a = new ItemSet(Lists.newArrayList(1,2,3), 0);
		ItemSet b = new ItemSet(Lists.newArrayList(1,2,3), 0);
		System.out.println("#Hashcode --> " + a.hashCode() + ", " + b.hashCode());
	}
	
	// Tests the efficient generation of candidate sets from a transaction using hash tree
	private static void testHashTreeUtils()
	{
		List<ItemSet> candidateItemsets = Lists.newArrayList();
		candidateItemsets.add(new ItemSet(Lists.newArrayList(1,2,3), 0));
		candidateItemsets.add(new ItemSet(Lists.newArrayList(1,2,5), 0));
		candidateItemsets.add(new ItemSet(Lists.newArrayList(3,4,6), 0));

		HashTreeNode hashTreeRoot = HashTreeUtils.buildHashTree(candidateItemsets, 3);
		HashTreeUtils.printHashTree(hashTreeRoot);
		
		List<Integer> items = Lists.newArrayList(0,1,2,3,5);
		Transaction t = new Transaction(1,1,items, Algorithm.APRIORI);
		List<ItemSet> itemsets = HashTreeUtils.findItemsets(hashTreeRoot, t, 0);
		System.out.println("Itemsets : " + itemsets.toString());
	}

	// Tests the logic for candidate generation of size K from K-1 large itemsets
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
	
	// Tests the logic for subset generation from a transaction. This method is slightly inefficient as compared
	// to hash tree generation method.
	private static void testSubsetFunction()
	{
		List<ItemSet> largeSets = Lists.newArrayList();
		ItemSet a = new ItemSet(Lists.newArrayList(1,2,3), 0);
		ItemSet b = new ItemSet(Lists.newArrayList(1,2,4), 0);
		ItemSet c = new ItemSet(Lists.newArrayList(1,3,4), 0);
		ItemSet d = new ItemSet(Lists.newArrayList(4,5,6), 0);
		largeSets.add(a);largeSets.add(b);largeSets.add(c);largeSets.add(d);
		
		Transaction t = new Transaction(1,1, Lists.newArrayList(1,2,3,4), Algorithm.APRIORI);
		
		List<ItemSet> subsets = Apriori.subset(largeSets, t, 3);
		System.out.println("Subsets : " + subsets.toString());
		
	}
	
}
