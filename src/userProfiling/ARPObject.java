package userProfiling;
/**
 * ARPObject class is designed to hold the required ARP data in one object.
 * All variables defined below are based on ARP file under Linux OS. 
 * The expected ARP table is given as follows:
 * 
 * IP address       HW type     Flags       HW address            Mask     Device \n
 * 192.168.178.1    0x1         0x2         00:24:fe:c5:11:bd     *        eth1
 * 
 * @author Cem Akpolat & Mursel Yildiz
 * */
public class ARPObject {

	private String iPAddress;
	private String hWType ;
	private String flag;
	private String hWAddress;
	private String mask;
	private String device;

	/**
	 * Receive ARP String line and extract data by splitting string line.
	 * @param string str
	 * */
	public ARPObject(String str){
		String[] elements=str.split("\\s+");//The spaces between terms are not same,thus \\s+ is solution
		this.iPAddress=elements[0].toString();
		//this.hWType=elements[1].toString();
		//this.flag=elements[2].toString();
		this.hWAddress=elements[3].toString();
		//this.mask=elements[4].toString();
		//this.device=elements[5].toString();
		System.out.println("IP ADDRESS: "+this.iPAddress+" MAC: "+this.hWAddress);
	}
	/**
	 * Ip address
	 * */
	public String getiPAddress() {
		return iPAddress;
	}
	/**
	 * set IP address
	 * */
	public void setiPAddress(String iPAddress) {
		this.iPAddress = iPAddress;
	}
	/**
	 * get hardware Type
	 * */
	public String gethWType() {
		return hWType;
	}
	/**
	 * set hardware type
	 * */
	public void sethWType(String hWType) {
		this.hWType = hWType;
	}
	/**
	 * get flag
	 * */
	public String getFlags() {
		return flag;
	}
	/**
	 * set flag
	 */
	public void setFlags(String flags) {
		this.flag = flags;
	}
	/**
	 * get hardware address (MAC address)
	 * */
	public String gethWAddress() {
		return hWAddress;
	}
	/**
	 * set hardware address (MAC address)
	 * */
	public void sethWAddress(String hWAddress) {
		this.hWAddress = hWAddress;
	}/**
	 * */
	public String getMask() {
		return mask;
	}/**
	 * */
	public void setMask(String mask) {
		this.mask = mask;
	}/**
	 * */
	public String getDevice() {
		return device;
	}
	/**
	 * */
	public void setDevice(String device) {
		this.device = device;
	}/**
	 * */
	public String showObject(){
		return " "+getiPAddress()+" "+gethWAddress();
	}
	
}
