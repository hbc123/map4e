package com.map4e.mapview;

import com.google.android.maps.GeoPoint;

public class EventItem {
	GeoPoint point;
	String	eventContext;
	long	start;
	long	end;
	String	title;
	String	snippet;
	
	public EventItem(GeoPoint p, String c, long start, long finish,
			String title, String snippet) {
		// TODO Auto-generated constructor stub
		this.point = p;
		this.eventContext = c;
		this.start = start;
		this.end = finish;
		this.title = title;
		this.snippet = snippet;
	}

	public GeoPoint getPoint() {
		return point;
	}

	public String getEventContext() {
		return eventContext;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public String getTitle() {
		return title;
	}

	public String getSnippet() {
		return snippet;
	}
	
	
}
