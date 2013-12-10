package fslt.lib.animation;

import java.util.ArrayList;
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
 * on views. Note that while AnimationManager can be used to pass a view to have animate, 
 * it can also hold list of views that should be animated on a fslt.lib.action. In this 
 * case when an action is received it will animate all views that are suppose to respond. 
 * <p>
 * Example of usage: 
 * <pre>
 * {@code
 * 
 * }
 * </pre>
 */
public class AnimationManager {
    public static final String TAG = AnimationManager.class.getSimpleName(); 

    // animation codes
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
    // action name to filter on for broadcast receivers 
    private String mActionName = "fslt.lib.animation";
    public static final String ANIMATION_STATUS = "animation_status"; 
    public static final int ANIMAITON_COMPLETED = 1; 
    
    private int mScreenWidth; 
    private int mScreenHeight; 

    // Convenient Distances defined from screen size 
    private int mTinyHop;
    private int mSmallHop; 
    private int mBigHop; 

    //Repeat values
    private static final int ONCE = 1;
    private static final int SOME = 4;
    private static final int LOTS = 8;
    
    // default times used for animations 
    private static final int FAST = 100;
    private static final int QUICK = 400;
    private static final int NORMAL = 1000;
    private static final int SLOW = 2000;
    private static final int REALLY_SLOW = 4000;

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
    /**
     * animateImage is primary public method used for animating a view
     * 
     * @param view
     * 				view to animate 
     * @param animationCode
     * 				animation code 
     * @param initX
     * 				starting x value of view 
     * @param initY
     * 				starting y value of view 
     * @param initAlpha
     * 				starting alpha value of view 
     * @param scaleX
     * 				starting x scale of view
     * @param scaleY
     * 				starting y scale of view
     * @param duration
     * 				time in milliseconds of animation, note that for animation with 
     * 				several steps or a chain of animation duration may be the time for 
     * 				each step. 
     * @return
     */
    public boolean animateImage(View view, int animationCode, int duration){
        //Every new animation 
    	int initX = (int)view.getX(); 
    	int initY = (int)view.getY(); 
    	float scaleX = view.getScaleX(); 
    	float scaleY = view.getScaleY(); 
    	float initAlpha = view.getAlpha(); 
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
        
        switch(animationCode){
            // Possible animations for views
            case AnimationManager.FLIP_HORIZONTAL:
            	this.flipIt(view, duration);
            	break; 
            case AnimationManager.SLIDE_RIGHT_AND_RETURN_ACTION:		//101 ok
                this.horizontalSlideAndReturnAction(view, initX, mBigHop, duration);
                break;
            case AnimationManager.SLIDE_LEFT_AND_RETURN_ACTION:
            	this.horizontalSlideAndReturnAction(view, initX, (-1*mBigHop), duration );
            	break;
            case AnimationManager.EXPAND_ACTION: 		    //102 ok
                this.expandAction(view, duration); 
                break; 
            case AnimationManager.SHRINK_ACTION: 		    //103 ok
                this.shrinkAction(view, duration); 
                break; 
            case AnimationManager.EXPAND_SHRINK_ACTION:	    //104 ok
                this.expandShrinkAction(view, duration); 
                break; 
            case AnimationManager.FADE_ACTION: 		        //105 ok
                this.fadeAction(view, initAlpha, duration); 
                break; 
            case AnimationManager.VERTICAL_SQUASH_ACTION:	    //106 very cute, but doesn't come back...?
                this.verticalSquishyAction(view, initY, scaleX, scaleY, duration);
                break;
            case AnimationManager.JUMP_ACTION: 		//107 confused
            	//TODO: make mSmallHop parameter from xml for size and direction of jump
            	// negative value means jump up
                this.jumpAction(view, initY, -1*mSmallHop, duration); 
                break; 
            case AnimationManager.FADE_OUT_ACTION: 	       	    //108 ok, for toolbar icons
                this.fadeOutAction(view, duration);
                break; 
            case AnimationManager.FADE_IN_ACTION:  				//109 ok, for toolbar icons
            	this.fadeInAction(view, duration);
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
                this.spinAction(view, 360f, duration);
                break;
            case AnimationManager.FADE_IN_OUT_ACTION:		//113 ok
                this.fadeInOutAction(view, duration);
                break;
            case AnimationManager.HORIZONTAL_SHAKE:			//114 ok
                this.vibrateAction(view, "x", initX, duration);
                break;
            case AnimationManager.VERTICAL_SHAKE:			//115 ok
                this.vibrateAction(view, "y", initY, duration);
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
    
    public void horizontalSlideAndReturnAction(View v, int start, int slideDistance, int duration){
        //TODO
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "x", start, start + slideDistance);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(duration);
        animation.addListener(animatorListener);
        animation.start();
    }

    // Vars used by flipIt
    private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();
    
    /**
     * Flips the view around the horizontal axis. 
     */
	public void flipIt(View view, int duration){
		ObjectAnimator visToInvis = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f);
        visToInvis.setDuration(500);
        visToInvis.setInterpolator(accelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(view, "rotationY",
                -90f, 0f);
        invisToVis.setDuration(duration);
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

    public void expandAction(View v, int duration){
        AnimatorSet as = new AnimatorSet();
        expand(v, as, duration);
        as.addListener(animatorListener);
        as.start();
    }
    public void shrinkAction(View v, int duration){
        AnimatorSet as = new AnimatorSet();
        shrink(v, as, duration);
        as.addListener(animatorListener);
        as.start();
    }
    public void expandShrinkAction(View v, int duration){
        AnimatorSet as = new AnimatorSet();
        expand(v, as, duration);
        AnimatorSet as2 = new AnimatorSet();
        as2.setStartDelay(REALLY_SLOW);
        shrink(v, as2, duration);
        as.start();
        as2.addListener(animatorListener);
        as2.start();
    }
    public void fadeAction(View v, float startAlpha, int duration){
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


    public void verticalSquishyAction(View v, int startY, float scaleX, 
    				float scaleY, int duration){
        int id = v.getId(); 
        Log.i("TAG", Integer.toString(id));
        ObjectAnimator moveAnimation; 
        ObjectAnimator shakeAnimation; 
        ObjectAnimator squashAnimation; 
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(animatorListener); 
        
        squashAnimation =  ObjectAnimator.ofFloat(v, "scaleY", 0.5f);
        squashAnimation.setDuration(duration);
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
    public void jumpAction(View v, int startY, int jumpSize, int duration){
        int id = v.getId(); 
        Log.i("TAG", Integer.toString(id));
        
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "y", startY, startY + jumpSize);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void spinAction(View v, float degrees, int duration){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "rotation", 0f, degrees);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void fadeInOutAction(View v, int duration){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 1.0f, 0.1f);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ONCE);
        animation.setDuration(SLOW);
        animation.addListener(animatorListener); 
        animation.start();
    }

    public void fadeInAction(View v, int duration){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 0.25f, 1.0f);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener); 
        animation.start();
    }
    

    public void fadeOutAction(View v, int duration){
        ObjectAnimator animation = ObjectAnimator.ofFloat(v,  "alpha", 1.0f, 0.25f);
        animation.setDuration(QUICK);
        animation.addListener(animatorListener); 
        animation.start();
    }
    /**
     * TODO: needs to be tested to see what duration value works best. 
     * @param v
     * @param direction
     * @param startCoordinate
     * @param vibrateAction
     * @param duration
     */
    public void vibrateAction(View v, String direction, int startCoordinate, int duration)
    {
    	//TODO: probably should get rid of mTinyHop. 
    	int speed = 2*mTinyHop; 
        AnimatorSet shake1 = new AnimatorSet();
        AnimatorSet shake2 = new AnimatorSet();
        AnimatorSet shake3 = new AnimatorSet();
        shake(v, shake1, direction, startCoordinate, startCoordinate + mTinyHop, duration);
        shake(v, shake2, direction, startCoordinate, startCoordinate - mTinyHop, duration);
        shake(v, shake3, direction, startCoordinate, startCoordinate + mTinyHop, duration);
        shake2.setStartDelay(duration*2);
        shake3.setStartDelay(duration*4);
        shake1.start();
        shake2.start();
        
        shake3.addListener(animatorListener);
        shake3.start(); 
    }

    public void glide(View v, float initX, float initY, float finalX, 
    			float finalY, int duration)
    {
        ObjectAnimator xComponent = ObjectAnimator.ofFloat(v, "x", initX, finalX);
        ObjectAnimator yComponent = ObjectAnimator.ofFloat(v, "y", initY, finalY);
        xComponent.setDuration(duration);
        yComponent.setDuration(duration);
        AnimatorSet as = new AnimatorSet();
        as.play(xComponent).with(yComponent);
        setIsAnimated(v, true);
        as.addListener(animatorListener);
        as.start();
    }

    private void expand(View v, AnimatorSet as, int duration){
        ObjectAnimator animationX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 2.0f);
        ObjectAnimator animationY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 2.0f);
        animationX.setRepeatMode(ValueAnimator.REVERSE);
        animationY.setRepeatMode(ValueAnimator.REVERSE);
        animationX.setRepeatCount(ONCE);
        animationY.setRepeatCount(ONCE);
        animationX.setDuration(duration);
        animationY.setDuration(duration);
        as.play(animationX).with(animationY);
    }

    private void shrink(View v, AnimatorSet as, int duration){
        ObjectAnimator animationX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.5f);
        ObjectAnimator animationY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.5f);
        animationX.setRepeatMode(ValueAnimator.REVERSE);
        animationY.setRepeatMode(ValueAnimator.REVERSE);
        animationX.setRepeatCount(ONCE);
        animationY.setRepeatCount(ONCE);
        animationX.setDuration(duration);
        animationY.setDuration(duration);
        as.play(animationX).with(animationY);
    }

    private void shake(View v, AnimatorSet as, String direction, 
    			int startCoordinate, int endCoordinate, int duration){
        ObjectAnimator shakeObject = ObjectAnimator.ofFloat(v, direction, 
        				startCoordinate, endCoordinate);
        shakeObject.setRepeatMode(ValueAnimator.REVERSE);
        shakeObject.setDuration(duration);
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
