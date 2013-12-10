/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.actions;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
/*
 * SpeechRecognition is meant to be used for recognition of speech events, which 
 * are then broadcast to anyone that is interested in receiving such information.
 * <p>
 * When using SpeechRecognition you will need to register a localBroadcastManager 
 * receiver, e.g. LocalBroadcastManager.getInstance(mycontext).registerReceiver(myreceiver), 
 * to receive speech recognition actions. Note that the intent will contain the results and 
 * confidence arrays in the intent bundle. User should unregister receiver when finished using 
 * SpeechRecognition. 
 * <p>
 * Note that SpeechRecognition does not continuously listen for speech. It will listen after
 * startSpeechListener called, but will either get results or stop at some point. After results
 * it does not continue listening for speech, so you would need to call startSpeechListener 
 * again. 
 * <p>
 * This class's methods must be invoked only from the main application thread. 
 * Please note that the application must have RECORD_AUDIO permission set in the 
 * project AndroidManifest.xml to use this class.
 * <p> Example usage follows: 
 * <pre>
 * {@code
 *		// initializing and starting 
 * 		SpeechRecognition mSpeech = new SpeechRecognition(mCtx); 
 *		mSpeech.initSpeechRecognizer();
 *		mSpeech.startSpeechListener();
 *		//receive broadcast
 *		BroadcastReceiver mSpeechReceiver = new BroadcastReceiver(){
 *			@Override
 *			public void onReceive(Context context, Intent intent) {
 *				//do something 
 *			}
 *    	};
 *		LocalBroadcastManager.getInstance(mCtx).registerReceiver(mSpeechReceiver,
 *				new IntentFilter(mSpeech.getActionName()));
 *		//stopping and closing up shop
 *    	mSpeech.stopSpeechListener();
 *    	LocalBroadcastManager.getInstance(this.mCtx).unregisterReceiver(mSpeechReceiver);
 *    	mSpeech.destroySpeechListener();
 * }
 * </pre>
 */
public class SpeechRecognition {

	private Context mCtx; 
	private SpeechRecognizer mSpeechRecognizer = null;
	private Intent mRecognitionIntent;
	private Boolean mListening = false; 
	private Boolean mStarted = false; 
	private Boolean mSpeechRecInitialized = false; 
	private String mActionName = "fslt.lib.actions.speechrecognition";
	public static final String RESULTS = "fslt.lib.actions.speechrecognition.results";
	public static final String CONFIDENCE = "fslt.lib.actions.speechrecognition.confidence"; 
	
	/*
	 * Constructor, note that the default action is 
     * "fslt.lib.actions.speechrecognition" This is the name that you would 
     * have a broadcast receiver intent filter setup to filter. The default 
     * action name can be changed {@link setActionName}.
	 * 
	 * @param context
	 */
	public SpeechRecognition(Context context) {
		this.mCtx = context;
	}
	/*
	 * Initialize speech recognition and set SpeechRecognitionListner.  
	 */
	public void initSpeechRecognizer(){
		SpeechRecognitionListner listener = new SpeechRecognitionListner();
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.mCtx);
		mSpeechRecognizer.setRecognitionListener(listener);
		mRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Given an hint to the recognizer about what the user is going to say
		mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
		mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 4);
		mSpeechRecInitialized = true; 
	}
	/*
	 * Set action name, this is action name of the intent that is broadcast on a
	 * speech recognition action. Subsequently any broadcast receiver will want to 
	 * filter for this action name. 
	 * 
	 * @param actionName 
	 * 				A String value representing the name of the speech recognition
	 * 				action. 
	 */
	public void setActionName(String actionName){
		mActionName = actionName;
	}
	/*
	 * @return action name as string.
	 */
	public String getActionName(){
		return mActionName;
	}
	/*
	 * Start initialized speech listener 
	 */
	public void startSpeechListener(){
		if(!mSpeechRecInitialized){
			//I don't know, rise an exception? 
		}
		mSpeechRecognizer.startListening(mRecognitionIntent);
	}
	/*
	 * Stop speech listener 
	 */
	public void stopSpeechListener(){
		mSpeechRecognizer.stopListening(); 
		mListening = mStarted = false;
	}
	/*
	 * Destroy speech recognition object.
	 */
	public void destroySpeechListener(){
		if( mSpeechRecognizer != null )
			mSpeechRecognizer.destroy();
	}
	/*
	 * Internal class implementing RecognitionListener that is called by 
	 * SpeechRecognizer on speech recognition events. The method onResults
	 * handles results from the speech recognizer. 
	 */
	private class SpeechRecognitionListner implements RecognitionListener {

		@Override
		public void onBeginningOfSpeech() {
			Log.d("Speech", "onBeginningOfSpeech");
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			Log.e("Speech", "onBufferReceived");
		}

		@Override
		public void onEndOfSpeech() {
			Log.e("Speech", "onEndOfSpeech");
		}

		@Override
		public void onError(int error) {
			switch (error) {
			case SpeechRecognizer.ERROR_AUDIO:
				Log.e("Speech", "ERROR AUDIO");
				break;
			case SpeechRecognizer.ERROR_CLIENT:
				Log.e("Speech", "ERROR CLIENT");
				break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				Log.e("Speech", "ERROR INSUFFICIENT PERMISSIONS");
				break;
			case SpeechRecognizer.ERROR_NETWORK:
				Log.e("Speech", "ERROR NETWORK");
				break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				Log.e("Speech", "ERROR NETWORK TIMEOUT");
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				Log.e("Speech", "ERROR NO MATCH");
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				Log.e("Speech", "ERROR RECOGNIZER BUSY");
				break;
			case SpeechRecognizer.ERROR_SERVER:
				Log.e("Speech", "ERROR SERVER");
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				Log.e("Speech", "ERROR SPEECH TIMEOUT");
				break;
			}
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.e("Speech", "onEvent");
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			mListening = true;
			Log.e("Speech", "onReadyForSpeech");
		}

		@Override
		public void onResults(Bundle results) {

			Log.d("Speech", "onResults");
			ArrayList<String> strlist = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] confidence = results
					.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			Intent intent = new Intent();
			intent.putExtra(RESULTS, strlist);
			intent.putExtra(CONFIDENCE, confidence);
			intent.setAction(mActionName);
			LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
			/*
			for (int i = 0; i < strlist.size(); i++) {
				tmp = (String) strlist.get(i);
				if (tmp.toLowerCase().contains("door")) {
					intent.putExtra("SUCCESS", true);
					intent.setAction(mActionName);
					intent.putExtra("RESULTS", strlist);
					intent.putExtra("CONFIDENCE", confidance);
					// handler.sendEmptyMessage(ImageMedia.ACTION_ON_SPEECH);
					LocalBroadcastManager.getInstance(mCtx).sendBroadcast(
							intent);
					break;
				}
				Log.d("Speech", "result=" + strlist.get(i));
			}
			*/
			mSpeechRecognizer.stopListening();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			Log.d("Speech", "onRmsChanged");
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			// TODO Auto-generated method stub
			
		}
	} // end MyRecognitionListner
}