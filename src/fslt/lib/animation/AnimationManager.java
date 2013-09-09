package fslt.lib.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import fslt.lib.views.ImageMediaView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;


/*
 * AnimationManager class defines simple animations that can be assigned and activated 
 * on views. Typically the views will be ImageMedia or TextMedia objects that are part 
 * of the story page. Animations build off of Android's animation api. 
 */
public class AnimationManager {
    public static final String TAG = AnimationManager.class.getSimpleName(); 

    // ActionType enums
    public static final int BOUNCE_BACK_ACTION_REMOVE = 100; 
    public static final int SLIDE_RIGHT_AND_RETURN_ACTION = 101; 
    public static final int SLIDE_LEFT_AND_RETURN_ACTION = 116; 
    public static final int EXPAND_ACTION = 102;
    public static final int SHRINK_ACTION = 103; 
    public static final int EXPAND_SHRINK_ACTION = 104; 
    public static final int FADE_ACTION = 105; 
    public static final int VERTICAL_SQUASH_ACTION = 106; 
    public static final int JUMP_ACTION = 107; 
    public static final int FADE_OUT_ACTION = 108; 
    public static final int FADE_IN_ACTION = 109; 
    public static final int DRAG_ACTION = 110;
    public static final int DRAG_GLIDE_BACK_ACTION = 111;
    public static final int SPIN_ACTION = 112;
    public static final int FADE_IN_OUT_ACTION = 113;
    public static final int HORIZONTAL_SHAKE = 114;
    public static final int VERTICAL_SHAKE = 115;
    public static final int FLIP_HORIZONTAL = 117;
    
    public static final int GOTO_ACTION = 200; 
    
    public static final int IOIO_PIN_01_OUT_ACTION = 301; 
    public static final int IOIO_PIN_01_OUT_TOGGLE_ACTION = 302; 
    
    public static final int NFC_APPEAR_DRAG = 400;
    
    public static final int NO_ANIMATION = 0;

    private String mActionName = "fslt.lib.animation";
    public static final String ANIMATION_STATUS = "animation_status"; 
    public static final int ANIMAITON_COMPLETED = 1; 
    
    private int mScreenWidth; 
    private int mScreenHeight; 

    // Convenient Distances
    private int mTinyHop;
    private int mSmallHop; 
    private int mBigHop; 

    //Repeat values
    private static int ONCE = 1;
    private static int SOME = 4;
    private static int LOTS = 8;

    private static int FAST = 100;
    private static int QUICK = 400;
    private static int NORMAL = 1000;
    private static int SLOW = 2000;
    private static int REALLY_SLOW = 4000;

    // Animation positions
    private int initX = 0;
    private int initY = 0;
    private float initAlpha = 1f; 
    private View view;

    // For images that react to non-touch events we need to have a list so that the animation
    // manager can listen for such events and animate any images that respond to such actions
    public final LinkedList<ImageMediaView> imageMedias = new LinkedList<ImageMediaView>();
    
    // Map of object name to ImageMediaView
    //public final HashMap<String, ImageMediaView> imIndex = new HashMap<String, ImageMediaView>();
    
    private Context mCtx; 
    public AnimationManager(Context ctx){
    	mCtx = ctx; 
    }
    /** Animates an object like an image, allowing all possible animations.
     */
    public boolean animateImage(View view, int actionType,
            int initX, int initY, float initAlpha,
            float scaleX, float scaleY){
        //Every new animation 
        if (getIsAnimated(view)){
            return false;
        } else {
        	Log.d(TAG, "Animating: " + view.toString());
            setIsAnimated(view, true);
        }
        this.initX = initX;
        this.initY = initY;
        this.view = view;
        this.initAlpha = initAlpha; 
        final Intent intent;
        
        switch(actionType){
            // Allow all possible animations for images.
            case AnimationManager.BOUNCE_BACK_ACTION_REMOVE:
                //this.bounceBackAction(view, initY);
            	//TODO: remove this, it is just a jump ... 
                this.jumpAction(view, initY, -1*mSmallHop);
                break;
            case AnimationManager.FLIP_HORIZONTAL:
            	this.flipIt(view);
            	break; 
            case AnimationManager.SLIDE_RIGHT_AND_RETURN_ACTION:		//101 ok
                this.horizontalSlideAndReturnAction(view, initX, mBigHop);
                break;
            case AnimationManager.SLIDE_LEFT_AND_RETURN_ACTION:
            	this.horizontalSlideAndReturnAction(view, initX, (-1*mBigHop) );
            	break;
            case AnimationManager.EXPAND_ACTION: 		    //102 ok
                this.expandAction(view); 
                break; 
            case AnimationManager.SHRINK_ACTION: 		    //103 ok
                this.shrinkAction(view); 
                break; 
            case AnimationManager.EXPAND_SHRINK_ACTION:	    //104 ok
                this.expandShrinkAction(view); 
                break; 
            case AnimationManager.FADE_ACTION: 		        //105 ok
                this.fadeAction(view, initAlpha); 
                break; 
            case AnimationManager.VERTICAL_SQUASH_ACTION:	    //106 very cute, but doesn't come back...?
                this.verticalSquishyAction(view, initY, scaleX, scaleY);
                break;
            case AnimationManager.JUMP_ACTION: 		//107 confused
            	//TODO: make mSmallHop parameter from xml for size and direction of jump
            	// negative value means jump up
                this.jumpAction(view, initY, -1*mSmallHop); 
                break; 
            case AnimationManager.FADE_OUT_ACTION: 	       	    //108 ok, for toolbar icons
                this.fadeOutAction(view);
                break; 
            case AnimationManager.FADE_IN_ACTION:  				//109 ok, for toolbar icons
            	this.fadeInAction(view);
                break; 
            case AnimationManager.DRAG_ACTION : 		        //110 Nothing should happen
                setIsAnimated(view, false);
                break;
            case AnimationManager.NFC_APPEAR_DRAG:
            	view.setVisibility(View.VISIBLE);
            	setIsAnimated(view, false);
            	
            	break;
            case AnimationManager.DRAG_GLIDE_BACK_ACTION: 		//111 This is handled earlier on.
            	// this is handled in ImageMedia 
                break; 
            case AnimationManager.SPIN_ACTION:				//112 ok
            	//postive is clockwise, negative counter clockwise
                this.spinAction(view, 360f);
                break;
            case AnimationManager.FADE_IN_OUT_ACTION:		//113 ok
                this.fadeInOutAction(view);
                break;
            case AnimationManager.HORIZONTAL_SHAKE:			//114 ok
                this.vibrateAction(view, "x", initX);
                break;
            case AnimationManager.VERTICAL_SHAKE:			//115 ok
                this.vibrateAction(view, "y", initY);
                break;
            case AnimationManager.IOIO_PIN_01_OUT_ACTION:			//115 ok
            	intent = new Intent(); 
   			 	intent.putExtra("SET_POWER", true); 
   			 	intent.setAction("edu.mit.media.affect.ioio"); 
   			 	LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
   			 	setIsAnimated(view, false);
                break;
            case AnimationManager.IOIO_PIN_01_OUT_TOGGLE_ACTION:			//115 ok
            	intent = new Intent(); 
   			 	intent.putExtra("SET_POWER", true); 
   			 	intent.setAction("edu.mit.media.affect.ioio"); 
   			 	LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
   			 	
   			 	final View v = view; 
	   			Handler handler = new Handler();
	   			handler.postDelayed(new Runnable() { 
	   			   @Override
	   			   public void run() {
	   			 	LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
	   			 	setIsAnimated(v, false);
	   			   }
	   			}, 300);
   			 	
                break;
            default:
                // Let the default behavior be a slight tremble
                this.defaultAction(view);
                break;
        }
        return true;
    }

    //TODO: currently hard codded, think if we even want to keep this. 
    public void setScreenSize(int width, int height){
        mScreenWidth = width; 
        mScreenHeight = height; 
        mTinyHop = (width + height) / 64; 
        mSmallHop = (width + height) / 8;
        mBigHop = (width + height) / 4;

    }

    //Animation actions
    public void bounceBackAction(View v, int start){
        //TODO add repeat value and duration as parameters, error checking 
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "y", start, start + mBigHop);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener);
        animation.start();
    }

    public void horizontalSlideAndReturnAction(View v, int start, int slideDistance){
        //TODO
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "x", start, start + slideDistance);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener);
        animation.start();
    }

    private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();
    
	public void flipIt(View view){
		ObjectAnimator visToInvis = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f);
        visToInvis.setDuration(500);
        visToInvis.setInterpolator(accelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(view, "rotationY",
                -90f, 0f);
        invisToVis.setDuration(500);
        invisToVis.setInterpolator(decelerator);
        visToInvis.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
            	invisToVis.addListener(animatorListener);
                invisToVis.start();
            }
        });
        visToInvis.start();
	}

    public void expandAction(View v){
        AnimatorSet as = new AnimatorSet();
        expand(v, as);
        as.addListener(animatorListener);
        as.start();
    }
    public void shrinkAction(View v){
        AnimatorSet as = new AnimatorSet();
        shrink(v, as);
        as.addListener(animatorListener);
        as.start();
    }
    public void expandShrinkAction(View v){
        AnimatorSet as = new AnimatorSet();
        expand(v, as);
        AnimatorSet as2 = new AnimatorSet();
        as2.setStartDelay(REALLY_SLOW);
        shrink(v, as2);
        as.start();
        as2.addListener(animatorListener);
        as2.start();
    }
    public void fadeAction(View v, float startAlpha){
    	ObjectAnimator animation; 
    	//0.06 instead of 0.05 because of rounding 
    	if(startAlpha > 0.06f)
    		animation = ObjectAnimator.ofFloat(v, "alpha", startAlpha, 0.05f);
    	else
    		animation = ObjectAnimator.ofFloat(v, "alpha", startAlpha, 1.0f);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener);
        animation.start();
    }


    public void verticalSquishyAction(View v, int startY, float scaleX, float scaleY){
        int id = v.getId(); 
        Log.i("TAG", Integer.toString(id));
        ObjectAnimator moveAnimation; 
        ObjectAnimator shakeAnimation; 
        ObjectAnimator squashAnimation; 
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(animatorListener); 
        
        squashAnimation =  ObjectAnimator.ofFloat(v, "scaleY", 0.5f);
        squashAnimation.setDuration(QUICK);
        squashAnimation.setRepeatCount(ONCE);
        squashAnimation.setRepeatMode(ValueAnimator.REVERSE);
        animatorSet.play(squashAnimation);
        animatorSet.addListener(animatorListener);
        animatorSet.start(); 
    }

    /** Intention is to fade while moving.
     * TODO (joyc): ASK_MICAH, what is this?
     *
     */
    public void jumpAction(View v, int startY, int jumpSize){
        int id = v.getId(); 
        Log.i("TAG", Integer.toString(id));
        
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "y", startY, startY + jumpSize);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener);
        animation.start();
    }

    /** Currently does a very fast tremble just once, to indicate that its been tapped.
     */

    public void defaultAction(View v)
	{
		/* Better to have default be nothing. IMPORTANT
		 * the SHAKE seams to have a bug in it, since the image does
		 * not reset to original 
		float startX = v.getX();
		ObjectAnimator shake = ObjectAnimator.ofFloat(v, "x", startX, startX + mTinyHop);
		shake.setRepeatMode(ValueAnimator.REVERSE);
		shake.setDuration(FAST);
		shake.setRepeatCount(LOTS);
        shake.start();
        */
	}

    public void spinAction(View v, float degrees){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "rotation", 0f, degrees);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void fadeInOutAction(View v){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 1.0f, 0.1f);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void fadeInAction(View v){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 0.25f, 1.0f);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener); 
        animation.start();
    }
    

    public void fadeOutAction(View v){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 1.0f, 0.25f);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void vibrateAction(View v, String direction, int startCoordinate)
    {
    	int speed = 2*mTinyHop; 
        AnimatorSet shake1 = new AnimatorSet();
        AnimatorSet shake2 = new AnimatorSet();
        AnimatorSet shake3 = new AnimatorSet();
        shake(v, shake1, direction, speed, startCoordinate, startCoordinate + mTinyHop);
        shake(v, shake2, direction, speed, startCoordinate, startCoordinate - mTinyHop);
        shake(v, shake3, direction, speed, startCoordinate, startCoordinate + mTinyHop);
        shake2.setStartDelay(speed*2);
        shake3.setStartDelay(speed*4);
        shake1.start();
        shake2.start();
        
        shake3.addListener(animatorListener);
        shake3.start(); 
    }

    public void glide(View v, float initX, float initY, float finalX, float finalY)
    {
        ObjectAnimator xComponent = ObjectAnimator.ofFloat(v, "x", initX, finalX);
        ObjectAnimator yComponent = ObjectAnimator.ofFloat(v, "y", initY, finalY);
        xComponent.setDuration(NORMAL);
        yComponent.setDuration(NORMAL);
        AnimatorSet as = new AnimatorSet();
        as.play(xComponent).with(yComponent);
        setIsAnimated(v, true);
        as.addListener(animatorListener);
        as.start();
    }

    private void expand(View v, AnimatorSet as){
        ObjectAnimator animationX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 2.0f);
        ObjectAnimator animationY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 2.0f);
        animationX.setRepeatMode(ValueAnimator.REVERSE);
        animationY.setRepeatMode(ValueAnimator.REVERSE);
        animationX.setRepeatCount(ONCE);
        animationY.setRepeatCount(ONCE);
        animationX.setDuration(SLOW);
        animationY.setDuration(SLOW);
        as.play(animationX).with(animationY);
    }

    private void shrink(View v, AnimatorSet as){
        ObjectAnimator animationX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.5f);
        ObjectAnimator animationY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.5f);
        animationX.setRepeatMode(ValueAnimator.REVERSE);
        animationY.setRepeatMode(ValueAnimator.REVERSE);
        animationX.setRepeatCount(ONCE);
        animationY.setRepeatCount(ONCE);
        animationX.setDuration(SLOW);
        animationY.setDuration(SLOW);
        as.play(animationX).with(animationY);
    }

    private void shake(View v, AnimatorSet as, String direction, int speed, int startCoordinate, int endCoordinate){
        ObjectAnimator shakeObject = ObjectAnimator.ofFloat(v, direction, startCoordinate, endCoordinate);
        shakeObject.setRepeatMode(ValueAnimator.REVERSE);
        shakeObject.setDuration(speed);
        shakeObject.setRepeatCount(ONCE);
        as.play(shakeObject);
    }
    
    public void setIsAnimated(View view, boolean isAnimated){
        if(view instanceof ImageMediaView){
            ImageMediaView im = (ImageMediaView)view;
            im.isAnimated = isAnimated; 
        }
    }

    public boolean getIsAnimated(View view){
        boolean rtn = false; 
        if(view instanceof ImageMediaView){
            ImageMediaView im = (ImageMediaView)view;
            rtn = im.isAnimated;
        }
        return rtn;
    }
    
    public Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
		public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub

        }

        @Override
		public void onAnimationRepeat(Animator animation) {
            // TODO Auto-generated method stub

        } 
        
        @Override
		public void onAnimationEnd(Animator animation) {
            Object obj = getTargetObjectFromAnimation(animation); 
            if(obj instanceof ImageMediaView){
                ImageMediaView im = (ImageMediaView)obj;
                setIsAnimated(im,false);
            }else if(obj == null){
                Log.d(TAG, "Problem with animaiton, getTargetObjectFromAnimation returned null"); 
            }
            //send message that animation done. 
            Intent intent = new Intent();
			intent.putExtra(ANIMATION_STATUS, ANIMAITON_COMPLETED);
			intent.setAction(mActionName);
			LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
        }

        @Override
		public void onAnimationCancel(Animator animation) {
            // TODO Auto-generated method stub

        }
    };

    //TODO: NEED to make sure only using animation sets and object animators. 
    public Object getTargetObjectFromAnimation(Animator animation){
        ObjectAnimator oba = null; 
        Object obj = null; 
        
        if(animation instanceof AnimatorSet){
            //TODO what if array = 0?
        	ArrayList<Animator> al = ((AnimatorSet)animation).getChildAnimations();
        	Animator anime = null; 
        	for( Animator animator : ((AnimatorSet)animation).getChildAnimations() ){
        		if( animator instanceof ObjectAnimator ){
        			anime = animator; 
        			break; 
        		}
        	}
            obj = ((ObjectAnimator) anime).getTarget(); 
            
        }else if(animation instanceof ObjectAnimator){
            obj = ((ObjectAnimator)animation).getTarget();
        }
        return obj;
         
    }

    public String getActionName(){
    	return mActionName; 
    }
    public void clearOfObjects(){
        imageMedias.clear();
    }

    public void addImageObject(ImageMediaView i ){
        imageMedias.add(i);
    }

    public LinkedList<ImageMediaView> soundActivatedImages() {
        LinkedList<ImageMediaView> result = new LinkedList<ImageMediaView>();

        for (int i = 0; i < imageMedias.size(); i++){
            if (imageMedias.get(i).actionOn == ImageMediaView.ACTION_ON_SOUND){
                result.add(imageMedias.get(i));
            }
        }
        return result;
    }
    /*
    public LinkedList<ImageMediaView> speechActivatedImages() {
        LinkedList<ImageMediaView> result = new LinkedList<ImageMediaView>();

        for (int i = 0; i < imageMedias.size(); i++){
            if (imageMedias.get(i).pm.mAnimateOn == PageMediaObjectInfo.ACTION_ON_SPEECH){
                result.add(imageMedias.get(i));
            }
        }
        return result;
    }
    */
    public LinkedList<ImageMediaView> NFCActivatedImages() {
        LinkedList<ImageMediaView> result = new LinkedList<ImageMediaView>();

        for (int i = 0; i < imageMedias.size(); i++){
            if (imageMedias.get(i).actionOn == ImageMediaView.ACTION_ON_NFC){
                result.add(imageMedias.get(i));
            }
        }
        return result;
    }

    public boolean hasSoundTriggeredMedia(){
    	
        if (soundActivatedImages().size() > 0) {
            return true;
        } else { 
            return false;
        }
    }
    /*
    public boolean hasSpeechTriggeredMedia(){
    	
        if (speechActivatedImages().size() > 0) {
            return true;
        } else { 
            return false;
        }
    }
    */

    /**
    * Triggers all the soundActivated Medias
    */
    public void soundActivate(){
    	LinkedList<ImageMediaView> ims = soundActivatedImages();
    	activateImageObjects(ims);

    }
    /*
    public void speechActivate(){
    	LinkedList<ImageMediaView> ims = speechActivatedImages();
    	activateImageObjects(ims);

    }
    */
    public void NFCActivate(){
    	LinkedList<ImageMediaView> ims = NFCActivatedImages();
    	activateImageObjects(ims);
    }
    
    private void activateImageObjects(LinkedList<ImageMediaView> imgObjects){
    	for (int i = 0; i < imgObjects.size(); i++){
    		imgObjects.get(i).activateMe();
        }
    }

}
