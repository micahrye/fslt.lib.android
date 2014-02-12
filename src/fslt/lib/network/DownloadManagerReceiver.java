/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import fslt.lib.file.UnzipService;

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
 * 
 */
public class DownloadManagerReceiver extends BroadcastReceiver {

	/**
	 * Listener class that handles what to do on key events.
	 * 
	 */
	public abstract static class DownloadManagerReceiverListener {

		public abstract void onRepeatDownloadRequest();

		public abstract void onSuccessfulDownload(Context context, String downloadedFilename);
	}

	private final static String TAG = DownloadManagerReceiver.class.getSimpleName();
	private static final long ALREADY_DOWNLOADING = -1l;

	private DownloadManager downloadManager;
	private final DownloadManagerReceiverListener listener;

	private final Collection<Long> downloadIDs = new ArrayList<Long>();

	public DownloadManagerReceiver(DownloadManagerReceiverListener listener) {
		this.listener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

			if (!downloadIDs.contains(downloadId)) {
				return;
			}

			Query query = new Query();
			query.setFilterById(downloadId);
			DownloadManager mDownloadMngr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

			Cursor cursor = mDownloadMngr.query(query);
			if (cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

				if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
					String downloadedFilename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
							.substring(7);

					listener.onSuccessfulDownload(context, downloadedFilename);

				} else {
					Log.d(TAG, "not success");
				}
			} else {
				Log.d(TAG, "not complete");
			}
		}
	}

	public long requestDownloadFile(Context context, String url, String fileName) {

		context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		Request request = new Request(Uri.parse(url));
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

		// Check if file is currently being downloaded, if so do not download it. 

		DownloadManager.Query q = new DownloadManager.Query();
		q.setFilterById(206);
		Cursor c = downloadManager.query(q);
		c.moveToFirst();

		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED);

		Cursor cursor = downloadManager.query(query);

		if (cursor.moveToFirst()) {

			int columnTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
			String downloadFileTitle = cursor.getString(columnTitle);

			if (downloadFileTitle.equalsIgnoreCase(fileName)) {

				listener.onRepeatDownloadRequest();
				return ALREADY_DOWNLOADING;

			} else {
				return requestDownload(context, request, fileName);
			}
		} else {
			return requestDownload(context, request, fileName);
		}

	}

	private long requestDownload(Context context, Request request, String filename) {
		request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
		request.setDescription("STORYSCAPE_STORY_DOWNLOAD");
		request.setTitle(filename);
		long requestID = downloadManager.enqueue(request);
		downloadIDs.add(requestID);
		return requestID;
	}

	/**
	 * 
	 * @param context
	 * @param downloadedFileUri
	 * @param unzipServiceIntent
	 */
	public void defaultUnzipFile(Context context, String downloadedFileUri, Intent unzipServiceIntent) {
		String fileName = "";
		String storyURI = downloadedFileUri.replaceAll("-\\d\\.zip\\z", ".zip");
		Pattern pattern = Pattern.compile(".*\\.zip\\z");
		Matcher matcher = pattern.matcher(storyURI);
		if (matcher.find()) {
			try {
				int start = matcher.group().lastIndexOf("/") + 1;
				int end = matcher.group().length() - 4;
				fileName = matcher.group().substring(start, end);
				/* Now, if it's a zip file,  we want to unzip it...
				 * It is possible to be download a story more than once, which results in 
				 * the story name being appended with '-2', i.e. dash number. We want to remove 
				 * that so that our unpacked folder name is only the story name. Otherwise, we 
				 * have the same story in our device library with the appended dash number, even
				 * though they are all the same story. 
				 */
			} catch (IllegalStateException e) {
				//Should raise an error 
				e.printStackTrace();
			}
			// zip is crated 'zip -rj' and opened 'unzip fileName.zip -d fileName/
			StringBuilder unzipLocation = new StringBuilder();
			unzipLocation.append(Environment.getExternalStorageDirectory());
			// unzipLocation.append(File.separator).append(mRootAppStorageDir);
			unzipLocation.append(File.separator).append(fileName).append(File.separator);

			unzipServiceIntent.putExtra(UnzipService.FILE_NAME, fileName);
			unzipServiceIntent.putExtra(UnzipService.ZIP_FILE_LOCATOIN, downloadedFileUri);
			unzipServiceIntent.putExtra(UnzipService.UNZIP_FILE_TO_LOCATION, unzipLocation.toString());
			unzipServiceIntent.putExtra(UnzipService.SHOW_TOAST, true);
			unzipServiceIntent.putExtra(UnzipService.DELETE_ZIP_FILE_AFTER_UNZIP, true);
			context.startService(unzipServiceIntent);
		} else {
			Log.d(TAG, "something about file name wacky");
		}
	}

}
