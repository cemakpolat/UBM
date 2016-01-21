package userProfiling;


import java.util.ArrayList;

import utils.BlackBoard;

import json.JSONException;
import json.JSONObject;

/***
 * User Bandwidth utilization is calculated in this class.
 *
 *@author Cem Akpolat & Mursel Yildiz
 */

public class UserBandwidth {
	/*A list contains bandwidth objects being user's packet*/
	public ArrayList<BandwidthObject> bWList;
	/*General bandwidth usage average*/
	public double 	bandwidthAverage;
	/*Total bandwidth which is computed until the concerning time.*/
	public double 	totalBW;
	/*Bandwidth consumption average within a specific time interval in other words session*/
	public double sessionBandwidthAveragePerMin;
	/*Total bandwidth usage for TCP*/
	public int  	totalTCPBW;
	/* TCP general Bandwidth usage average */
	public double 	TCPAverage;
	/*Total bandwidth usage for UDP*/
	public int 		totalUDPBW;
	/* UDP general Bandwidth usage average */
	public double 	UDPAverage;
	
	public String className;
	
	
	UserBandwidth(){
		className=this.getClass().getName();
		bWList=new ArrayList<BandwidthObject>();
	}
	/***
	 * Received and Sent packets captured by Conntrack added in a list within the authentication session.
	 * @param packet
	 */
	public void addPacketIntoList(JSONObject packet){
		//upload -> origin
		String packetTrafficType="";
		int packetSize=0;
		try {
			packetTrafficType = (String) packet.getJSONObject("flow").getJSONArray("meta").getJSONObject(0).getJSONObject("layer4").get("protoname");
			//System.out.println("packetTrafficType: "+packetTrafficType);
			packetSize =  packet.getJSONObject("flow").getJSONArray("meta").getJSONObject(0).getJSONObject("counters").getInt("bytes");
			//System.out.println("packet size: "+packetSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
		if(!packetTrafficType.equalsIgnoreCase("")){
			this.bWList.add(new BandwidthObject(packetSize,packetTrafficType,"TX"));
		}
		
		//download -> reply
		try {
			packetTrafficType = (String) packet.getJSONObject("flow").getJSONArray("meta").getJSONObject(1).getJSONObject("layer4").get("protoname");
			//System.out.println("packetTrafficType: "+packetTrafficType);
			packetSize =  packet.getJSONObject("flow").getJSONArray("meta").getJSONObject(1).getJSONObject("counters").getInt("bytes");
			//System.out.println("packet size: "+packetSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}		
		if(!packetTrafficType.equalsIgnoreCase("")){
				
			this.bWList.add(new BandwidthObject(packetSize,packetTrafficType,"RX"));
			//System.out.println("packet added list size for download: "+this.bWList.size());
		}
	}
	//Calculate total Bandwidth Upload
	/***
	 * This function is used temporary,instead of this the following two functions will be utilized
	 * namely calculateTotalUploadTX() and calculateTotalDownloadRX 
	 */
	public void calculateTotalBandwidth(){
		
		for(int i=0;i<bWList.size();i++){
			totalBW=totalBW+bWList.get(i).packetSize;			
			if(bWList.get(i).packetTrafficType.equalsIgnoreCase("tcp")){
				totalTCPBW=totalTCPBW+bWList.get(i).packetSize;	
			}
			else if(bWList.get(i).packetTrafficType.equalsIgnoreCase("udp")){		
				totalUDPBW=totalUDPBW+bWList.get(i).packetSize;
			}
		}
		bWList.clear();//remove all element from list
		BlackBoard.writeConsole(className, "totalBW: "+totalBW);
		BlackBoard.writeConsole(className, "totalTCPBW: "+totalTCPBW);
		BlackBoard.writeConsole(className,"totalUDPBW: "+totalUDPBW);
		
	}
	/***
	 * Calculate total uploaded packets amount in byte.
	 * Called ORIGIN packets in conntrack language.
	 */
	public void calculateTotalUploadTX(){
		
		for(int i=0;i<bWList.size();i++){
			totalBW=totalBW+bWList.get(i).packetSize;
			if(bWList.get(i).paketDirection.equalsIgnoreCase("TX")){
				if(bWList.get(i).packetTrafficType.equalsIgnoreCase("tcp")){
					totalTCPBW=totalTCPBW+bWList.get(i).packetSize;	
				}
				else if(bWList.get(i).packetTrafficType.equalsIgnoreCase("udp")){
				
					totalUDPBW=totalUDPBW+bWList.get(i).packetSize;
				}
			}
		}
		bWList.clear();//remove all element from list
		BlackBoard.writeConsole(className, "totalBW: "+totalBW);
		BlackBoard.writeConsole(className, "totalTCPBW: "+totalTCPBW);
		BlackBoard.writeConsole(className,"totalUDPBW: "+totalUDPBW);
		
	}
	/***
	 * Calculate total downloaded packet in byte.
	 * Called REPLY in conntrack language.
	 */
	public void calculateTotalDownloadRX(){
			
			for(int i=0;i<bWList.size();i++){
				totalBW=totalBW+bWList.get(i).packetSize;
				if(bWList.get(i).paketDirection.equalsIgnoreCase("RX")){
					if(bWList.get(i).packetTrafficType.equalsIgnoreCase("tcp")){
						totalTCPBW=totalTCPBW+bWList.get(i).packetSize;	
					}
					else if(bWList.get(i).packetTrafficType.equalsIgnoreCase("udp")){
					
						totalUDPBW=totalUDPBW+bWList.get(i).packetSize;
					}
				}
			}
			bWList.clear();//remove all element from list
			BlackBoard.writeConsole(className, "totalBW: "+totalBW);
			BlackBoard.writeConsole(className, "totalTCPBW: "+totalTCPBW);
			BlackBoard.writeConsole(className,"totalUDPBW: "+totalUDPBW);
			
	}
	/***
	 * Bandwidth average consumption is calculated during the authenticated session by taking into account 
	 * the periodic time intervals in kbit/s. 
	 * @param uprofile
	 */
	public void calculateSessionBandwidthAveragePerMin(UserSessionProfile uprofile ){
		calculateTotalBandwidth();
		//calculateTotalUploadTX();
		//calculateTotalDownloadRX();
		sessionBandwidthAveragePerMin=(this.sessionBandwidthAveragePerMin*(uprofile.session.authenticationNumber-1)+this.totalBW/(uprofile.timeInterval*60))/(uprofile.session.authenticationNumber);		

	}
	/***
	 * Calculate general bandwidth usage average in the end of the session in kbit/s.
	 * @param session
	 */
		public void calculateBandwidthAverage(UserSession session){
			calculateProtocolsBW( session);
			bandwidthAverage=(this.bandwidthAverage*(session.authenticationNumber-1)+this.totalBW/(session.sessionTime/1000))/session.authenticationNumber;		
		}
	/***
	 * Calculate bandwidth usage for TCP and UDP amount in the end of the session.
	 * @param session
	 */
	public void calculateProtocolsBW(UserSession session){
		this.TCPAverage = (TCPAverage*(session.authenticationNumber-1)+this.totalTCPBW/(session.sessionTime/1000))/session.getAuthenticationNumber();
		this.UDPAverage = (UDPAverage*(session.authenticationNumber-1)+this.totalUDPBW/(session.sessionTime/1000))/session.getAuthenticationNumber();		
	}
	/***
	 * Once this function is invoked, current calculated bandwidth data is stored 
	 * into the given JSONModel as a parameter.
	 * @param userModel
	 * @param uprofile
	 */
	public void setParametersToUserModel(JSONObject userModel,UserSessionProfile uprofile) {
			
			try {
				
				if(uprofile.session.isTimeAssigned){
					this.calculateBandwidthAverage(uprofile.session);
					userModel.put("bandwidthAverage",this.bandwidthAverage);
					userModel.put("TCPAverage",this.TCPAverage);
					userModel.put("UDPAverage",this.UDPAverage);	
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	/***
	 * Before handling the userModel bandwidth information, 
	 * bandwidths data is gotten from user Model and are assigned to object variables.
	 * @param userModel
	 */
	public void getParametersFromUserModel(JSONObject userModel){
			try {
				this.bandwidthAverage=userModel.getDouble("bandwidthAverage");
				this.TCPAverage=userModel.getDouble("TCPAverage");
				this.UDPAverage=userModel.getDouble("UDPAverage");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
}
/**
 * Bandwidth Class 
 * this class is utilized in order to keep together the attributes of a packet 
 * received GlobalPackerList(originally from conntrack) in one object.
 * */
class BandwidthObject{
	/*this specifies whether a packet is upload or download packet*/
	public String paketDirection;
	/*the following parameters indicates the type of the ongoing traffic such as TCP, UDP*/
	public String packetTrafficType;
	/*the below mentioned parameter gives the size of processing packet*/
	public int packetSize;
	/***
	 * Create a bandwidth Object with the following parameters.
	 * @param ps
	 * @param pktTT
	 * @param pktDirection
	 */
	BandwidthObject(int ps,String pktTT,String pktDirection ){
		this.packetSize=ps;
		this.packetTrafficType=pktTT;
		this.paketDirection=pktDirection;
	}
}




