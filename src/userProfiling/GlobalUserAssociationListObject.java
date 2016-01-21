package userProfiling;



/**
 * 
 * @author Cem Akpolat & Mursel Yildiz
 * */
public class GlobalUserAssociationListObject {

	private String MAC;
	private String userId;
	/**
	 * */
	protected GlobalUserAssociationListObject(String MAC,String userId){
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
	protected String getMAC(){
		return this.MAC;
	}/**
	 * */
	protected void setMAC(String MAC){
		this.MAC=MAC;
	}

}
