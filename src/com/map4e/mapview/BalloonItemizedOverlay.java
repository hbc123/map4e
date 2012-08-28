/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.map4e.mapview; 
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.example.maplearn.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * An abstract extension of ItemizedOverlay for displaying an information balloon
 * upon screen-tap of each marker overlay.
 * 
 * @author Jeff Gilfelt
 */
public abstract class BalloonItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {

	private MapView mapView;
	private BalloonOverlayView balloonView;
	private BalloonMultiEventOverlayView titleListView;
	private View clickRegion;
	private int viewOffset;
	List<EventBucketOverlayItem> listAtPoint = new LinkedList<EventBucketOverlayItem>();
	final MapController mc;
	
	/**
	 * Create a new BalloonItemizedOverlay
	 * 
	 * @param defaultMarker - A bounded Drawable to be drawn on the map for each item in the overlay.
	 * @param mapView - The view upon which the overlay items are to be drawn.
	 */
	public BalloonItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(defaultMarker);
		this.mapView = mapView;
		viewOffset = 0;
		mc = mapView.getController();
	}
	
	/**
	 * Set the horizontal distance between the marker and the bottom of the information
	 * balloon. The default is 0 which works well for center bounded markers. If your
	 * marker is center-bottom bounded, call this before adding overlay items to ensure
	 * the balloon hovers exactly above the marker. 
	 * 
	 * @param pixels - The padding between the center point and the bottom of the
	 * information balloon.
	 */
	public void setBalloonBottomOffset(int pixels) {
		viewOffset = pixels;
	}
	
	/**
	 * Override this method to handle a "tap" on a balloon. By default, does nothing 
	 * and returns false.
	 * 
	 * @param item - The index of the item whose balloon is tapped.
	 * @return true if you handled the tap, otherwise false.
	 */
	protected boolean onBalloonTap(EventBucketOverlayItem item) {
		return false;
	}
	
	protected final void showSingleHeaderView(EventBucketOverlayItem bucket) {
		GeoPoint point;
		boolean isRecycled;
		
		EventItem item = bucket.get(0);
		point = item.getPoint();
		
		if (balloonView == null) {
			balloonView = new BalloonOverlayView(mapView.getContext(), viewOffset);
			clickRegion = (View) balloonView.findViewById(R.id.balloon_inner_layout);
			isRecycled = false;
		} else {
			isRecycled = true;
		}
	
		if (titleListView != null) {
			titleListView.setVisibility(View.GONE);
		}
		
		if (balloonView != null) {
			balloonView.setVisibility(View.GONE);
		}
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		if (mapOverlays.size() > 1) {
			hideOtherBalloons(mapOverlays);
		}
		
		balloonView.setData(item);
		
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				MapView.LayoutParams.BOTTOM_CENTER);
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		setBalloonTouchListener(bucket, item);
		
		balloonView.setVisibility(View.VISIBLE);

		if (isRecycled) {
			balloonView.setLayoutParams(params);
		} else {
			mapView.addView(balloonView, params);
		}
		
		mc.animateTo(point);

	}
	
	protected final void showHeaderListView(EventBucketOverlayItem bucket) {
		GeoPoint point;
		boolean isRecycled;
		
		point = bucket.getPoint();
		
		if (titleListView == null) {
			titleListView = new BalloonMultiEventOverlayView(mapView.getContext(), viewOffset);
			clickRegion = (View) titleListView.findViewById(R.id.balloon_multi_event_layout);
			isRecycled = false;
		} else {
			isRecycled = true;
		}
	
		if (titleListView != null) {
			titleListView.setVisibility(View.GONE);
		}
		
		if (balloonView != null) {
			balloonView.setVisibility(View.GONE);
		}
				
		List<Overlay> mapOverlays = mapView.getOverlays();
		if (mapOverlays.size() > 1) {
			hideOtherBalloons(mapOverlays);
		}
		
		titleListView.setData(this, bucket);
		
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				MapView.LayoutParams.BOTTOM_CENTER);
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		//setBalloonTouchListener(item);
		
		titleListView.setVisibility(View.VISIBLE);

		if (isRecycled) {
			titleListView.setLayoutParams(params);
		} else {
			mapView.addView(titleListView, params);
		}
		
		mc.animateTo(point); 
	}
	
	/*
 	public final boolean onTap(GeoPoint p, MapView mv) {
 
		listAtPoint.clear();
		super.onTap(p, mv);
		
		if (listAtPoint.size() == 1) {
			showSingleHeaderView();
		}
		else if (listAtPoint.size() > 1) {
			showHeaderListView();
		}
		return true;
	}
	*/

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	@Override
	protected final boolean onTap(int index) {

		boolean isRecycled;
		GeoPoint point;
		
		EventBucketOverlayItem item = (EventBucketOverlayItem) createItem(index);

		if (item.size() == 1) {
			showSingleHeaderView(item);
		}
		else {
			showHeaderListView(item);
		}

		return true;
	}
	
	/**
	 * Sets the visibility of this overlay's balloon view to GONE. 
	 */
	protected void hideBalloon() {
		if (balloonView != null) {
			balloonView.setVisibility(View.GONE);
		}
		if (titleListView != null) {
			titleListView.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Hides the balloon view for any other BalloonItemizedOverlay instances
	 * that might be present on the MapView.
	 * 
	 * @param overlays - list of overlays (including this) on the MapView.
	 */
	protected void hideOtherBalloons(List<Overlay> overlays) {
		
		for (Overlay overlay : overlays) {
			if (overlay instanceof BalloonItemizedOverlay<?> && overlay != this) {
				((BalloonItemizedOverlay<?>) overlay).hideBalloon();
			}
		}
		
	}
	
	/**
	 * Sets the onTouchListener for the balloon being displayed, calling the
	 * overridden onBalloonTap if implemented.
	 * 
	 * @param thisIndex - The index of the item whose balloon is tapped.
	 */
	private void setBalloonTouchListener(final EventBucketOverlayItem bucket, final EventItem item) {
		
		try {
			@SuppressWarnings("unused")
			Method m = this.getClass().getDeclaredMethod("onBalloonTap", int.class);
			
			clickRegion.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					
					View l =  ((View) v.getParent()).findViewById(R.id.balloon_main_layout);
					Drawable d = l.getBackground();
					
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						int[] states = {android.R.attr.state_pressed};
						if (d.setState(states)) {
							d.invalidateSelf();
						}
						return true;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						int newStates[] = {};
						if (d.setState(newStates)) {
							d.invalidateSelf();
						}
						// call overridden method
						onBalloonTap(bucket);
						return true;
					} else {
						return false;
					}
					
				}
			});
			
		} catch (SecurityException e) {
			Log.e("BalloonItemizedOverlay", "setBalloonTouchListener reflection SecurityException");
			return;
		} catch (NoSuchMethodException e) {
			// method not overridden - do nothing
			return;
		}

	}
	
}