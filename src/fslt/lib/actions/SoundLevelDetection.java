/*
 * Copyright 2013 StoryScape Project. All rights reserved.
 *  
 * 
 */
package fslt.lib.actions;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SoundLevelDetection is meant to be used for detection of sound events, which
 * are then broadcast to anyone that is interested in receiving such
 * information. If ambient noise is too great sound events cannot be detected.
 * Ambient noise threshold and sound event threshold can be set by users
 * (threshold in decibles). Note that SoundLevelDetection uses a
 * LocalBroadcastManager for sending broadcast.
 * <p>
 * When using SoundLevelDetection you will need to register a
 * localBroadcastManager receiver, e.g.
 * LocalBroadcastManager.getInstance(mycontext).registerReceiver(myreceiver), to
 * receive sound actions. Note that broadcast intent contains the decible value.
 * User should unregister receiver when finished using SoundLevelDetection.
 * <p>
 * This class's methods must be invoked only from the main application thread.
 * Please note that the application must have RECORD_AUDIO permission set in the
 * project AndroidManifest.xml to use this class. In your manifest add:
 * <uses-permission android:name="android.permission.RECORD_AUDIO"/>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	// initializing and starting 
 * 	SoundLevelDetection mSoundLevelDetection = new SoundLevelDetection(mCtx);
 * 	mSoundLevelDetection.startSoundLevelDetection();
 * 	//receive broadcast
 * 	BroadcastReceiver mSoundLevelReceiver = new BroadcastReceiver() {
 * 		&#064;Override
 * 		public void onReceive(Context context, Intent intent) {
 * 			//do something 
 * 		}
 * 	};
 * 	LocalBroadcastManager.getInstance(mCtx).registerReceiver(mSoundLevelReceiver,
 * 			new IntentFilter(mSoundLevelDetection.getActionName()));
 * 	// Now you can let it run in the background and it will broadcast messages when
 * 	// louder sounds are detected. 
 * 
 * 	//when you want to stop and closing up shop
 * 	mSoundLevelDetection.stopSoundLevelDetection();
 * 	// before you leave the activity/application 
 * 	LocalBroadcastManager.getInstance(this.mCtx).unregisterReceiver(mSoundLevelReceiver);
 * 
 * }
 * </pre>
 */
public class SoundLevelDetection {
	private static final String TAG = SoundLevelDetection.class.getSimpleName();
	private final Context mCtx;
	private MediaRecorder mRecorder = null;
	private boolean mRecorderStarted = false;
	private CheckAmbientNoiseTask mAmbientNoiseTask;
	private SoundLevelTask mSoundLevelTask;

	//string that is used for local broadcasting, set IntentFilter to this filter for this name. 
	private final String mActionName;

	private final int mCheckForAmbientNoiseDuration;
	private final int mPollForSound;
	private final Double mSoundThreshold;
	private final Double mAmbientNoiseThreshold;

	private static String DEFAULT_ACTION_NAME = "fslt.lib.action.soundleveldetection";
	private static int DEFAULT_AMBIENT_DUR = 500;
	private static int DEFAULT_POLL_INT = 100;
	private static Double DEFAULT_TRIGGER_THRESHOLD = (double) 87;
	private static Double DEFAULT_AMBIENT_NOISE = (double) 65;

	public static boolean NOISY_ENVIRONMENT = true;

	/**
	 * Constructor, note that the default action is
	 * "fslt.lib.action.soundleveldetection" This is the name that you would
	 * have a broadcast receiver intent filter setup to filter. Use the other
	 * constructor to set parameter values.
	 */
	public SoundLevelDetection(Context context) {
		this(context, DEFAULT_ACTION_NAME, DEFAULT_AMBIENT_DUR, DEFAULT_POLL_INT, DEFAULT_AMBIENT_NOISE,
				DEFAULT_TRIGGER_THRESHOLD);
	}

	/**
	 * 
	 * @param context
	 * @param actionName
	 *            the intent filter string to broadcast with
	 * @param duration
	 *            in milliseconds to check ambient noise level. Values should be
	 *            between 250 and 1500 milliseconds. Invalid values will result
	 *            in default value of 500 ms being used.
	 * @param pollInterval
	 *            How often this listener checks for loud sounds
	 * @param noiseThreshold
	 *            Double value representing decibel value above which it is a
	 *            noisy environment
	 * @param soundLevelThreshold
	 *            Double value representing decibel value above which we
	 *            interpret something as a trigger
	 */
	public SoundLevelDetection(Context context, String actionName, int ambientDuration, int pollInterval, Double noiseThreshold,
			Double soundLevelThreshold) {
		mCtx = context;
		mAmbientNoiseTask = new CheckAmbientNoiseTask();
		mSoundLevelTask = new SoundLevelTask();

		mActionName = actionName;
		if (ambientDuration < 250 || ambientDuration > 1500) {
			ambientDuration = 500;
		}
		mCheckForAmbientNoiseDuration = ambientDuration;

		mPollForSound = pollInterval;
		mAmbientNoiseThreshold = noiseThreshold;
		mSoundThreshold = soundLevelThreshold;
	}

	/**
	 * @return action name
	 */
	public String getActionName() {
		return mActionName;
	}

	/**
	 * Open microphone from a MediaRecorder object. Method initializes and
	 * starts a MediaRecorder object with the audio source set to the devices
	 * microphone.
	 * 
	 * @returns boolean value, true if microphone set up, false otherwise.
	 */

	private boolean openMicrophone() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			try {
				// /dev/null is a special file that discards all data written to it
				// but reports that the write operation succeeded
				mRecorder.setOutputFile("/dev/null");
				mRecorder.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				mRecorder.start();
				mRecorderStarted = true;
			} catch (IllegalStateException e) {
				e.printStackTrace();
				mRecorderStarted = false;
				mRecorder = null;
			}
		}
		return mRecorderStarted;
	}

	/**
	 * Close microphone opened by {@openMicrophone}.
	 */
	private void closeMicrophone() {
		try {
			if (mRecorder != null) {
				mRecorder.reset();
				mRecorder.release();
				mRecorder = null;
				mRecorderStarted = false;
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "SOUNDLEVEL STOP threw an IllegalStateException, but we caught it :P ");
			e.printStackTrace();
		}
	}

	/**
	 * Starts the threads used for detecting if a sound event has happened and
	 * if a sound action should be broadcast. Note that this threads run
	 * continuously until {@link stopSoundLevelDetection} or
	 * {@link destroySoundLevelDetection} called. Each thread sleeps for {#link
	 * mPollInterval} time.
	 * 
	 * @see openMicrophone
	 * @see CheckAmbientNoiseTask
	 * @see SoundLevelTask
	 */
	public void startSoundLevelDetection() {
		if (openMicrophone()) {
			//Allow for concurrent running of tasks
			mAmbientNoiseTask = new CheckAmbientNoiseTask();
			mSoundLevelTask = new SoundLevelTask();
			mAmbientNoiseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			mSoundLevelTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	/**
	 * Stops threads associated with detecting and broadcasting sound events.
	 * Also, closes the microphone. After calling this method you would have to
	 * call {@link openMicrophone} and {@link startSoundLevelDetection} to
	 * resume.
	 */
	public void stopSoundLevelDetection() {
		mAmbientNoiseTask.cancel(false);
		mSoundLevelTask.cancel(false);
		NOISY_ENVIRONMENT = true;
		closeMicrophone();
	}

	/**
	 * Returns the maximum amplitude detected since last call to this method
	 * 
	 * @return double value representing amplitude of last sound detected
	 */
	public double getAmplitude() {
		int amplitude = 0;
		if (mRecorder != null && mRecorderStarted) {
			try {
				amplitude = mRecorder.getMaxAmplitude();
			} catch (RuntimeException e) {
				Log.d(TAG, e.getMessage());
			}
			return amplitude;
		} else {
			return 0;
		}
	}

	/**
	 * Converts amplitude to decibels
	 * 
	 * @param amp
	 *            the amplitude of the last sound detected
	 * @return double value representing decibels of last sound detected
	 */
	public static double amplitudeToDecibels(double amp) {
		double dec = 20 * Math.log10(amp);
		if (dec < 0)
			dec = 0;
		return dec;
	}

	/**
	 * CheckAmbientNoiseTask runs continuously after being started in
	 * {@link startSoundLevelDetection}. samples sound over time (t = sampleRate
	 * * mPollInterval) and takes the average decibel of sample to compare to
	 * mAmbientNoiseThreshold to determine if device in noisy environment. If
	 * noisy it will ignore sound events, since device threshold likely at
	 * ceiling.
	 * <p>
	 * Important that {@link stopSoundLevelDetection} or
	 * {@link destroySoundLevelDetection} called so that this thread is stopped.
	 */
	private class CheckAmbientNoiseTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			int sampleRate = 10;
			int threadSleepTime = mCheckForAmbientNoiseDuration / 10;
			Double[] samples = new Double[sampleRate];
			Double sum = 0.0;
			Double dec = 0.0;
			//NOTE: if we are polling for sound level ever N ms then 
			//ambient is updating environment noise var every sampleRate*N ms
			while (true) {
				sum = 0.0;
				dec = 0.0;
				for (int i = 0; i < sampleRate; i++) {
					if (this.isCancelled()) {
						return NOISY_ENVIRONMENT = true;
					}
					dec = amplitudeToDecibels(getAmplitude());
					samples[i] = dec;
					sum += dec;
					try {
						Thread.sleep(threadSleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (this.isCancelled()) {
					return NOISY_ENVIRONMENT = true;
				}

				Double avg_dec = sum / sampleRate;
				if (avg_dec > mAmbientNoiseThreshold) {
					Log.d(TAG, "It's too loud here! Noise = " + avg_dec.toString());
					NOISY_ENVIRONMENT = true;
				} else {
					Log.d(TAG, "Noise level is fine. Noise = " + avg_dec.toString());
					NOISY_ENVIRONMENT = false;
					//return true;
				}
			}
		}
	}

	/**
	 * SoundLevelTask runs continuously after being started in
	 * {@link startSoundLevelDetection}. samples sound every mPollInterval to
	 * determine if a sound action should take place. Sound actions only happen
	 * if not a noisy environment.
	 * <p>
	 * If sound detected over threshold a sound action is broadcast using a
	 * LocalBroadcastManager.
	 * <p>
	 * Important that {@link stopSoundLevelDetection} or
	 * {@link destroySoundLevelDetection} called so that this thread is stopped.
	 * 
	 * @see CheckAmbientNoiseTask
	 */
	private class SoundLevelTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			while (true) {
				if (this.isCancelled()) {
					return false;
				}
				if (!NOISY_ENVIRONMENT) {
					Double dec = amplitudeToDecibels(getAmplitude());
					if (dec > mSoundThreshold) {
						Log.d(TAG, "I hear a scream! Dec = " + dec.toString());
						Intent intent = new Intent();
						intent.putExtra("SOUND_DETECTED", true);
						intent.setAction(mActionName);
						intent.putExtra("DECIBLES", dec);
						// istantiator must setup a BroadcastReceiver to list for 
						// message (mActionName)
						LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
					}
					try {
						Thread.sleep(mPollForSound);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
