package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Represents itemsets i.e. a group of items that are bought together in a transaction, along
 * with their support counts in the input transaction dataset.
 * 
 * @author shishir
 *
 */
public class ItemSet implements Comparable<ItemSet> 
{
	private List<Integer> items;
	private int supportCount;
	
	public ItemSet(List<Integer> items, int supportCount) {
		super();
		setItems(items);
		this.supportCount = supportCount;
	}
	
	public ItemSet()
	{
		this(new ArrayList<Integer>(), 0);
	}

	@Override
	public String toString() {
		StringBuilder objStr = new StringBuilder();
		for(Integer item : items) {
			objStr.append(item).append(" ");
		}
		objStr.append("-");
		objStr.append(" ").append(supportCount);

		return objStr.toString();
	}

	// Two itemsets are equal if they have the same set of items.
	@Override
	public int hashCode() {
		return Objects.hashCode(this.items);
	}

	@Override
	public boolean equals(Object obj) {
		ItemSet other = (ItemSet) obj;
		return Objects.equal(this.items, other.items);
	}

	public List<Integer> getItems() {
		return items;
	}

	public void setItems(List<Integer> items) {
		this.items = items;
		Collections.sort(this.items); // Items must be sorted for the frequent itemset mining algos
	}

	public int getSupportCount() {
		return supportCount;
	}

	public void setSupportCount(int supportCount) {
		this.supportCount = supportCount;
	}
	
	@Override
	public int compareTo(ItemSet that) {
		return ComparisonChain.start().
				compare(this.items, that.items, Ordering.<Integer>natural().lexicographical()).
				result();
	}
	
	public void incrementSupportCount()
	{
		this.supportCount++;
	}
	
}
