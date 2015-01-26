package com.test.placesapiloadchecker;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

public class PlacesApiService extends IntentService{
	public LocationManager locationManager;
	public MyLocationListener listener;
	public Location previousBestLocation = null;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	String placesSearchStr = null;
	String status=null;
	int radius=500;
	SharedPreferences sp;
	int counter,p;
	

	public PlacesApiService() {
		super("PlacesApiService");
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		sp=getSharedPreferences("STATUS", Context.MODE_PRIVATE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        StrictMode.ThreadPolicy policy = new
          		 StrictMode.ThreadPolicy.Builder().permitAll().build();
          		        StrictMode.setThreadPolicy(policy);
        sp=getSharedPreferences("STATUS", Context.MODE_PRIVATE);
        
	}


	@Override
	protected void onHandleIntent(Intent arg0) {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        StrictMode.ThreadPolicy policy = new
          		 StrictMode.ThreadPolicy.Builder().permitAll().build();
          		        StrictMode.setThreadPolicy(policy);
		
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    public class MyLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(final Location loc) {
			if(isBetterLocation(loc, previousBestLocation)){
				String lngVal = Double.toString(loc.getLongitude());
	            String latVal = Double.toString(loc.getLatitude());
	           
	            try {
					placesSearchStr= "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
							
								    "location="+URLEncoder.encode(latVal,"UTF-8")+","+URLEncoder.encode(lngVal,"UTF-8")
								    +"&radius="+URLEncoder.encode(String.valueOf(radius),"UTF-8") +
								    "&key="+URLEncoder.encode(Constants.apikey2,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            StringBuilder placesBuilder= new StringBuilder();
        		HttpClient placesClient=new DefaultHttpClient();
        		try {
        			HttpGet placesGet = new HttpGet(placesSearchStr);
        			
        			HttpResponse placesResponse = placesClient.execute(placesGet);
        			StatusLine placesSS= placesResponse.getStatusLine();
        			if(placesSS.getStatusCode()==200)
        			{
        				HttpEntity placesEntity= placesResponse.getEntity();
        				InputStream placesContent = placesEntity.getContent();
        				InputStreamReader placesInput = new InputStreamReader(placesContent);
        				BufferedReader placesReader = new BufferedReader(placesInput);
        				String lineIn;
        				while ((lineIn = placesReader.readLine()) != null) {
        					
        					placesBuilder.append(lineIn);
        				}
        			}

        		} catch (Exception e) {
        			e.printStackTrace();
        		}

        		String result= placesBuilder.toString();
        		try {
        			JSONObject resultObject = new JSONObject(result);
        			status=resultObject.getString("status");
        			JSONArray placesArray = resultObject.getJSONArray("results");
        			 p=placesArray.length();
        		}
        		catch(JSONException e)
        		{
        			e.printStackTrace();
        		}
        		
        			Log.d("status_code",status);
        			Editor ed=sp.edit();
        			if(status.contentEquals("OK"))
        			{
        				counter=sp.getInt("OK", 0);
        				counter++;
        				Log.d("counter",status+" "+counter+" array length "+p);
        				ed.putInt("OK", counter);
        			}
        			if(status.contentEquals("ZERO_RESULTS"))
        			{
        				counter=sp.getInt("ZERO_RESULTS", 0);
        				counter++;
        				ed.putInt("ZERO_RESULTS", counter);
        			}
        			if(status.contentEquals("OVER_QUERY_LIMIT"))
        			{
        				counter=sp.getInt("OVER_QUERY_LIMIT", 0);
        				counter++;
        				Log.d("counter",status+" "+counter+" array length "+p);
        				ed.putInt("OVER_QUERY_LIMIT", counter);
        			}
        			if(status.contentEquals("REQUEST_DENIED"))
        			{
        				counter=sp.getInt("REQUEST_DENIED", 0);
        				counter++;
        				ed.putInt("REQUEST_DENIED", counter);
        			}
        			if(status.contentEquals("INVALID_REQUEST"))
        			{
        				counter=sp.getInt("INVALID_REQUEST", 0);
        				counter++;
        				ed.putInt("INVALID_REQUEST", counter);
        			}
        			ed.commit();

			}
			
			
			
			
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
    	
    }

}
