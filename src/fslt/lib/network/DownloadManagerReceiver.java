/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fslt.lib.file.utilites.UnzipService;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

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
 */
public abstract  class DownloadManagerReceiver extends BroadcastReceiver {
	//TODO: probably want a way to check if download manager is downloading a file and if so 
	//don't download it again. check DownloadManager.STATUS_RUNNING | STATUS_PENDING | STATUS_PAUSED
	private final static String TAG = DownloadManagerReceiver.class.getSimpleName(); 
	public String mPathInfo = null; 
	//TODO: since this is a broadcast receiver that  
	protected String mRootAppStorageDir = "StoryScape"; 
	
	protected abstract void setRootApplicationStorageDirectory();	
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		setRootApplicationStorageDirectory(); 
		
		String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            Query query = new Query();
            query.setFilterById(downloadId);
            DownloadManager mDownloadMngr = 
            		(DownloadManager)ctx.getSystemService(Context.DOWNLOAD_SERVICE);
             
            Cursor c = mDownloadMngr.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c
                        .getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex) ) {
                    String zipUri = c
                            .getString(c
                                    .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            .substring(7);
                    //String download_uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                    //String media_uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI));
                    
                    //String lf_uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    // Now, if it's a zip file,  we want to unzip it...
                    Log.e(TAG, "DID" + downloadId + " downloaded " + zipUri);
                    String storyURI = zipUri.replaceAll("-\\d\\.zip\\z", ".zip"); 
                    //String storyName = "";
                    Pattern pattern = Pattern.compile(".*\\.zip\\z");
                    Matcher matcher = pattern.matcher(storyURI);
                    //IF we have a zip file contineu 
                    if(matcher.find()){
                    	unpackageZip(ctx, matcher, zipUri); 
                    }
                }else{
                	Log.d(TAG, "not success"); 
                }
            }else{
            	Log.d(TAG, "not complete"); 
            }
        }
    }
	private void unpackageZip(Context ctx, Matcher matcher, String zipUri){
		String storyName = "";
		try{
    		int start = matcher.group().lastIndexOf("/") + 1;
    		int end = matcher.group().length()-4; 	
    		storyName = matcher.group().substring(start, end);
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
        StringBuilder unzipLocation = new StringBuilder();
        /* IMPORTANT: MRE
         * need to add story directory name and unzip into that and then remove zip file. 
         * zip is crated 'zip -rj' and opened 'unzip storyname.zip -d storyname/
         */
        String pathInfo = "/" + mRootAppStorageDir  + "/" + storyName + "/"; 
        //Currently unziping to SDCARD
        //TODO: give ability to change location, may want internal storage location or other. 
        unzipLocation.append(Environment.getExternalStorageDirectory() + pathInfo);  
        //String loc = unzipLocation.toString(); 
        Log.e(TAG, "call unzip for " + pathInfo);
        
        //new UnzipAStory().execute(zipUri, unzipLocation.toString(), storyName);
        
        Intent unzipIntent = new Intent(ctx, UnzipService.class);
        unzipIntent.putExtra("STORY_NAME", storyName);
        unzipIntent.putExtra("UNZIP_LOCATION", unzipLocation.toString() );
        unzipIntent.putExtra("ZIP_FILE_LOCATION", zipUri);
        ctx.startService(unzipIntent);
	}
	
}
