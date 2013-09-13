/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.Preference;
import android.util.DisplayMetrics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fslt.lib.file.FileOperations;

public class BitmapFileOperations {

	public static final int INTERANL_STORAGE = FileOperations.INTERANL_STORAGE;
	public static final int EXTERNAL_STORAGE = FileOperations.EXTERNAL_STORAGE; 
	public static final int ASSETS_STORAGE = FileOperations.ASSETS_STORAGE; 
	
	private Context mCtx; 
	private Preference mPrefs;
	private DisplayMetrics mDisplayMetrics;
	private FileOperations mFileOperations; 
	
	public BitmapFileOperations(Context context){
		mCtx = context; 
		mPrefs = null; //new Preference(mCtx);
		mDisplayMetrics = mCtx.getResources().getDisplayMetrics();
		mFileOperations = new FileOperations(mCtx); 
	}
	/**
	 * Open bitmap file from storage location 
	 * 
	 * @param  location
	 *  			Use FileOperations.INTERNAL_STORAGE or FileOperations.EXTERNAL_STORAGE
	 *  			or FileOperations.ASSETS_STORAGE to indicate root of file location.
	 *  @param  fileName
	 * 				The file name including any parent directory structure 
	 * 				information that will all be relative to the storage 'location' 
	 * 				directory.
	 * @param openWithScreenDpi
	 * 				boolean value that determines if the bitmap should be opened with 
	 * 				the device screens density. In most situations this is what you will
	 *   			want to do for correct scaling. True means that a 100x100 bitmap at 
	 *   			mdpi will be 100x100 bitmap at xhdpi also. 
	 * @return Bitmap
	 * 				Returns the opened and scaled bitmap or null if the bitmap was not
	 * 				able to be opened 
	 * 	
	 */
	public Bitmap openBitmapFromStorageLocation(int location, String fileName, 
							boolean openWithScreenDpi) throws IOException{
		File file = null;
		InputStream inputStream = null; 
		Bitmap bmp = null; 
		inputStream = getBitmapInputStreamFromStorageLocation(location, fileName);
		if( inputStream == null ) return null; 
		BitmapFactory.Options options = getDefaultBitmapOptionsForScreenDpi(openWithScreenDpi);
		bmp = BitmapFactory.decodeStream(inputStream, null, options);
		try{
			inputStream.close();
		}catch(IOException e){
			// hum
		}
		
		return bmp;
	}
	/**
	 * 
	 * @param location
	 * @param fileName
	 * @return
	 * @see openBitmapFromStorageLocation
	 */
	private InputStream getBitmapInputStreamFromStorageLocation(int location, String fileName){
		InputStream inputStream = null; 

		inputStream = mFileOperations.getInputStreamFromStorageLocation(location, fileName);
		
		return inputStream;
	}
	/**
	 * Open bitmap file from storage location at size specified by input parameters  
	 * 
	 * @param  location
	 *  			Use FileOperations.INTERNAL_STORAGE or FileOperations.EXTERNAL_STORAGE
	 *  			or FileOperations.ASSETS_STORAGE to indicate root of file location.
	 * @param  fileName
	 * 				The file name including any parent directory structure 
	 * 				information that will all be relative to the storage 'location' 
	 * 				directory.
	 * @param width
	 * 				desired width of scaled bitmap
	 * @param height 
	 * 				desired height of scaled bitmap
	 * @param openWithScreenDpi
	 * 				boolean value that determines if the bitmap should be opened with 
	 * 				the device screens density. In most situations this is what you will
	 *   			want to do for correct scaling. 
	 * @return Bitmap
	 * 				Returns the opened and scaled bitmap or null if the bitmap was not 
	 * @throws IOException 
	 * 	
	 */
	public Bitmap openBitmapAtSizeFromStorageLocation(int location, String fileName, 
				int width, int height, boolean openWithScreenDpi) throws IOException{
		
		InputStream inputStream = getBitmapInputStreamFromStorageLocation(location, fileName);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		//if(openWithScreenDpi) options = getDefaultBitmapOptionsForScreenDpi(openWithScreenDpi); 
		options.inJustDecodeBounds = true;
		Bitmap bmp = null; 
		bmp = BitmapFactory.decodeStream(inputStream, null, options); 
		
		int sampleSize = calculateInSampleSize(options, width, height);
		if(openWithScreenDpi) options = getDefaultBitmapOptionsForScreenDpi(openWithScreenDpi);
		options.inSampleSize = sampleSize; 
		options.inJustDecodeBounds = false;
		// sample the image to open as close to desired size as possible, this will 
		// be significantly less memory usage when large image that you want to open
		// at small size. 
		inputStream = getBitmapInputStreamFromStorageLocation(location, fileName);
		bmp = BitmapFactory.decodeStream(inputStream, null, options);
		inputStream.close();
		return Bitmap.createScaledBitmap(bmp, width, height, true);
	}
	/**
	 * Given actual image width/height in options and desired width/height
	 * calculate the appropriate in sample size to read image from file. 
	 * 
	 * @param options
	 * 				BitmapFactory.Options that contain bitmap meta-data for the
	 * 				bitmap to be used for calculations 
	 * @param desiredWidth
	 * 				Desired width of bitmap when opened
	 * @param desiredHeight
	 * 				Desired height of bitmap when opened
	 * @return int	
	 * 				sample size to use given bitmap and desired open size
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options, 
				int desiredWidth, int desiredHeight){
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > desiredHeight || width > desiredWidth) {
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) desiredHeight);
	        final int widthRatio = Math.round((float) width / (float) desiredWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	    return inSampleSize;
	}
	/**
	 * Get FileOutputStream for bitmap at storage location 
	 * 
	 * @see saveBitmapToStorageLocation
	 */
	private FileOutputStream getBitmapFileSaveLocation(int location, String fileName) throws IOException{
		FileOutputStream file = null; 
		if(location == EXTERNAL_STORAGE){
			if(!mFileOperations.externalStorageAvailable()){
				//TODO: talk with others, do we want to throw an exception or something else?
				throw new IOException("sdcard not readable, cannot open file");
			}
			//TODO: think about how you want to handle possible nullpointerexception from null fileName
			file = new FileOutputStream(Environment.getExternalStorageDirectory() + fileName);
		}else if(location == INTERANL_STORAGE){
			String path = new File(mCtx.getFilesDir(), "").getAbsolutePath() +fileName;
			try{
				file = new FileOutputStream(path);
			}catch(FileNotFoundException e){
				e.printStackTrace(); 
			}
		}
		return file;
	}
	/**
	 * Set options used when decoding bitmap based on the screenDpi
	 * 
	 * @param screenDpi 
	 * 			Integer value for the devices screenDpi. If value 0 then 
	 * 			device screen dpi will be ignored. 
	 */
	public BitmapFactory.Options getDefaultBitmapOptionsForScreenDpi(boolean atScreenDpi){
		BitmapFactory.Options options = new BitmapFactory.Options();
		if( !atScreenDpi ) return options;  
		
		// inDensity is the density that the image was created at, assume 160
		options.inDensity = DisplayMetrics.DENSITY_MEDIUM;
		// inTargetDensity is the density that that the bitmap will be at 
		options.inTargetDensity = mDisplayMetrics.densityDpi; 
		// inScaled true be default, means if inDensity and inTargetDensity not = 0 
		// then bitmap will be scaled to target density. suppose indensity mdpi
		// and targetdensity xhdpi then image will be scaled by 2 to device density
		// so 100 width would be 200 width at device density. If false then 
		// 100 width is 100 width at device density. 
		options.inScaled = false;
		return options; 
	}
	/**
	 * Save bitmap to INTERANL_STORAGE or EXTERNAL_STORAGE, note that these are the only two
	 * locations that can be saved to, you cannot save to assets or resources folders. 
	 * 
	 * @param  location
	 *  			Use FileOperations.INTERNAL_STORAGE or FileOperations.EXTERNAL_STORAGE
	 *  			to indicate root of file location.
	 * @param  fileName
	 * 				The file name including any parent directory structure 
	 * 				information that will all be relative to the storage 'location' 
	 * 				directory.
	 * @param bmp
	 * 				Bitmap file to save to storage location
	 * @throws
	 * 				IOException
	 */
	public void saveBitmapToStorageLocation(int location, String fileName, Bitmap bmp) throws IOException{
		FileOutputStream fOut = null;//new FileOutputStream(Environment.getExternalStorageDirectory() + fileName);
		fOut = getBitmapFileSaveLocation(location, fileName);
		try{
			bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut); 
			fOut.flush(); 
			fOut.close(); 
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
