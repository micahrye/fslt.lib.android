/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.network;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fslt.lib.file.UnzipService;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/* 
 * DownloadManagerReceiver is used in conjunction with the Android native DownloadManager
 * and receives the downloaded data and saves it to disk. 
 * 
 * Add to Manifest
 * <pre>
 * <receiver android:name="DownloadManagerReceiver">
            <intent-filter >
                <action android:name="android.intent.action.DOWNLOAD_COMPLETE" />
            </intent-filter>
        </receiver>
 * </pre>
 * <p> 
 * In your code to download you will want to request the download from DownloadManager, and 
 * you may want to check to see if the file is already in the queue for download. 
 * 
 */
public abstract  class DownloadManagerReceiver extends BroadcastReceiver {
	private final static String TAG = DownloadManagerReceiver.class.getSimpleName(); 
	public String mPathInfo = null; 
	//TODO: since this is a broadcast receiver that  
	protected String mRootAppStorageDir = ""; 

	protected abstract void setRootApplicationStorageDirectory();	

	@Override
	public void onReceive(Context context, Intent intent) {
		setRootApplicationStorageDirectory(); 

		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			long downloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			Query query = new Query();
			query.setFilterById(downloadId);
			DownloadManager mDownloadMngr = 
					(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

			Cursor c = mDownloadMngr.query(query);
			if (c.moveToFirst()) {
				int columnIndex = c
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex) ) {
					String downloadedFileUri = c
							.getString(c
									.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
									.substring(7);

					handleDownloadedFile(context, downloadedFileUri);

				}else{
					Log.d(TAG, "not success"); 
				}
			}else{
				Log.d(TAG, "not complete"); 
			}
		}
	}

	private DownloadManager mDownloadMngr;

	public long requestDownloadFile(Context context, String url, String fileName){

		mDownloadMngr = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

		Request request = new Request(Uri.parse(url));
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(
				DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		//String fileName = url.substring(URL.lastIndexOf("/")+1, url.length());

		//Check if file is currently being downloaded, if so do not download it. 
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterByStatus(DownloadManager.STATUS_RUNNING | 
				DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED);
		Cursor cursor = mDownloadMngr.query(query);
		if(cursor.moveToFirst()){
			int columnTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
			String downloadFileTitle = cursor.getString(columnTitle);
			boolean same = downloadFileTitle.equalsIgnoreCase(fileName); 
			same = downloadFileTitle.equalsIgnoreCase("omg_bees".toString()); 
			if(downloadFileTitle.equalsIgnoreCase( fileName )){
				Toast.makeText(context, "Alreading downloading file", 1000).show();
				return -1l;
			}else{
				return requestDownload(context, request, fileName);
			}
		}else{
			return requestDownload(context, request, fileName);
		}

	}

	private long requestDownload(Context context, Request request, String filename){
		request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
		request.setDescription("STORYSCAPE_STORY_DOWNLOAD");
		request.setTitle(filename);
		return mDownloadMngr.enqueue(request);
	}
	/**
	 * 
	 * @param context
	 * @param downloadedFileUri
	 */
	public abstract void handleDownloadedFile(Context context, String downloadedFileUri); 

	/**
	 * 
	 * @param context
	 * @param downloadedFileUri
	 * @param unzipServiceIntent
	 */
	public void defaultUnzipFile(Context context, String downloadedFileUri, Intent unzipServiceIntent){
		String fileName = "";
		String storyURI = downloadedFileUri.replaceAll("-\\d\\.zip\\z", ".zip"); 
		String storyName = "";
		Pattern pattern = Pattern.compile(".*\\.zip\\z");
		Matcher matcher = pattern.matcher(storyURI);
		if(matcher.find()){
			try{
				int start = matcher.group().lastIndexOf("/") + 1;
				int end = matcher.group().length()-4; 	
				fileName = matcher.group().substring(start, end);
				/* Now, if it's a zip file,  we want to unzip it...
				 * It is possible to be download a story more than once, which results in 
				 * the story name being appended with '-2', i.e. dash number. We want to remove 
				 * that so that our unpacked folder name is only the story name. Otherwise, we 
				 * have the same story in our device library with the appended dash number, even
				 * though they are all the same story. 
				 */
			}catch( IllegalStateException e ){
				//Should raise an error 
				e.printStackTrace(); 
			}
			// zip is crated 'zip -rj' and opened 'unzip fileName.zip -d fileName/
			StringBuilder unzipLocation = new StringBuilder();
			unzipLocation.append(Environment.getExternalStorageDirectory());
			unzipLocation.append(File.separator).append(mRootAppStorageDir); 
			unzipLocation.append(File.separator).append(fileName).append(File.separator);

			unzipServiceIntent.putExtra(UnzipService.FILE_NAME, fileName);
			unzipServiceIntent.putExtra(UnzipService.ZIP_FILE_LOCATOIN, downloadedFileUri);
			unzipServiceIntent.putExtra(UnzipService.UNZIP_FILE_TO_LOCATION, unzipLocation.toString() ); 
			unzipServiceIntent.putExtra(UnzipService.SHOW_TOAST, true); 
			unzipServiceIntent.putExtra(UnzipService.DELETE_ZIP_FILE_AFTER_UNZIP, true);
			context.startService(unzipServiceIntent);
		}else{
			Log.d(TAG, "something about file name wacky"); 
		}
	}

}
