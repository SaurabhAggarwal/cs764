/**
 * 
 */
package model.aprioritid;

import java.util.ArrayList;
import java.util.List;

/**
 * @author saurabh
 *
 */
public class CandidateItemsetBar {
	private List<ItemSetBar> itemsetbars;
	
	public CandidateItemsetBar()
	{
		itemsetbars = new ArrayList<ItemSetBar>();
	}

	public List<ItemSetBar> getItemsetbars() {
		return itemsetbars;
	}

	public void setItemsetbars(List<ItemSetBar> itemsetbars) {
		this.itemsetbars = itemsetbars;
	}
}
