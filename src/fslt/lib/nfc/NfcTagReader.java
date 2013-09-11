package fslt.lib.nfc;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.text.TextUtils;

public class NfcTagReader {

	/**
	 * 
	 * @param intent
	 * 				The intent received from Nfc event, this intent has the payload. 
	 * 				Note that it is expected to be a string. 
	 * @return
	 * 				return string from intent payload. Returns null if the intent was
	 * 				not a Nfc event or if empty payload or if other exception. 
	 */
	public String readTagMessage(Intent intent) {
		String rtnMsg = ""; 
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) return rtnMsg; 
		
		List<NdefMessage> intentMessages = NfcUtils.getMessagesFromIntent(intent);
		List<String> payloadStrings = new ArrayList<String>(intentMessages.size());

		for (NdefMessage message : intentMessages) {
			for (NdefRecord record : message.getRecords()) {
				byte[] payload = record.getPayload();
				String payloadString = new String(payload);

				if (!TextUtils.isEmpty(payloadString)){
					try{
						payloadStrings.add(payloadString);
					}catch( UnsupportedOperationException e){ return ""; }
					catch(ClassCastException e){ return "";} 
					catch(IllegalArgumentException e){ return "";} 
				}
			}
		}
		
		if (!payloadStrings.isEmpty()) {
			rtnMsg =  TextUtils.join(",", payloadStrings);
		}
		return rtnMsg;
	}
}
