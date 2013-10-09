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
	private final float mThreshold;

	private static final String SHAKEN = "SHAKEN";
	private static final String SHAKE_STRENGTH = "SHAKE_STRENGTH";
	private static final float SHAKE_THRESHOLD_DEFAULT = 7.0f;

	private final Context mCtx;

	public ShakeDetection(Context c) {
		this(c, SHAKE_THRESHOLD_DEFAULT);
	}

	public ShakeDetection(Context c, float threshold) {
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
		mCtx = c;
		mThreshold = threshold;
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		float x = se.values[0];
		float y = se.values[1];
		float z = se.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter

		if (mAccel > mThreshold) {
			Log.d(TAG, "Shaken Strength: " + mAccel);
			Intent intent = new Intent();
			intent.putExtra(SHAKEN, true);
			intent.setAction(TAG);
			intent.putExtra(SHAKE_STRENGTH, mAccel);
			LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public static String getActionName() {
		return TAG;
	}
}
