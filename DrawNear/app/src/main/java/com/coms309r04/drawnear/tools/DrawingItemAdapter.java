package com.coms309r04.drawnear.tools;

import java.util.ArrayList;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.data.DrawingItem;
import com.parse.ParseUser;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawingItemAdapter extends ArrayAdapter<DrawingItem> {
	public DrawingItemAdapter(Context context,
			ArrayList<DrawingItem> drawingItems) {
		super(context, 0, drawingItems);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		DrawingItem drawingItem = getItem(position);

		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.drawing_information_list_layout, parent, false);
		}

		// Lookup view for data population
		TextView distance = (TextView) convertView.findViewById(R.id.distance);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView creator = (TextView) convertView.findViewById(R.id.creator);
		ImageView edit = (ImageView) convertView.findViewById(R.id.drawing_edit);
		TextView privacy = (TextView) convertView.findViewById(R.id.drawing_privacy);

		ImageView imagePreview = (ImageView) convertView.findViewById(R.id.drawing_preview);

		title.setText(drawingItem.getTitle());

		if (drawingItem.getCreator() != null) {
			ParseUser u = drawingItem.getCreator();
			if (u.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
				creator.setText("Drawn by you");
			} else {
				creator.setText("Drawn by " + u.getString("username"));
			}
		}

		float miles = (float) drawingItem.getDistInMiles();
		if (miles >= 0.05) {
			distance.setText(String.format("%.2f", miles) + " miles away");
		} else {
			distance.setText((int) (miles * 5280) + " feet away");
		}

		if (drawingItem.getThumbnail() != null) {
			imagePreview.setImageBitmap(drawingItem.getThumbnail());
		}

		if (drawingItem.isEditable()) {
			edit.setVisibility(View.VISIBLE);
		} else {
			edit.setVisibility(View.INVISIBLE);
		}

		if (drawingItem.getPrivacy() != null) {
			switch (drawingItem.getPrivacy()) {
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

		// Return the completed view to render on screen
		return convertView;
	}
}
