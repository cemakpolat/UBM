package userProfiling;

import java.io.IOException;
import utils.BlackBoard;
import utils.Definitions;

import communicationPlatform.Server;


import json.JSONException;
import json.JSONObject;

/**
 * This class buffers the informatio on network packages fetched by patched conntrackd. 
 * All packets with the exception of local packets are sent by conntrack.
 * Whether a packet should be kept or discarded depends on whether packet's
 * owner is authenticated or not. In case of being authenticated of a user, user's
 * packets will be collected in list in order to be processed. The buffered packets
 * are freed from the buffer once the corresponding user monitoring thread processes the 
 * related packet.
 * .
 * 
 * @author Cem Akpolat & Mursel Yildiz
 * */
public class PacketBufferThread implements Runnable {
	// Communication channel between conntrack and packet buffer thread
	public int PortNumber = Definitions.PacketBufferServerPortNumber;// default port
	public UPMain upmain;
	public static Server ourServer;
	// Packets is received as an JSON Object from Server
	private JSONObject jObj;
	public static String className;

	/**
	 * Unique UPMain's instance is called and its reference address is assigned
	 * to the concerning parameter.
	 * */
	public PacketBufferThread() {
		className=this.getClass().getName();
		//System.out.println("Packet Buffer");
		
		upmain = UPMain.getInstance();
	}

	@Override
	/**
	 * */
	public void run() {

		// In order to release the port number 5010 for the future use,
		// we are releasing the port number at shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				BlackBoard.writeConsole(className, "Shutdown hook ran!");
				try {
					ourServer.closesocket();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		try {
			ourServer = new Server(PortNumber);
			ourServer.connect();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (true) {
			try {
				jObj = this.getNewPacket();

				String PacketIP = null;
				try {

					PacketIP = (String) jObj.getJSONObject("flow")
							.getJSONArray("meta").getJSONObject(0)
							.getJSONObject("layer3").get("src");

					BlackBoard.writeConsole(className, "Received PacketIP : " + PacketIP);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Once the user is not authenticated... this is a warning case
				// for security... should be checked
				if (checkUserAuthenticated(PacketIP)) {
					BlackBoard.writeConsole(className,"Packet for IP: " + PacketIP + " is granted to be added into the list");
					lookForProcessedPacket();
					appendPacketToBuffer(PacketIP, jObj, false);
				}


				ourServer.closesocket();
				ourServer.connect();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * user's packets are received through this function. If you want to know in
	 * depth how this reception occurs, please look at the Server class
	 * 
	 * @return packet
	 * */
	public JSONObject getNewPacket() throws IOException {
		JSONObject obj = new JSONObject();
		int[] value = new int[1];
		// firstly the packet size is sent by conntrack with the aim of
		// allocating the required resource.
		int packetSize = ourServer.recv_PacketSize(value, 1);
		// Packet itself is transmitted after its place is allocated.
		char[] packet = new char[packetSize];

		obj = ourServer.recv_packet(packet, packet.length);
		return obj;
	}

	/**
	 * Rx signifies received packet by concerning User and Tx sent packet
	 * 
	 * @param packet
	 * @return type Rx or Tx
	 * */
	public String checkPacketType(JSONObject packet) throws JSONException {
		String type = "";
		if (packet.get("packetType") == "Rx"
				|| packet.get("packetType") == "Tx") {
			if (packet.get("packetType") == "Tx")
				type = "Tx";
			else
				type = "Tx";
		} else {
			type = "unknown";
		}
		return type;
	}

	/**
	 * Check user whether is authenticated or not
	 * 
	 * @param IP
	 * @return true or false
	 * */
	public   boolean checkUserAuthenticated(String IP) {
		boolean state = false;
		
		synchronized (UPMain.GlobalIPMACMatchList) {
			int index=0;																	
			while (index<UPMain.GlobalIPMACMatchList.size()) {
				if (UPMain.GlobalIPMACMatchList.get(index).getIP().equalsIgnoreCase(IP)) {
					state = true;
					break;
				}
			index++;
			}
		}
		return state;
	}

	/**
	 * The processed packet being comprised during the extraction of new user
	 * Profile should be removed from the packet list so that more resource
	 * could be available for the next packets. In other words, the lack of the
	 * resource could be prevented.
	 * */
	public  void lookForProcessedPacket() {

		synchronized (UPMain.GlobalPacketList) {
			
			int index = 0;// used as indexNumber
			while (index < UPMain.GlobalPacketList.size()) {
				try{
					
					if ( UPMain.GlobalPacketList.get(index).getPacketProcessed() == true) {
						UPMain.GlobalPacketList.remove(index);
						System.out.println("Packet buffer: packet removed and its INDEX: "+index);
					}
					index++;
				}
				catch(Exception e){

					BlackBoard.writeConsole(className,"Packet buffer: Entered catch exception waiting for second try " + e.getMessage());
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} 
			
		}
		
	}

	/**
	 * Provided that a user is authenticated, his/her packets could be appended
	 * during the authentication time
	 * 
	 * @param IP
	 *            , packet, packet Processed Statu
	 * */
	public  void appendPacketToBuffer(String IP, JSONObject packet,
			boolean packetProcessed) {
		GlobalPacketListObject listObj = new GlobalPacketListObject(IP, packet,
				packetProcessed);
		BlackBoard.writeConsole(className, "packet buffer size:"+ UPMain.GlobalPacketList.size());
		//upmain.GlobalPacketList.notifyAll();
		synchronized (UPMain.GlobalPacketList) {
			UPMain.GlobalPacketList.add(listObj);
		}


	}

}
