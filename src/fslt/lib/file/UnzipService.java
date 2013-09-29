package fslt.lib.file;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * NOTE: when extending if you want to debug your extended service you will need
 * to make sure that android:process is not set. Setting 'process' gives the
 * service its own process to run in. Default
 * android:process=":fslt.lib.file.utilites.UnzipService"
 * 
 * @author affect
 * @see http://developer.android.com/guide/components/processes-and-threads.html
 * @see http://developer.android.com/guide/topics/manifest/service-element.html
 */
public abstract class UnzipService extends Service {

	private final static String TAG = UnzipService.class.getSimpleName();
	public final static String FILE_NAME = "FILE_NAME";
	public final static String UNZIP_FILE_TO_LOCATION = "UNZIP_LOCATION";
	public final static String ZIP_FILE_LOCATOIN = "ZIP_FILE_LOACTION";
	public final static String SHOW_TOAST = "SHOW_TOAST";
	public final static String DELETE_ZIP_FILE_AFTER_UNZIP = "DELETE_ZIP_FILE_AFTER_DOWNLOAD";
	private boolean mShowToast;
	private Context mContext;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mContext = this.getApplicationContext();
		// Toast.makeText(mContext, "fslt: unziping story", Toast.LENGTH_SHORT).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * If startService(intent) is called while the service is running, its 
		 * onStartCommand() is also called. Therefore your service needs to be 
		 * prepared that onStartCommand() can be called several times.
		 */
		String fileName = intent.getStringExtra(FILE_NAME);
		String zipFileLocation = intent.getStringExtra(ZIP_FILE_LOCATOIN);
		String unzipFileToLocation = intent.getStringExtra(UNZIP_FILE_TO_LOCATION);
		mShowToast = intent.getBooleanExtra(SHOW_TOAST, false);

		if (mShowToast) {
			Toast.makeText(mContext, fileName + " unzipping :fslt: ", Toast.LENGTH_SHORT).show();
		}
		//start thread to unzip
		handleZipFile(zipFileLocation, unzipFileToLocation, fileName);
		return Service.START_NOT_STICKY;
	}

	/**
	 * Implement your own method for handeling downloaded zip of use
	 * DefaultUnzipTask to complete the unzip. NOTE that you will want to run a
	 * thread to complete your action on.
	 * 
	 * @param zipFileLoaction
	 * @param unzipFileToLocation
	 * @param fileName
	 */
	public abstract void handleZipFile(String zipFileLoaction, String unzipFileToLocation, String fileName);

	public static abstract class UnzipFinishedListener {
		public abstract void onUnzipFinished();

		public abstract void onUnzipFailure(Exception e);

		public void onZipDeleted() {

		}
	}

	/**
	 * 
	 * @author affect
	 * 
	 */
	public static class DefaultUnzipFileTask extends AsyncTask<String, Integer, Boolean> {

		private final UnzipFinishedListener listener;
		private boolean deleteZipFile = false;

		public DefaultUnzipFileTask(UnzipFinishedListener listener, boolean deleteZipFile) {
			this.listener = listener;
			this.deleteZipFile = deleteZipFile;
		}

		public DefaultUnzipFileTask() {
			this.listener = null;
		}

		@Override
		protected Boolean doInBackground(String... locations) {
			// Delete the entire old folder if it existed
			FileOperations.deleteRecursive(new File(locations[1]));
			String fileToUnzip = locations[0];
			String locationToUnzipFile = locations[1];
			//Try to unzip the file
			try {
				Log.d(TAG, "start to decompress " + fileToUnzip);
				Decompress fileToDecompress = new Decompress(fileToUnzip, locationToUnzipFile);
				fileToDecompress.unzip();
				if (deleteZipFile) {
					deleteZipFile(fileToUnzip);
				}
			} catch (Exception e) {

				//Before we were looking for FileNotFoundExcption, but we where not catching or throwing that.

				Log.d(TAG, "error decompressing " + fileToUnzip);
				e.printStackTrace();
				if (listener != null) {
					listener.onUnzipFailure(e);
				}
				return false;
			}
			if (listener != null) {
				listener.onUnzipFinished();
			}

			return true;
		}

		private boolean deleteZipFile(String zipFileToDelete) {
			File file = new File(zipFileToDelete);
			boolean deleted = file.delete();
			if (listener != null) {
				listener.onZipDeleted();
			}
			return deleted;
		}

		@Override
		protected void onPostExecute(Boolean result) {

		}
	};

	public static void unzipFile(String zipFileLocation, String unzipFileToLocation, UnzipFinishedListener listener) {

		new DefaultUnzipFileTask(listener, true).execute(zipFileLocation, unzipFileToLocation);
	}

}
