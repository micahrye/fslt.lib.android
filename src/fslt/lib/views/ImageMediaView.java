package fslt.lib.views;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.lang.Math;

import fslt.lib.animation.AnimationManager;

/*
 * ImageMedia class extends ImageView by adding specific variables and methods for layout
 * and interaction dynamics. 
 */
public class ImageMediaView extends ImageView {
	private String TAG = ImageMediaView.class.getName();
	private float initX, initY;
	public float mLastRawX, mLastRawY;
	public float scaleFactorX = 1.0f; 
	public float scaleFactorY = 1.0f; 
	public AnimationManager animator = null;
	public int animationDuration = 500; //in milliseconds
	public boolean isAnimated = false;
	private boolean mIsDraggable = false;
	private boolean mBeingDragged = false;
	public int actionOn = 0; 
	public int action = 0;
	public static int NO_ACTION = 0;
	public static int ACTION_ON_TOUCH = 1; 
	public static int ACTION_ON_SOUND = 2;
	public static int ACTION_ON_NFC = 3; 
	public static int ACTION_ON_FACE = 4; 
	public static int ACTION_ON_SPEECH = 5; 

	private Context mCtx;

	public ImageMediaView(Context context /*, String name, long uid*/) {
		super(context);
		//this.mCtx = context;
		//this.setAdjustViewBounds(true);
	} // End ImageMedia
	public void setDraggable(boolean isDraggable){
		mIsDraggable = isDraggable; 
	}
	public boolean getDraggable(){
		return mIsDraggable;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if( actionOn == NO_ACTION ) return true;
		
		float deltaX, deltaY;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:

			if (mIsDraggable == false) {
				clickEvent(event); 
				/* FIXED subtle bug here. Was returning false, which resulted
				 * in the click be propagated through the stack to lower items 
				 * in the parent view. Should return true*/
				return true;
			}

			if(mIsDraggable){
				initX = this.getX();
				initY = this.getY();
				mBeingDragged = true;
				mLastRawX = event.getRawX(); 
				mLastRawY = event.getRawY();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			//TODO: currently image can be dragged off of screen, in the future stop that
			if(mIsDraggable){
				deltaX = event.getRawX() - mLastRawX; 
				deltaY = event.getRawY() - mLastRawY;
				float newX = this.getX() + deltaX; 
				float newY = this.getY() + deltaY;
				this.setX(newX);
				mLastRawX = event.getRawX();
				this.setY(newY); 
				mLastRawY = event.getRawY();

				mIsDraggable = true; 
			}
			break; 
		case MotionEvent.ACTION_UP:

			if(mIsDraggable){
				deltaX = event.getRawX() - mLastRawX; 
				deltaY = event.getRawY() - mLastRawY;
				float newX = this.getX() + deltaX; 
				float newY = this.getY() + deltaY;
				this.setX(newX);
				mLastRawX = event.getRawX();
				this.setY(newY);
				mLastRawY = event.getRawY();
				mIsDraggable = true;
				mBeingDragged = false;
			}
			if (action == AnimationManager.DRAG_GLIDE_BACK_ACTION)
			{
				if (this.isAnimated){
					break;
				} else {
					this.isAnimated = true;
				}
				this.animator.glide(this, this.getX(), this.getY(), 
						initX, initY, 500);
			}
			break; 
		}
		return mIsDraggable;
	}


	private void clickEvent(MotionEvent event) {
		if(this.animator == null){
			Log.e(TAG, "animator for null");
			//TODO: Throw error or something bad...
			//output log throw error something
			return; 
		}
		// Call animation manager to handle animation
		if (actionOn == ACTION_ON_TOUCH){
			activateMe();
		} 
	}

	/**
	 * Activates the image's animation
	 * 
	 * @return whether or not an animation happened.
	 */
	public boolean activateMe(){
		return this.animator.animateImage(this,
				this.action,
				Math.round(this.getX()),
				Math.round(this.getY()),
				this.getAlpha(),
				this.scaleFactorX,
				this.scaleFactorY, this.animationDuration);
		//NOTE: changes to 
	}
}// End ImageMedia
