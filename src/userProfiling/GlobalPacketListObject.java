package userProfiling;

import json.JSONObject;


/**
 * 
 *GlobalPacketListObject covers the IP address of the received packet, the content of the packet which is sent
 *by conntrack and packet processing status.
 * 
 *@author Cem Akpolat & Mursel Yildiz
 * */
public class GlobalPacketListObject {

	/*IP address of the incoming packet*/
	private String IP;
	/*the whole information about packet which is provided by conntrack
	 * please look at also conntrack-tools as to understand the received packet data.
	 * */
	private JSONObject packet; 
	/*processed status indicated whether a packet is already utilized/processed */
	private boolean packetProcessed;
	/**
	 * Initiate a packetlist object 
	 * */
	protected GlobalPacketListObject(String IP,JSONObject packet,boolean packetProcessed){
		this.IP=IP;
		this.packet=packet;
		this.packetProcessed=packetProcessed;
	}
	/**
	 * */
	protected String getIP(){
		return this.IP;
	}/**
	 * */
	protected void setIP(String IP){
		this.IP=IP;
	}/**
	 * */
	protected JSONObject getPacket(){
		return this.packet;
	}/**
	 * */
	protected void setPacket(JSONObject packet){
		this.packet=packet;
	}
	/**
		get packets status whether it was processed or not.
	 * */
	protected boolean getPacketProcessed(){
		return this.packetProcessed;
		
	}/**
		set packet as processed or not.
	 * */
	protected void setPacketProcessed(boolean packetProcessed){
		this.packetProcessed=packetProcessed;
	}
	
}
