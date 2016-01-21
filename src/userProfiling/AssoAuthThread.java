package userProfiling;

import java.io.IOException;
import utils.BlackBoard;
import utils.Definitions;

import communicationPlatform.Server;

import databaseConnection.ReadFile;


import json.JSONException;
import json.JSONObject;

/**
 * The authentication procedure is performed in hostapd. This class receives messages from hostapd
 * whether user is authenticated or not. On these messages, the appropriate
 * function or functions is/are called.
 * 
 * hostapd Message Format:
 *  
 * 	{\n
		"userId":"", \n
		"MAC":""	\n
		"State":"" \n
	}
	
 * @author Cem Akpolat & Mursel Yildiz
 * */

public class AssoAuthThread implements Runnable {
	// Port number
	public int PortNumber = Definitions.AssoAuthThreadServerPortNumber;// default port
	public static String className;
	public UPMain upmain;
	public static Server ourServer;
	/*Message States which are defined in hostapd*/
	final static int Association = 0;
	final static int Authentication = 1;
	final static int Disassociation = 2;
	final static int Deauthentication = 3;
	
	public ReadFile readFile;

	public String[] Messages = { "Association", "Authentication",
			"Disassociation", "Deauthentication" };
	/* processed MAC and processedUserId denotes that the MAC and userID are now processing
	   processedState shows current authentication situation of concerning user.
	 */
	public String processedMAC, processedUserId, processedState, processedIP;

	/* Messages are received as a JSON Object from Server object*/
	JSONObject jsonObject;
	/**
	 * Unique UPMain is instance is called and its reference address is assigned
	 * to the concerning parameter.
	 * */
	public AssoAuthThread() {
		className=this.getClass().getName();
		upmain = UPMain.getInstance();
	}

	/**
	 * 
	 */
	@Override
	public void run() {

		System.out.println("AutheAsso listens hostapd messages...");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				BlackBoard.writeConsole(className, "Shutdown hook ran!");
				try {
					//add Here Last state of user Model should be registered.
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
			System.out.println(e1.toString());
			e1.printStackTrace();
		}

		while (true) {
			try {
				jsonObject = getMessage();
				processedUserId = (String) jsonObject.get("userId");
				processedState = (String) jsonObject.get("state");
				processedMAC = (String) jsonObject.get("MAC");

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			switch (getRelatedMessageNumber(processedState)) {
			case Association:
				insertNewUserAssociation(processedMAC, processedUserId);
				BlackBoard.writeConsole(className, processedUserId + " is associated");
				break;
			case Authentication:
				// authentication has no info about userId,thus we have to fetch
				// it from AssociationList
				BlackBoard.writeConsole(className, " Authentication Message for "+processedUserId);

				if(!userLocatedInUserStateList(processedMAC)){
					processedUserId = getUserIdFromAssociationList(processedMAC);
					processedIP = getUserIpFromARPList(processedMAC);
					BlackBoard.writeConsole(className,"UserID: " + processedUserId
							+ " with IP: " + processedIP + " is authenticated");
					if (processedIP.equalsIgnoreCase( "0.0.0.0")) {
						/*
						 * 
						 */
						Thread arpThread = new Thread( new Runnable() {
							@Override
							public void run() {
								processedIP = getUserIpFromARPList(processedMAC);
								int i = 0;
								while(i<20 ){
									if ( !processedIP.equalsIgnoreCase( "0.0.0.0") ) {
										System.out.println("sent IP " + processedIP);
										// new user should be added into the UserStateList
										if(!userLocatedInUserStateList(processedMAC)){
											insertNewIPMAC(processedIP, processedMAC);
											insertNewUser(processedMAC, processedUserId);
											startThread(processedMAC, processedUserId, processedIP);
											BlackBoard.writeConsole(className, " new Thread created for "+processedUserId);
										}
										i = 21; 
									}else{
										processedIP = getUserIpFromARPList(processedMAC);
									}
									
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									i = i + 1; 
								}
							}
						}); 
						arpThread.start();
					}
				else{ 
					if(!userLocatedInUserStateList(processedMAC)){
						// new user should be added into the UserStateList
						insertNewIPMAC(processedIP, processedMAC);
						insertNewUser(processedMAC, processedUserId);
	
						startThread(processedMAC, processedUserId, processedIP);
						
						BlackBoard.writeConsole(className, " new Thread created for "+processedUserId);
					}

				}
			}
				break;
			case Disassociation:
				BlackBoard.writeConsole(className,processedIP + " disassociated");
				releaseIPMAC(processedMAC);
				releaseUser(processedMAC, "unknown");
				releaseUserFromAssociation(processedMAC, "unknown");
				removeThreadForThisUSer(processedMAC);
				break;
			case Deauthentication:
				BlackBoard.writeConsole(className,processedIP + " deauthenticated");
				releaseIPMAC(processedMAC);
				releaseUser(processedMAC, "unknown");
				releaseUserFromAssociation(processedMAC, "unknown");
				
				removeThreadForThisUSer(processedMAC);
				break;
			default:
				BlackBoard.writeConsole(className,"This message is discarded");
				return;
			}

			try {
				ourServer.closesocket();
				ourServer.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	/**
	 * Remove user Thread by using his/her MAC address in case of deauthentication or dissassociation of user. 
	 * @param processedMAC2
	 */
	private void removeThreadForThisUSer(String processedMAC2) {
		// TODO Auto-generated method stub
		
		synchronized (UPMain.GlobalUserThreadList) {
			System.out.println("current Thread list for "+processedMAC2);
			for(int j=0;j<UPMain.GlobalUserThreadList.size();j++){
				if(UPMain.GlobalUserThreadList.get(j).mac.equalsIgnoreCase(processedMAC2))
				{
					System.out.println("Thread ID: "+UPMain.GlobalUserThreadList.get(j).threadId+" MAC: "+UPMain.GlobalUserThreadList.get(j).mac);
				}
			}
			for(int i=0;i<UPMain.GlobalUserThreadList.size();i++){
				//if((UPMain.GlobalUserThreadList.get(i).mac.equalsIgnoreCase(processedMAC2)) && (UPMain.GlobalUserThreadList.get(i).finish==true))
				try{
					if((UPMain.GlobalUserThreadList.get(i).mac.equalsIgnoreCase(processedMAC2)))
					{
						//UPMain.GlobalUserThreadList.get(i).thread.
						UPMain.GlobalUserThreadList.get(i).thread=null;
						System.gc();System.gc();System.gc();
						BlackBoard.writeConsole(className,"Terminated thread are removed for"+processedMAC2 );
						UPMain.GlobalUserThreadList.remove(i);
						//i=UPMain.GlobalUserThreadList.size();
					}
				} catch(Exception e){
					BlackBoard.writeConsole(className,e.getMessage()); 
				}
			}
		}
	}

	/**
	 * A new IP and relevant MAC address is added in the concerning List
	 * 
	 * @param processedIP
	 *            ,processedMAC
	 * */
	private  void insertNewIPMAC(String processedIP, String processedMAC) {
		//System.out.println("The tuple of " + processedIP + " " + processedMAC
		//		+ "is stored in List ");
		
		if (!userLocatedInUserStateList(processedMAC)) {
			synchronized (UPMain.GlobalIPMACMatchList) {
				UPMain.GlobalIPMACMatchList.add(new GlobalIPMACMatchListObject(
						processedIP, processedMAC));
			}

		}
	}

	/**
	 * Get UserId with the help of user's MAC Address From GlobalAssociationList.
	 * 
	 * @param processed
	 *            MAC
	 * @return userId
	 * */
	private String getUserIdFromAssociationList(String processedMAC) {//ArrayList
		String userId = null;
		
		for (GlobalUserAssociationListObject item : UPMain.GlobalAssociationList) {
			if (item.getMAC().equalsIgnoreCase(processedMAC)) {
				userId = item.getUserId();
				break;
			}
		}
		return userId;
	}

	/**
	 * Obtain User IP address by employing user's MAC Address.
	 * 
	 * @param processed MAC
	 * @return String IP
	 * */
	private String getUserIpFromARPList(String processedMAC) {
		String IP = "0.0.0.0";
		readFile = new ReadFile();
		String IPTemp = readFile.getIP(processedMAC);
		if (IPTemp != null) {
			IP = IPTemp;
		}
		
		return IP;
	}

	/**
	 * According to the received Message, the relevant number representing the
	 * incoming Message is returned
	 * 
	 * @param Message
	 * @return Message number
	 * */
	public int getRelatedMessageNumber(String message) {
		int stateNumber = Disassociation;
		for (int i = 0; i < Messages.length; i++) {
			if (message.equalsIgnoreCase(Messages[i].toString()))
				stateNumber = i;
		}
		return stateNumber;
	}

	/**
	 * This function provides the messages sent by hostapd in the form of JSON.
	 * 
	 * @retun obj JSON
	 * */
	public JSONObject getMessage() throws JSONException, IOException {
		JSONObject obj = new JSONObject();
		int[] value = new int[1];
		// firstly the message size is sent by hostapd with the aim of
		// allocating the required resource.
		int packetSize = ourServer.recv_PacketSize(value, 1);
		// Packet itself is transmitted after its place is allocated.
		char[] packet = new char[packetSize];
		//System.out.println("packetsize:" + packet.length);
		// json object could be a problem here
		obj = ourServer.recv_packet(packet, packet.length);

		return obj;
	}

	
	/**
	 * In case of receiving an authentication message for a user, the concerning
	 * user credentials such as her/his ID and MAC address is stored a list.
	 * @param MAC
	 * @param userId
	 */
	public void insertNewUser(String MAC, String userId) {
		//this.showUserList();
		// check whether MAC address is in the list
		
		if (!userLocatedInUserStateList(MAC)) {
			synchronized (UPMain.GlobalUserStateList) {
				UPMain.GlobalUserStateList.add(new GlobalUserStateListObject(
						MAC, userId));
			}
			//System.out.println("User List Size "
			//		+ UPMain.GlobalUserStateList.size());
		}
	}

	/**
	 * Before authentication process, the ID of user and her/his MAC address is
	 * stored in a list. In case of being accomplished the authentication phase these information is used,
	 * because the ID of user is solely obtained during association.
	 *
	 * @param MAC
	 * @param userId
	 */
	public void insertNewUserAssociation(String MAC, String userId) { //ArrayList
		//System.out.println("Current Association List: ");
		//this.showAssociationList();
		if (!userLocatedInAssociationList(MAC, userId)) {
			UPMain.GlobalAssociationList
					.add(new GlobalUserAssociationListObject(MAC, userId));
		}
	}
	/**
	  * Sometimes two association messages could be transmitted by hostapd
	 * back-to-back. In such a case, the redundant informations should be
	 * prevented. This function checks whether user ID and MAC is already in the
	 * respective list or not. 
	 * @param mAC
	 * @param userId
	 * @return true/false
	 */
	private boolean userLocatedInAssociationList(String mAC, String userId) { //ArrayList
		boolean inList = false;
		for (GlobalUserAssociationListObject item : UPMain.GlobalAssociationList) {
			if (item.getMAC().equalsIgnoreCase(mAC)) {
				inList = true;
				break;
			}
		}
		return inList;
	}

	/**
	 * The List is controlled whether it contains the user's MAC Address or not.
	 * @param mAC
	 * @return true/false
	 */
	private  boolean userLocatedInUserStateList(String mAC) {
		boolean inList = false;
		//GlobalUserStateListObject obj;
		synchronized (UPMain.GlobalUserStateList) {
			int index=0;
			while (index<UPMain.GlobalUserStateList.size()) {
				if (UPMain.GlobalUserStateList.get(index).getMAC().equalsIgnoreCase(mAC)) {
					inList = true;
					BlackBoard.writeConsole(className, mAC +" is in the Authentication List");
				//	break;
				}
				index++;
			}
		}

		return inList;
	}

	/**
	 * In case a user is disassociated, her/his information is removed from the
	 * GlobalAssociationList.
	 * @param MAC
	 * @param userId
	 */
	public  void releaseUserFromAssociation(String MAC, String userId) {//arrayList
		if (userLocatedInAssociationList(MAC, userId)) {// check the list
			GlobalUserAssociationListObject obj;
			for (int i = 0; i < UPMain.GlobalAssociationList.size(); i++) {
				obj = UPMain.GlobalAssociationList.get(i);
				if (obj.getMAC().equalsIgnoreCase(MAC)) {
					UPMain.GlobalAssociationList.remove(i);
				//	break;
				}
			}
		}
	}


	/**
	 * In case a user is deauthenticated, her/his data is omitted from the
	 * GlobalUserStateList.
	 * @param MAC
	 * @param userId
	 */
	public  void releaseUser(String MAC, String userId) {

		if (userLocatedInUserStateList(MAC)) {// check the list
			synchronized (UPMain.GlobalUserStateList) {
				int index = 0;
				while (index<UPMain.GlobalUserStateList.size()) {					
					if (UPMain.GlobalUserStateList.get(index).getMAC().equalsIgnoreCase(MAC)) {
						UPMain.GlobalUserStateList.remove(index);
				//		break;
					}
					index++;
				}
			}

		}
	}
	/**
	 * Given MAC Address is removed from the GlobalIPMACMatchList
	 * @param processedMAC2
	 */
	private  void releaseIPMAC(String processedMAC2) {
		synchronized (UPMain.GlobalIPMACMatchList) {
			int index = 0;
			while (index<UPMain.GlobalIPMACMatchList.size()) {

				if (UPMain.GlobalIPMACMatchList.get(index).getMAC().equalsIgnoreCase(processedMAC2)) {
					UPMain.GlobalIPMACMatchList.remove(index);
				//	break;
				}
				index++;
			}
		}
	}

	/**
	 * Once a user is authenticated, he/she is granted to a new thread in order
	 * to be processed his/her packets coming from conntrack.
	 * @param MAC
	 * @param userId
	 * @param processedIP
	 */
	public void startThread(String MAC, String userId, String processedIP) {
		Runnable userMonitoring = new UPMonitoringThread(MAC, userId,
				processedIP);
		Thread userMonitoringThread = new Thread(userMonitoring);
		
		GlobalUserThreadListObject uThreadObj= new GlobalUserThreadListObject(MAC,userMonitoringThread.getId(), false, userMonitoringThread);
		synchronized(UPMain.GlobalUserThreadList){
			UPMain.GlobalUserThreadList.add(uThreadObj);
		}
		userMonitoringThread.start();

	}

	
	//The following codes should be adjusted in term of synchronization as performed above.
	
	/**
	 * All elements in packet list are shown.
	 * */
	public void showPacketList() {
		for (GlobalPacketListObject item : UPMain.GlobalPacketList) {
			BlackBoard.writeConsole(item.getIP());
		}
	}

	/**
	 * All current authenticated users are shown.
	 * */
	public void showUserList() {
		/*for (GlobalUserStateListObject item : UPMain.GlobalUserStateList) {
			System.out.println("MAC: " + item.getMAC() + "UserID: "
					+ item.getUserId());
		}
		*/
	}

	/**
	 * All current associated users are listed.
	 * */
	public void showAssociationList() {
		/*for (GlobalUserAssociationListObject item : UPMain.GlobalAssociationList) {
			//System.out.println("MAC: " + item.getMAC() + "UserID: "
			//		+ item.getUserId());
		}*/
	}

}
