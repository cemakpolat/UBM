package userProfiling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import databaseConnection.DAO;


import json.JSONArray;
import json.JSONException;
import json.JSONObject;

/***
 * UPMain is the core class of this framework which activates many fundamental parts such as gathering packets 
 * from conntrack-tools, obtaining authentication message by means of hostapd tool and monitoring packets 
 * as well as handling them in order to store in database/file . The whole architecture could be also seen
 * in the following figure which indicates the relation between UPMain and these parts.
 * On the other hand, UPMain provides user's information in case any request is done from the MeasurementPlane side.
 * 
 * <img src="img/UPMain.jpeg"/>
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */

public class UPMain {
	/*
	 * In order to keep one instance during the execution, singleton pattern is
	 * used. Because all threads is employing the same resources such as
	 * arrayList.
	 */
	private static final UPMain instance = new UPMain();
	/*
	 * The following list keeps the list of the authenticated users and its
	 * tuple is consist of MAC and UserId
	 */
	public static List<GlobalUserStateListObject> GlobalUserStateList;
	/*
	 * Contanins Packets,its own IP address and also its statu whether packet is
	 * processed or not. its tuple looks like |IP|Packet|StateOfPacket
	 */
	public static List<GlobalPacketListObject> GlobalPacketList;
	/*
	 * Before the authentication, associated user list should be stored and the
	 * below list contains user's MAC and it's id after association process
	 * occuring in hostapd. Tuple : MAC|userId
	 */
	public static ArrayList<GlobalUserAssociationListObject> GlobalAssociationList;
	/*
	 * As Conntrack sends packet without their MAC addresses, in order to figure
	 * out which packet belongs to which MAC address, we have a list storing IP
	 * and its MAC address provided by ARP file after authentication.
	 */
	public static List<GlobalIPMACMatchListObject> GlobalIPMACMatchList; // List->|IP|MAC|
	/*
	 * The following list contains the user threads which are running
	 */
	public static List<GlobalUserThreadListObject> GlobalUserThreadList;
	static String strDirectoy ="Users";
	public UPMain() {
	}

	/***
	 * Provide only one instance of this Class
	 * @return
	 */
	public static UPMain getInstance() {
		return instance;
	}
	
	/**???
	 * Create user directory for collecting user data and initiate list
	 * Start threads in order to gather data from packet buffer and authentication
	 * @see hostpad.
	 * @see conntrack-tools
	 * @param args
	 * @throws IOException
	 */

	public void main() throws IOException {

	
		// Create one directory
		boolean success = (
		new File(strDirectoy)).mkdir();
		if (success) {
			  System.out.println("Directory: " + strDirectoy + " created");
		}  
		
		GlobalUserStateList = Collections
		.synchronizedList(new ArrayList<GlobalUserStateListObject>());
		
		GlobalPacketList = Collections
		.synchronizedList(new ArrayList<GlobalPacketListObject>());
		
		GlobalIPMACMatchList = Collections
		.synchronizedList(new ArrayList<GlobalIPMACMatchListObject>());
		
		GlobalUserThreadList=Collections.synchronizedList(new ArrayList<GlobalUserThreadListObject>());
		
		GlobalAssociationList = new ArrayList<GlobalUserAssociationListObject>();
		
	

		Runnable packetBuffer = new PacketBufferThread();
		Thread threadPacketBuffer = new Thread(packetBuffer);
		threadPacketBuffer.start();

	
		Runnable assoAuth = new AssoAuthThread();
		Thread threadAssoAuth = new Thread(assoAuth);
		threadAssoAuth.start();
		
		
		Thread timerThread = new Thread (new Runnable() {
	           @Override
			public void run() {
	               //System.out.println(className + "starting the timer for conntrack");
	              try {
	                   while(true) {
	                	   
	                          //Process p = Runtime.getRuntime().exec("conntrack -E .... ");
	                       Process p=Runtime.getRuntime().exec(
	                    			 new String[] { "conntrack", "-E","-o", "xml" });   
	                	   Thread.sleep(100000);
	                          p.destroy();
	                   }
	               } catch (InterruptedException e) {
	                   // TODO Auto-generated catch block
	                   e.printStackTrace();
	               } catch (IOException e) {
	                   // TODO Auto-generated catch block
	                   e.printStackTrace();
	               }
	           }
	       	}
	       );
		
	       try{
	           timerThread.start();
	       }catch(Exception e){
	           timerThread.run();
	       }
	     
		
		
		// Runtime.getRuntime().exec(
		// new String[]
		// {"./../../autonomous/conntrack-tools-1.0.0/src/conntrack"
		// ,"-E ","-o", "xml" });

		//Runtime.getRuntime().exec(
		//		new String[] { "./conntrack-tools-1.0.0/src/conntrack", "-E",
		//				"-o", "xml" });// new String[]{"conntrack -E -o XML" }
		
		// Runtime.getRuntime()
		// .exec(new String[]{"./consoleVersion/demoClient"});

		// new thread for conntrack restarting

		//Runnable connRestarter = new ConntrackRestarter();
		//Thread threadConnRe = new Thread(connRestarter);
		//threadConnRe.start();
	}
	/***
	 * Get all current authenticated user ID 
	 * @return JSONArray
	 */
	//Return current Authenticated UserModel
	public JSONArray getAuthenticatedUsers(){
		JSONArray userids= new JSONArray();
		synchronized (UPMain.GlobalUserStateList) {
			int index=0;
			while (index<UPMain.GlobalUserStateList.size()) {
				userids.put(UPMain.GlobalUserStateList.get(index).getUserId());
				index++;
			}
		}
		return userids;
		
	}
	/**
	 * Get all User Model of authenticated users 
	 * @return JSONArray
	 * @throws JSONException
	 */
	//check user IDs
	//Return all UserModel of authenticated users
	public JSONArray getAllAuthenticatedUserModels() throws JSONException{
		JSONArray arr= new JSONArray();
		DAO db= new DAO();
		
		synchronized (UPMain.GlobalUserStateList) {
			int index=0;
			while (index<UPMain.GlobalUserStateList.size()) {
				 arr.put( db.getUserModelFromFile(UPMain.GlobalUserStateList.get(index).getUserId()));
				index++;
			}
		}
		return arr;
	}
	/***
	 * Get the specific user model
	 * @param userId
	 * @return JSONObject
	 * @throws JSONException
	 */
	public JSONObject getSpecificUserModel(String userId) throws JSONException{
		JSONObject userModel= new JSONObject();
		DAO db= new DAO();
		userModel=db.getUserModelFromFile(userId);
		return userModel;
	}
	/***
	 * Get All User ID 
	 * @return JSONArray
	 */
	//get All User Id from file or database
	public JSONArray getAllUsersId(){
		JSONArray arr= new JSONArray();
		DAO db= new DAO();
		ArrayList<String> users=db.getAllUserIDs();
		for(int j=0;j<users.size();j++){
			System.out.println(users.get(j));
			  arr.put(users.get(j));
		}
		return arr;
	}
	/***
	 * Get All User Model
	 * @return JSONArray
	 * @throws JSONException
	 */
	public JSONArray getAllUsersModel() throws JSONException{
		JSONArray arr= new JSONArray();
		DAO db= new DAO();
		ArrayList<String> users=db.getAllUserIDs();
		for(int j=0;j<users.size();j++){
			System.out.println(users.get(j));
			  arr.put( db.getUserModelFromFile(users.get(j)));
		}
		return arr;

	}
	/***
	 * Get the count of all users 
	 * @return JSONObject
	 * @throws JSONException
	 */
	public JSONObject getNumberOfKnownUsers() throws JSONException{
		DAO db= new DAO();
		JSONObject usersCount= new JSONObject();
		ArrayList<String> users=db.getAllUserIDs();
		if (users == null) {
			usersCount.put("usersCount", 0);
			//return 0; 
		}else{
			usersCount.put("usersCount", users.size());
		//	return users.size();
		}
		//add here JSON Message
		return usersCount;
	}
	/***
	 * Get a specific User Model of the user through given client ID
	 * @param clientID
	 * @return JSONObject
	 * @throws JSONException
	 */
	//return JSON Model of user 
	public JSONObject getModelOfKnownId(String clientID) throws JSONException{
		DAO db= new DAO();
		return db.getUserModelFromFile(clientID);

	}
	/***
	 * Get any required information belonging to an user with the conjunction of
	 * userId and requested parameter.This data is fetched from the user specific file.
	 * @param userId
	 * @param requiredParameter 
	 * @return JSONObject
	 * @throws JSONException
	 */
	//return any information according to the requirementParameter
	public JSONObject getAnyRequiredInfoForUser(String userId,
			String requiredParameter) throws JSONException {
			String value=null;
			JSONObject userModel= new JSONObject();
			JSONObject paramWillBeSent= new JSONObject();
			DAO db= new DAO();
			userModel=db.getUserModelFromFile(userId);
		
			if(requiredParameter=="bandwidthAverage"){
			value=Double.toString(userModel.getDouble("bandwidthAverage"));
			paramWillBeSent.put(value, value);
			}
			else if(requiredParameter=="authenticationNumber"){
				value=Integer.toString(userModel.getInt("authenticationNumber"));
				paramWillBeSent.put(value, value);
			}
			else if(requiredParameter=="sessionAverageTime"){
				value=(String)userModel.get("sessionAverage");
				paramWillBeSent.put(value, value);
			}
			else if(requiredParameter=="sessionsPerMin"){
				value=(String)userModel.get("sessionsPerMin");
				paramWillBeSent.put(value, value);
			}
		//add here JSON Message
		return	paramWillBeSent;	
		//return value;
	}
	

}
