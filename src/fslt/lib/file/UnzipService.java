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
 * to make sure that android:process is not set. Setting 'process' gives the service
 * its own process to run in. Default android:process=":fslt.lib.file.utilites.UnzipService"
 * 
 * @author affect
 * @see http://developer.android.com/guide/components/processes-and-threads.html
 * @see http://developer.android.com/guide/topics/manifest/service-element.html
 */
public abstract  class UnzipService extends Service {

	private final static String TAG = UnzipService.class.getSimpleName();
	public final static String FILE_NAME = "FILE_NAME"; 
	public final static String UNZIP_FILE_TO_LOCATION = "UNZIP_LOCATION"; 
	public final static String ZIP_FILE_LOCATOIN = "ZIP_FILE_LOACTION";
	public final static String SHOW_TOAST = "SHOW_TOAST"; 
	public final static String DELETE_ZIP_FILE_AFTER_UNZIP = "DELETE_ZIP_FILE_AFTER_DOWNLOAD"; 
	private boolean mShowToast; 
	private boolean mDeleteZipFile; 
	private Context mContext; 
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
    public void onCreate() {
		mContext = this.getApplicationContext(); 
        Toast.makeText(mContext, "fslt: unziping story", Toast.LENGTH_SHORT).show();
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
    	mDeleteZipFile = intent.getBooleanExtra(DELETE_ZIP_FILE_AFTER_UNZIP, true);
    	
    	if(mShowToast){
    		Toast.makeText(mContext, fileName + " unzipping :fslt: ", Toast.LENGTH_SHORT).show();
    	}
    	//start thread to unzip
    	handleZipFile(zipFileLocation, unzipFileToLocation, fileName); 
    	return Service.START_NOT_STICKY;
    }
    
    /**
     * Implement your own method for handeling downloaded zip of use DefaultUnzipTask
     * to complete the unzip. NOTE that you will want to run a thread to complete your
     * action on.
     * 
     * @param zipFileLoaction
     * @param unzipFileToLocation
     * @param fileName
     */
    public abstract void handleZipFile(String zipFileLoaction, String unzipFileToLocation, String fileName); 
    
    private boolean deleteZipFile(String zipFileToDelete){
    	File file = new File(zipFileToDelete);
    	boolean deleted = file.delete();
    	if(!deleted){
    		if(mShowToast){
    			Toast.makeText(mContext, "Problem delete zip file:  " + zipFileToDelete, 
    					Toast.LENGTH_SHORT).show();
    		}
    	}
    	return deleted;
    }
    /**
     * 
     * @author affect
     *
     */
    public class DefaultUnzipFileTask extends AsyncTask<String, Integer, Boolean> {
        @Override
		protected Boolean doInBackground(String... locations)
        {
        	String fileToUnzip = locations[0]; 
        	String locationToUnzipFile = locations[1]; 
        	// Delete the entire old folder if it existed
        	FileOperations.deleteRecursive(new File(locationToUnzipFile)); 
            //Try to unzip the file
            try{
            	Log.d(TAG, "start to decompress " + fileToUnzip);
                Decompress fileToDecompress = new Decompress(fileToUnzip, locationToUnzipFile);
                fileToDecompress.unzip();
                if(mDeleteZipFile){
                	deleteZipFile(fileToUnzip); 
                }
            } catch (Exception e){
            	//Before we were looking for FileNotFoundExcption, but we where not catching or throwing that.
            	if(mShowToast){
            		Toast.makeText(mContext, fileToUnzip + ": error unziping ", Toast.LENGTH_SHORT).show();
            	}
            	Log.d(TAG, "error decompressing " + fileToUnzip);
                e.printStackTrace();
                return false;
            }
            //return file location so that we can delete the original zip file. 
            return true;
        }

        @Override
		protected void onPostExecute(Boolean result)
        {
        	
        }
    };

}
