package model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * HashTrees are used for efficiently searching for a pattern of items in a transaction in frequent 
 * itemset mining algorithms. This represents the structure of a hashtree node.
 *
 * @author shishir
 */
public class HashTreeNode 
{
	private Map<Integer, HashTreeNode> mapAtNode;
	private boolean isLeafNode;
	private List<ItemSet> itemsets;
	private HashTreeNode child;
	
	public HashTreeNode() {
		mapAtNode = Maps.newHashMap();
		isLeafNode = false;
		itemsets = Lists.newArrayList();
		child = null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IsLeaf : ").append(Boolean.toString(isLeafNode)).append("\t");
		builder.append("MapKeys :").append(mapAtNode.keySet().toString()).append("\t");
		builder.append("Itemsets : ").append(itemsets.toString());

		return builder.toString();
	}

	public Map<Integer, HashTreeNode> getMapAtNode() {
		return mapAtNode;
	}

	public void setMapAtNode(Map<Integer, HashTreeNode> mapAtNode) {
		this.mapAtNode = mapAtNode;
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public void setLeafNode(boolean isLeafNode) {
		this.isLeafNode = isLeafNode;
	}

	public List<ItemSet> getItemsets() {
		return itemsets;
	}

	public void setItemsets(List<ItemSet> itemsets) {
		this.itemsets = itemsets;
	}

	public HashTreeNode getChild() {
		return child;
	}

	public void setChild(HashTreeNode child) {
		this.child = child;
	}
	
}
