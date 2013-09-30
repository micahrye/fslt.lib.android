package fslt.lib.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fslt.lib.R;
import fslt.lib.views.EditableImageView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Use with EditableImageView
 * 
 * @author affect
 *
 */
public class EditImageViewActivity extends Activity{
	public static final String TAG = "ImageEditActivity";
	public static final String INPUT_URI = "inputUri"; 
	public static final String OUTPUT_URI = "outputUri"; 
	private Uri mImageInputUri;
	private Uri mImageOutputUri; 
	private EditableImageView mRemixImage;
	private Button mUndon, mRedo;
	private RadioButton mCut;
	private RadioButton mErase;
	private Button mDone;
	private Intent mIntent;

	// keep track of possible number of redos
	private int mNumRedos = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView( R.layout.fslt_edit_image_activity );

		// Grab elements on page
		mRemixImage = (EditableImageView) findViewById(R.id.fslt_edit_image);
		mUndon = (Button) findViewById(R.id.undo_btn);
		mRedo = (Button) findViewById(R.id.redo_btn);
		mCut = (RadioButton) findViewById(R.id.radio_cut);
		mErase = (RadioButton) findViewById(R.id.radio_erase);
		mDone = (Button) findViewById(R.id.edit_done_btn);

		// Open up image just taken
		mIntent = getIntent();
		mImageInputUri = (Uri) mIntent.getParcelableExtra(INPUT_URI);
		mImageOutputUri = (Uri) mIntent.getParcelableExtra(OUTPUT_URI);
		Log.d(TAG, "Loading image:" + mImageInputUri.toString());

		ContentResolver cr = this.getContentResolver();
		Bitmap newImg;

		try {
			newImg = android.provider.MediaStore.Images.Media
					.getBitmap(cr, mImageInputUri);

			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE); 
			windowManager.getDefaultDisplay().getMetrics(metrics);
			int displayWidth = metrics.widthPixels; 
			int displayHeight = metrics.heightPixels; 
			

			int newW = 0; 
			int newH = 0; 
			// just getting info about the on device camera, mostly for getting at size....  
			int numberOfCameras = Camera.getNumberOfCameras();
			Camera camera = Camera.open(); 
			// camera null if only front facing camera
			if(numberOfCameras == 1 || camera == null){
				//check to see if it is a front facing camera and open correctly if it is. 
				CameraInfo info = new CameraInfo();
				Camera.getCameraInfo(0, info);
				if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
					camera = Camera.open(0);
				}
			}

			try{ 
				android.hardware.Camera.Parameters parameters = camera.getParameters(); 
				List<Size> sizes = parameters.getSupportedPictureSizes(); 

				Size displaySize = camera.new Size(displayWidth, displayHeight);
				
				camera.release(); 
				Collections.sort(sizes, new Comparator<Camera.Size>(){

					@Override
					public int compare(Size a, Size b) {
						// TODO Auto-generated method stub
						return a.width * a.height - b.width * b.height;
					}

				});

				boolean setSize = false; 
				for(int i = 0; i < sizes.size(); i++){
					if( displaySize.width == sizes.get(i).width ){
						newW = sizes.get(i).width; 
						newH = sizes.get(i).height; 
						setSize = true; 
						break; 
					}
				}
				
				if( !setSize ){
					for(int i = sizes.size()-1; i > 0; i--){
						Size size = sizes.get(i);
						if(size.width <= 1280){
							newW = size.width; 
							newH = size.height; 
							break; 
						}
					}	
				}
				
			}catch(NullPointerException e){
				//problem opening camera, this may be because camera not yet released. 
			}

			newImg = Bitmap.createScaledBitmap(newImg, newW, newH, false);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		mRemixImage.setBitmap(newImg);

		// set default tool to be the contour tool
		mRemixImage.setCutMode(true);
		mCut.setChecked(true);
		mRedo.setEnabled(false);


		// END SETUP. BEGIN LISTENERS
		mRemixImage.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mRedo.setEnabled(false);
				return mRemixImage.gotTouched(event);
			}
		});

		mUndon.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "Undoing an action.");
				incrRedo();
				if (mRemixImage.undo()){
					Toast.makeText(getApplicationContext(), "No more actions to undo", Toast.LENGTH_SHORT).show();
				}
			}
		});

		mRedo.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "Redoing an action.");
				mRedo.setEnabled(mRemixImage.redo());
			}
		});

		mDone.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				// disable button since we are finishing and leaving this activity. 
				mDone.setEnabled(false);
				mRemixImage.cropToBounds();
				File imageFile = new File(mImageOutputUri.getPath());
				try {
					//save remixed image
					FileOutputStream fos = new FileOutputStream(imageFile, false);
					boolean fileSaved = mRemixImage.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
					if (!fileSaved){
						Log.e(TAG, "mRemixImage compression failed.");
					}
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Saving mRemixImage failed.");
					e.printStackTrace();
					setResult(Activity.RESULT_CANCELED, mIntent);
				}
				// image was edited and saved successfully 
				setResult(RESULT_OK, mIntent);
				finish();
			}
		});

		mCut.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.d(TAG, "mCut Checked:" + isChecked);
				mRemixImage.setCutMode(isChecked);
			}
		});

		mErase.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.d(TAG, "Erase Checked:" + isChecked);
				mRemixImage.setEraseMode(isChecked);
			}
		});
	}

	private void incrRedo(){
		mRedo.setEnabled(true);
	}
}
