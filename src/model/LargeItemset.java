package model;

import java.util.ArrayList;
import java.util.List;

public class LargeItemset {
	private List<Integer> itemsetids;
	
	public LargeItemset()
	{
		this.itemsetids = new ArrayList<Integer>();
	}

	public List<Integer> getItemsetIds() {
		return itemsetids;
	}

	public void setItemsetIds(List<Integer> itemsets) {
		this.itemsetids = itemsets;
	}
}
