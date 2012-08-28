package com.map4e.mapview;

import java.util.LinkedList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

class EventBucketOverlayItem extends OverlayItem {
	
	List<EventItem> events = new LinkedList<EventItem>(); 
	boolean			modified;
	
	public EventBucketOverlayItem(GeoPoint p, String title, String snippet) {
		super(p, title, snippet);
		modified = false;
	}
		
	public void add(EventItem evt) {
		events.add(evt);
		modified = true;
	}
	
	public int size() {
		return events.size();
	}
	
	public EventItem get(int index) {
		return events.get(index);
	}
	
	public boolean hasModified() {
		return modified;
	}
}

