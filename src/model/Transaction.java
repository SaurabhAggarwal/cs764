package model;

import java.util.Collections;
import java.util.List;

import util.Constants;

/**
 * Represents a real-world retail store transaction. It consists of :
 * 	- tid : Transaction Id
 * 	- cid : Customer Id
 *  - items : List of items purchased in this transaction.
 *  
 *  If transaction id is not present in the dataset, it is defaulted to customer id.
 *  @author shishir
 */
public class Transaction 
{
	private int tid;
	private int cid;
	private List<Integer> items; // Required for Apriori Algorithm
	private int[] itemVector;	// Required for AIS Algorithm
	
	public Transaction(int tid, int cid, List<Integer> items, Algorithm algo) {
		super();
		this.tid = tid;
		this.cid = cid;
		
		if(algo.equals(Algorithm.AIS)) {
			setItemVector(items);
		}
		else {
			setItems(items);
		}

		this.items = items;
	}

	@Override
	public String toString() {
		return "Transaction [tid=" + tid + ", cid=" + cid + ", items=" + items + "]";
	}

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public List<Integer> getItems() {
		return items;
	}

	public void setItems(List<Integer> items) {
		this.items = items;

		// Keep the list of items in a transaction sorted. This aids in the candidate generation and pruning phase.
		Collections.sort(this.items);
	}

	public int[] getItemVector() {
		return itemVector;
	}

	public void setItemVector(List<Integer> items) {
		int numItems = Constants.NUMBER_OF_ITEMS;
		this.itemVector = new int[numItems];
		for(Integer item : items) {
			if(item > numItems) {
				System.err.println("Item number " + item + " is greater than the number of items " +  numItems);
				continue;
			}

			this.itemVector[item] = 1; // Set flag for the presence of this item in transaction
		}
	}
	
}
