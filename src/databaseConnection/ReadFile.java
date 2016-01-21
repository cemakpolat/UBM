package databaseConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import userProfiling.ARPObject;
import userProfiling.DHCPObject;
import utils.BlackBoard;
import utils.Definitions;


/**
 * ReadFile is only specialized for reading the related file and then return the read files to the requesters.
 *  
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class ReadFile {
	/*ARP Object*/
	public static ARPObject arpObj;
	/*DHCP Object*/
	public static DHCPObject dhcpObj;
	/*ARP ArrayList*/
	public static ArrayList<ARPObject> arpList;
	/*DHCP ArrayList*/
	public static ArrayList<DHCPObject> dhcpList;
	/*ARP Table File Path */
	public static String arpFileName=Definitions.ARPFILEPATH;
	/*DHCP File Path*/
	public static String dhcpFileName=Definitions.DHCPFILEPATH;
	public String className;
	
	String IP=null;
	public ReadFile(){
		className=this.getClass().getName();
	}
	//This function should be called regularly, because after authentication this function can be sensible
	/**
	 * Get IP Address of the given MAC Address by using DHCP or ARP file.
	 * 
	 * @param MAC
	 * @return String IP
	 */
	public String getIP(String MAC){
		try {
			Thread.sleep(2000);//I have to check this time
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//we have to read file each time, because new User would be added to the file
		readARPFile();
		//readDHCPFile();
		
		requestediPAddressForGivenMacFromARP(MAC);
		//requestediPAddressForGivenMacFromDHCP(MAC);
		BlackBoard.writeConsole(className, "requested IP: "+IP);
		return IP;
	}
	/**
	 * Get the IP Address of the given MAC Address by matching the MAC addresses in ARP List.
	 * FIXME: As ARP File can contains a MAC address corresponding the different IPs, it is possible
	 * to face an comparison issue. In order to prevent that, please remove ARP Records which is localized
	 * in /proc/net/ARP in Linux OS. 
	 * @param MAC
	 * @return String IP
	 */
	private String requestediPAddressForGivenMacFromARP(String MAC){
		for(ARPObject item : arpList){
			BlackBoard.writeConsole(className, "ARP List: "+item.showObject());
			if(item.gethWAddress().equalsIgnoreCase(MAC)){
				IP=item.getiPAddress();
				break;
			}
		}
		return IP;
	}
	/**
	 * Get the IP Address of the given MAC Address by matching the MAC addresses in DHCP List. 
	 * @param MAC
	 * @return String IP 
	 */
	
	private String requestediPAddressForGivenMacFromDHCP(String MAC){
		
		for(DHCPObject item : dhcpList){
			BlackBoard.writeConsole(className, "DHCP List IP: "+item.iP+" MAC: "+item.mAC);
			if(item.mAC.equalsIgnoreCase(MAC)){
				IP=item.iP;
			}
		}
		return IP;
	}
	/**
	 *  Read ARP File and insert the required information in the ARP List.
	 */
	private void readARPFile(){
		// TODO Auto-generated method stub
		arpList= new ArrayList<ARPObject>();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(arpFileName));
		    String str;
		    int i=0;
		    while ((str = in.readLine()) != null) {
		    	if(i>0){
		    		System.out.println(str);
		    		arpObj=new ARPObject(str);
		    		arpList.add(arpObj);
		    	}
		    	i++;
		    }
		    in.close();
		} catch (IOException e) {
		}	
	}
	/**
	 * Read DHCP File and insert the required information in the DHCP List.
	 */
	private void readDHCPFile(){
		dhcpList= new ArrayList<DHCPObject>();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(dhcpFileName));
		    String str="";
		    String iP="";
		    //String ends="";
		    //String starts="";
		    String hw="";
		    int i=0;
			while ((str = in.readLine()) != null) {
				if(i>2){
				System.out.println("LINE :"+str);
				
				
				if (str.contains("lease")) {
					//String temp[] = str.split("\\s+")[1].toString();
					iP= str.split("\\s+")[1].toString();
			//	} else if (str.contains("starts")) {
			//		starts= str.split("\\s+")[3].split(";")[0];
			//	} else if (str.contains("ends")) {
			//		ends =str.split("\\s+")[3].split(";")[0];
				} else if (str.contains("hardware ethernet")) {
					hw = str.split("\\s+")[3].split(";")[0];
				} else if (str.contains("}")) {
					//dhcpObj = new DHCPObject(iP,starts,ends,hw);
					BlackBoard.writeConsole(className,"iP"+ iP+" hw: "+hw);
					dhcpObj = new DHCPObject(iP,hw);
					dhcpList.add(dhcpObj);					
				}
				}
			i++;
			}
		    in.close();
		    
		} catch (IOException e) {
		}		
	}
	/**
	 * List all ARP Objects in the ARP List
	 */
	private void showARPList(){
		
		for(ARPObject item : arpList){
			System.out.println("List: "+item.showObject());
			}
	}

}
