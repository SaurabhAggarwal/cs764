package util;

import java.sql.Connection;

import model.aprioritid.ItemSetBar;

/**
 * For AprioriTid - read/write Cbar to database.
 * 
 * @author saurabh
 *
 */
public class AprioriDBUtils {
	
	private Connection dbConn = null;
	
	private String getItemsetBarTableNameForItemsetLength(int length)
	{
		return "C_bar_" + length;
	}
	
	public void createItemsetBarTable(int itemsetLength)
	{
		String tableName = getItemsetBarTableNameForItemsetLength(itemsetLength);
	}
	
	public void dropItemsetBarTable(int itemsetLength)
	{
		String tableName = getItemsetBarTableNameForItemsetLength(itemsetLength);
	}
	
	public void writeItemSetBar(ItemSetBar itemsetbar, int length)
	{
		String tableName = getItemsetBarTableNameForItemsetLength(length);
	}
	
	public ItemSetBar getNextItemSetBar()
	{
		return null;
	}
	
	public boolean hasNextItemSetBar() {
		return true;
	}
}
