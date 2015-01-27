package za.co.toasteacomputing.nycpepperclub;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LocationAcitvity extends Activity implements OnMarkerClickListener, OnInfoWindowClickListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener{
	
	private GoogleMap gmMap;
	private boolean mapAvialable = false;
	private int selectedItemID;
	ArrayList<ItineraryItem> itemList = new ArrayList<ItineraryItem>();
	private ArrayList<Marker> locMarkerList = new ArrayList<Marker>();
	ArrayList<Bitmap> imagesList = new ArrayList<Bitmap>();
	
	//Variables for location services
	private LocationClient gmLocationClient;
	private LocationRequest gmLocationRequest;
	private LatLng whereAmILocation;
	private Marker myLocation;
	private boolean haveLocation = false;
	
	//constants
	private final static int MILLISECONDS_PER_SECOND = 1000;
	public final static int UPDATE_INTERVAL_IN_SECONDS = 5;
	public final static int UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	private final static int FASTEST_INTERVAL_IN_SECONDS = 1;
	private final static int FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	private final static String COMPANY_TEL = "0810213160";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_acitvity);
		
		Intent intent = getIntent();		
		ItineraryItemWrapper wrappedItemList = (ItineraryItemWrapper)intent.getSerializableExtra("ItemList");
		itemList = wrappedItemList.getItemList();
		selectedItemID = Integer.parseInt(intent.getStringExtra("SelectedItemID"));	
		
		//Prepare all location images
		for(int i = 0; i < itemList.size(); i++)
		{
			DownloadImageTask dlImageTask = new DownloadImageTask();
			dlImageTask.execute(itemList.get(i).getImageURL());
		}
		
		setUpMap();
		setUpLocationServices();
		//rest of setup only gets called on postExecute when all images have been downloaded
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		//connect location client
		gmLocationClient.connect();
		
		if(mapAvialable)
			gmMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	}
	
	@Override
	protected void onStop()
	{
		//disconnect location client
		gmLocationClient.disconnect();
		
		super.onStart();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		setUpMap();
	}
	
	public void setUpLocationServices()
	{
		//Create and initialize locationRequest object
		gmLocationRequest = new LocationRequest();
		gmLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		gmLocationRequest.setInterval(UPDATE_INTERVAL);
		gmLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		//Create location client
		gmLocationClient = new LocationClient(this, this, this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connResult) 
	{
		Toast.makeText(this, "Woops Location services has failed!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnected(Bundle dataBundle) 
	{
		Toast.makeText(this, "Location Services Connected", Toast.LENGTH_LONG).show();
		gmLocationClient.requestLocationUpdates(gmLocationRequest, this);		
	}

	@Override
	public void onDisconnected() 
	{
		Toast.makeText(this, "Location Services Disconnected", Toast.LENGTH_LONG).show();		
	}

	@Override
	public void onLocationChanged(Location location) 
	{
		//Receive updated location
		whereAmILocation = new LatLng(location.getLatitude(), location.getLongitude());				
	}
	
	//Place all items on map
	public void showLocations(ArrayList<ItineraryItem> itemList)
	{		
		for(int i = 0; i < itemList.size(); i++)
		{		
			if(itemList.get(i).getItemID() == selectedItemID)
			{
				locMarkerList.add(itemList.get(i).addItemToMap(gmMap, true, this, imagesList.get(i)));
				updateAddressBar(itemList.get(i).getAddressLine1(), itemList.get(i).getAddressLine2(), itemList.get(i).getAddressLine3());
			}
			else
				locMarkerList.add(itemList.get(i).addItemToMap(gmMap, false, this, null));
		}
	}
	
	public void updateAddressBar(String AddressLine1, String AddressLine2, String AddressLine3)
	{
		TextView txtvAddressLine1 = (TextView)findViewById(R.id.txtvStreet);
		TextView txtvAddressLine2 = (TextView)findViewById(R.id.txtvCity);
		TextView txtvAddressLine3 = (TextView)findViewById(R.id.txtvPostalCode);
		
		txtvAddressLine1.setText(AddressLine1);
		txtvAddressLine2.setText(AddressLine2);
		txtvAddressLine3.setText(AddressLine3);
	}	
	
	public void setUpMap()
	{
		
		//Check that map has not already been instantiated
		if(gmMap == null)
		{
			//get Handle to map
			gmMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.Map)).getMap();
			
			//Test if map was successfully instantiated
			if(gmMap != null)
			{
				mapAvialable = true;	
				gmMap.setOnMarkerClickListener(this);
				gmMap.setOnInfoWindowClickListener(this);
			}
			else
				Toast.makeText(this, "Map could not be intialized", Toast.LENGTH_LONG).show();
		}
		
	}
	
	public void onClickWhereAmI(View view)
	{
		Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();
		if(haveLocation)
			myLocation.remove();
		
		myLocation = gmMap.addMarker(new MarkerOptions().position(whereAmILocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.you_are_here_marker)));
		gmMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
		gmMap.animateCamera(CameraUpdateFactory.newLatLng(whereAmILocation), 2000, null);
		haveLocation = true;
		myLocation.hideInfoWindow();
	}
	
	public void onClickCall(View view)
	{
		String uri = "tel:" + COMPANY_TEL.trim();
		Intent callIntent = new Intent(Intent.ACTION_DIAL);
		callIntent.setData(Uri.parse(uri));
		startActivity(callIntent);
	}

	@Override
	public boolean onMarkerClick(Marker markerClicked) 
	{
		for(int i = 0; i < locMarkerList.size(); i++)
		{
			if(markerClicked.equals(locMarkerList.get(i)))
			{
				//marker array and itemList array are parallel
				updateAddressBar(itemList.get(i).getAddressLine1(), itemList.get(i).getAddressLine2(), itemList.get(i).getAddressLine3());
				
				final int j = i;
				
				gmMap.setInfoWindowAdapter(new InfoWindowAdapter() {
					
					@Override
					public View getInfoWindow(Marker marker) {
						return null;
					}
					
					@SuppressLint("InflateParams")
					@Override
					public View getInfoContents(Marker marker) 
					{
							LayoutInflater layInflter = getLayoutInflater();
							View vInfoWin = layInflter.inflate(R.layout.gm_info_window, null);
							
							ImageView ivPlaceImage = (ImageView)vInfoWin.findViewById(R.id.imgvPlaceImage);
							ivPlaceImage.setImageBitmap(imagesList.get(j));
							
							TextView txtvTitle = (TextView)vInfoWin.findViewById(R.id.txtvTitle);
							txtvTitle.setText(itemList.get(j).getName());
							
							TextView txtvDateAndTime = (TextView)vInfoWin.findViewById(R.id.txtvDateAndTime);
							txtvDateAndTime.setText(itemList.get(j).getDateAndTime());
							
							TextView txtvSummary = (TextView)vInfoWin.findViewById(R.id.txtvSummary);
							txtvSummary.setText(itemList.get(j).getSummary());
							
							return vInfoWin;
					}
				});
			}
		}
		return false;
	}

	@Override
	public void onInfoWindowClick(Marker marker) 
	{
		for(int i = 0; i < locMarkerList.size(); i++)
		{
			if(marker.equals(locMarkerList.get(i)))
			{
				//Use of itemList possible becuase itemList and locMarkerList a parallel
				String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", locMarkerList.get(i).getPosition().latitude, locMarkerList.get(i).getPosition().longitude, itemList.get(i).getName());
				Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				navigationIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				
				try
				{
					startActivity(navigationIntent);
				}
				catch(ActivityNotFoundException anfe)
				{
					try
					{
						Intent anyMapsAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(anyMapsAppIntent);
					}
					catch(ActivityNotFoundException innerAnfe)
					{
						Toast.makeText(this, "Please install any navigation application", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}
	
	
	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		public DownloadImageTask()
		{
			
		}
		
	    @Override
	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap bitmPlaceImage = null;
	        try {
	          InputStream in = new java.net.URL(urldisplay).openStream();
	          bitmPlaceImage = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return bitmPlaceImage;
	    }
	    @Override
	    protected void onPostExecute(Bitmap result) 
	    {            
	    	imagesList.add(result);
	    	
	    	//Check if all images have been downloaded
	    	if(imagesList.size() == itemList.size())
	    	{
	    		//Check image dl is done if so show all views and hide progress circle		
	    		//Get all views to unhide
	    		LinearLayout linlayAddress = (LinearLayout)findViewById(R.id.linlayAddress);
	    		LinearLayout linlayMap = (LinearLayout)findViewById(R.id.linlayMap);
	    		LinearLayout linlayButtons = (LinearLayout)findViewById(R.id.linlayButtons);
	    		
	    		//Unhide all
	    		linlayAddress.setVisibility(View.VISIBLE);
	    		linlayMap.setVisibility(View.VISIBLE);
	    		linlayButtons.setVisibility(View.VISIBLE);
	    		
	    		//Hide progress circle
	    		ProgressBar progbImageDL = (ProgressBar)findViewById(R.id.progbLoadImages);
	    		progbImageDL.setVisibility(View.GONE);
	    		
	    		//PLace all items on map
	    		showLocations(itemList);
	    	}
	    }
	  }
	

}
