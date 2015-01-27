package za.co.toasteacomputing.nycpepperclub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import za.co.toasteacomputing.nycpepperclub.R;

public class ItineraryActivity extends Activity{

	ArrayList<ItineraryItem> itemList = new ArrayList<ItineraryItem>();
	ListView lstvItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary);
		
		//Get extras
		Intent parentIntent = getIntent();
		
		String UserID = parentIntent.getStringExtra("UserID");
		String FirstName = parentIntent.getStringExtra("FirstName");
		String LastName = parentIntent.getStringExtra("LastName");
		
		//Set welcoming msg
		TextView txtvWelcome = (TextView)findViewById(R.id.txtvWelcome);
		txtvWelcome.setText("Welcome \n\t\t" + FirstName + " " + LastName);
		
		//Get List items. No Encryption atm	
		String serviceUrl = "http://www.toasteacomputing.co.za/service_nyc_pepper_club_events.php?UserID=" + UserID;		
		new ReadJSONItineraryTask().execute(serviceUrl);
		
		lstvItems = (ListView)findViewById(R.id.lstvItinerary);
		//Set on item click listener
		lstvItems.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				LocationManager locationManager = (LocationManager)ItineraryActivity.this.getSystemService(Context.LOCATION_SERVICE);
												
				if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
				{
					 // Provider not enabled, prompt user to enable it
		            Toast.makeText(context, "GPS is not enabled, please take a momment to enable it.", Toast.LENGTH_LONG).show();
		            Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		            startActivity(enableGPSIntent);
		            
		        } 
				else 
				{ 					
					Toast.makeText(context, "Loading Google Maps, this may take a momment", Toast.LENGTH_LONG).show();
					
		            // Provider is enabled
					//Open Maps activity
					Intent locationIntent = new Intent(context, LocationAcitvity.class);
					
					//Add extras to intent
					//Wrap itemList
					ItineraryItemWrapper wrappedItemList = new ItineraryItemWrapper(itemList);
					locationIntent.putExtra("ItemList", wrappedItemList);
					locationIntent.putExtra("SelectedItemID", ((TextView)view.findViewById(R.id.txtvID)).getText());				
					startActivity(locationIntent);
		        }			
			}			
		});
		
	}
	
	public void onClickHelp(View view)
	{
		Intent helpIntent = new Intent(ItineraryActivity.this, Help.class);
		startActivity(helpIntent);
	}
	
	public void buildListView(ArrayList<ItineraryItem> iList)
	{
				
		//create hash map list for list adapter
		ArrayList<HashMap<String, String>> itemHashMapList = new ArrayList<HashMap<String, String>>();
		
		for(int i = 0; i < iList.size(); i++)
		{
			//Create hash map and populate
			HashMap<String, String> hmMap = new HashMap<String, String>();
			
			//Add each child node to hash map Key : Value pairs
			hmMap.put("EventID", iList.get(i).getItemID() + "");
			hmMap.put("Name", iList.get(i).getName());
			hmMap.put("WhenWhere", iList.get(i).getDateAndTime() + "\n" + iList.get(i).getAddressFormatted());
			
			//add to hash list
			itemHashMapList.add(hmMap);
		}
		
		//Create list adapter
		ListAdapter lstAdapter = new SimpleAdapter(context, itemHashMapList, R.layout.itinerary_item, new String[] {"EventID", "Name", "WhenWhere"}, new int[] {R.id.txtvID, R.id.txtvTitle, R.id.txtvWhenWhere});
		
		//Update list view
		lstvItems.setAdapter(lstAdapter);	
	}
	
	//List of Itinerary objects
		boolean success;
		Context context = this;
		
		// Read custom JSON service from PHP Service
	    public String readJSONItinerary(String URL) {
	    	
	    	Log.d("Login", "Attempting comms");
	        StringBuilder stringBuilder = new StringBuilder();
	        HttpClient client = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(URL);

	        try {
	        	Log.d("GetEvents", "atempting httpGet");
	            HttpResponse response = client.execute(httpGet);
	            Log.d("GetEvents", "httpGet completed, getting response line");
	            StatusLine statusLine = response.getStatusLine();
	            Log.d("GetEvents", "responese line recieved, getting status code");
	            int statusCode = statusLine.getStatusCode();
	            
	            Log.d("Login", "Status code:" + statusLine.getStatusCode());
	            
	            if (statusCode == 200) {
	            	Log.d("GetEvents", "Content provided");
	                HttpEntity entity = response.getEntity();
	                InputStream content = entity.getContent();
	                BufferedReader reader = new BufferedReader(
	                        new InputStreamReader(content));

	                String line;
	                while ((line = reader.readLine()) != null) 
	                {
	                    stringBuilder.append(line);
	                }
	            } else {
	                Log.e("JSON", "Failed to download file");
	            }
	        } catch (ClientProtocolException e) {
	        	Log.d("GetEvents", "Exception");
	            e.printStackTrace();
	        } catch (IOException e) {
	        	Log.d("GetEvents", "Exception");
	            e.printStackTrace();
	        }

	        return stringBuilder.toString();
	    }

	    private class ReadJSONItineraryTask extends AsyncTask<String, Void, String> {
	        protected String doInBackground(String... urls) {
	            return readJSONItinerary(urls[0]);
	        }

	        protected void onPostExecute(String result) {
	            
	        	Log.d("Login", "JSON recieved");

	            try 
	            {
	                // JSONObject for whole JSON structure
	                JSONObject jObjAll = new JSONObject(result);
	                
	                try {
	                       //Check for successful dl
	                		success = jObjAll.getBoolean("Success");
	                		
	                		if(success)
	                		{
	                			//Extract and create list of ItineraryItem objects
	                			JSONArray events = jObjAll.getJSONArray("Events");                			
	                			               			
	                			for(int i = 0; i < events.length(); i++)
	                			{
	                				JSONObject jObjItem = events.getJSONObject(i);
	                				
	                				//Collect each items details
	                				int EventID = jObjItem.getInt("EventID");
	                				Double lat = jObjItem.getDouble("Latitude");
	                				Double lng = jObjItem.getDouble("Longitude");
	                				String Name = jObjItem.getString("EventName");
	                				String DateAndTime = jObjItem.getString("DateAndTime");
	                				String AddressLine1 = jObjItem.getString("AddressLine1");
	                				String AddressLine2 = jObjItem.getString("AddressLine2");
	                				String AddressLine3 = jObjItem.getString("AddressLine3");
	                				String Summary = jObjItem.getString("Summary");
	                				String ImageURL = jObjItem.getString("ImageURL");
	                				
	                					                				
	                				//create object
	                				ItineraryItem iItem = new ItineraryItem(EventID, lat, lng, Name, DateAndTime, AddressLine1, AddressLine2, AddressLine3, Summary, ImageURL);
	                				itemList.add(iItem);
	                			}                 			
	                			
	                			//Call method to construct list view
	                			buildListView(itemList);
	                		}
	                		else
	                		{
	                			
	                		}
	                        
	                    } catch (JSONException e) {
	                        e.printStackTrace();
	                    }
	                    
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        
	    }
	    
}
	        
	   
	    
	

