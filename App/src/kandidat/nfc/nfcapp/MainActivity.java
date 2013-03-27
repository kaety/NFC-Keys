package kandidat.nfc.nfcapp;

import java.nio.charset.Charset;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CreateNdefMessageCallback {

	private String latestRecievedMsg;
	private NFCPMessage nfcpMessage;
	private final long TIMEOUT = 60 *1000;//GONE IN 60seconds
	private Long loginTime;
	// Objekt som representerar NFC adaptern
	private NfcAdapter nfcAdapter;
	private DAO dao = new DAO(this);

	@SuppressLint("NewApi")
	@Override
	/**
	 * Force user to start NFC and Android Beam. Enables callback.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
		
		
		// F�r tag i ett objekt som representerar NFC-adaptern
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// Kolla om anv�ndarens NFC �r p�slagen
		if (!nfcAdapter.isEnabled()) {
			// �ppnar menyn s� att anv�ndaren kan aktivera NFC
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		}
		// F�ljande eftersom metoden endast finns f�r API 16 och h�gre medan
		// t.ex. LG L5 har version 15
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			if (!nfcAdapter.isNdefPushEnabled()) {
				startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
			}
		}

		nfcAdapter.setNdefPushMessageCallback(this, this);
		dao.open();
	}

	/**
	 *  Called when the user clicks the Settings button 
	 *  
	 */
	public void settingsAct(View view) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	/**
	 * Because we don't want anything to happen when we click optionsbutton.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		
		////////////////////////////////DEBUG//////////////////////////////
		//Toast.makeText(this, "onPause", 1).show();
		///////////////////////////////////////////////////////////////////
		
		dao.close();
	}
	/**
	 * Get LoginTime. Check if you have to login again.
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		////////////////////////////DEBUG////////////////////////////////////////////
		//Toast.makeText(this, "onResume", 1).show();
		/////////////////////////////////////////////////////////////////////////////

		// Get the between instance stored values
	//	SharedPreferences pre = getSharedPreferences("login", 1);
		//logintime blir login eller 0 om inget v�rde finns!
	//	loginTime = pre.getLong("login", 0L);
		
	//	if((System.currentTimeMillis()-loginTime) > TIMEOUT){
	//		startActivity(new Intent(this,LoginActivity.class));
	//		Toast.makeText(this, "TIMEOUT: PLEASE LOG IN AGAIN", 1).show();
	//		finish();
	//	}else
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
				processIntent(getIntent());
		}
	}

	/**
	 * Receives NFC-intents and displays them in view. Called from above.
	 * 
	 * @param intent
	 */
	void processIntent(Intent intent) {
			setMessage(getLastestNFCMessage(intent));
			nfcpMessage = new NFCPMessage(latestRecievedMsg);

			if (nfcpMessage.getStatus().equals(NFCPMessage.STATUS_OK) && nfcpMessage.getType().equals(NFCPMessage.MESSAGE_TYPE_RESULT)) {
				
				startActivity(new Intent(this,AccessActivity.class));
				
			} else if (nfcpMessage.getStatus().equals(NFCPMessage.STATUS_ERROR) && nfcpMessage.getType().equals(NFCPMessage.MESSAGE_TYPE_RESULT)) {
				// NOT NFC ACCESS
				Intent deniedIntent = new Intent(this,
						DeniedActivity.class);
				deniedIntent.putExtra("ErrorCode", nfcpMessage.getErrorCode());
				startActivity(deniedIntent);
				nfcpMessage.clear();
			}
			getIntent().setAction("");
	}

	@Override
	/**
	 * NDEF message handler
	 * much debug here. In futher add cases and delete cases.
	 */
	public NdefMessage createNdefMessage(NfcEvent event) {
		
		NFCPMessage sendMsg = null;

		if (nfcpMessage == null) { //If no message has been received. This should be done by controller but is here for debugging purpose.
			
			sendMsg = new NFCPMessage("TE","01",NFCPMessage.STATUS_OK, NFCPMessage.MESSAGE_TYPE_BEACON,NFCPMessage.ERROR_NONE, "Anna");
			
		} else if (nfcpMessage.getType().equals(NFCPMessage.MESSAGE_TYPE_BEACON)) {
			
			//Check if key is in database
			String unlockId = dao.get(nfcpMessage.getUniqueId());
			
			if(unlockId != null){ //If a key exists in the Database send unlock message(type 2)
				
				sendMsg = new NFCPMessage(nfcpMessage.getName(), nfcpMessage.getId(),
						NFCPMessage.STATUS_OK,NFCPMessage.MESSAGE_TYPE_UNLOCK,NFCPMessage.ERROR_NONE, unlockId);
			
			} else {
				//Can't toast in automatic handler so we have to run in UI-thread
				runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		            	Toast.makeText(MainActivity.this,"The Database contains no key for this door",Toast.LENGTH_LONG).show();
		            }
		        });
				return null;		
			
			}
			
		} else if (nfcpMessage.getType().equals(NFCPMessage.MESSAGE_TYPE_UNLOCK)) { //If message is of type 2(unlock command). Here only for debugging. Should be handled by controller.

			sendMsg = new NFCPMessage("TE", "01", NFCPMessage.STATUS_OK, NFCPMessage.MESSAGE_TYPE_RESULT,NFCPMessage.ERROR_NONE, "Anna");

		} else if (nfcpMessage.getType().equals(NFCPMessage.MESSAGE_TYPE_RESULT)) { //If latest received is of type 3 we should check the result
			
			return null;

		} else {			// Should never happen because there is only 3 types.
			
			//Can't toast in automatic handler so we have to run in UI-thread
			runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	Toast.makeText(MainActivity.this, "Illegal message type", Toast.LENGTH_SHORT).show();
	            }
	        });
			return null;
		
		}

		
		NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"text/plain".getBytes(Charset.forName("US-ASCII")),
				new byte[0], sendMsg.toString().getBytes(
						Charset.forName("US-ASCII")));
		return new NdefMessage(new NdefRecord[] { record });
	}

	/**
	 * Gets the latest NFC message and return it as a string
	 * 
	 * @param intent
	 * @return
	 */
	public String getLastestNFCMessage(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		return latestRecievedMsg = new String(msg.getRecords()[0].getPayload());
		// record 0 contains the MIME type, record 1 is the AAR, if present
	}

	/**
	 * Sets string s into debugging messagefield on the first screen.
	 * 
	 * @param s
	 */
	public void setMessage(String s) {

		TextView view = (TextView) findViewById(R.id.message);
		view.setText(s + "\n");
		
	}
}