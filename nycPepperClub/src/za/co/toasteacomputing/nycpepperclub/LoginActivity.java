package za.co.toasteacomputing.nycpepperclub;


//Imports for http services and json
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

//Imports for stream reading
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Other imports
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
public class LoginActivity extends Activity {

	private String userID;
	@SuppressWarnings("unused")
	private String Username;
	@SuppressWarnings("unused")
	private String Password;
	private String FirstName;
	private String LastName;
	@SuppressWarnings("unused")
	private String Telli;
	@SuppressWarnings("unused")
	private Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}
	
	public void onClickHelp(View view)
	{
		Intent helpIntent = new Intent(LoginActivity.this, Help.class);
		startActivity(helpIntent);
	}
	
	public void onClickLogin(View view)
	{
		//Get username & password from user
		EditText etxtUsername = (EditText)findViewById(R.id.etxtUsername);
		EditText etxtPassword = (EditText)findViewById(R.id.etxtPassword);
		
		//Send username & password to server for verification via post
		//Encryption not added atm.	
		
		String serviceUrl = "http://www.toasteacomputing.co.za/service_nyc_pepper_club_login.php";
		String postData = "?Username=" + (etxtUsername.getText().toString()).trim() + "&Password=" + etxtPassword.getText().toString();
		serviceUrl += postData;
		
		new ReadJSONFeedTask().execute(serviceUrl);
		
		//Login perfomed in postExecute method, after json has been read
	}
	
	
	// --- Connect and read JSON service from php service
    public String readJSONFeed(String URL) {
    	
    	Log.d("Login", "Attempting comms");
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);

        try {
        	Log.d("Login", "atempting httpGet");
            HttpResponse response = client.execute(httpGet);
            Log.d("Login", "httpGet completed, getting response line");
            StatusLine statusLine = response.getStatusLine();
            Log.d("Login", "responese line recieved, getting status code");
            int statusCode = statusLine.getStatusCode();
            
            Log.d("Login", "Status code:" + statusLine.getStatusCode());
            
            if (statusCode == 200) {
            	Log.d("Login", "Content provided");
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } else {
                Log.e("JSON", "Failed to download file");
            }
        } catch (ClientProtocolException e) {
        	Log.d("Login", "Exception");
            e.printStackTrace();
        } catch (IOException e) {
        	Log.d("Login", "Exception");
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            
        	Log.d("Login", "JSON recieved");

            try {
                // JSONObject for whole JSON structure
                JSONObject jObjAll = new JSONObject(result);

                // Loop through the array of daily forecasts, extracting each day's details
                
                    try {
                        
                        // Get UserID
                        userID = jObjAll.getString("UserID");
                        
                        // Get Username
                        Username = jObjAll.getString("Username");
                        
                        // Get Password
                        Password= jObjAll.getString("Password");
                        
                        // Get Frist Name
                        FirstName = jObjAll.getString("FirstName");
                        
                        //Get LastName
                        LastName = jObjAll.getString("LastName");
                        
                        //Get Telephone
                        Telli = jObjAll.getString("Telephone");
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }                 
                                        
                    //Login
                  //Check if login was successfull
                    TextView txtvError = (TextView)findViewById(R.id.txtvError);
            		if(userID.equalsIgnoreCase("null"))
            		{
            			//Display result                         
                        txtvError.setText("Username or Password incorrect");
                        txtvError.setVisibility(View.VISIBLE);
            		}
            		else
            		{
            			txtvError.setVisibility(View.GONE);
            			
            			//Open itinerary activity
            			Intent itineraryIntent = new Intent(LoginActivity.this, ItineraryActivity.class);
            			
            			//Add extras to intent
            			itineraryIntent.putExtra("UserID", userID);		
            			itineraryIntent.putExtra("FirstName", FirstName);
            			itineraryIntent.putExtra("LastName", LastName);	
            			
            			startActivity(itineraryIntent);
            		}
                    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
