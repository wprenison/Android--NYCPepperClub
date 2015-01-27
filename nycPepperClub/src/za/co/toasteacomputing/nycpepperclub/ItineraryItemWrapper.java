package za.co.toasteacomputing.nycpepperclub;

import java.io.Serializable;
import java.util.ArrayList;

public class ItineraryItemWrapper implements Serializable
{
	private final static long serialVersionUID = 1L;
	private ArrayList<ItineraryItem> itemList;
	
	public ItineraryItemWrapper(ArrayList<ItineraryItem> itemList)
	{
		this.itemList = itemList;
	}
	
	public ArrayList<ItineraryItem> getItemList()
	{
		return itemList;
	}
	
}
