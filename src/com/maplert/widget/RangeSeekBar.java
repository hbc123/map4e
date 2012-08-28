package com.maplert.widget;

import java.math.BigDecimal;

import com.example.maplearn.R;
import com.googlecode.android.widgets.DateSlider.SliderContainer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * Widget that lets users select a minimum and maximum value on a given numerical range. The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.<br />
 * <br />
 * Improved {@link MotionEvent} handling for smoother use, anti-aliased painting for improved aesthetics.
 * 
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * @author Peter Sinnott (psinnott@gmail.com)
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 * 
 * @param <T>
 *            The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 */
public class RangeSeekBar<T extends Number> extends ImageView {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
        private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
        private final float thumbWidth = thumbImage.getWidth();
        //private final float thumbWidth = 8;
        private final float thumbHalfWidth = 0.5f * thumbWidth;
        private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();
        private final float lineHeight = 0.3f * thumbHalfHeight;
        private final float padding = thumbHalfWidth;
        private T absoluteMinValue, absoluteMaxValue;
        private final NumberType numberType;
        private double absoluteMinValuePrim;
		private double absoluteMaxValuePrim;
        private double normalizedMinValue = 0d;
        private double normalizedMaxValue = 1d;
        private Thumb pressedThumb = null;
        private boolean notifyWhileDragging = false;
        private OnRangeSeekBarChangeListener<T> listener;
        private OnLongClickDispMinListener<T> lcDispMinListener;
        private OnLongClickThumbMinListener<T> lcThumbMinListener;
        private OnLongClickThumbMaxListener<T> lcThumbMaxListener;
        private OnLongClickDispMaxListener<T> lcDispMaxListener;

        private LinearLayout timeSelect;
        private PopupWindow sliderPopup;
        private Activity activity;

        /**
         * Default color of a {@link RangeSeekBar}, #FF33B5E5. This is also known as "Ice Cream Sandwich" blue.
         */
        public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);

        /**
         * An invalid pointer id.
         */
        public static final int INVALID_POINTER_ID = 255;

        // Localized constants from MotionEvent for compatibility
        // with API < 8 "Froyo".
        public static final int ACTION_POINTER_UP = 0x6, ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

        private float mDownMotionX;
        private float mLastMotionX;

        private int mActivePointerId = INVALID_POINTER_ID;

        /**
         * On touch, this offset plus the scaled value from the position of the touch will form the progress value. Usually 0.
         */
        float mTouchProgressOffset;

        private int mScaledTouchSlop;
        private boolean mIsDragging;
		private double mNormalizedOffset;

        /**
         * Creates a new RangeSeekBar.
         * 
         * @param absoluteMinValue
         *            The minimum value of the selectable range.
         * @param absoluteMaxValue
         *            The maximum value of the selectable range.
         * @param context
         * @throws IllegalArgumentException
         *             Will be thrown if min/max value type is not one of Long, Double, Integer, Float, Short, Byte or BigDecimal.
         */
        public RangeSeekBar(Activity act, T absoluteMinValue, T absoluteMaxValue, Context context) throws IllegalArgumentException {
                super(context);
                activity = act;
                this.absoluteMinValue = absoluteMinValue;
                this.absoluteMaxValue = absoluteMaxValue;
                absoluteMinValuePrim = absoluteMinValue.doubleValue();
                absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
                numberType = NumberType.fromNumber(absoluteMinValue);

                // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScollViews.
                setFocusable(true);
                setFocusableInTouchMode(true);
                
        		setOnLongClickListener(new OnLongClickListener() {
        		    @Override
        		    public boolean onLongClick(View v) {
        		        //your stuff
        		    	if (mLastMotionX == mDownMotionX) {
        		    		double screenMinThumb = normalizedToScreen(normalizedMinValue);
        		    		double screenMaxThumb = normalizedToScreen(normalizedMaxValue);
         		    		if (mLastMotionX < mScaledTouchSlop) {
        		    			setAbsDisplayLeft();
        		    		}
        		    		else if (Math.abs(mLastMotionX - screenMinThumb) < mScaledTouchSlop) {
        		    			setAbsLeftThumb();
        		    		}
        		    		else if (Math.abs(mLastMotionX - screenMaxThumb) < mScaledTouchSlop) {
        		    			setAbsRightThumb();
        		    		}
        		    		else if (Math.abs(mLastMotionX - getWidth()) < mScaledTouchSlop) {
        		    			setAbsDisplayRight();
        		    		}
        		    	}
        		    	return true;
        		    }
        		});
        		
                init();
        }
        
        private final void init() {
            mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            //We need to get the instance of the LayoutInflater, use the context of this activity

    		LayoutInflater inflater = (LayoutInflater) getContext()
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

             //Inflate the view from a predefined XML layout
            timeSelect = (LinearLayout) inflater.inflate(R.layout.time_select,
                    (ViewGroup) findViewById(R.id.timesel_popup));
            
            // display the popup in the center
            // pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
     
            //makeBlack(cancelButton);
            //cancelButton.setOnClickListener(cancel_button_click_listener);
        }
        
        public void invalidate() {
        	super.invalidate();
    		if (listener != null) {
    			listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
    		}
        }

        /**
         * Open a date time selection dialog to let the user enters the time for the begin time for the visible time range.
         */
        private final void setAbsDisplayLeft() {
	    		Toast.makeText(getContext(), "display left", Toast.LENGTH_SHORT).show();  
	    		if (lcDispMinListener != null) {
	    			lcDispMinListener.onLongClickDispMin(this);
	    		}
        }
        
        private final void setAbsLeftThumb() {
	    		Toast.makeText(getContext(), "thumb left", Toast.LENGTH_SHORT).show();  
	    		if (lcThumbMinListener != null) {
	    			lcThumbMinListener.onLongClickThumbMin(this);
	    		}
        }
  
        private final void setAbsRightThumb() {
	    		Toast.makeText(getContext(), "thumb right", Toast.LENGTH_SHORT).show();        	
	    		if (lcThumbMaxListener != null) {
	    			lcThumbMaxListener.onLongClickThumbMax(this);
	    		}
        }

        private final void setAbsDisplayRight() {
	    		Toast.makeText(getContext(), "display right", Toast.LENGTH_SHORT).show();        	
	    		if (lcDispMaxListener != null) {
	    			lcDispMaxListener.onLongClickDispMax(this);
	    		}
        }

        public boolean isNotifyWhileDragging() {
                return notifyWhileDragging;
        }

        /**
         * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
         * 
         * @param flag
         */
        public void setNotifyWhileDragging(boolean flag) {
                this.notifyWhileDragging = flag;
        }

        /**
         * Returns the absolute minimum value of the range that has been set at construction time.
         * 
         * @return The absolute minimum value of the range.
         */
        public T getAbsoluteMinValue() {
                return absoluteMinValue;
        }
        
        public void setAbsoluteMinValue(T value) {
            
        	absoluteMinValue = value;
        	absoluteMinValuePrim = absoluteMinValue.doubleValue();
        }

        /**
         * Returns the absolute maximum value of the range that has been set at construction time.
         * 
         * @return The absolute maximum value of the range.
         */
        public T getAbsoluteMaxValue() {
                return absoluteMaxValue;
        }
        
        public void setAbsoluteMaxValue(T value) {
        	absoluteMaxValue = value;
        	absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        }

        /**
         * Returns the currently selected min value.
         * 
         * @return The currently selected min value.
         */
        public T getSelectedMinValue() {
                return normalizedToValue(normalizedMinValue);
        }

        /**
         * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
         * 
         * @param value
         *            The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
         */
       // public void setSelectedMinValue(T value) {
        public void setSelectedMinValue(T value) {
        	               // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
                if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
                        setNormalizedMinValue(0d);
                }
                else {
                        setNormalizedMinValue(valueToNormalized(value));
                }
        }

        /**
         * Returns the currently selected max value.
         * 
         * @return The currently selected max value.
         */
        public T getSelectedMaxValue() {
                return normalizedToValue(normalizedMaxValue);
        }

        /**
         * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
         * 
         * @param value
         *            The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
         */
        public void setSelectedMaxValue(T value) {
                // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
                if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
                        setNormalizedMaxValue(1d);
                }
                else {
                        setNormalizedMaxValue(valueToNormalized(value));
                }
        }

        /**
         * Registers given listener callback to notify about changed selected values.
         * 
         * @param listener
         *            The listener to notify about changed selected values.
         */
        public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener) {
                this.listener = listener;
        }
        
        public void setOnLongClickDispMinListener(OnLongClickDispMinListener<T> listener) {
            this.lcDispMinListener = listener;
        }

        public void setOnLongClickThumbMinListener(OnLongClickThumbMinListener<T> listener) {
            this.lcThumbMinListener = listener;
        }

        public void setOnLongClickThumbMaxListener(OnLongClickThumbMaxListener<T> listener) {
            this.lcThumbMaxListener = listener;
        }

        public void setOnLongClickDispMaxListener(OnLongClickDispMaxListener<T> listener) {
            this.lcDispMaxListener = listener;
        }

        /**
         * Handles thumb selection and movement. Notifies listener callback on certain events.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
        		super.onTouchEvent(event);
        		
                if (!isEnabled())
                        return false;

                int pointerIndex;

                final int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                        // Remember where the motion event started
                        mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        mDownMotionX = event.getX(pointerIndex);
                        mLastMotionX = mDownMotionX;
                        mNormalizedOffset = screenToNormalized(mDownMotionX) - normalizedMinValue;

                        pressedThumb = evalPressedThumb(mDownMotionX);

                        // Only handle thumb presses.
                        if (pressedThumb == null)
                        	return super.onTouchEvent(event);

                        setPressed(true);
                        invalidate();
                        onStartTrackingTouch();
                        trackTouchEvent(event);
                        attemptClaimDrag();

                        break;
                case MotionEvent.ACTION_MOVE:
                	if (pressedThumb != null) {

                		// Scroll to follow the motion event
                		pointerIndex = event.findPointerIndex(mActivePointerId);
                		final float x = event.getX(pointerIndex);
                		if (mIsDragging) {
                			trackTouchEvent(event);
                		}
                		else {

                			if (Math.abs(x - mLastMotionX) > mScaledTouchSlop) {
                				setPressed(true);
                				invalidate();
                				onStartTrackingTouch();
                				trackTouchEvent(event);
                				attemptClaimDrag();
                			}
                		}

                		if (notifyWhileDragging && listener != null) {
                			listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                		}

                		mLastMotionX = x;                    		  
                	}
                	break;
                case MotionEvent.ACTION_UP:
                        if (mIsDragging) {
                                trackTouchEvent(event);
                                onStopTrackingTouch();
                                setPressed(false);
                        }
                        else {
                                // Touch up when we never crossed the touch slop threshold
                                // should be interpreted as a tap-seek to that location.
                                onStartTrackingTouch();
                                trackTouchEvent(event);
                                onStopTrackingTouch();
                        }

                        pressedThumb = null;
                        invalidate();
                        if (listener != null) {
                                listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                        }
                        break;
                case MotionEvent.ACTION_POINTER_DOWN: {
                        final int index = event.getPointerCount() - 1;
                        // final int index = ev.getActionIndex();
                        mDownMotionX = event.getX(index);
                        mActivePointerId = event.getPointerId(index);
                        invalidate();
                        break;
                }
                case MotionEvent.ACTION_POINTER_UP:
                        onSecondaryPointerUp(event);
                        invalidate();
                        break;
                case MotionEvent.ACTION_CANCEL:
                        if (mIsDragging) {
                                onStopTrackingTouch();
                                setPressed(false);
                        }
                        invalidate(); // see above explanation
                        break;
                }
                return true;
        }

        private final void onSecondaryPointerUp(MotionEvent ev) {
                final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose
                        // a new active pointer and adjust accordingly.
                        // TODO: Make this decision more intelligent.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mDownMotionX = ev.getX(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                }
        }

        private final void trackTouchEvent(MotionEvent event) {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);

                if (Thumb.MIN.equals(pressedThumb)) {
                	setNormalizedMinValue(screenToNormalized(x));
                }
                else if (Thumb.MAX.equals(pressedThumb)) {
                 	setNormalizedMaxValue(screenToNormalized(x));
                }
                else if (Thumb.INBETWEEN.equals(pressedThumb)) {
                	double normalizedRange = normalizedMaxValue - normalizedMinValue;

                	// Check if direction of range bar movement has reached the limit
                	float delta = x - mLastMotionX;
                	if ((delta > 0f && normalizedMaxValue < 1.0f)
                			||
                			(delta < 0f && normalizedMinValue > 0f))
                	{
                		setNormalizedMinValue(screenToNormalized(x) - mNormalizedOffset);
                		setNormalizedMaxValue(screenToNormalized(x) - mNormalizedOffset + normalizedRange);
                	}
                	else if ((delta > 0f && normalizedMaxValue == 1.0f)
                			||
                			(delta < 0f && normalizedMinValue == 0f))
                	{
                		// The bar has hit the left or right edge of the window, so instead of moving the
                		// bar, scroll the display window.
                		// Convert delta (in screen coordinates) into the absolute coordinate
                        mNormalizedOffset = screenToNormalized(x) - normalizedMinValue;
                		double deltaF = (absoluteMaxValuePrim - absoluteMinValuePrim) * delta / getWidth();
                		absoluteMaxValuePrim += deltaF;
                		absoluteMinValuePrim += deltaF;
                		invalidate();
                	}
                }
                else if (Thumb.LEFT_OF_THUMBS.equals(pressedThumb)) {
                	// Ratio of increase / decrease to either side of normalizeMaxValue must be the
                	// same in order to keep the absolute coordinate of the upper thumb invariant
                	double thisX = screenToNormalized(x);
                	if (thisX < normalizedMinValue) {
                		double lastX = screenToNormalized(mLastMotionX);
                		double ratioChange = (normalizedMaxValue - thisX) / (normalizedMaxValue - lastX);
                		double absMaxValue = absoluteMinValuePrim + normalizedMaxValue * (absoluteMaxValuePrim - absoluteMinValuePrim);
                		absoluteMinValuePrim = absMaxValue - (absMaxValue - absoluteMinValuePrim) / ratioChange;
                		absoluteMaxValuePrim = absMaxValue + (absoluteMaxValuePrim - absMaxValue) / ratioChange;
                	}
                }
                else if (Thumb.RIGHT_OF_THUMBS.equals(pressedThumb)) {
                	double thisX = screenToNormalized(x);
                	if (thisX > normalizedMaxValue) {
                		double lastX = screenToNormalized(mLastMotionX);
                		double ratioChange = (thisX - normalizedMinValue) / (lastX - normalizedMinValue);
                		double absMinValue = absoluteMinValuePrim + normalizedMinValue * (absoluteMaxValuePrim - absoluteMinValuePrim);
                		absoluteMinValuePrim = absMinValue - (absMinValue - absoluteMinValuePrim) / ratioChange;
                		absoluteMaxValuePrim = absMinValue + (absoluteMaxValuePrim - absMinValue) / ratioChange;
                	}
                }
        }

        /**
         * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
         */
        private void attemptClaimDrag() {
                if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                }
        }

        /**
         * This is called when the user has started touching this widget.
         */
        void onStartTrackingTouch() {
                mIsDragging = true;
        }

        /**
         * This is called when the user either releases his touch or the touch is canceled.
         */
        void onStopTrackingTouch() {
                mIsDragging = false;
        }

        /**
         * Ensures correct size of the widget.
         */
        @Override
        protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = 200;
                if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
                        width = MeasureSpec.getSize(widthMeasureSpec);
                }
                int height = thumbImage.getHeight();
                if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
                        height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }
                setMeasuredDimension(width, height);
        }

        /**
         * Draws the widget on the given canvas.
         */
        @Override
        protected synchronized void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                // draw seek bar background line
                //final RectF rect = new RectF(padding, 0.5f * getHeight() - 0.1f * lineHeight, getWidth() - padding, 0.5f * getHeight() + 0.1f * lineHeight);
                RectF rect;
                if (Thumb.LEFT_OF_THUMBS.equals(pressedThumb) 
                		||
                		Thumb.RIGHT_OF_THUMBS.equals(pressedThumb)) {
                	rect = new RectF(0, 0.5f * getHeight() - 0.5f * lineHeight, getWidth(), 0.5f * getHeight() + 0.5f * lineHeight);
                	paint.setColor(Color.BLUE);
                }
                else
                {
                	rect = new RectF(0, 0.5f * getHeight() - 0.1f * lineHeight, getWidth(), 0.5f * getHeight() + 0.1f * lineHeight);
                	paint.setColor(Color.GRAY);                	
                }
                paint.setStyle(Style.FILL);
                paint.setAntiAlias(true);
                canvas.drawRect(rect, paint);

                // draw seek bar active range line
                rect.left = normalizedToScreen(normalizedMinValue);
                rect.right = normalizedToScreen(normalizedMaxValue);

                if (Thumb.MIN.equals(pressedThumb) 
                		||
                		Thumb.MAX.equals(pressedThumb)
                		||
                		Thumb.INBETWEEN.equals(pressedThumb)) {
                	// range bar color
                	rect.top -= 3;
                	rect.bottom += 3;
                	paint.setColor(Color.BLUE);
                }
                else {
                	// range bar color
                	rect.top -= 2;
                	rect.bottom += 2;
                	paint.setColor(Color.DKGRAY);
                }
                canvas.drawRect(rect, paint);

                // draw minimum thumb
                drawThumb(normalizedToScreen(normalizedMinValue), 
                		Thumb.MIN.equals(pressedThumb) || Thumb.INBETWEEN.equals(pressedThumb), 
                		canvas);

                // draw maximum thumb
                drawThumb(normalizedToScreen(normalizedMaxValue), 
                		Thumb.MAX.equals(pressedThumb) || Thumb.INBETWEEN.equals(pressedThumb), 
                		canvas);
        }

        /**
         * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method. Other members of this class than the normalized min and max values don't need to be saved.
         */
        @Override
        protected Parcelable onSaveInstanceState() {
        	final Bundle bundle = new Bundle();
        	bundle.putParcelable("SUPER", super.onSaveInstanceState());
                bundle.putDouble("MIN", normalizedMinValue);
                bundle.putDouble("MAX", normalizedMaxValue);
                return bundle;
        }

        /**
         * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method.
         */
        @Override
        protected void onRestoreInstanceState(Parcelable parcel) {
                final Bundle bundle = (Bundle) parcel;
                super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
                normalizedMinValue = bundle.getDouble("MIN");
                normalizedMaxValue = bundle.getDouble("MAX");
        }

        /**
         * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
         * 
         * @param screenCoord
         *            The x-coordinate in screen space where to draw the image.
         * @param pressed
         *            Is the thumb currently in "pressed" state?
         * @param canvas
         *            The canvas to draw upon.
         */
        private void drawThumb(float screenCoord, boolean pressed, Canvas canvas) {
                canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, screenCoord - thumbHalfWidth, (float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
        }

        /**
         * Decides which (if any) thumb is touched by the given x-coordinate.
         * 
         * @param touchX
         *            The x-coordinate of a touch event in screen space.
         * @return The pressed thumb or null if none has been touched.
         */
        private Thumb evalPressedThumb(float touchX) {
                Thumb result = null;
                boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
                boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
                if (minThumbPressed && maxThumbPressed) {
                        // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
                        result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
                }
                else if (minThumbPressed) {
                        result = Thumb.MIN;
                }
                else if (maxThumbPressed) {
                        result = Thumb.MAX;
                }
                else if (isInbetweenThumbs(touchX, normalizedMinValue, normalizedMaxValue))
                {
                		result = Thumb.INBETWEEN;
                }
                else if (isLessThanThumb(touchX, normalizedMinValue)) {
                		result = Thumb.LEFT_OF_THUMBS;
                }
                else if (isGreaterThanThumb(touchX, normalizedMaxValue)) {
                		result = Thumb.RIGHT_OF_THUMBS;
                }
            
                return result;
        }

 		/**
         * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
         * 
         * @param touchX
         *            The x-coordinate in screen space to check.
         * @param normalizedThumbValue
         *            The normalized x-coordinate of the thumb to check.
         * @return true if x-coordinate is in thumb range, false otherwise.
         */
        private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
                return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
        }
        
        /**
         * Decides if given x-coordinate in screen space lies in between the min and max thumbs
         * 
         * @param touchX
         *            The x-coordinate in screen space to check.
         * @param minThumbValue
         *            The x-coordinate of the min thumb.
         * @param maxThumbValue
         *            The x-coordinate of the max thumb.
         * @return true if x-coordinate is in within the min and max thumb range, false otherwise.
         */
        private boolean isInbetweenThumbs(float touchX, double minThumbValue, double maxThumbValue) {
                return (touchX > (normalizedToScreen(minThumbValue) + thumbHalfWidth)
                		&&
                		touchX < (normalizedToScreen(maxThumbValue) - thumbHalfWidth));
        }

        /**
         * Decides if given x-coordinate in screen space lies on the left of the given thumb
         * 
         * @param touchX
         *            The x-coordinate in screen space to check.
         * @param thumbValue
         * 
         * @return true if x-coordinate is in within the min and max thumb range, false otherwise.
         */
        private boolean isLessThanThumb(float touchX, double thumbValue) {
                return (touchX < (normalizedToScreen(thumbValue) - thumbHalfWidth));
        }
        
        /**
         * Decides if given x-coordinate in screen space lies on the right of the given thumb
         * 
         * @param touchX
         *            The x-coordinate in screen space to check.
         * @param thumbValue
         * 
         * @return true if x-coordinate is in within the min and max thumb range, false otherwise.
         */
        private boolean isGreaterThanThumb(float touchX, double thumbValue) {
                return (touchX > (normalizedToScreen(thumbValue) + thumbHalfWidth));
        }

        /**
         * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
         * 
         * @param value
         *            The new normalized min value to set.
         */
        public void setNormalizedMinValue(double value) {
                normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
                invalidate();
        }

        /**
         * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
         * 
         * @param value
         *            The new normalized max value to set.
         */
        public void setNormalizedMaxValue(double value) {
                normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
                invalidate();
        }

        /**
         * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
         * 
         * @param normalized
         * @return
         */
        @SuppressWarnings("unchecked")
        private T normalizedToValue(double normalized) {
                return (T) numberType.toNumber(absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim));
        }

        /**
         * Converts the given Number value to a normalized double.
         * 
         * @param value
         *            The Number value to normalize.
         * @return The normalized double.
         */
        private double valueToNormalized(T value) {
                if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
                        // prevent division by zero, simply return 0.
                        return 0d;
                }
                return (value.doubleValue() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
        }

        /**
         * Converts a normalized value into screen space.
         * 
         * @param normalizedCoord
         *            The normalized value to convert.
         * @return The converted value in screen space.
         */
        private float normalizedToScreen(double normalizedCoord) {
                //return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
                return (float) (normalizedCoord * getWidth());
        }

        /**
         * Converts screen space x-coordinates into normalized values.
         * 
         * @param screenCoord
         *            The x-coordinate in screen space to convert.
         * @return The normalized value.
         */
        private double screenToNormalized(float screenCoord) {
                int width = getWidth();
                if (width <= 2 * padding) {
                        // prevent division by zero, simply return 0.
                        return 0d;
                }
                else {
                        //double result = (screenCoord - padding) / (width - 2 * padding);
                        double result = screenCoord / width;
                        return Math.min(1d, Math.max(0d, result));
                }
        }

        /**
         * Callback listener interface to notify about changed range values.
         * 
         * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
         * 
         * @param <T>
         *            The Number type the RangeSeekBar has been declared with.
         */
        public interface OnRangeSeekBarChangeListener<T> {
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, T minValue, T maxValue);
        }
        
        
        public interface OnLongClickDispMinListener<T> {
        	public void onLongClickDispMin(RangeSeekBar<?> bar);
        }
        
        public interface OnLongClickThumbMinListener<T> {
        	public void onLongClickThumbMin(RangeSeekBar<?> bar);
        }
        
        public interface OnLongClickThumbMaxListener<T> {
        	public void onLongClickThumbMax(RangeSeekBar<?> bar);
        }

        public interface OnLongClickDispMaxListener<T> {
        	public void onLongClickDispMax(RangeSeekBar<?> bar);
        }
        
        /**
         * Thumb constants (min and max).
         */
        private static enum Thumb {
                MIN, MAX, INBETWEEN, LEFT_OF_THUMBS, RIGHT_OF_THUMBS
        };

        /**
         * Utility enumaration used to convert between Numbers and doubles.
         * 
         * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
         * 
         */
        private static enum NumberType {
                LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

                public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
                        if (value instanceof Long) {
                                return LONG;
                        }
                        if (value instanceof Double) {
                                return DOUBLE;
                        }
                        if (value instanceof Integer) {
                                return INTEGER;
                        }
                        if (value instanceof Float) {
                                return FLOAT;
                        }
                        if (value instanceof Short) {
                                return SHORT;
                        }
                        if (value instanceof Byte) {
                                return BYTE;
                        }
                        if (value instanceof BigDecimal) {
                                return BIG_DECIMAL;
                        }
                        throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
                }

                public Number toNumber(double value) {
                        switch (this) {
                        case LONG:
                                return new Long((long) value);
                        case DOUBLE:
                                return value;
                        case INTEGER:
                                return new Integer((int) value);
                        case FLOAT:
                                return new Float(value);
                        case SHORT:
                                return new Short((short) value);
                        case BYTE:
                                return new Byte((byte) value);
                        case BIG_DECIMAL:
                                return new BigDecimal(value);
                        }
                        throw new InstantiationError("can't convert " + this + " to a Number object");
                }
        }


}