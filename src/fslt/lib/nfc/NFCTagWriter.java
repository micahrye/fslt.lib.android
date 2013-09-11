package fslt.lib.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;


public class NFCTagWriter {

	//NOTE that this should match your intent filter in your manifest file!!! 
	private static String MIME_TYPE = "application/com.fslt.lib.nfc.tag";
	/**
	 * 
	 * @param intent
	 * @param msg
	 * @return
	 */
	public boolean writeStringToNfcTag(Intent intent, String msg) {
		//make sure we are dealing with an Nfc intent
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			byte[] payload = new String(msg).getBytes();

			if (detectedTag != null && NfcUtils.writeTag(
					NfcUtils.createMessage(MIME_TYPE, payload), detectedTag)) {
				
				return true; 
			} else {
				return false; 
			}
		}
		return false;
	}
	/**
	 * 
	 * @param type
	 */
	public void setMimeType(String type){
		MIME_TYPE = type; 
	}
	/**
	 * 
	 * @return
	 * 			returns string mime type
	 */
	public String getMimeType(){
		return MIME_TYPE; 
	}
	
}
