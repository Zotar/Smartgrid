package dk.mercantec.smartgrid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import dk.mercantec.smartgrid.DataBaseHelper;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


import android.R.menu;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity 
{

	//google map object
	private GoogleMap gMap;
	
	//create SharedPreferences storage space
	private static final String PREFS_SETTING = "PrefsStorage";
    
	//progress object
	private ProgressBar progressbar;
   
	//SOAP from eurisco A/S
    //following variables according to the WSDL
    private final String NAMESPACE = "http://flexprice.dk/ns";
    private final String URL = "http://backend.eurisco.dk:4040/webservices/flexprice?wsdl";
    private final String SOAP_ACTION = "http://flexprice.dk/ns/GetFlexPrice";
    private final String METHOD_NAME = "GetFlexPrice";

	/*
	//test other webservice from http://www.w3schools.com/webservices/tempconvert.asmx
    private final String NAMESPACE = "http://www.w3schools.com/webservices/";
    private final String URL = "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL";
    private final String SOAP_ACTION = "http://www.w3schools.com/webservices/FahrenheitToCelsius";
    private final String METHOD_NAME = "FahrenheitToCelsius";
*/

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	
    	//Getting SharedPreferences from internal storage with "settings" and 
		//a default of "WindTurbine" , default is only used if nothing is save in storage
    	SharedPreferences settings = getSharedPreferences(PREFS_SETTING,0);
    	String settingTitle = settings.getString("settings", "WindTurbine");
   	
    	//Set new Title on App with last used settings
    	String title = getText(R.string.app_name).toString();
        setTitle(title + " - " + settingTitle);
	
        // Update the action bar title (test only working API 11+)
        //ActionBar actionBar = getActionBar();
        //actionBar.setTitle(R.string.app_name + " - " + settingTitle);
        
		//start main activity layout
		setContentView(R.layout.activity_main);		
	
		//progressbar handler
		progressbar = (ProgressBar) findViewById(R.id.action_progress);		
		
        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        gMap = fm.getMap();
    	
    	// Creating a LatLng object for the current location and move to Center af denmark
        LatLng latLng = new LatLng(55.844482, 10.874634);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 
        // Zoom in the Google Map at a level where all (most) of Denmark will be visible
        gMap.animateCamera(CameraUpdateFactory.zoomTo(7));
         
        //Custom pop-up windows
        gMap.setInfoWindowAdapter(new InfoWindowAdapter()
        {
        
        	@Override
        	public View getInfoWindow(Marker arg0)
        	{
        		return null;
        	}
        	
        	@Override
        	public View getInfoContents(Marker arg0)
        	{
        		View v = getLayoutInflater().inflate(R.layout.infowindow, null);
        		
        		TextView tvTitle = (TextView) v.findViewById(R.id.tv_Title);
        		
        		TextView tvData = (TextView) v.findViewById(R.id.tv_Data);
        		
        		tvTitle.setText(Html.fromHtml("<b>" + arg0.getTitle() + "</b"));
        		
        		tvData.setText(arg0.getSnippet());
        		
        		return v;
        	}
        		
        });
        
        
        //Start AsyncTask
        //new DownloadSOAPTask().execute("1013");
        
        MarkerMap();		        
        
        System.out.println("All done");
	}
	
	
	//Draw Androidmarker and popup , from weather info in database
	//Get data from database and starter DownloadWeatherTask as a AsyncTask
	private void MarkerMap()
	{
		
		//DatabaseHelper object
		final DataBaseHelper myDbHelper = new DataBaseHelper(this.getApplicationContext());
	
    	Log.i("database", "Start db load");
        try 
        {
        	myDbHelper.createDataBase();
        	myDbHelper.openDataBase();
	 	}
        catch (IOException ioe)
        {
	 		throw new Error("Unable to create database");
	 	}
        catch(SQLException sqle)
        {
	 		throw sqle;
	 	}
               		
		String Contry = "Denmark";

        ArrayList<Weather> listweather = myDbHelper.listWeatherInCountry(Contry);
    
        myDbHelper.close();
              
        //Start AsyncTask
        new DownloadWeatherTask().execute(listweather);
	}
	
	//convert Java date to SOAPdatetime
	public static String getFullDateTime(Date oldTime){  
		  
		String NewTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(oldTime);  
	  
		Log.i("soap",NewTime);
		
	    return NewTime;
	} 
	
	//Download price data from EURISCO A/s webserver with SOAP as a AsyncTask  
	private class DownloadSOAPTask extends AsyncTask<String, Void, String> 
	{
			
	     protected String doInBackground(String... params) 
	     {

	 		String SOAPresponse ="";
			Log.i("soap", "start");
			
	        //get Current javadate
			Date oldTime = new Date();
			
    		try{
/*		
    	        ObjectFactory fact = new ObjectFactory();
    	        FlexPriceRequest fpRequest = fact.createFlexPriceRequest();
    			
    			//FlexPriceRequest fpRequest = new FlexPriceRequest();
                RequestId reqID = new RequestId();
                reqID.setDeviceId("1013");
                reqID.setMeterId("1013");
                  
                XMLGregorianCalendar xmlGC = new XMLGregorianCalendar();
                xmlGC.setYear(2014);
                xmlGC.setMonth(2);
                xmlGC.setDay(27);
                xmlGC.setHour(8);
                xmlGC.setMinute(45);
                xmlGC.setSecond(0);
                
                
                fpRequest.setStartingTime(xmlGC);
                String[] signalTypeId = new String[] { "1" };
                fpRequest.setId(reqID);
                fpRequest.setDuration(7200);

                FlexPriceSignal[] resp = getFlexPrice(fpRequest.getId(), signalTypeId, fpRequest.getStartingTime(), fpRequest.getDuration());

                String Price = resp[0].getEntries().toString();
*/
 		
    			    //SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			      	/* 
			      	 //Test
			         PropertyInfo idProp =new PropertyInfo();
			         idProp.setName("Fahrenheit");
			         idProp.setValue("100");
			         idProp.setType(String.class);
			         request.addProperty(idProp);
			      	 */		      	 
/*			     
			      	 SoapObject id = new SoapObject (NAMESPACE , "id");
                     id.addProperty("deviceId", "1013");
                     id.addProperty("meterId", "1013");
                     request.addSoapObject(id);

			         request.addProperty("duration" ,7200);	
			         request.addProperty("startingTime", getFullDateTime(oldTime));	
			         request.addProperty("signalTypeId" , "1");	
			         
                     Log.i("soap", "Setting up request...");
			         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			         envelope.dotNet = false;
			         envelope.setOutputSoapObject(request);
			         Log.i("soap", request.toString() );
			         
			         Log.i("soap", "About to send request...");
			         HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			         androidHttpTransport.call(SOAP_ACTION, envelope);
			         Log.i("soap", "Receiving now...");
			         Log.i("soap",  envelope.bodyIn.toString());
			         SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
			         SOAPresponse = response.toString();
			         Log.i("soap", SOAPresponse);
*/			         
			         
			}
		     
		    catch(Exception e){
		     e.printStackTrace();
		    }
		
		    Log.i("soap", SOAPresponse);
		    return SOAPresponse;
	     }

		@Override
		protected void onPostExecute(String result) 
		{			
						
			if(result!=null)
			{
				Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
			}
						
			super.onPostExecute(result);		
			
		}
		
	 }	
	
	//Read HTTP from URL line for line and return the response out from each icao code
	private String readGeonames(String icaoCode) 
	{
			
	    String GeonamesURL = "http://api.geonames.org/weatherIcaoJSON?ICAO=" + icaoCode + "&username=bigherman";
	
		StringBuilder builder = new StringBuilder();
	    	    
	    HttpGet httpGet = new HttpGet(GeonamesURL);
	    HttpParams httpParameters = new BasicHttpParams();
	    
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			
		client.setParams(httpParameters);
	    
	    try
	    {
	    	HttpResponse response = client.execute(httpGet);
	    	StatusLine statusLine = response.getStatusLine();
	    	int statusCode = statusLine.getStatusCode();
	    	//200 is server response code (ok)
	    	if (statusCode == 200)
	    	{
	    		HttpEntity entity = response.getEntity();
	    		InputStream content = entity.getContent();
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    		String line;
	    		while ((line = reader.readLine()) != null)
	    		{
	    			builder.append(line);
	    		}
	    	}
	    	else
	    	{
	    		Log.e(MainActivity.class.toString(), "Failed to download file");
	    	}
	    }
	    catch (ClientProtocolException e)
	    {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error connecting to server during download process");
	    }
	    catch (IOException e)
	    {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error during download process - all Weatehrstatision may not be displayed");
	    }
	    
	    return builder.toString();
	}
	
	//AsyncTask download weather information form geonames
	private class DownloadWeatherTask extends AsyncTask<ArrayList<Weather>, MarkerOptions, String> 
	{

	    protected void onPreExecute()
	    {
	    	progressbar.setProgress(0);
	    	
	        progressbar.setVisibility(View.VISIBLE);

	        Log.i("progressbar", "0");
	        
	        
	    };
	
	     protected String doInBackground(ArrayList<Weather>... listweather) 
	     {
    	 	String exception = null;
	    	JSONObject jsonObject = null;
	    	JSONObject jsonData = null;

	    	String clouds = "";
			String windDirection = "";
			int windSpeed = 0;
			String datetime = "";
			String SnippedText = "";
	    	
			String temperature = "";
			String stationName = "";
			
    	 	int icon_state=R.drawable.icn_empty;
    	 	
    	 	String readGeonamesData;

            for (int i=0; i<listweather[0].size();i++)
            {
            	          	
/*            	
            	// test code
 
    	    	MarkerOptions marker = null;
    		    
    	    	marker = new MarkerOptions()   	
    			.position(new LatLng(listweather[0].get(i).getLat(), listweather[0].get(i).getLng()))
    			.title(listweather[0].get(i).getName())
    			.snippet(listweather[0].get(i).getIcaoCode())
    			.icon(BitmapDescriptorFactory.fromResource(icon_state));
				
    	    	if (marker!=null)
				{
					publishProgress(marker);
				}
            	
 */   	    	
            	//Getting SharedPreferences from internal storage with "settings" and 
            	//a default of "WindTurbine" , default is only used if nothing is save in storage
            	SharedPreferences settings = getSharedPreferences(PREFS_SETTING,0);
            	String settingsave = settings.getString("settings", "WindTurbine");
            	
            	try
            	{
            		readGeonamesData = readGeonames(listweather[0].get(i).getIcaoCode());
	            	
	            	if(readGeonamesData != null)
	            	{
	            		Log.i("weatherdata", readGeonamesData);
	            		
	            		try 
	            		{
      			
							jsonObject = new JSONObject(readGeonamesData);
							jsonData = jsonObject.getJSONObject("weatherObservation");								
							
							clouds = jsonData.getString("clouds");
							
							//Log.i("clouds", clouds); // test log info window
						
							windSpeed = Integer.parseInt(jsonData.getString("windSpeed"));						
							windDirection = jsonData.getString("windDirection");
							datetime = jsonData.getString("datetime");
							
							//temperature = jsonData.getString("temperature"); 
							//stationName = jsonData.getString("stationName");
						
							if(settingsave.contentEquals ("SunCells"))
							{
							
		                            //Suncells icon out from clouds density in octave 
								    //test for 0/8 clouds cover
		                            if (clouds.contentEquals("no clouds detected") || clouds.contentEquals("nil significant cloud") 
		                            	|| clouds.contentEquals("clouds and visibility OK"))
				        	    	{
				        	    		icon_state=R.drawable.icn_green;
				        	    	}
		                            //Test for 2-3/8 clouds cover
				        	    	else if (clouds.contentEquals("few clouds") || clouds.contentEquals("scattered clouds"))
				        	    	{
				        	    		icon_state=R.drawable.icn_yellow;
				        	    	}
		                            //test for 5-8/8 clouds cover
				        	    	else if (clouds.contentEquals("overcast") || clouds.contentEquals("broken clouds"))
				        	    	{
				        	    		icon_state=R.drawable.icn_red;
				        	    	}
		                            //no info available 
				        	    	else
				        	    	{
				        	    	 	 icon_state=R.drawable.icn_empty;		        	    		
				        	    	}
		                            //Set text snipped
		                            SnippedText = "Date:" + datetime + " Clouds density:" + clouds ;
		                            
							}
							
							if(settingsave.contentEquals ("WindTurbine"))
							{
	                            	//Windtubines icon out from windspeed
								    //windSpeed is >15 kts and <= 50 kts
		                            if (windSpeed > 15 &&  windSpeed <=50 )
				        	    	{
				        	    		icon_state=R.drawable.icn_green;
				        	    	}
		                            //windSpeed is >5 and <= 15 kts
				        	    	else if (windSpeed > 5 &&  windSpeed <=15 )
				        	    	{
				        	    		icon_state=R.drawable.icn_yellow;
				        	    	}
		                            // windSpeed is >50 or <= 5 kts
				        	    	else if (windSpeed > 50  ||  windSpeed <=5 )
				        	    	{
				        	    		icon_state=R.drawable.icn_red;
				        	    	}
		                            //no info available 
				        	    	else
				        	    	{
				        	    	 	 icon_state=R.drawable.icn_empty;		        	    		
				        	    	}
								    //Set text snipped
		                            SnippedText  = "Date:" + datetime + " Windspeed :" + windSpeed + "kpt" +
								    " Winddirection:" + windDirection + (char) 0x00B0; // (char) 0x00B0 = Degrees symbol
							
							}
							
		        	    	MarkerOptions marker = null;
		        	    	
		        	    	marker = new MarkerOptions()
							.position(new LatLng(listweather[0].get(i).getLat(), listweather[0].get(i).getLng()))
							.title(listweather[0].get(i).getName())
							//test json full response text
							//.snippet(jsonData.toString())
							.snippet(SnippedText)
							.icon(BitmapDescriptorFactory.fromResource(icon_state));
		        	    	
							if (marker!=null)
							{								

								publishProgress(marker);
							}
	            		}
	            		
	            		catch (JSONException e)
	        	    	{
							Log.i("ex", e.getMessage());
						}
	            	}
	            	
	            	else
	            	{
	            		MarkerOptions marker = null;
		            	
						marker = new MarkerOptions()
						.position(new LatLng(listweather[0].get(i).getLat(), listweather[0].get(i).getLng()))
						.title(listweather[0].get(i).getName())
						.snippet("No Geonames data available")
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.icn_empty));
						if (marker!=null)
						{							
							publishProgress(marker);
						}
	            	}
            	}
            	
            	catch (Exception ex)
            	{
            		Log.i("ex", ex.getMessage());
            		exception = ex.getMessage();
            	}
 
            	
    		}
            return exception;
	     }

	     @Override
	     protected void onProgressUpdate(MarkerOptions... marker)
	     {
	    	 gMap.addMarker(marker[0]);
	    	 
	    	 //test code
	    	 //iProgress = iProgress + iMaxlistweather;
	    	 //progressbar.setProgress(iProgress);
		     // Log.i("IncrementProgressBy", String.valueOf(iMaxlistweather));
		     // Log.i("Progress", String.valueOf(iProgress));

	    	 progressbar.incrementProgressBy(6);
    	
	    	 super.onProgressUpdate();
	     }

		@Override
		protected void onPostExecute(String result) 
		{			
			
			progressbar.setProgress(100);
			
			progressbar.setVisibility(View.GONE);			
			
			if(result!=null)
			{
				Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
			}
			
			Toast.makeText(getApplicationContext(), "Update Finished", Toast.LENGTH_LONG).show();
			
			//reset progress bar again 
			progressbar.setProgress(0);
			
			super.onPostExecute(result);		
			
		}
		
	 }	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		
	    //Get SharedPreferences and Editor object to make preference changes.
	    SharedPreferences settings = getSharedPreferences(PREFS_SETTING, 0);
	    SharedPreferences.Editor editor = settings.edit();
	      
	    String title = "";
	    
	    // Handle presses on the action bar items
	    switch (item.getItemId())
	    {
	        case R.id.SunCells:
	    		gMap.clear();
	    		
	    		//Set setting value
	    	    editor.putString("settings", "SunCells");
	    	    // Commit the edits to 
	    	    editor.commit();

	    	    //Set new Title
	        	title = getText(R.string.app_name).toString();
	            setTitle(title + " - " + "SunCells");
	            
	           //Start again with marking map
	            MarkerMap();
	            return true;
	        case R.id.WindTurbine:
	    		gMap.clear();
	    		  
	    		//Set setting value
	    	    editor.putString("settings", "WindTurbine");
	    	    // Commit the edits!
	    	    editor.commit();

	    	    //Set new Title
	            title = getText(R.string.app_name).toString();
	            setTitle(title + " - " + "WindTurbine");
	            
	            //Start again with marking map
	        	MarkerMap();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
