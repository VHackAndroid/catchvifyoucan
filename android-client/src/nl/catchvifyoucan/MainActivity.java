package nl.catchvifyoucan;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity {

	private LocationManager locationManager;
	private TelephonyManager telman;
	private MyItemizedOverlay itemizedoverlay;
	private OverlayItem overlayitem;
	private List<Overlay> mapOverlays;
	private Handler mHandler;
	private com.google.android.maps.MapView mapView;
	private int longitude = 0;
	private int latitude = 0;
	private String imei = null;
	int updated = 0;
	

	private static final double LOCATIONMULTIPLIER = 1000000.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mapView = (com.google.android.maps.MapView) findViewById(R.id.mainMV);
		mapView.setBuiltInZoomControls(true);

		/** Here we get the overlay from the map on the view */
		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.investigator);
		itemizedoverlay = new MyItemizedOverlay(drawable, this);

		/** Instance of the locationmanager */
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		/** With the location wwe can request the lat and lon */
		Location location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		/** Every 10 seconds we want an update of the location 
		 * the distance is set to zero meters*/
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				10 * 1000, 0, new MyLocationListener());

		/**
		 * Google maps GeoPoint coordinates are specified in microdegrees
		 * (degrees * 1e6)
		 */
		if (location != null) {
			latitude = (int) (location.getLatitude() * LOCATIONMULTIPLIER);
			longitude = (int) (location.getLongitude() * LOCATIONMULTIPLIER);
		} else {
			latitude = 52000000;
			longitude = 4000000;
		}

		/** Show the item on the current position */
		GeoPoint currentPoint = new GeoPoint(latitude, longitude);

		/** This is the overlayitem */
		overlayitem = new OverlayItem(currentPoint, "Hallo",
				"I'm in Amsterdam!");

		/** Add the item to the instance of MyItemizedOverla */
		itemizedoverlay.addOverlay(overlayitem);

		/** Add the array of overlayitems to the map */
		mapOverlays.add(itemizedoverlay);
		
		/**Get the Imei number to give to the server*/
		setImeiNumber();
		
		/**Send all necesary data to the server*/
		pushPosition();
		
		/**Set a repeating task to send updates to the server*/
		mHandler = new Handler();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 5000);
		
		
	}

	private void updateLocation(Location location) {
		/**
		 * Google maps GeoPoint coordinates are specified in microdegrees
		 * (degrees * 1e6)
		 */

		updated += 1;

		latitude = (int) (location.getLatitude() * LOCATIONMULTIPLIER);
		longitude = (int) (location.getLongitude() * LOCATIONMULTIPLIER);
		/** Show the item on the current position */
		GeoPoint currentPoint = new GeoPoint(latitude, longitude);

		Drawable drawable = this.getResources().getDrawable(
				R.drawable.investigator);
		itemizedoverlay = new MyItemizedOverlay(drawable, this);

		overlayitem = new OverlayItem(currentPoint, "" + updated,
				"keer geupdate");

		mapOverlays.clear();

		runOnUiThread(new Runnable() {
			public void run() {
				itemizedoverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedoverlay);
				// Toast.makeText(MainActivity.this, "Update!",
				// Toast.LENGTH_SHORT).show();
				mapView.invalidate();
			}
		});
	}

	private void pushPosition() {
		/**The timer is calling this method every minute*/
		LocationDataManager dm = new LocationDataManager();
		dm.pushLocationToServer(imei, latitude, longitude);
		Toast.makeText(this, "Timer", Toast.LENGTH_SHORT).show();
	}


	

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Set the imei from the device so it can be pushed to the server */
	private void setImeiNumber() {
		telman = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imei = telman.getDeviceId();
	}

	
	private Runnable mUpdateTimeTask = new Runnable() {
		
		@Override
		public void run() {
			pushPosition();
			mHandler.postAtTime(this, 15000);
		}
	};

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				/** Now we have the new location */
				updateLocation(loc);
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// Do something here if you would like to know when the provider is
			// disabled by the user
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// Do something here if you would like to know when the provider is
			// enabled by the user
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// Do something here if you would like to know when the provider
			// status changes
		}
	}
}