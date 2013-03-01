package model;

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
	
	public ItemSet(List<Integer> items, int supportCount) {
		super();
		this.items = items;
		this.supportCount = supportCount;
		
		Collections.sort(this.items);
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
		
		for(int i=0; i < thisItems.size(); i++) {
			int diff = thisItems.get(i).compareTo(thatItems.get(i));
			if(diff != 0) {
				return diff;
			}
		}

		return 0;
	}

	
}
