package com.map4e.mapview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import com.example.maplearn.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateSlider.OnDateSetListener;
import com.googlecode.android.widgets.DateSlider.labeler.TimeLabeler;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;
import com.maplert.widget.RangeSeekBar;
import com.maplert.widget.RangeSeekBar.OnLongClickDispMaxListener;
import com.maplert.widget.RangeSeekBar.OnLongClickDispMinListener;
import com.maplert.widget.RangeSeekBar.OnLongClickThumbMaxListener;
import com.maplert.widget.RangeSeekBar.OnLongClickThumbMinListener;
import com.maplert.widget.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GooglemapsActivity extends MapActivity {

	protected static final String TAG = "GooglemapsActivity";
	LocationManager locMgr;
	MyLocationListener locLstnr;
	MapController mc;
	MapView mapView;
	RangeSeekBar<Long> mSeekBar;
	LinkedList<EventItem> eventList = new LinkedList<EventItem>();
	EventBucketItemizedOverlay itemizedOverlay;
	
	final int SEEKBAR_TIMESEL_DIALOG = 0;
	
	
	/*My overlay Class starts*/
	class MyMapOverlays extends com.google.android.maps.Overlay
	{
		GeoPoint location = null;

		public MyMapOverlays(GeoPoint location)
		{
			super();
			this.location = location;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow)
		{

			super.draw(canvas, mapView, shadow);
			//translate the screen pixels
			Point screenPoint = new Point();
			mapView.getProjection().toPixels(this.location, screenPoint);

			//add the image
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete),
					screenPoint.x, screenPoint.y , null); //Setting the image &nbsp;location on the screen (x,y).
		}
	}
	 /*My overlay Class ends*/


	public class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			/*
			loc.getLatitude();
			loc.getLongitude();
			String Text = "My current location is: " +
					"Latitud = " + loc.getLatitude() +
					"Longitud = " + loc.getLongitude();
			Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

			String coordinates[] = {""+loc.getLatitude(), ""+loc.getLongitude()};
			 double lat = Double.parseDouble(coordinates[0]);
			 double lng = Double.parseDouble(coordinates[1]);

			 GeoPoint p = new GeoPoint(
			 (int) (lat * 1E6),
			 (int) (lng * 1E6));

			 mc.animateTo(p);
			 mc.setZoom(7);
			 
			//add a location marker.

			 MyMapOverlays marker = new MyMapOverlays(p) ;
			 List listOfOverLays = mapView.getOverlays();
			 listOfOverLays.clear();
			 listOfOverLays.add(marker);

			 mapView.invalidate();
			 */
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
					"Gps Disabled",
					Toast.LENGTH_SHORT ).show();
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
					"Gps Enabled",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{

		}

	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);        
    	final LinearLayout v = (LinearLayout) findViewById(R.id.topview);

    	try {
    		final TextView tvStart = (TextView) findViewById(R.id.startTime);
    		final TextView tvEnd = (TextView) findViewById(R.id.endTime);

    		// create RangeSeekBar as Date range between 2000-12-01 and now
    		Date maxDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-12-31");
    		Date minDate = new Date();

    		mSeekBar = new RangeSeekBar<Long>(this, minDate.getTime(), maxDate.getTime(), getBaseContext());
    		mSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>() {
    			@Override
    			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
    				// handle changed range values
    				Log.i(TAG, "User selected new date range: MIN=" + new Date(minValue) + ", MAX=" + new Date(maxValue));
    				SimpleDateFormat df = new SimpleDateFormat("EEE dd MMM yyyy kk:mm");
    				tvStart.setText(df.format(new Date(minValue)));
    				tvEnd.setText(df.format(new Date(maxValue)));
    	    		updateOverlayItems(mSeekBar.getSelectedMinValue(), mSeekBar.getSelectedMaxValue());
    			}
    		}); 
    	
     		
    		mSeekBar.setOnLongClickThumbMinListener(new OnLongClickThumbMinListener<Long>() {
    			public void onLongClickThumbMin(RangeSeekBar<?> bar) {	
    				final Calendar c = Calendar.getInstance();
    				c.setTime(new Date(mSeekBar.getSelectedMinValue()));
    				final DateTimeSlider dialog = new DateTimeSlider (v.getContext(), 
    						new DateSlider.OnDateSetListener() {
    					public void onDateSet(DateSlider view, Calendar selectedDate) {
    						Date d = selectedDate.getTime();
    						mSeekBar.setSelectedMinValue(Long.valueOf(d.getTime()));
    						mSeekBar.invalidate();
    					}
    				}, c);
    				dialog.setTitle("Set Event Window Start Time");
    				dialog.setContentView(R.layout.datetimeslider);
    				Button dialogButton = (Button) dialog.findViewById(R.id.dateSliderCancelButton);
    				// if button is clicked, close the custom dialog
    				dialogButton.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						dialog.dismiss();
    					}
    				});
    	 
    				dialog.show();
    			}
    		});

    		mSeekBar.setOnLongClickThumbMaxListener(new OnLongClickThumbMaxListener<Long>() {
    			public void onLongClickThumbMax(RangeSeekBar<?> bar) {	
    				final Calendar c = Calendar.getInstance();
    				c.setTime(new Date(mSeekBar.getSelectedMaxValue()));
    				final DateTimeSlider dialog = new DateTimeSlider (v.getContext(), 
    						new DateSlider.OnDateSetListener() {
    					public void onDateSet(DateSlider view, Calendar selectedDate) {
    						Date d = selectedDate.getTime();
    						mSeekBar.setSelectedMaxValue(Long.valueOf(d.getTime()));
    						mSeekBar.invalidate();
    					}
    				}, c);
    				dialog.setTitle("Set Event Window End Time");
    				dialog.setContentView(R.layout.datetimeslider);
    				Button dialogButton = (Button) dialog.findViewById(R.id.dateSliderCancelButton);
    				// if button is clicked, close the custom dialog
    				dialogButton.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						dialog.dismiss();
    					}
    				});
    	 
    				dialog.show();
    			}
    		});
 
    		mSeekBar.setOnLongClickDispMinListener(new OnLongClickDispMinListener<Long>() {
    			public void onLongClickDispMin(RangeSeekBar<?> bar) {	
    				final Calendar c = Calendar.getInstance();
    				c.setTime(new Date(mSeekBar.getAbsoluteMinValue()));
    				final DateTimeSlider dialog = new DateTimeSlider (v.getContext(), 
    						new DateSlider.OnDateSetListener() {
    					public void onDateSet(DateSlider view, Calendar selectedDate) {
    						Date d = selectedDate.getTime();
    						mSeekBar.setAbsoluteMinValue(Long.valueOf(d.getTime()));
    						mSeekBar.invalidate();
    					}
    				}, c);
    				dialog.setTitle("Set Display Window Start Time");
    				dialog.setContentView(R.layout.datetimeslider);
    				Button dialogButton = (Button) dialog.findViewById(R.id.dateSliderCancelButton);
    				// if button is clicked, close the custom dialog
    				dialogButton.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						dialog.dismiss();
    					}
    				});
    	 
    				dialog.show();
    			}
    		});
    		 
    		mSeekBar.setOnLongClickDispMaxListener(new OnLongClickDispMaxListener<Long>() {
    			public void onLongClickDispMax(RangeSeekBar<?> bar) {	
    				final Calendar c = Calendar.getInstance();
    				c.setTime(new Date(mSeekBar.getAbsoluteMaxValue()));
    				final DateTimeSlider dialog = new DateTimeSlider (v.getContext(), 
    						new DateSlider.OnDateSetListener() {
    					public void onDateSet(DateSlider view, Calendar selectedDate) {
    						Date d = selectedDate.getTime();
    						mSeekBar.setAbsoluteMaxValue(Long.valueOf(d.getTime()));
    						mSeekBar.invalidate();
    					}
    				}, c);
    				dialog.setTitle("Set Display Window End Time");
    				dialog.setContentView(R.layout.datetimeslider);
    				Button dialogButton = (Button) dialog.findViewById(R.id.dateSliderCancelButton);
    				// if button is clicked, close the custom dialog
    				dialogButton.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						dialog.dismiss();
    					}
    				});
    	 
    				dialog.show();
    			}
    		}); 		
    		
    		mSeekBar.setNotifyWhileDragging(true);
    		v.addView(mSeekBar);
    	}
    	catch (Exception e) {
    		Log.e(TAG, "Exception caught: " + e.getMessage() + e.getCause());
    	}

    	mapView = (MapView) findViewById(R.id.mapview1);

    	mc = mapView.getController();

        locMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locLstnr = new MyLocationListener();
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locLstnr);

        GeoPoint point = new GeoPoint(-33956436, 151236463);

        // Create Test Data
        createTestData();
		updateOverlayItems(mSeekBar.getSelectedMinValue(), mSeekBar.getSelectedMaxValue());
 
        MapController mc = mapView.getController();
        mc.setCenter(new GeoPoint(-33956436, 151236463)); 
        mc.zoomToSpan(itemizedOverlay.getLatSpanE6(), itemizedOverlay.getLonSpanE6());

        /*
        String coordinates[] = {"30", "71"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
      
        GeoPoint p = new GeoPoint(
        (int) (lat * 1E6),
        (int) (lng * 1E6));
       
        mc.animateTo(p);
        mc.setZoom(7);
        mapView.invalidate();
        */
    }
    
    public void updateOverlayItems(long winStart, long winEnd) {
    	
    	if (itemizedOverlay != null) {
    		itemizedOverlay.clearOverlayItem();
    	}
    	else {
    		Drawable makerDefault = this.getResources().getDrawable(R.drawable.star_big_on);
    		itemizedOverlay = new EventBucketItemizedOverlay(makerDefault, mapView);
    		mapView.getOverlays().add(itemizedOverlay);
    	}

    	for (EventItem e:eventList) {
    		if (e.getStart() <= winEnd && e.getEnd() >= winStart){
    			itemizedOverlay.addEventItem(e);
        	}
        }
    	mapView.invalidate();
    }
    
    
    private void createTestData() {

    	try {
    		eventList.add(
    				new EventItem(
    						new GeoPoint(-33946302, 151236463), 
    						"test", 
    						(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-08-20").getTime(),
    						(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-08-21").getTime(),
    						"Mario's Fruit World", 
    						"Get your bargain today!"));

    		eventList.add(new EventItem(
    				new GeoPoint(-33956302, 151236463), 
    				"test", 
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-09-10").getTime(),
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-09-11").getTime(),
    				"Junior School Dance Night", 
    				"See the kids having fun. There will be a special performce by the invited guest Blue River Ballet Academy."
    				));
    		eventList.add(new EventItem(
    				new GeoPoint(-33956302, 151236463), 
    				"test", 
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-09-10").getTime(),
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-09-11").getTime(),
    				"Senior School Speech and Songs", 
    				"See the best talents in the school performing on stage"));
    		eventList.add(new EventItem(
    				new GeoPoint(-33956302, 151236463), 
    				"test", 
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-10-18").getTime(),
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-10-19").getTime(),
    				"Jazz In The Open Space", 
    				"See how our young jazz musicians entertain the crowd in the open"
    				));
    		eventList.add(new EventItem(
    				new GeoPoint(-33952436, 151232698), 
    				"test", 
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-11-26").getTime(),
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-12-06").getTime(),
    				"Spring Festival 2012", 
    				"More than you can eat"));
    		eventList.add(new EventItem(
    				new GeoPoint(-33952436, 151237698), 
    				"test", 
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-11-25").getTime(),
    				(new SimpleDateFormat("yyyy-MM-dd")).parse("2012-11-27").getTime(),
    				"Duffy's Fruit Specials - Up to 50%", 
    				"Daily fresh produce direct from nearby farms"));

    	}
    	catch (ParseException e) {
    		Log.e(TAG, "ParseException caught when creating test data");
    	}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_googlemaps, menu);
        return true;
    }
    
    protected Dialog onCreateDialog(int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the dialog to its caller
    	
    	// get today's date and time
    	Dialog dlg = null;
        final Calendar c = Calendar.getInstance();
        OnDateSetListener onDateSet = new OnDateSetListener() {
        		public void onDateSet(DateSlider ds, Calendar c) {
        			
        		}
        };
        
        switch (id) {
        case SEEKBAR_TIMESEL_DIALOG:
            dlg = new DateTimeSlider(this,onDateSet,c);
        }
        return dlg;
    }

    public boolean isRouteDisplayed() {
    	return false;
    }
}
