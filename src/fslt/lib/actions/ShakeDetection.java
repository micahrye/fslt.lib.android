package fslt.lib.actions;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ShakeDetection implements SensorEventListener {
	private static final String TAG = "fslt.lib.actions.shakedetection";

	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private static final String SHAKEN = "SHAKEN";
	private static final String SHAKE_STRENGTH = "SHAKE_STRENGTH";
	private static final float SHAKE_THRESHOLD = 7.0f;

	private final Context mCtx;
	
	public ShakeDetection(Context c){
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
		mCtx = c;
	}

	public void onSensorChanged(SensorEvent se) {
		float x = se.values[0];
		float y = se.values[1];
		float z = se.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter

		if (mAccel > SHAKE_THRESHOLD) {
			Log.d(TAG, "Shaken Strength: " + mAccel);
			Intent intent = new Intent();
			intent.putExtra(SHAKEN, true);
			intent.setAction(TAG);
			intent.putExtra(SHAKE_STRENGTH, mAccel);
			LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	public static String getActionName(){
		return TAG;
	}
}
