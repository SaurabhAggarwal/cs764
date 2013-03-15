package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

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
	
	private int[] generators;
	private List<Integer> extensions;
	
	public ItemSet(List<Integer> items, int supportCount) {
		super();
		setItems(items);
		this.supportCount = supportCount;
		this.generators = new int[2];
		this.extensions = new ArrayList<Integer>();
		
		Collections.sort(this.items);
	}
	
	public ItemSet()
	{
		this(new ArrayList<Integer>(), 0);
	}

	public ItemSet(List<Integer> items, int supportCount, Integer generator1, Integer generator2) {
		this(items, supportCount);
		this.generators[0] = generator1;
		this.generators[1] = generator2;
	}

	@Override
	public String toString() {
		return "ItemSet [items=" + Arrays.toString(items.toArray()) + ", supportCount=" + supportCount + "]";
	}

	// Two itemsets are equal if they have the same set of items.
	@Override
	public int hashCode() {
		return Objects.hashCode(this.items);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ItemSet other = (ItemSet) obj;
		Collections.sort(this.items);
		Collections.sort(other.items);

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
		List<Integer> thisItems = this.getItems();
		List<Integer> thatItems = that.getItems();
		if(thisItems == thatItems) {
			return 0;
		}
		
		// Compare individual items in the list. At the first mismatch based on the difference in the two sets,
		// we can determine which list is supposed to lexically appear first.
		for(int i=0; i < thisItems.size(); i++) {
			int diff = thisItems.get(i).compareTo(thatItems.get(i));
			if(diff != 0) {
				return diff;
			}
		}

		return 0;
	}
	
	public void incrementSupportCount()
	{
		this.supportCount++;
	}
	
	public void addExtension(Integer extension)
	{
		this.extensions.add(extension);
	}
}
