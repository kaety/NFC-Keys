package kandidat.nfc.nfcapp;

public class NFCPMessage {

	//Values for test
	public static final String TEST_NAME = "TE";
	public static final String TEST_ID = "01";
	public static final String TEST_UNLOCKID = "Anna";
	public static final String TEST_UNIQUEID = TEST_NAME + TEST_ID;

	//The three types of message
	public static final String MESSAGE_TYPE_BEACON = "1";
	public static final String MESSAGE_TYPE_UNLOCK = "2";
	public static final String MESSAGE_TYPE_RESULT = "3";
	public static final String MESSAGE_TYPE_SHARE = "4";
	
	//Different status
	public static final String STATUS_OK = "0";
	public static final String STATUS_ERROR = "1";
	
	//Errors as int codes
	public static final int INT_ERROR_NONE = 0;
	public static final int INT_ERROR_NO_SECURITY = 1;
	
	//Errors codes
	public static final String ERROR_NONE = Integer.toString(INT_ERROR_NONE);
	public static final String ERROR_NO_SECURITY = Integer.toString(INT_ERROR_NO_SECURITY);
	
	
	//Fields
	private String name; //2 bytes allts� tv� ASCII-tecken
	private String id; //2 bytes allts� tv� ASCII-tecken
	private String typeOfMessage; //1 byte
	private String status;//1 byte
	private String errorCode;//1 byte //Only checked if status = "1"
	private String unlockId; //4 bytes
	private String publicKey; //This will be added if message is of type 1
	
	
	//Constructor Type2 Message
	public NFCPMessage(String name, String id, String status, String typeOfMessage,
			String errorCode,String unlockId) {
		this.name = name;
		this.id = id;
		this.status = status;
		this.typeOfMessage= typeOfMessage;
		this.errorCode = errorCode;
		this.unlockId = unlockId;
		this.publicKey = "";
	}
	
	//Constructor Type1 Message
	public NFCPMessage(String name, String id, String status, String typeOfMessage,
			String errorCode) {
		this.name = name;
		this.id = id;
		this.status = status;
		this.typeOfMessage = typeOfMessage;
		this.errorCode = errorCode;
		this.unlockId = "    ";
		this.publicKey = "";
	}
	
	/**
	 * Takes raw message in form of a string and constructs NFCPMessage
	 * @param s raw message
	 */
	public NFCPMessage(String s){
		publicKey = "";
		if (s.length() >= 11){
			name = s.substring(0, 2); //2 bytes allts� tv� ASCII-tecken
			id = s.substring(2, 4); //2 bytes allts� tv� ASCII-tecken
			typeOfMessage = s.substring(4, 5); //1 byte
			status = s.substring(5,6);//1 byte
			errorCode = s.substring(6, 7);
			//If it is of type SHARE then the rest of message is the encrypted ulockId
			if (typeOfMessage.equals(NFCPMessage.MESSAGE_TYPE_SHARE)){
				unlockId = s.substring(7);
			//If it is of other type it is unlockId maybe followed by a public key
			}else{
				unlockId = s.substring(7, 11); //4 bytes
				if(s.length() > 11){
					publicKey = s.substring(11);
				}
			}
		}
	}
	
	public void clearAll(){
		name = "";
		id = "";
		typeOfMessage = "";
		status="";
		errorCode = "";
		unlockId = "";
		publicKey = "";
	}
	
	@Override
	public String toString(){
		
		return name +
				id +
				typeOfMessage + 
				status +
				errorCode +
				unlockId +
				publicKey;
		
	}

	public String getType(){
		return typeOfMessage;
}

	public String getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}
	public String getUniqueId(){
		return name + id;
	}
	public String getName(){
		return name;
	}
	public String getId(){
		return id;
	}
	public String getPublicKey(){
		if(this.publicKey.equals("")){
			return null;
		}else{
			return publicKey;
		}
	}
	public void setPublicKey(String publicKey){
		this.publicKey = publicKey;
	}

	public String getUnlockId() {
		return unlockId;
	}

	public void setUnlockId(String unlockId) {
		this.unlockId = unlockId;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
}
