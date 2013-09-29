package fslt.lib.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

public class TakePhoto {

	static public void captureImageAndStoreToLocation(Context context, Uri location, int requestCode){
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		/*
		 * The name of the Intent-extra used to indicate a content resolver Uri 
		 * to be used to store the requested image.
		 */
		intent.putExtra(MediaStore.EXTRA_OUTPUT, location);
		// we start intent to take picture and get results in  onActivityResults
		// of activity that contains the StoryRemixUtility... 
		((Activity) context).startActivityForResult(intent, requestCode);
		
	}
}
