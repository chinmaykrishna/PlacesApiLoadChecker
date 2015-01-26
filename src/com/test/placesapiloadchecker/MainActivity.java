package com.test.placesapiloadchecker;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	TextView ok,zero_results,over_query_limit,request_denied,invalid_request;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = new Intent(this, PlacesApiService.class);
  	  intent.putExtra("flag", "alarm");
  	  PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
  	  Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
  	  AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
  	  alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*1000, pintent);
  	  SharedPreferences sp=getSharedPreferences("STATUS",Context.MODE_PRIVATE);
  	  ok=(TextView)findViewById(R.id.ok);
  	  zero_results=(TextView)findViewById(R.id.zero_results);
  	  over_query_limit=(TextView)findViewById(R.id.over_query_limit);
  	  request_denied=(TextView)findViewById(R.id.request_denied );
  	  invalid_request=(TextView)findViewById(R.id.invalid_request);
  	  
  	  ok.setText(ok.getText()+" "+sp.getInt("OK", 0));
  	  zero_results.setText(zero_results.getText()+" "+sp.getInt("ZERO_RESULTS", 0));
  	over_query_limit.setText(over_query_limit.getText()+" "+sp.getInt("OVER_QUERY_LIMIT", 0));
  	request_denied.setText(request_denied.getText()+" "+sp.getInt("REQUEST_DENIED", 0));
  	invalid_request.setText(invalid_request.getText()+" "+sp.getInt("INVALID_REQUEST", 0));
  	
  	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
