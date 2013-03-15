package model.aprioritid;

import java.util.List;

import model.ItemSet;

public class CandidateItemset {
	
	private ItemSet[] itemsets;
	
	public CandidateItemset(int size)
	{
		this.itemsets = new ItemSet[size];
	}
	
	public ItemSet[] getItemsets() {
		return itemsets;
	}

	public void setItemsets(ItemSet[] itemsets) {
		this.itemsets = itemsets;
	}
	
	public void setItemsets(List<ItemSet> itemsets)
	{
		this.itemsets = new ItemSet[itemsets.size()];
		int i = 0;
		for(ItemSet itemset : itemsets)
			this.itemsets[i++] = itemset;
	}
}
