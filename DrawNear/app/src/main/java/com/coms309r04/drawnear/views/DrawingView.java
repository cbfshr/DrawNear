package com.coms309r04.drawnear.views;

import android.view.View;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.connection.DrawingManager;
import com.coms309r04.drawnear.data.DrawingItem;
import com.parse.GetDataCallback;
import com.parse.ParseException;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;
import android.view.Display;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.util.DisplayMetrics;


public class DrawingView extends View {

	// drawing path
	private Path drawPath;
	// drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	// initial color
	private int paintColor = 0xFF990000;
	// canvas
	private Canvas drawCanvas;
	// canvas bitmap
	private Bitmap canvasBitmap;
	private float brushSize, lastBrushSize;
	private boolean erase = false;

static final String TAG = "DrawView"; 
	
	//These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = 1f; 
    private static float MAX_ZOOM = 5f; 
    private float scaleFactor = 1.f; 
    private ScaleGestureDetector detector; 
    
  //These constants specify the mode that we're in
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private int mode;
    
  //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f; 
    private float startY = 0f; 
    
  //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f; 
    private float translateY = 0f; 
    
  //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f; 
    private float previousTranslateY = 0f; 

    private boolean dragged = false; 

// Used for set first translate to a quarter of screen
    private float displayWidth; 
    private float displayHeight; 

    Context ctx;
	private DrawingItem toEdit;
	
	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpDrawing();
		 detector = new ScaleGestureDetector(context, new ScaleListener());

	        setFocusable(true);
	        setFocusableInTouchMode(true);  
		
	}

	private void setUpDrawing() {
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		// set up drawing area for interaction
		drawPath = new Path(); // drawing path
		drawPaint = new Paint(); // drawing paint

		drawPaint.setColor(paintColor);

		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);

		canvasPaint = new Paint(Paint.DITHER_FLAG); // canvas paint
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// view given size
		super.onSizeChanged(w, h, oldw, oldh);
		
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// canvas
		drawCanvas = new Canvas(canvasBitmap);
		
		//Load in image to edit if neccessary
		if (toEdit != null)
		{
			if (toEdit.getBitmapFile() != null) {
				toEdit.getBitmapFile().getDataInBackground(new GetDataCallback() {
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
							canvasBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
							drawCanvas = new Canvas(canvasBitmap);
							invalidate();
						} else {
							e.printStackTrace();
						}
					}

				});
			}
			// bitmap
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw view
		super.onDraw(canvas);
		

        //We're going to scale the X and Y coordinates by the same amount
        //canvas.scale(scaleFactor, scaleFactor, 0, 0);
	canvas.scale(this.scaleFactor, this.scaleFactor, this.detector.getFocusX(), this.detector.getFocusY());

      //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound
        if((translateX * -1) < 0) {
        translateX = 0;
        }
        
      //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if((translateX * -1) > (scaleFactor - 1) * displayWidth) {
        translateX = (1 - scaleFactor) * displayWidth;
        }
        if(translateY * -1 < 0) {
        translateY = 0;
        }
        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((translateY * -1) > (scaleFactor - 1) * displayHeight) {
        translateY = (1 - scaleFactor) * displayHeight;
        }

        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        canvas.translate((translateX) / scaleFactor, (translateY) / scaleFactor);

       // canvas.drawRect(0, 0, 100, 100, drawPaint);
       // canvas.drawBitmap(canvasBitmap, 0, 0, drawPaint);
        
        canvas.save();
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
		
		 
        canvas.restore(); 	
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// detect user touch
		float touchX = event.getX();
		float touchY = event.getY();

switch (event.getAction() & MotionEvent.ACTION_MASK) {
		
		
		case MotionEvent.ACTION_DOWN:
			
		//drawPath.moveTo(touchX, touchY);
			
			mode = DRAG;
			startX = event.getX()-previousTranslateX;
            startY = event.getY()- previousTranslateY;
       
			
		    drawPath.moveTo(touchX, touchY);
		    
		    break;
		    
		    
		case MotionEvent.ACTION_MOVE:
			//  drawPath.lineTo(touchX, touchY);
			// first finger is moving on screen  
			// update translation value to apply on Path

			            translateX = event.getX() - startX;
			            translateY = event.getY() - startY;  
			     
			        
			            double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
			            		Math.pow(event.getY() - (startY + previousTranslateY), 2)
			            	
			            		);
			            		
			            	if(distance > 0) {
			            	dragged = true;
			            	distance *= scaleFactor;
			            	}      
			            	
			            	drawPath.lineTo(touchX, touchY);       
		    break;
		    
		case MotionEvent.ACTION_POINTER_DOWN:
			mode = ZOOM;
			break;

		case MotionEvent.ACTION_UP:
			mode = NONE;
			dragged = false;
			// No more fingers on screen
			  drawCanvas.drawPath(drawPath, drawPaint);
			 drawPath.reset();
            // All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
            //previousTranslate
            previousTranslateX = translateX;
            previousTranslateY = translateY;
		  
		    break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = DRAG;
			//This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
			//and previousTranslateY when the second finger goes up
			previousTranslateX = translateX;
			previousTranslateY = translateY;
			break;   
		    
		default:
	    return false;
		}
		detector.onTouchEvent(event);
		
		if (mode == DRAG ||mode == ZOOM || mode == NONE) {
		//	mode = NONE;
			invalidate();
		}
		return true;
	}
	
	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {


            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

        //    invalidate();
            return true;
        }   
    }

	public void setColor(String newColor) {
		// set color
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);

	}

	public void setBrushSize(float newSize) {
		// update size
		float pixelAmount = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, newSize, getResources()
						.getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize) {
		lastBrushSize = lastSize;
	}

	public float getLastBrushSize() {
		return lastBrushSize;
	}

	public void setErase(boolean isErase) {
		// set erase true or false
		erase = isErase;
		if (erase)
			drawPaint
					.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else
			drawPaint.setXfermode(null);
	}

	public void startNew() {
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}

	public DrawingView setDrawingToEdit(DrawingItem toEdit) {
		this.toEdit = toEdit;
		
		
		
		return this;
	}

}
