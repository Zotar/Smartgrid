package dk.mercantec.smartgrid;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper
{		 
	    //The Android's default system path of your application database.
		private String DB_PATH; 
	    private static String DB_NAME = "weather.rdb";
	 
	    private SQLiteDatabase myDataBase; 
	 
	    private Context myContext;	 

	    // Constructor
	    // Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	    //@param context	     
	    public DataBaseHelper(Context context) 
	    {
	    	super(context, DB_NAME, null, 1);
	        this.myContext = context;
	        
	        DB_PATH = myContext.getFilesDir().getPath().toString();
	    }	
	 
	    //Creates a empty database on the system and rewrites it with your own database.
	    public void createDataBase() throws IOException
	    {
	    	boolean dbExist = checkDataBase();
	 
	    	if(dbExist)
	    	{
	    		//do nothing - database already exist
	    	}
	    	else
	    	{
	    		//By calling this method an empty database will be created into the default system path
	            //of your application so we are going to be able to overwrite that database with our database.
	        	this.getReadableDatabase();
	 
	        	try
	        	{
	    			copyDataBase();
	    		}
	        	catch (IOException e)
	        	{
	        		throw new Error("Error copying database");
	        	}
	    	}
	    }
	 
	    
	    //Check if the database already exist to avoid re-copying the file each time you open the application.
	    //@return true if it exists, false if it doesn't	    
	    private boolean checkDataBase()
	    {
	    	SQLiteDatabase checkDB = null;
	 
	    	try
	    	{
	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    	}
	    	catch(SQLiteException e)
	    	{
	    		//database doesn't exist yet.
	    	}
	 
	    	if(checkDB != null)
	    	{
	    		checkDB.close();
	    	}
	 
	    	boolean existDB;
	    	if (checkDB != null)
	    		existDB = true;
	    	else
	    		existDB = false;
	    	
	    	checkDB = null;
	    	
	    	return existDB;
	    }
	 
	    
	   //Copies your database from your local assets-folder to the just created empty database in the
	   //system folder, from where it can be accessed and handled.
	   //This is done by transfering bytestream.

	    private void copyDataBase() throws IOException
	    {
	    	//Open your local db as the input stream
	    	InputStream myInput = myContext.getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_PATH + DB_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ( (length = myInput.read(buffer)) > 0)
	    	{
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
	   
	    //Open for the database
	    public void open() throws SQLException
	    {
	        String myPath = DB_PATH + DB_NAME;
	    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    }
	 
	    //Close for the database
	    @Override
		public void close()
	    {
    	    if(myDataBase != null)
    	    {
    		    myDataBase.close();
    	    }
    	    super.close();
    	    this.myContext = null;
    	    this.myDataBase = null;
		}
	 
		@Override
		public void onCreate(SQLiteDatabase db) 
		{
	 
		}
	 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
	 
		}
		
		// listWeatherInCountry is a public helper methods to access and get content from the database.
        // and return a ArrayList with contents name, icao , lat, lng (Class Weather) from a country string 		
		public ArrayList<Weather> listWeatherInCountry(String countryName)
		{
			ArrayList<Weather> weather = new ArrayList<Weather>();
			
			Cursor myCursor = myDataBase.rawQuery("SELECT icao, lat, long, name FROM weather WHERE country ='"
												  + countryName +"' ORDER BY name", null);
			myCursor.moveToNext();
			
			while (!myCursor.isAfterLast()) 
			{
				Weather weatherData = new Weather(myCursor.getString(0), myCursor.getDouble(1), myCursor.getDouble(2), myCursor.getString(3));
				
				weather.add(weatherData);
				myCursor.moveToNext();
			}
			
			myCursor.close();
			
			return weather;
		}
}
