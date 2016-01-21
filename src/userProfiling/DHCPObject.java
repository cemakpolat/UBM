package userProfiling;



/**
 * DHCP class, as in ARP class, were conceptualized for maintaining the
 * corresponding DHCP records for a client. The main aim of DHCP and ARP is to
 * figure out which MAC-IP pair is currently on. A simple DHCP record is
 * illustrated below.
 * 
 * lease 192.168.127.6 { \n 
 * 		starts 6 2000/01/01 01:43:50;\n 
 * 		ends 6 2000/01/01 01:53:50;\n 
 * 		tstp 6 2000/01/01 01:53:50;\n 
 * 		cltt 6 2000/01/01 01:43:50;\n
 * 		binding state free;\n 
 * 		hardware ethernet 00:24:d2:fe:bd:fc;\n 
 * }\n
 * 
 * @author Cem Akpolat & Mursel Yildiz
 */


public class DHCPObject {
	public String iP;
	public String mAC;
	public String ends;
	public String starts;	
	/**
	 * Instantiate DHCP object by receiving four variables
	 * IP,starts,ends and MAC
	 * */
	public DHCPObject(String iP, String starts, String ends, String hw){
		this.iP=iP;
		this.starts=starts;
		this.ends=ends;
		this.mAC=hw;
	}
	/**
	 * 
	 * */
	public DHCPObject(String iP, String hw){
		this.iP=iP;
		this.mAC=hw;
	}
}
