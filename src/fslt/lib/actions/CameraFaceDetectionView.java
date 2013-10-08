/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.actions;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * FaceDetection allows for fast detection of faces from video preview. When
 * a face(s) is detected LocalBroadcastManager sends a broadcast out to any 
 * receivers, the default action name to filter on is  
 * fslt.lib.actions.facedetection. The default preview surface size is 320x240
 * and can be changed, not that if you do not want the preview to display
 * set size to 0x0. The default image size, for processing for faces, is 
 * 320x240, typically this should not be changed unless you have a good reason. 
 * <p>
 * Note that by default the camera is in landscape view.
 * <p>
 * Example usage from an activity: 
 * <pre>
 * {@code
 * 
 * 		private CameraFaceDetectionView mCamerafaceDetectionView;
 * 		@Override
 *		protected void onCreate (Bundle savedInstanceState) {
 *			super.onCreate(savedInstanceState);
 * 			mCamerafaceDetectionView = new CameraFaceDetectionView(this);
 * 			setContentView(mCamerafaceDetectionView);  
 * 		}
 * 		@Override 
 *		protected void onPause(){
 *			mCamerafaceDetectionView.runFaceDetector = false; 
 *			super.onPause(); 
 *		}
 *	
 *		@Override 
 *		protected void onResume(){
 *			super.onResume(); 
 *			mCamerafaceDetectionView.runFaceDetector = true; 
 *		}
 *
 * }
 * </pre>
 */
public class CameraFaceDetectionView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private static final String TAG = CameraFaceDetectionView.class.getSimpleName(); 

	private String mActionName = "fslt.lib.actions.facedetection";
	private Context mCtx; 
	//private static Display mDisplay; 
	private int mSurfaceWidth = 320; 
	private int mSurfaceHeight = 240; 
	private SurfaceHolder mSurfaceHolder;
	private FaceDetectionAsyncTask mFDAsyncTask;
	private boolean mFrontCam = false;
	private boolean mRearCam = false; 

	private Camera mCamera;
	public static final int IMAGE_WIDTH = 320;
	public static final int IMAGE_HEIGHT = 240;
	private byte[] mCopiedData = new byte[IMAGE_WIDTH*IMAGE_HEIGHT];
	private int[] mDecodedData = new int[IMAGE_WIDTH*IMAGE_HEIGHT];

	private FaceDetector mFaceDetector = new FaceDetector(IMAGE_WIDTH, IMAGE_HEIGHT, 1);
	private Bitmap mBitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.RGB_565);
	private Face[] mFaces = new Face[1];

	private boolean mBusyFindFaces = false; 
	// set by caller
	public boolean runFaceDetector = false;
	private int OPEN_CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT; 

	/*
	 * Constructor initializes aspects of the surface
	 * 
	 *  @param context 
	 *  			application context 
	 */
	public CameraFaceDetectionView(Context context) {
		super(context);
		mCtx = context; 
		mSurfaceHolder = getHolder(); 
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		PackageManager pm = context.getPackageManager();
		//Must have a targetSdk >= 9 defined in the AndroidManifest
		mFrontCam = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
		mRearCam = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
		if( !mFrontCam ) OPEN_CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; 
	}
	/*
	 * Set internal variable so that front facing camera used for face detection
	 */
	public void setUseFrontFacingCamera(){
		OPEN_CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT; 
	}
	/*
	 * @return true if device has front facing camera, false otherwise.
	 */
	public boolean hasFrontFacingCamera(){
		return mFrontCam; 
	}
	/*
	 * set interanal variable so that back facing camera used for face detection
	 */
	public void setUseBackFacingCamera(){
		OPEN_CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; 
	}
	/*
	 * @return true if device has back facing camera, false otherwise.
	 */
	public boolean hasBackFacingCamera(){
		return mRearCam; 
	}
	public void setSurfaceSize(int width, int height){
		setSurfaceWidth(width);
		setSurfaceHeight(height);
	}
	/*
	 * Set the preview surface width 
	 */
	public void setSurfaceWidth(int width){
		mSurfaceWidth = width;
	}
	/*
	 * @return the preview surface width
	 */
	public int getSurfaceWidth(){
		return mSurfaceWidth; 
	}
	/*
	 * Set the preview surface height
	 */
	public void setSurfaceHeight(int height){
		mSurfaceHeight = height; 
	}
	/*
	 * @return the preview surface height 
	 */
	public int getSurfaceHeight(){
		return mSurfaceHeight; 
	}
	/*
	 * Set action name, this is action name of intent that is broadcast on a
	 * sound level action. Subsequently any broadcast receiver will want to filter 
	 * for this action name. 
	 * 
	 * @param actionName 
	 * 				A String value representing the name of the sound action. 
	 */
	public void setActionName(String actionName){
		mActionName = actionName; 
	}
	/*
	 * Return action name
	 */
	public String getActionName(){
		return mActionName; 
	}
	/*
	 * (non-Javadoc)
	 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)
	 * 
	 * If {@link FaceDetectionAsyncTask} not running start new task and pass current 
	 * preview data for face detection. 
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if( !mBusyFindFaces && runFaceDetector ) {
			mBusyFindFaces = true; 
			System.arraycopy(data, 0, mCopiedData, 0, IMAGE_WIDTH*IMAGE_HEIGHT);
			mFDAsyncTask = new FaceDetectionAsyncTask(); 
			AsyncTask.Status status = mFDAsyncTask.getStatus(); 
			if( (status == AsyncTask.Status.RUNNING) || (status == AsyncTask.Status.FINISHED) )
				Log.d(TAG, "task still running"); 
			try{
				mFDAsyncTask.execute(mCopiedData);
			}catch( IllegalStateException e){
				Log.d(TAG, "IllegalStateException "); 
				e.printStackTrace();
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 * 
	 * Set surface layout width/height for preview in addition to frame rate.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.getLayoutParams();
		layoutParams.width = mSurfaceWidth;
		layoutParams.height = mSurfaceHeight;
		this.setLayoutParams(layoutParams);

		Camera.Parameters parameters = mCamera.getParameters();

		parameters.setPreviewSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		parameters.setPreviewFpsRange(15000, 15000);
		mCopiedData = new byte[IMAGE_WIDTH*IMAGE_HEIGHT];
		mDecodedData = new int[IMAGE_WIDTH*IMAGE_HEIGHT];
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
	/**
	 * NOTE, currently should use landscape mode for camera face detection. 
	 * 
	 * @param cameraId
	 * 				cameraId of front or back camera, which ever is active
	 * @return
	 * 				return degree of orientation
	 */
	/*
	private int setCameraDisplayOrientation(int cameraId ){

		android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	    android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = mDisplay.getRotation(); 
		int degrees = 0; 
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		return result; 
	}
	*/


	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 * 
	 * Open camera and set preview display and callback. 
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = openCamera(OPEN_CAMERA_FACING); //Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(this);
		} catch (IOException exception) {
			mCamera.setPreviewCallback(null);  // Workaround for Android bug
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 * 
	 * Stop camear callback, preview, and release camera
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		//Log.v("APG", "surfaceDestroyed");
		mCamera.setPreviewCallback(null);  // Workaround for Android bug
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
	private int mCameraId; 
	/*
	 * Open desired device camera
	 * 
	 * @param openCameraFacing
	 * 				Integer value representing front or back facing camera 
	 * 				to be opened. 
	 * @return Camera 
	 * 				return the opened camera object, if camera does not exist
	 * 				or problem opening camera a null object is returned.  
	 */
	private Camera openCamera(int openCameraFacing) {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo );
			if ( cameraInfo.facing == openCameraFacing  ) {
				try {
					mCameraId = camIdx;
					cam = Camera.open( mCameraId );
				} catch (RuntimeException e) {
					Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}
		// IF only 1 camera, but not camera wanted by user should we return null
		// or should we return the only camera? 
		return cam;
	}
	/*
	 *  FaceDetectionAsyncTask is a AsyncTask that takes a preview image
	 *  and runs a face detector on it to detect if a face is present in the 
	 *  image. 
	 *  
	 *  @param byte[]
	 *  			byte array representation of image from camera preview
	 */
	private class FaceDetectionAsyncTask extends AsyncTask<byte[], Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(byte[]... _images) {

			for (int i = 0; i < mCopiedData.length; i++) {
				int pixel = mCopiedData[i]; 
				//pixel = pixel > 0 ? pixel : pixel + 255;
				mDecodedData[i] = 0xFF000000 | pixel << 16 | pixel << 8 | pixel << 0;
			}

			mBitmap.setPixels(mDecodedData, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
			try{
				if (mFaceDetector.findFaces(mBitmap, mFaces) > 0) {
					// PROCESS FOUND FACE
					Log.e(TAG, "HEY, Found a FACE!!!!!");
					//final PointF point = new PointF();
					//mFaces[0].getMidPoint(point);
					//final float eyesDistance = mFaces[0].eyesDistance();
					return true; 
				}
			}catch(IllegalArgumentException e){
				//TODO: what do we want to do on exception ? 
				throw new RuntimeException("Incorrect deminstions in setup of " +
						"CameraFaceDetction. findFaces IllegaleArgumentExcpetion");
			}
			return false;
		}
		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 * 
		 * Sends out local braodcast that face was detected. 
		 */
		@Override
		protected void onPostExecute(Boolean result){
			mBusyFindFaces = false; 
			if(result){
				Toast.makeText(mCtx, "Found your face", 500).show();
				//TODO: Should we send back the face? 
				Intent intent = new Intent();
				intent.setAction(mActionName);
				LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
			}
		}

	}

}
