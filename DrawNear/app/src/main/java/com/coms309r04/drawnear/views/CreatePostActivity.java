package com.coms309r04.drawnear.views;

import com.coms309r04.drawnear.R;
import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.coms309r04.drawnear.connection.DrawingManager;
import com.coms309r04.drawnear.connection.GPSManager;
import com.coms309r04.drawnear.connection.IGPSActivity;
import com.coms309r04.drawnear.data.DrawingItem;
import com.coms309r04.drawnear.tools.MyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.OnClickListener;

public class CreatePostActivity extends Activity implements OnClickListener, IGPSActivity {
	private GPSManager gps;

	public DrawingView drawView;
	private float smallBrush, mediumBrush, largeBrush;
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn;
	private boolean connectedToLocationServices = false;

	final ArrayList<String> privateRecepients = new ArrayList<String>();
	final ArrayList<String> usernameList = new ArrayList<String>();
	HashMap<String, String> usernameToID = new HashMap<String, String>();

	ArrayAdapter<String> adapter = null;
	SparseBooleanArray checkedFriends = null;

	DrawingItem toEdit = null;

	private static final int MENU_DRAWING_COMPLETED = 9001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String drawingId = extras.getString("id");
			toEdit = DrawingManager.getInstance().getCurrentDrawingById(
					drawingId);
		}

		gps = new GPSManager(this);

		setContentView(R.layout.activity_create_post);

		if (toEdit == null) // creating new post
		{
			drawView = (DrawingView) findViewById(R.id.drawing);
		} else { // loading post to edit
			drawView = ((DrawingView) findViewById(R.id.drawing))
					.setDrawingToEdit(toEdit);
		}

		LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
		currPaint = (ImageButton) paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(
				R.drawable.paint_pressed));
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		drawBtn = (ImageButton) findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);
		drawView.setBrushSize(mediumBrush);
		eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);
		newBtn = (ImageButton) findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.createpost, menu);

		/*
		 * MenuItem deleteDrawing = menu.add(0, MENU_DELETE_DRAWING, Menu.NONE,
		 * R.string.).setIcon( R.drawable.ic_new_drawing) );
		 * 
		 * deleteDrawing.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		 */

		MenuItem drawingComplete = menu.add(0, MENU_DRAWING_COMPLETED,
				Menu.NONE, R.string.completed).setIcon(
				R.drawable.ic_drawing_complete);
		drawingComplete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	/*
	 * @Override// checks if user is there or not public boolean
	 * onPrepareOptionsMenu(Menu menu) { super.onPrepareOptionsMenu(menu);
	 * 
	 * boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser
	 * .getCurrentUser());
	 * 
	 * menu.findItem(R.id.action_login).setVisible(!realUser);
	 * menu.findItem(R.id.action_logout).setVisible(realUser);
	 * menu.findItem(R.id.action_goto_profile).setVisible(true);
	 * menu.findItem(R.id.action_goto_create_post).setVisible(false);
	 * menu.findItem(R.id.action_goto_settings).setVisible(true);
	 * menu.findItem(R.id.action_goto_display_post).setVisible(true);
	 * menu.findItem(R.id.action_goto_map).setVisible(true);
	 * menu.findItem(R.id.action_goto_nearby).setVisible(realUser); return true;
	 * }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Temporary demo functionality to access all views with the settings
		// menu
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else {

			Intent intent = MyUtils.onOptionsNavigationSelected(
					item.getItemId(), this);
			if (intent != null) {
				startActivity(intent);
			} else if (item.getItemId() == MENU_DRAWING_COMPLETED) {

				// Save new post
				if (toEdit == null) {
					// Retrieve friends list in background
					List<ParseUser> friends = (List<ParseUser>) ParseUser.getCurrentUser().get("friendsList");

					if (friends != null) {
						usernameToID.clear();
						usernameList.clear();
						for (final ParseUser friend : friends) {
							friend.fetchIfNeededInBackground(new GetCallback<ParseUser>() {

								@Override
								public void done(ParseUser object,
										com.parse.ParseException e) {
									usernameToID.put(
											(String) friend.get("username"),
											friend.getObjectId());
									usernameList.add((String) friend
											.get("username"));
									Collections.sort(usernameList);

									if (adapter != null) {
										adapter.notifyDataSetChanged();
									}
								}

							});
						}
					}

					// Open dialog
					final Dialog saveDialog = new Dialog(this);
					saveDialog.setContentView(R.layout.save_drawing_layout);
					saveDialog.setTitle("Drawing Complete");
					Button positiveButton = (Button) saveDialog
							.findViewById(R.id.dialogButtonPositive);

					RadioButton selectRecepients = (RadioButton) saveDialog
							.findViewById(R.id.radio_select_who);
					selectRecepients.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectRecepients();
						}
					});

					positiveButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// save drawing
							if (connectedToLocationServices) {
								//get it to unzoom
								
								drawView.setDrawingCacheEnabled(true);
								
								// Get bitmap from canvas and compress it
								Bitmap image = drawView.getDrawingCache();
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								image.compress(Bitmap.CompressFormat.PNG, 100,
										stream);
								byte[] data = stream.toByteArray();
								image.recycle();
								image = null;
								// String imgSaved =
								// MediaStore.Images.Media.insertImage(getContentResolver(),
								// drawView.getDrawingCache(),
								// UUID.randomUUID().toString()+".bmp",
								// "drawing");
								DrawingItem toSave = new DrawingItem();
								toSave.setBitmapByteArray(data);

								// Drawing name
								EditText name = (EditText) saveDialog.findViewById(R.id.save_drawing_name);
								toSave.setTitle(name.getText().toString());
								toSave.setLocation(gps.getLastLocation());
								toSave.setCreator(ParseUser.getCurrentUser());

								// Drawing "leave for" permission
								RadioGroup leaveFor = (RadioGroup) saveDialog
										.findViewById(R.id.leave_for);
								int radioButtonID = leaveFor
										.getCheckedRadioButtonId();
								switch (radioButtonID) {
								case R.id.radio_public:
									toSave.setPrivacy(DrawingItem.PRIV_TYPE.PUBLIC);
									Log.i("privacy", "Public permission");
									break;
								case R.id.radio_friends:
									toSave.setPrivacy(DrawingItem.PRIV_TYPE.FRIENDS);
									Log.i("privacy", "Friends permission");
									break;
								case R.id.radio_select_who:
									toSave.setPrivacy(DrawingItem.PRIV_TYPE.PRIVATE);

									ArrayList<String> recepIDs = new ArrayList<String>();
									for (String username : privateRecepients) {
										recepIDs.add(usernameToID.get(username));
									}
									toSave.setPrivateRecepients(recepIDs);

									Log.i("privacy", "Individual permission");
									break;
								default:
									toSave.setPrivacy(DrawingItem.PRIV_TYPE.PUBLIC);
									break;
								}

								// Editable permission
								CheckBox editable = (CheckBox) saveDialog
										.findViewById(R.id.check_editable);
								if (editable.isChecked()) {
									toSave.setEditable(true);
									Log.i("privacy", "Editable true");
								} else {
									toSave.setEditable(false);
									Log.i("privacy", "Editable false");
								}

								DrawingManager.getInstance()
										.saveDrawingToRemoteServer(toSave,
												drawView);
								// drawView.destroyDrawingCache();
								Toast.makeText(CreatePostActivity.this,
										"Drawing dropped on map",
										Toast.LENGTH_SHORT).show();

								finish();
							} else {
								Toast.makeText(
										CreatePostActivity.this,
										"Unable to find your location. Cannot drop drawing.",
										Toast.LENGTH_SHORT).show();
							}
						}
					});

					Button negativeButton = (Button) saveDialog.findViewById(R.id.dialogButtonNegative);
					negativeButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							gps.stopGPS();
							saveDialog.cancel();
						}
					});

					saveDialog.show();
				} else { // Finished editing drawing
					// Get bitmap from canvas and compress it
					drawView.setDrawingCacheEnabled(true);
					Bitmap image = drawView.getDrawingCache();
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					image.compress(Bitmap.CompressFormat.PNG, 100, stream);
					final byte[] data = stream.toByteArray();
					image.recycle();
					image = null;

					final ParseFile bmpFile = new ParseFile(
						toEdit.getTitle().toString() +
							"_" +
							UUID.randomUUID().toString() +
							".bmp",
						data
					);

					// Update ParseFile
					DrawingManager.getInstance().getCurrentNearbyDrawings()
							.get(toEdit.getId()).setBitmapFile(bmpFile);

					ParseQuery<ParseObject> query = ParseQuery.getQuery("DrawingItem");
					query.getInBackground(toEdit.getId(),
							new GetCallback<ParseObject>() {
								public void done(final ParseObject drawing,
										ParseException e) {
									if (e == null) {
										bmpFile.saveInBackground(new SaveCallback() {
											@Override
											public void done(ParseException e) {
												if (e == null) {
													drawing.put("bmp", bmpFile);
													drawing.saveInBackground();
													drawView.destroyDrawingCache();

													BitmapFactory.Options options = new BitmapFactory.Options();
													options.inJustDecodeBounds = true;
													BitmapFactory
															.decodeByteArray(
																	data,
																	0,
																	data.length,
																	options);
													// Calculate inSampleSize
													options.inSampleSize = DrawingManager
															.calculateInSampleSize(
																	options,
																	200, 200);
													// Decode bitmap with
													// inSampleSize set
													options.inJustDecodeBounds = false;
													Bitmap thumbBitmap = BitmapFactory
															.decodeByteArray(
																	data,
																	0,
																	data.length,
																	options);
													// Set this as the thumbnail
													// for the appropriate
													// drawing
													DrawingManager
															.getInstance()
															.getCurrentNearbyDrawings()
															.get(toEdit.getId())
															.setThumbnail(
																	thumbBitmap);
												} else {
													e.printStackTrace();
												}
											}
										});
									} else {
										e.printStackTrace();
									}
								}
							});
					
					setResult(RESULT_OK, new Intent().putExtra("id", toEdit.getId()));
					finish();
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void selectRecepients() {
		final Dialog selectRecepientsDialog = new Dialog(this);
		selectRecepientsDialog
				.setContentView(R.layout.select_recepients_layout);
		selectRecepientsDialog.setTitle("Select Recepients");

		final ListView friendsListView = (ListView) selectRecepientsDialog
				.findViewById(R.id.select_friends);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, usernameList);
		friendsListView.setAdapter(adapter);

		friendsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		friendsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				checkedFriends = friendsListView.getCheckedItemPositions();
				privateRecepients.clear();
				for (int i = 0; i < friendsListView.getAdapter().getCount(); i++) {
					if (checkedFriends.get(i)) {
						privateRecepients.add((String) parent
								.getItemAtPosition(i));
					}
				}
			}
		});

		// if there are currently recepients specified, select these recepients
		// when dialog opens
		if (checkedFriends != null) {
			for (int i = 0; i < friendsListView.getAdapter().getCount(); i++) {
				if (checkedFriends.get(i)) {
					friendsListView.setItemChecked(i, true);
				}
			}
		}

		Button ok = (Button) selectRecepientsDialog.findViewById(R.id.select_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectRecepientsDialog.dismiss();
			}
		});
		selectRecepientsDialog.show();
	}

	public void paintClicked(View view) {
		drawView.setErase(false);
		drawView.setBrushSize(drawView.getLastBrushSize());
		if (view != currPaint) {
			// update color
			ImageButton imgView = (ImageButton) view;
			String color = view.getTag().toString();

			drawView.setColor(color);

			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint = (ImageButton) view;
		}
	}

	@Override
	public void onClick(View view) {
		// respond to clicks
		if (view.getId() == R.id.draw_btn) {
			// draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			ImageButton smallBtn = (ImageButton) brushDialog
					.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(smallBrush);
					drawView.setLastBrushSize(smallBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(mediumBrush);
					drawView.setLastBrushSize(mediumBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			ImageButton largeBtn = (ImageButton) brushDialog
					.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(largeBrush);
					drawView.setLastBrushSize(largeBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();

		} else if (view.getId() == R.id.erase_btn) {
			// switch to erase - choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			ImageButton smallBtn = (ImageButton) brushDialog
					.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton) brushDialog
					.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton) brushDialog
					.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		} else if (view.getId() == R.id.new_btn) {
			// new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog
					.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							drawView.startNew();
							dialog.dismiss();
						}
					});
			newDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			newDialog.show();
		}
	}

	@Override
	public void locationUpdatesAvailable() {
		/*
		 * Toast.makeText(this, "Connected to location services",
		 * Toast.LENGTH_LONG).show();
		 */
		connectedToLocationServices = true;
	}

	@Override
	public void locationChanged(double lat, double lng, float distanceFromLastLocation, boolean updatedLastLocation) {
		/*
		 * Toast.makeText(this, "Lat: " + lat + " Long: " + lng,
		 * Toast.LENGTH_SHORT).show();
		 */
	}
}
