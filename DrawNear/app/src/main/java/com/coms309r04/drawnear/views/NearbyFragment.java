package com.coms309r04.drawnear.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.connection.DrawingManager;
import com.coms309r04.drawnear.connection.GPSManager;
import com.coms309r04.drawnear.connection.ILocationUpdater;
import com.coms309r04.drawnear.data.DrawingItem;
import com.coms309r04.drawnear.tools.DrawingItemAdapter;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import android.os.AsyncTask;

public class NearbyFragment extends Fragment implements ILocationUpdater, SwipeRefreshLayout.OnRefreshListener {
	ListView listView;

	private static final int MENU_REFRESH_LIST = 9002;
	private static final int ALLOW_LOCATION_SERVICES = 9999;

	ProgressBar pb;
	TextView noContent;

	private static final double ISU_LAT = 42.025410, ISU_LNG = -93.646085;

	private SwipeRefreshLayout swipeRefreshLayout;
	DrawingItemAdapter drawingAdapter = null;

	private GPSManager gps;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment NearbyFragment.
	 */
	public static NearbyFragment newInstance() {
		NearbyFragment fragment = new NearbyFragment();
		return fragment;
	}

	// Required empty public constructor
	public NearbyFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gps = new GPSManager(getActivity(), this);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadAndUpdateDrawings();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_nearby, container, false);

		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);

		pb = (ProgressBar) view.findViewById(R.id.progressBar1);
		pb.setVisibility(View.VISIBLE);

		noContent = (TextView) view.findViewById(R.id.no_content);
		noContent.setText("There are no drawings nearby.\nMake one and be the first!");
		noContent.setVisibility(View.INVISIBLE);

		//Set up ListView
		if(drawingAdapter == null) {
			listView = (ListView) view.findViewById(R.id.nearby_list_view);
			drawingAdapter = new DrawingItemAdapter(getActivity(), DrawingManager.getInstance().getCurrentDrawingsList());
			listView.setAdapter(drawingAdapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					DrawingItem drawing = (DrawingItem) parent.getItemAtPosition(position);
					Intent intent = new Intent(getActivity(), DisplayPostActivity.class);
					intent.putExtra("id", drawing.getId());
					startActivity(intent);
				}
			});
		}

		// If there are already drawings in ArrayList from before, get rid of
		// the progress bar
		if(DrawingManager.getInstance().getCurrentDrawingsList().size() > 0) {
			pb.setVisibility(View.INVISIBLE);
		}

		return view;
	}

	private void loadImageAtListPosition(final String drawingKey, ParseFile bitmap) {
		bitmap.getDataInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] data, ParseException e) {
				if(e == null) {
					//Resize drawing to thumbnail size
					// First decode with inJustDecodeBounds=true to check dimensions
					BitmapFactory.Options options=new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeByteArray(data, 0, data.length, options);

					// Calculate inSampleSize
					options.inSampleSize = DrawingManager.calculateInSampleSize(options, 200, 200);

					// Decode bitmap with inSampleSize set
					options.inJustDecodeBounds = false;
					Bitmap thumbBitmap=BitmapFactory.decodeByteArray(data, 0, data.length, options);

					//Set this as the thumbnail for the appropriate drawing
					DrawingManager.getInstance().getCurrentNearbyDrawings().get(drawingKey).setThumbnail(thumbBitmap);
					drawingAdapter.notifyDataSetChanged(); // refresh list view
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	public void loadAndUpdateDrawings() {
		//if(gps.getLastLocation() == null) {
			// This Toast is shown A LOT - not sure why
			//Toast.makeText(getActivity(), "Nearby: Can't get last location.", Toast.LENGTH_SHORT).show();
		//}

		/*Toast.makeText(this, "Checking for new drawings...", Toast.LENGTH_SHORT).show();*/
		MyTask getDrawingsThread = new MyTask();
		getDrawingsThread.execute();
	}

	@Override
	public void locationChanged(double lat, double lng, float distanceFromLastLocation, boolean updatedLastLocation) {
		/*Toast.makeText(this, "Lat: " + lat + " Long: " + lng + "_" + distanceFromLastLocation, Toast.LENGTH_SHORT).show();*/

		if(updatedLastLocation) {
			loadAndUpdateDrawings();
			/*Toast.makeText(this, "Updating drawings.", Toast.LENGTH_SHORT).show();*/
		} else {
			/*Toast.makeText(this, "Not far enough from last location to update drawings.", Toast.LENGTH_SHORT).show();*/
		}
	}

	// this method is called when the location client has connected
	@Override
	public void locationUpdatesAvailable() {
		/*Toast.makeText(this, "Connected to location services", Toast.LENGTH_LONG).show();*/
		loadAndUpdateDrawings();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(!gps.isRunning()) {
			gps.resumeGPS();
		}
		loadAndUpdateDrawings();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		gps.stopGPS();
	}

	// Swipe Refresh
	@Override
	public void onRefresh() {
		loadAndUpdateDrawings();
		swipeRefreshLayout.setRefreshing(false);
	}

	private class MyTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// has access to main thread
			Log.i("MAIN", "Starting task");

			if(drawingAdapter == null) {
				pb.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected String doInBackground(String... params) {
			ParseGeoPoint last = gps.getLastLocation();

			DrawingManager.getInstance().getNearbyDrawings(last, 100);
			// Delete drawings that aren't nearby anymore
			DrawingManager.getInstance().removeDrawingsNoLongerNearby(last);

			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {
			// has access to main thread
			// update display here

			if(result == null) {
				Toast.makeText(getActivity(), "Could not connect to receive drawings", Toast.LENGTH_SHORT).show();
			}

			Log.i("MAIN", "New nearby drawings size is: " + DrawingManager.getInstance().getCurrentNearbyDrawings().size());

			drawingAdapter.notifyDataSetChanged(); // refresh list view

			// load in each picture one by one (if they have not previously been loaded)
			for(DrawingItem d : DrawingManager.getInstance().getCurrentNearbyDrawings().values()) {
				if(d.getBitmapFile() != null && d.getThumbnail() == null) {
					loadImageAtListPosition(d.getId(), d.getBitmapFile());
				}
			}

			pb.setVisibility(View.INVISIBLE);
			// IF there is still no content to display after a request, show
			// message "No content to display"
			if(DrawingManager.getInstance().getCurrentNearbyDrawings().size() == 0) {
				noContent.setVisibility(View.VISIBLE);
			} else{
				noContent.setVisibility(View.INVISIBLE);
			}
		}
	}
}
