package fslt.lib.actions;

public class NfcAction {

	/**
	 * 
	 * This is an empty class used just to get your attention. 
	 * NOTE that for an NFC action you must have an intent-filter
	 * in your activity for the desired NFC tag. There are two approaches 
	 * to using NFC in your applicaiton/activity. First approach is completly 
	 * programatic, the second uses the Manifest and is programatic. See
	 * NFCee for example.
	 * The following descripes the enable foreground dispatch approach: 
	 * <p>
	 * <pre>
	 * @code{
	 * 
	 * 	private Context mCtx; 
		private NfcAdapter mNfcAdapter;
		private PendingIntent mPendingIntent;
		private IntentFilter[] mFilters;
		private String[][] mTechLists;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			...
			mCtx = this.getApplicationContext();

			mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter == null) {
				Toast.makeText(this, "Sorry, NFC is not available on this device", Toast.LENGTH_SHORT).show();
			}
			String NfcMimeTypeToFilter = "application/com.fslt.action.nfc.nfcee.tag"; 
			mPendingIntent = PendingIntent.getActivity(this, 0,
	                new Intent(mCtx, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			try {
				ndef.addDataType(NfcMimeTypeToFilter);
			} catch (MalformedMimeTypeException e) {
				throw new RuntimeException("fail", e);
			}
			...
			
			@Override
			public void onPause(){
				if (mNfcAdapter != null ){
					mNfcAdapter.disableForegroundDispatch(this);
				}
				super.onPause(); 
			}
			
			@Override
			public void onResume() {
				super.onResume();
				if (mNfcAdapter != null ){ 
					mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
		                mTechLists);
				}
			}
		}
		
	 * }
	 * </pre>
	 * 
	 * 
	 * 
	 * The following describes how to set up your manifest and activity to 
	 * respond to a NFC tag event and how to use the the fslt.lib.nfc
	 * classes. 
	 * <pre>
	 * Example of manifest file
	 * <activity
            android:name="com.fslt.android.app.nfcee.NFCeeActivity"
            android:label="@string/app_name" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity-alias
            android:name=".NFCeeActivityAlias"
            android:enabled="false"
            android:targetActivity=".NFCeeActivity" >
			<intent-filter>
                <action android:name="android.nfc.action.NDEF_DISCOVERED" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/com.fslt.action.nfc.nfcee.tag" />
            </intent-filter>
        </activity-alias>
	 * </pre>
	 * 
	 * We use an activity-alias as a convenient way to reference the activity we are 
	 * interested in while having a seperate set of intent filters, in this case NFC. 
	 * 
	 * Notice in the activity-alias that "android-enabled" is set to false. These means
	 * that NFC broadcast will not be recognized unless during exucution of the applicaiton
	 * you specify to activate the intent-fileter via the PackageManager, eg.
	 * 
	 * getPackageManager().setComponentEnabledSetting(
				new ComponentName("com.fslt.android.app.nfcee", 
						"com.fslt.android.app.nfcee.NFCeeActivityAlias"),
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED , 
						PackageManager.DONT_KILL_APP);
	 * 
	 * No the that activity will respond to NFC events. Likewise you can turn that off 
	 * via COMPONENT_ENABLED_STATE_DISABLED, you should probably do that in onPause or
	 * onDestroy depending on what you want. Setting "android:enabled" to true means that
	 * even if you application is not being used it will respond (open) to a NFC event 
	 * given the correct action (mimeType). 
	 * 
	 * In either case you will want 
	 */

}
