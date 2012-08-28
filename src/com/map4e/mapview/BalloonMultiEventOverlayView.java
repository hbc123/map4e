package com.map4e.mapview;

import com.example.maplearn.R;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class BalloonMultiEventOverlayView extends FrameLayout {

	private LinearLayout layout;
	private TableLayout table;

	/**
	 * Create a new BalloonMultiEventOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonMultiEventOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.multi_event_overlay, layout);
		table = (TableLayout) v.findViewById(R.id.balloon_multi_event_layout);

		ImageView close = (ImageView) v.findViewById(R.id.multi_event_close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT, 
				android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(final BalloonItemizedOverlay<?> overlay, final EventBucketOverlayItem bucket) {
		
		layout.setVisibility(VISIBLE);
		
		table.removeAllViews();

		for (int i = 0; i < bucket.size(); i++) {
			final EventItem event = bucket.get(i);
			TableRow tr = new TableRow(getContext());
			tr.setLayoutParams(new android.widget.TableRow.LayoutParams(
					android.widget.TableRow.LayoutParams.FILL_PARENT,
					android.widget.TableRow.LayoutParams.WRAP_CONTENT));
			TextView title = new TextView(getContext());
			title.setText(event.getTitle());
			title.setLayoutParams(
					new android.widget.TableRow.LayoutParams(
							android.widget.TableRow.LayoutParams.FILL_PARENT,
							android.widget.TableRow.LayoutParams.WRAP_CONTENT)
					);
			
			SpannableString content = new SpannableString(event.getTitle());
			content.setSpan(new UnderlineSpan(), 0, event.getTitle().length(), 0);
			title.setText(content);
			title.setOnClickListener( 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// create a temp bucket to show just this item
							EventBucketOverlayItem bkt = new EventBucketOverlayItem(
									event.getPoint(), event.getTitle(), event.getSnippet());
							bkt.add(event);
							overlay.showSingleHeaderView(bkt);
						}
					}				
			);
			tr.addView(title);
			table.addView(tr,
					new android.widget.TableRow.LayoutParams(
							android.widget.TableRow.LayoutParams.FILL_PARENT,
							android.widget.TableRow.LayoutParams.WRAP_CONTENT));
		}
	}


}
