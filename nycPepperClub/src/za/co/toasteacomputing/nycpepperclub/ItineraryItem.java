package za.co.toasteacomputing.nycpepperclub;

import java.io.Serializable;

import za.co.toasteacomputing.nycpepperclub.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ItineraryItem implements Serializable
{
	private final static long serialVersionUID = 1L;
	private int ItemID;
	private Double Lat;
	private Double Lng;
	private String Name;
	private String DateAndTime;
	private String AddressLine1;
	private String AddressLine2;
	private String AddressLine3;
	private String Summary;
	private String ImageURL;
	
	
	public ItineraryItem()
	{
		
	}
	
	public ItineraryItem(int ItemID, Double Lat, Double Lng, String Name, String DateAndTime, String AddressLine1, String AddressLine2, String AddressLine3, String Summary, String ImageURL)
	{
		this.ItemID = ItemID;
		this.Lat = Lat;
		this.Lng = Lng;
		this.Name = Name;
		this.DateAndTime = DateAndTime;
		this.AddressLine1 = AddressLine1;
		this.AddressLine2 = AddressLine2;
		this.AddressLine3 = AddressLine3;
		this.Summary = Summary;
		this.ImageURL = ImageURL;
	}
	
	public ItineraryItem(Parcel source)
	{
		this.ItemID = source.readInt();
		this.Lat = source.readDouble();
		this.Lng = source.readDouble();
		this.Name = source.readString();
		this.DateAndTime = source.readString();
		this.AddressLine1 = source.readString();
		this.AddressLine2 = source.readString();
		this.AddressLine3 = source.readString();
		this.Summary = source.readString();
		this.ImageURL = source.readString();
	}
	
	//Getters and setters
	public int getItemID() {
		return ItemID;
	}

	public void setItemID(int itemID) {
		ItemID = itemID;
	}

	public Double getLat() {
		return Lat;
	}
	
	public Double getLng() {
		return Lng;
	}

	public void setLat(Double Lat) {
		this.Lat = Lat;
	}
	
	public void setLng(Double Lng) {
		this.Lng = Lng;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getDateAndTime() {
		return DateAndTime;
	}

	public void setDateAndTime(String dateAndTime) {
		DateAndTime = dateAndTime;
	}

	public String getAddressLine1() {
		return AddressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		AddressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return AddressLine2;
	}
	
	public String getAddressFormatted()
	{
		String formattedAddress = AddressLine1 + ", " + AddressLine2 + ", " + AddressLine3;
		return formattedAddress;
	}

	public void setAddressLine2(String addressLine2) {
		AddressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return AddressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		AddressLine3 = addressLine3;
	}

	public String getSummary() {
		return Summary;
	}

	public void setSummary(String summary) {
		Summary = summary;
	}

	public String getImageURL() {
		return ImageURL;
	}

	public void setImageURL(String imageURL) {
		ImageURL = imageURL;
	}	
	
	@SuppressLint("InflateParams")
	public Marker addItemToMap(GoogleMap gmMap, boolean focus, final Context context, final Bitmap bitmImgPlace)
	{
		LatLng Location = new LatLng(Lat, Lng);
		Marker locMarker;
		
		if(focus)
		{
			locMarker = gmMap.addMarker(new MarkerOptions().title(Name + " - " + DateAndTime).snippet(Summary).position(Location).icon(BitmapDescriptorFactory.fromResource(R.drawable.pepper_club_marker)));				
			gmMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Location, 12.0f), 2000, null);
			
			//need to set info window adapter here as well for initial displaying of window to be the same as others
			gmMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				
				@Override
				public View getInfoWindow(Marker marker) 
				{
					return null;
				}
				
				@Override
				public View getInfoContents(Marker marker) 
				{
					LayoutInflater layInflter = LayoutInflater.from(context);
					View vInfoWin = layInflter.inflate(R.layout.gm_info_window, null);
					
					ImageView ivPlaceImage = (ImageView)vInfoWin.findViewById(R.id.imgvPlaceImage);
					ivPlaceImage.setImageBitmap(bitmImgPlace);
					
					TextView txtvTitle = (TextView)vInfoWin.findViewById(R.id.txtvTitle);
					txtvTitle.setText(Name);
					
					TextView txtvDateAndTime = (TextView)vInfoWin.findViewById(R.id.txtvDateAndTime);
					txtvDateAndTime.setText(DateAndTime);
					
					TextView txtvSummary = (TextView)vInfoWin.findViewById(R.id.txtvSummary);
					txtvSummary.setText(Summary);
					
					return vInfoWin;
				}
			});
			
			locMarker.showInfoWindow();	
			
		}
		else
		{
			locMarker = gmMap.addMarker(new MarkerOptions().title(Name + " - " + DateAndTime).snippet(Summary).position(Location).icon(BitmapDescriptorFactory.fromResource(R.drawable.pepper_club_marker)));
		}		
		
		return locMarker;
	}
		
}
