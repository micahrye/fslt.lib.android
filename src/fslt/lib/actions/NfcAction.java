package fslt.lib.actions;

public class NfcAction {
	/**
	 * 
	 * This is an empty class used just to get your attention. 
	 * NOTE that for an NFC action you must have an intent-filter
	 * in your activity for the desired NFC tag. The 
	 * following describes how to set up your manifest and activity to 
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
	 * 
	 * 
	 */

}
