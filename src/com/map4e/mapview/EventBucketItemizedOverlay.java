package com.map4e.mapview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class EventBucketItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private List<OverlayItem> mOverlays  = Collections.synchronizedList(new ArrayList<OverlayItem>());
	private MapView			  mapView;	
	
	public EventBucketItemizedOverlay(Drawable defaultMarker, MapView mv) {
		//super(defaultMarker);
		super(boundCenterBottom(defaultMarker), mv);
		mapView = mv;
		setBalloonBottomOffset(20);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlayItem(OverlayItem overlayItem) {
		mOverlays.add(overlayItem);
		populate();
	}
	
	public void clearOverlayItem() {
		hideBalloon();
		mOverlays.clear();
		populate();
	}
	
	/**
	 * Add an event to the overlay bucket list.
	 * Each bucket contains a list of events that occur at the same location.
	 * Before adding a new bucket, check that there is no bucket with the same
	 * coordinates is in the list.
	 * 
	 * @param e	Event item to add
	 */
	public void addEventItem(EventItem e) 
	{
		synchronized(mOverlays) {
			int i;
			for (i = 0; i < mOverlays.size(); i++) {
				EventBucketOverlayItem bitem = (EventBucketOverlayItem) mOverlays.get(i);
				if (e.getPoint().equals(bitem.getPoint())) {
					break;
				}
			}
			if (i < mOverlays.size()) {
				EventBucketOverlayItem bitem = (EventBucketOverlayItem) mOverlays.get(i);
				bitem.add(e);
			}
			else {
				EventBucketOverlayItem overlayItem = new EventBucketOverlayItem(
						e.getPoint(), e.getTitle(), e.getSnippet());
				addOverlayItem(overlayItem);
				overlayItem.add(e);
			}
		}
	}

	@Override
	protected boolean onBalloonTap(EventBucketOverlayItem item) {
		Toast.makeText(mapView.getContext(), "onBalloonTap for overlay title - " + item.getTitle(),
				Toast.LENGTH_LONG).show();
		return true;
	}

}


