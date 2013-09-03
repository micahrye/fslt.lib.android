package fslt.lib.file.utilites;

import java.io.File;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class UnzipService extends Service {

	private final static String TAG = UnzipService.class.getSimpleName();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.e(TAG, "UNZIP!!!");
		return null;
	}
	
	@Override
    public void onCreate() {
		Log.e(TAG, "UNZIP!!!");
        Toast.makeText(this, "fslt: unziping story", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	/*
    	 * If startService(intent) is called while the service is running, its 
    	 * onStartCommand() is also called. Therefore your service needs to be 
    	 * prepared that onStartCommand() can be called several times.
    	 */
    	Log.e(TAG, "UNZIP!!!");
    	String storyName = intent.getStringExtra("STORY_NAME");
    	String unzipLocation = intent.getStringExtra("UNZIP_LOCATION");
    	String zipLocation = intent.getStringExtra("ZIP_FILE_LOCATION");
    	
    	Toast.makeText(this, storyName + " unzipping :fslt: ", Toast.LENGTH_SHORT).show();
    	new UnzipAStory().execute(zipLocation, unzipLocation.toString(), storyName);
    	
    	return Service.START_NOT_STICKY;
    }
    
    private class UnzipAStory extends AsyncTask<String, Integer, String[]> {
        @Override
		protected String[] doInBackground(String... locations)
        {
        	Log.e(TAG, "UNZIP!!!");
        	// Delete the entire old folder if it existed
        	FileOperations.deleteRecursive(new File(locations[1])); 
        	
            //Try to unzip the file
            try{
            	Log.d(TAG, "start to decompress " + locations[0]);
                Decompress d = new Decompress(locations[0], locations[1]);
                d.unzip();
                Log.d(TAG, locations[0] + " now unziped");
            } catch (Exception e){
            	//Before we were looking for FileNotFoundExcption, but we where not catching or throwing that.
            	// changed to Exception and this is now thrown from unzip
            	Log.d(TAG, "error decompressing " + locations[0]);
                e.printStackTrace();
            }
            //return file location so that we can delete the original zip file. 
            return locations;
        }

        @Override
		protected void onPostExecute(String[] result)
        {
        	// IMPORTANT
        	// Appears that you can get old zips that don't get deleted in 
        	Log.e(TAG, "delete " + result[0]);
        	File file = new File(result[0]);
        	boolean deleted = file.delete();
        	if( !deleted )
        		Log.e(TAG, result[0] + " not deleted");
            return;
        }
    }; // End UnzipAStory

    
}
