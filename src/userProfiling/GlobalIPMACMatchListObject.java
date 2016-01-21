package userProfiling;

/**
 * The main goal of this class is to hold IP and MAC pair in one object.
 * @author Cem Akpolat & Mursel Yildiz
 * 
 * */
public class GlobalIPMACMatchListObject {
	private String MAC;
	private String IP;
	/**
	 * Creating a new Object with IP and MAC couple.
	 * */
	protected GlobalIPMACMatchListObject(String IP,String MAC){
		this.MAC=MAC;
		this.IP=IP;
	}/**
		get IP address
	 * */
	public String getIP() {
		return this.IP;
	}/**
	set IP address
	 * */
	public void setIP(String userId) {
		this.IP = userId;
	}
	/**
	 * get MAC address
	 * */
	public String getMAC(){
		return this.MAC;
	}/**
		set MAC address
	 * */
	protected void setMAC(String MAC){
		this.MAC=MAC;
	}
}
