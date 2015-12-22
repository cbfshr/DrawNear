package com.coms309r04.drawnear.views;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.connection.DrawingManager;
import com.coms309r04.drawnear.data.DrawingItem;
import com.coms309r04.drawnear.tools.MyUtils;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayPostActivity extends Activity {

	private static final int REQUEST_EDIT = 91;

	private static ParseUser drawingOwner;
	private static DrawingItem d;
	private static ParseObject drawingToDelete;

	ImageView iv;
	TextView title;
	TextView creator;
	TextView date;
	TextView distance;
	TextView privacy;
	TextView rating;
	ImageButton edit;

	ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_post);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String drawingId = extras.getString("id");

			d = DrawingManager.getInstance().getCurrentDrawingById(drawingId);

			iv = (ImageView) findViewById(R.id.display_drawing);
			title = (TextView) findViewById(R.id.display_title);
			creator = (TextView) findViewById(R.id.display_creator);
			date = (TextView) findViewById(R.id.display_date);
			distance = (TextView) findViewById(R.id.display_distance);
			privacy = (TextView) findViewById(R.id.display_privacy);
			rating = (TextView) findViewById(R.id.display_rating);
			edit = (ImageButton) findViewById(R.id.display_edit);

			if (d.isEditable())
				edit.setVisibility(View.VISIBLE);
			else
				edit.setVisibility(View.INVISIBLE);

			pb = (ProgressBar) findViewById(R.id.display_pb);
			pb.setVisibility(View.VISIBLE);

			getActionBar().setDisplayHomeAsUpEnabled(true);

			/*
			 * if (d.getBitmapByteArray() != null) { int dataLength =
			 * d.getBitmapByteArray().length;
			 * iv.setImageBitmap(BitmapFactory.decodeByteArray(
			 * d.getBitmapByteArray(), 0, dataLength)); } else {
			 * Toast.makeText(this, "Could not load drawing",
			 * Toast.LENGTH_SHORT).show(); }
			 */

			getDrawingAndUpdateView();

			title.setText(d.getTitle());

			if (d.getCreator() != null) {
				drawingOwner = d.getCreator();
				creator.setText(drawingOwner.getUsername());
			}

			Date dateCreated = d.getDateCreated();
			DateFormat formatter = DateFormat.getDateInstance();
			date.setText(formatter.format(dateCreated));

			float miles = (float) d.getDistInMiles();
			if (miles >= 0.05) {
				distance.setText(String.format("%.2f", miles) + " miles away");
			} else {
				distance.setText((int) (miles * 5280) + " feet away");
			}

			if (d.getPrivacy() != null) {
				switch (d.getPrivacy()) {
				case PUBLIC:
					privacy.setText("Public");
					break;
				case FRIENDS:
					privacy.setText("Friends Only");
					break;
				case PRIVATE:
					privacy.setText("Private");
					break;
				default:
					privacy.setText("Public");
					break;
				}
			} else {
				privacy.setText("Public");
			}
			rating.setText("+5");
		} else {
			Toast.makeText(this, "Could not load drawing", Toast.LENGTH_SHORT).show();
		}
	}

	private void getDrawingAndUpdateView() {
		if (d.getBitmapFile() != null) {
			d.getBitmapFile().getDataInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					if (e == null) {
						// Resize drawing to thumbnail size
						// First decode with inJustDecodeBounds=true to
						// check dimensions
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeByteArray(data, 0, data.length,
								options);
						// Calculate inSampleSize
						options.inSampleSize = DrawingManager
								.calculateInSampleSize(options, 450, 900);
						// Decode bitmap with inSampleSize set
						options.inJustDecodeBounds = false;
						Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,
								data.length, options);
						// Set this as the thumbnail for the appropriate
						// drawing
						iv.setImageBitmap(bmp);
						pb.setVisibility(View.INVISIBLE);
					} else {
						e.printStackTrace();
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation, menu);

		// if(ParseUser.getCurrentUser().equals(drawingOwner)) {
		if (drawingOwner != null) {
			if (ParseUser.getCurrentUser().getObjectId()
					.equals(drawingOwner.getObjectId())) {
				MenuItem deleteDrawing = menu.add(0, R.menu.displaypost,
						Menu.NONE, R.string.delete_post).setIcon(
						R.drawable.ic_delete);
				deleteDrawing.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Temporary demo functionality to access all views with the settings
		// menu
		final DisplayPostActivity _this = this;
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else if (item.getItemId() == R.menu.displaypost) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("DrawingItem");
			query.getInBackground(d.getId(), new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						/*
						 * Toast.makeText(_this, "Deleting Drawing",
						 * Toast.LENGTH_SHORT).show();
						 */
						object.deleteInBackground();

						finish();
					} else {
						// something went wrong
						Toast.makeText(_this, "Error", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
		} else {
			startActivity(MyUtils.onOptionsNavigationSelected(item.getItemId(),
					this));
		}

		return super.onOptionsItemSelected(item);
	}

	public void editPost(View v) {
		Intent intent = new Intent(this, CreatePostActivity.class);
		intent.putExtra("id", d.getId());
		startActivityForResult(intent, REQUEST_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EDIT && resultCode == Activity.RESULT_OK) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String drawingId = extras.getString("id");

				d = DrawingManager.getInstance().getCurrentDrawingById(drawingId);
				getDrawingAndUpdateView();
			}
		}
	}
}
