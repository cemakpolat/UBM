package userProfiling;



/**UserStateList class contains user MAC address and his/her userId provided by hostapd.
 * @author Cem Akpolat & Mursel Yildiz
 * */
public class GlobalUserStateListObject {

	private String MAC;
	private String userId;
	/**
	 * Initiate an object holding user's Mac address and his/her id.
	 * */
	protected GlobalUserStateListObject(String MAC,String userId){
		this.setMAC(MAC);
		this.setUserId(userId);
	}/**
	 * */
	public String getUserId() {
		return this.userId;
	}/**
	 * */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * */
	public String getMAC(){
		return this.MAC;
	}/**
	 * */
	protected void setMAC(String MAC){
		this.MAC=MAC;
	}

}
