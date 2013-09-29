package fslt.lib.views;

import java.util.Stack;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Used with EditImageViewActivity
 * 
 * @author affect
 *
 */
public class EditableImageView extends ImageView {
	private static final String TAG = "RemixImageView";

	// IMAGE MANIP
	public Bitmap bitmap; 
	private Bitmap mTempEditBitmap;
	private Canvas mCanvas;
	private boolean mInCutMode = false;
	private boolean mInEraseMode = false;
	private int mOrigX = 0, mOrigY = 0, mNewX = 0, mNewY = 0;
	
	private int mOriginalBitmapWidth;
	private int mOriginalBitmapHeight;
	
	int[] pixels;
	private int minX, minY, maxX, maxY;
	
	// CROPPING AND TRIMMING
	private Path mPath = new Path();
	private Paint mRedStroke = new Paint();
	private Paint mEraseStroke = new Paint();

	private Stack<Bitmap> undoImageStack = new Stack<Bitmap>(),
			redoImageStack = new Stack<Bitmap>();

	public EditableImageView(Context context) {
		super(context);
	}
	
	public EditableImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
	
    public EditableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
    /**
     * Set this to show an image in the pane
     * @param newBitmap
     */
	public void setBitmap(Bitmap newBitmap){
		mOriginalBitmapWidth = newBitmap.getWidth();
		mOriginalBitmapHeight = newBitmap.getHeight();
		minX = minY = 0;
		maxX = mOriginalBitmapWidth;
		maxY = mOriginalBitmapHeight;
		bitmap = Bitmap.createBitmap(mOriginalBitmapWidth, mOriginalBitmapHeight, Bitmap.Config.ARGB_8888);
		
		pixels = new int[mOriginalBitmapWidth * mOriginalBitmapHeight];
		newBitmap.getPixels(pixels, 0, mOriginalBitmapWidth, 0, 0, mOriginalBitmapWidth, mOriginalBitmapHeight);
		bitmap.setPixels(pixels, 0, mOriginalBitmapWidth, 0, 0, mOriginalBitmapWidth, mOriginalBitmapHeight);
		mTempEditBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		this.setImageBitmap(bitmap);
		
		/*
		this.setOnTouchListener(new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent me)
        	{
        		return gotTouched(me);
        	}
        });
		*/
	}

	public void setCutMode(boolean cut_on) {
		mInCutMode = cut_on;
	}
	

	public void setEraseMode(boolean erase_on) {
		mInEraseMode = erase_on;
	}

	public boolean undo() {
		if (!undoImageStack.isEmpty()) {
			this.setImageBitmap(undoImageStack.peek());
			redoImageStack.push(bitmap.copy(Config.ARGB_8888, true));
			bitmap = undoImageStack.pop();
		}
		return undoImageStack.isEmpty();
	}

	public boolean redo() {
		if (!redoImageStack.isEmpty()) {
			this.setImageBitmap(redoImageStack.peek());
			undoImageStack.push(bitmap.copy(Config.ARGB_8888, true));
			bitmap = redoImageStack.pop();
		}
		return !redoImageStack.isEmpty();
	}
	
	private void pushToUndo(){
		Log.d(TAG, "Undo size: " + undoImageStack.size());
		Log.d(TAG, "Redo size: " + redoImageStack.size());
		Log.d(TAG, "Pushing to undo stack");
		undoImageStack.push(bitmap.copy(Config.ARGB_8888, true));
		redoImageStack.clear();
	}
	
	public void cropToBounds(){
		verifyMinMax(); 
		int width = (maxX - minX);
		int height = (maxY- minY);
		Log.d(TAG, "Cropping: " + width + ", " + height);
		bitmap.getPixels(pixels, 0, width, minX, minY, width, height);
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	}
	private void verifyMinMax(){
		minX = minX < 0 ? 0 : minX;
		maxX = maxX > mOriginalBitmapWidth ? mOriginalBitmapWidth : maxX; 
		
		minY = minY < 0 ? 0 : minY;
		maxY = maxY > mOriginalBitmapHeight ? mOriginalBitmapHeight : maxY;	
	}

	private void setNewTouchPosition(float x, float y){
		float[] eventXY = new float[] { x, y };

		Matrix invertMatrix = new Matrix();
		this.getImageMatrix().invert(invertMatrix);

		invertMatrix.mapPoints(eventXY);

		mNewX = (int) eventXY[0];
		mNewY = (int) eventXY[1];
	}
	
	private void updateMaxMin(){
		 
		if (mNewX < minX){
			minX = mNewX;
		} else if (mNewX > maxX){
			maxX = mNewX;
		}
		
		if (mNewY < minY){
			minY = mNewY;
		} else if (mNewY > maxY){
			maxY = mNewY; 
		}
		// NEED to confirm that min/max not greater/less than image bounds also.
		verifyMinMax(); 
	}
	
	public boolean gotTouched(MotionEvent me) {
		switch (me.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// CONVERT INITIAL POSITION AND SAVE
			float[] eventXY = new float[] { me.getX(), me.getY()};

			Matrix invertMatrix = new Matrix();
			this.getImageMatrix().invert(invertMatrix);

			invertMatrix.mapPoints(eventXY);
			int transX = (int) eventXY[0];
			int transY = (int) eventXY[1];

			mOrigX = transX;
			mOrigY = transY;
			mNewX = transX;
			mNewY = transY;
			Log.d(TAG, "oX:" + mOrigX + ", oY: "+ mOrigY + ", nX: " + mNewX + ", nY:" + mNewY);
			
			mTempEditBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			mCanvas = new Canvas(mTempEditBitmap);
			
			mPath = new Path();
	        mPath.moveTo(mNewX, mNewY);

			mEraseStroke.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			mEraseStroke.setColor(Color.TRANSPARENT);
			mEraseStroke.setStyle(Paint.Style.STROKE);
			mEraseStroke.setStrokeWidth(50.0f);

			mRedStroke.setColor(android.graphics.Color.RED);
			mRedStroke.setStyle(Paint.Style.STROKE);
			mRedStroke.setStrokeWidth(5.0f);
			
			if (mInCutMode) // We want to make transparent
			{
				minX = maxX = transX;
				minY = maxY = transY;
			}
			break;
		case MotionEvent.ACTION_UP:
			pushToUndo();
			if (mInCutMode){
				Log.d(TAG, "closing cut");
			    mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
		        mPath.lineTo(mOrigX, mOrigY);
		        mPath.close();
		        
				mRedStroke.setStyle(Paint.Style.FILL);
				mRedStroke.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
				mRedStroke.setColor(Color.TRANSPARENT);
				
				mCanvas = new Canvas(bitmap);
		        mCanvas.drawPath(mPath, mRedStroke);
				this.setImageBitmap(bitmap);
			} else if (mInEraseMode){
				bitmap = mTempEditBitmap.copy(Bitmap.Config.ARGB_8888, true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			setNewTouchPosition(me.getX(), me.getY());
			Log.d(TAG, "oX:" + mOrigX + ", oY: " + mOrigY + ", nX: " + mNewX
					+ ", nY:" + mNewY);
			if (mInCutMode) {
				// Continue to draw a red line
		        mPath.lineTo(mNewX, mNewY);
		        mCanvas.drawPath(mPath, mRedStroke);
				this.setImageBitmap(mTempEditBitmap);
				
				// update bounds for cropping later
				this.updateMaxMin();
			} else if (mInEraseMode) {
				// erase
				mPath.lineTo(mNewX, mNewY);
		        mCanvas.drawPath(mPath, mEraseStroke);
				this.setImageBitmap(mTempEditBitmap);
			}
			break;
		case MotionEvent.ACTION_CANCEL: {
			return true;
			}
		}
		return true;
	}
}