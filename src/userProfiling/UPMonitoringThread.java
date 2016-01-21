package userProfiling;


import java.util.Date;
import java.util.Timer;

import utils.BlackBoard;
import utils.Definitions;

import databaseConnection.DAO;


import json.JSONException;
import json.JSONObject;


/**
 * Once a user is authenticated, his/her packets are granted to be processed for accounting. This
 * class is in the charge of the packet-processing of users. The needed
 * information is obtained by the two main thread which are PacketBuffer and
 * AssociationAuthentication classes. Collected packets are processed and periodically
 * are stored in the corresponding file. 
 * @author Cem Akpolat & Mursel Yildiz
 * 
 * */

public class UPMonitoringThread implements Runnable {
	public static String className;
	
	UPMain upmain;
	/*MAC address of the user*/
	private String MAC;
	/*User ID of the user*/
	private String userId;
	/*User Model in the form of JSON */
	private JSONObject userJsonModel;
	/*User's IP Address*/
	private String IP;
	/*Index of the packet in the PacketBufferList*/
	private int indexOfPacket;
	private static GlobalUserStateListObject userIDMacTuple;
	/*Data Access Object for storing the current user model */
	DAO dao;
	/*Processing User Model*/
	public UserSessionProfile uprofile;
	
	Timer time;
	/*Time interval for registering periodically*/
	public long timeInterval=Definitions.TimeIntervalForStroingUserProfile;
	/*In case of unsolicitated interruption, this thread will be invoked  to finalize
	 the current tasks prior to leaving the execution */
	public Thread hook;
	
	/**
	 * Initiation of class and assigning of the parameters.
	 * @param MAC 
	 * @param userId
	 * @param IP 
	 * */
	public UPMonitoringThread(String MAC, String userId, String iP) {
		className=this.getClass().getName();
		this.userId = userId;
		this.MAC = MAC;
		this.IP = iP;
		upmain = UPMain.getInstance();
		UPMonitoringThread.userIDMacTuple = new GlobalUserStateListObject(MAC,
				userId);
	}

	@Override
	/**
	 * The whole process is performed in this block. Basically, this thread registers itself
	 * against to any interruption, then packet processing is realized. In case of the deauthentication
	 * of the concerning user, processed data is stored and the thread unregister itself.
	 * */

	public void run() {
		
		// In order to release the port number 5010 for the future use,
				// we are releasing the port number at shutdown hook
		Runtime.getRuntime().addShutdownHook(hook=new Thread() {
			@Override
			public void run() {
				BlackBoard.writeConsole(className, "Shutdown hook ran!");
				try {
					storeUserModelToDB(userJsonModel, userId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		// boolean authenticationState = true;
		BlackBoard.writeConsole(className,"Monitoring thread is started for " + userId
				+ " MAC is " + MAC + " and IP is " + IP);
		boolean authenticationState = checkUserIsAuthenticated(UPMonitoringThread.userIDMacTuple);
		if (authenticationState) {
			//arrival Time
			this.userJsonModel = getUserModelFromDB(userId);
			uprofile= new UserSessionProfile(this.userJsonModel,userId,timeInterval);
			uprofile.session.setArrivalTime(new Date().getTime());//as milisecond
			time=new Timer();
			timer(time,uprofile,false);
			uprofile.session.setAuthenticationNumber(uprofile.session.getAuthenticationNumber()+1);//increase authentication number
 
		}
		while (authenticationState) {
			
			try {
				BlackBoard.writeConsole(className,"Thread is sleeping for user: " + userId + " device mac is " + MAC);
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			
				authenticationState = checkUserIsAuthenticated(UPMonitoringThread.userIDMacTuple);
				if (authenticationState) {
					// sleep for a while
					// |duration|packet count|IP
					JSONObject newPacket = getUserPacket(IP);
					if (newPacket != null) {
						BlackBoard.writeConsole(className,"User-Packet is now being processed");
						processUserModel(newPacket, userJsonModel, userId);
						updateProcessedPacket(this.indexOfPacket);
					} else {
						BlackBoard.writeConsole(className,"No packet to process");
					}
				} /*else if (isThereStillPacketsForMe(IP)) {
					//departure Time
					if(uprofile.session.isTimeAssigned()==false){
						uprofile.session.setDepartureTime(new Date().getTime());
						uprofile.session.setTimeAssigned(true);
						timer(time,uprofile,true);
					}
					
					BlackBoard.writeConsole(className,"Not Authenticated but there are still packages to be processed");
					authenticationState = true;
					JSONObject newPacket = getUserPacket(IP);
					if (newPacket != null) {
						System.out.println("User-Packet is now processing");
						processUserModel(newPacket, userJsonModel, userId);
						updateProcessedPacket(this.indexOfPacket);
					} else {
						
						BlackBoard.writeConsole(className,"No packet to process");
					}
				}*/ else {
					//departure Time
					if(uprofile.session.isTimeAssigned()==false){
						uprofile.session.setDepartureTime(new Date().getTime());
						uprofile.session.setTimeAssigned(true);
						timer(time,uprofile,true);
					}
					
					BlackBoard.writeConsole(className,"User is now unauthenticated and no packet to be processed ");
					try {
						storeUserModelToDB(userJsonModel, userId);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

		}
		notifyThreadFinishState(Thread.currentThread().getId());
		System.gc();System.gc();System.gc();
		BlackBoard.writeConsole(className,"Finished Monitoring thread for " + userId
				+ " MAC is " + MAC + " and IP is " + IP);
		
		Runtime.getRuntime().removeShutdownHook(hook);
	}

	/**
	 *When User is deauthenticated, packet processing will be ended and this function
	 *marks that the current thread finished.
	 *@param threadId
	 * */

	private void notifyThreadFinishState(long threadId) {
		synchronized (UPMain.GlobalUserThreadList) {
			for(int i=0;i<UPMain.GlobalUserThreadList.size();i++){
				if(UPMain.GlobalUserThreadList.get(i).threadId==threadId){
					UPMain.GlobalUserThreadList.get(i).finish=true;
					BlackBoard.writeConsole(className, "Thread-"+threadId+" notified for its termination");
					i=UPMain.GlobalUserThreadList.size();
				}
				
			}
		}
	}

	/**
	 * In case of being invoked, it is checked whether the given IP has still
	 * its own unprocessed packets or not.
	 * 
	 * @param IP
	 *            address of the related user
	 * @return true or false
	 * */
	private  boolean isThereStillPacketsForMe(String iP2) {
		boolean state = false;

		//GlobalPacketListObject obj;
		
		synchronized (UPMain.GlobalPacketList) {
			int index=0;
			while (index<UPMain.GlobalPacketList.size()) {
				if (UPMain.GlobalPacketList.get(index).getIP().equalsIgnoreCase(iP2)
						&& UPMain.GlobalPacketList.get(index).getPacketProcessed() == false) {
					state = true;
					break;
				}
				index++;
			}
		}
		return state;
	}

	/**
	 * The status of User's authentication is controlled by being called this
	 * function. In case user is still authenticated, the response will be true
	 * otherwise false
	 * 
	 * @param userIDMacTuple
	 * @return true or false
	 * */
	private  boolean checkUserIsAuthenticated(
			GlobalUserStateListObject userIDMacTuple) {
		boolean state = false;

		synchronized (UPMain.GlobalUserStateList) {
			int index=0;
			while (index<UPMain.GlobalUserStateList.size()) {
				if (UPMain.GlobalUserStateList.get(index).getMAC().equalsIgnoreCase(userIDMacTuple.getMAC())
						&& UPMain.GlobalUserStateList.get(index).getUserId().equalsIgnoreCase(
								userIDMacTuple.getUserId())) {
					state = true;
				}
				index++;
			}
		}
		return state;
	}

	/**
	 * The user model of the given userId is returned in the form of JSON.
	 * 
	 * @return obj JSON user model
	 * */
	public JSONObject getUserModelFromDB(String userId) {
		JSONObject obj = null;
		// connection with database
		dao = new DAO();
		try {
		//	obj = dao.getUserModel(userId);
			obj=dao.getUserModelFromFile(userId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * With respect to IP address, the relevant Packet is fetched from synchronized list.
	 * 
	 * @param IP
	 * @return jobj JSON Object packet
	 * */
	public  JSONObject getUserPacket(String IP) {

		JSONObject jobj = null;
		// get the packet if exists
		//GlobalPacketListObject obj;
		
		synchronized (UPMain.GlobalPacketList) {
		//	System.out.println("GlobalPacketList Size"+upmain.GlobalPacketList.size());
			int index = 0;// used as indexNumber
			while (index<UPMain.GlobalPacketList.size()) {
				if (UPMain.GlobalPacketList.get(index).getIP().equalsIgnoreCase(IP)
						&& UPMain.GlobalPacketList.get(index).getPacketProcessed() == false) {
					this.setPacketIndex(index);
					return UPMain.GlobalPacketList.get(index).getPacket();
					// jobj=obj.getPacket();
					// break;
				}
				index++;
			}
		}
		return jobj;
	}

	/**
	 * Processed packet is updated by this function in order to be removed later
	 * from the PacketBufferList.
	 * 
	 * @param indexOFNewPacket
	 *            packet index should be given to find the packet.
	 * */
	public  void updateProcessedPacket(int indexOfNewPacket) {
		//BlackBoard.writeConsole(className,"Updating the packet as processed");
		synchronized (UPMain.GlobalPacketList) {
			UPMain.GlobalPacketList.get(indexOfNewPacket).setPacketProcessed(true); 
		}
	}

	/**
	 * User model is processed with regards to the incoming parameters.
	 * 
	 * @param packet
	 *            , oldUserModel,userID
	 * */
	
	public int processUserModel(JSONObject packet, JSONObject oldUserModel,
			String userID) {
		// newUserModel= new UserModelProcessing(packet,oldUserModel);
		BlackBoard.writeConsole(className,"Now processing the packet and updating local user Model for User Id "
				+ userID);
		uprofile.bandwidth.addPacketIntoList(packet);
		
		//System.out.println("Packet content: "+ packet.toString());
		
		return 0;

	}

	/**
	 * Processed user model is stored into DataBase/File
	 * 
	 * @param userModel
	 *            ,userId
	 * @throws JSONException 
	 * */
	public void storeUserModelToDB(JSONObject userModel, String userId) throws JSONException {
		
		uprofile.transferUserProfileInfoIntoUserModel(userModel);
		dao = new DAO();
		//dao.storeUserModelintoDB(userId, userModel);
		dao.storeUserModelInFile(userModel,userId);
		BlackBoard.writeConsole(className,"user Model is being storing for User Id "+ userId);
	}

	/**
	 * Get index of the packet.
	 * 
	 * @return index of the packet
	 * */
	public int getPacketIndex() {
		return this.indexOfPacket;
	}

	/**
	 * Set the index of the packet
	 * 
	 * @param index
	 *            of the packet
	 * */
	public void setPacketIndex(int i) {
		this.indexOfPacket = i;
	}
	/**
	 * Get the count of all unprocessed packet of the user.
	 **/
	public int getUnprocessedPacketsForUser(String IP) {

		//GlobalPacketListObject obj;
		int packetcount = 0;

		synchronized (UPMain.GlobalPacketList) {
			int index=0;
			while (index<UPMain.GlobalPacketList.size()) {
				if (UPMain.GlobalPacketList.get(index).getIP().equalsIgnoreCase(IP)
						&& UPMain.GlobalPacketList.get(index).getPacketProcessed() == false) {
					packetcount++;
				}
				index++;
			}
		}

		return packetcount;
	}
	/**
	 * Timer function is responsible for calling the storing request 
	 * of user's data in a regular period as long as it does not receive an exit command.
	 * @param timer 
	 * @param uprofile : user profile
	 * @param exit : timer status 
	 **/
	public void timer(Timer timer, UserSessionProfile uprofile,boolean exit){
	    long restTime=0;
	   	//String[] time=(DateTransformer.transformMiliSecondToString(uprofile.session.arrivalTime)).split(":");
	   	//restTime=((Integer.parseInt(time[1])-(Integer.parseInt(time[1])/10)*10))*1000*60;//as millisecond
	   	restTime=(10-((uprofile.session.arrivalTime/(1000*60))%10))*1000*60;//rest time as millisecond
	    	

	   	if(exit==false){
	    	timer.schedule(new java.util.TimerTask() {
	    		@Override
				public void run(){
		    	tenMinuteSession();
		    	}
		    	},restTime,timeInterval);//remove /(600*10) this is just for this case 
	    	}
	    	else{
	    		timer.cancel();
	    		tenMinuteSession();//added due to the last change	    		
	    		//BlackBoard.writeConsole(className,"statu: "+statu);
	    	}

	    }
	

	/***
	 * This function is called by a timer in order to perform the task of storing a given data. 
	 */
	protected void tenMinuteSession() {
		int timeInterval=(int) (this.timeInterval/(1000*60));//time Inerval should be converted to minute from milisecond
		uprofile.session.addSessionSample(this.uprofile,timeInterval);
		try {
			storeUserModelToDB( userJsonModel,userId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			}
		}
}
