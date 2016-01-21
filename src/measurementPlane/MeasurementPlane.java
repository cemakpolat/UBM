package measurementPlane;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import communicationPlatform.JavaToJavaClient;
import communicationPlatform.JavaToJavaServer;
import communicationPlatform.JavaToJavaServerTimeOut;

import qoETracker.QoETracker;

import userProfiling.UPMain;
import utils.BlackBoard;
import utils.CommunicationLanguage;
import utils.Definitions;


import json.JSONArray;
import json.JSONException;
import json.JSONObject;
/**
 * \mainpage Measurement Plane 
 * <p>
 * MeasurementPlane is placed on top of this framework which is able to communicate with other softwares
 * using a common communication protocol. It consists of many sub-components (UPMain and QoETracker) 
 * which are responsible for responding the requests of MeasurementPlane with the help of the functions defined
 * in MeasurementPlane. MeasurementPlane is the platform bringing the client specific model with the 
 * decision making engines of the Access Point together.
 * The software architecture is outlined in the figure below.
 * </p>
 * 
 * <img src="MeasurementPlane.png"/>
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class MeasurementPlane {
	/*Assigning Server Port Number*/
	private static int serverPort=Definitions.MPServerPortNumber;//default
	/*Assigning Client port Number*/
	private static int clientPort=Definitions.MPClientPortNumber;//default
	/*Instantiation of UPMain*/
	public static UPMain measurementPoint = new UPMain(); 
	/*Instantiation of QoETracker */
	public static QoETracker qoETracker= new QoETracker();
	/*Java Server with Timeout*/
	public static JavaToJavaServerTimeOut MessageServer = new JavaToJavaServerTimeOut(serverPort);
	//public static JavaToJavaClient mainClient = new JavaToJavaClient(clientPort);  
	/*IP Address*/
	public static String ipAddress; 
	/**/
	public static String className="MeasurementPlane";
	
	
	public static void main(String[] args) throws IOException {

		System.out.println("Starting the Measurement Point "); 
		ipAddress = getIpAddress();
		
		runComponents();
		/**
		 * 
		 */
		Thread serverThread = new Thread (new Runnable() {
	           @Override
			public void run() {
	        	
	        	   while(true){
	        		   
	        		   //Receive Message From Observer's Server
	 	        		String message = MessageServer.getMessageJSON();
	 	        		BlackBoard.writeConsole(className,message);
	 	        		if (message.equalsIgnoreCase("timeout") ) { 
	 	        			BlackBoard.writeConsole(this.getClass().getName(),"Time out in receiving Request ");
	 	        		}
	 	        		else{
		 	        		JSONObject jobject = null; 
		 	        		JSONObject messageJsonObject = null; 
		 	        		int selectedOption=0;
		 	        		
							try {
								jobject = new JSONObject(message);
								messageJsonObject = jobject.getJSONObject("receivedMessage");
								clientPort=Integer.parseInt(messageJsonObject.getString("serverSocketNumber"));
								selectedOption=Integer.parseInt(messageJsonObject.getString("option"));
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
	
							switch(selectedOption){
							case CommunicationLanguage.AuthenticatedUserModels:
								sendMessage(getAuthenticatedUserModels());
								break;
							case CommunicationLanguage.AuthenticatedUsers:
								sendMessage(getAuthenticatedUsers());
								break;
							case CommunicationLanguage.NumberOfKnownUsersID:
								sendMessage(getNumberOfKnownUsersID()); 
								break;
							case CommunicationLanguage.GetAllKnownUsersID:
								sendMessage(getAllKnownUsersID());
								break;
							case CommunicationLanguage.GetModelOfKnownID:
								try {
									sendMessage(getModelOfKnownID(messageJsonObject.getString("userId")));
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} 
								break;
							case CommunicationLanguage.GetTotalConsumedByteOfKnownID:
								try {
									sendMessage(getRequiredInfoForKnownID(messageJsonObject.getString("userId"),"sessionsPerMin"));
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							case CommunicationLanguage.GetQoEResultForKnownUserId:
							case CommunicationLanguage.AllUserQoEs:
							case CommunicationLanguage.AuthenticatedUserQoEs:
								
							case CommunicationLanguage.GetQoEAverageDelayStatus:
								sendMessage(getAverageDelayState());
								break;
							case CommunicationLanguage.GetFirstCongestedUser:
								try {
									sendMessage(getFirstCongestedUser());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							
							case CommunicationLanguage.GetWorstCongestedUser:
								try {
									sendMessage(getWorstCongestedUser());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
								
							//NEW ADDED FOR MAC ADDRESSES FROM QOE
							case CommunicationLanguage.GetUserMACAddressFromQOE:
								sendMessage(getCurrentUserMACFromQOE());
								
							default :
								//String ourResponse="Your input is not adequate according to given options!";
								//sendMessage(ourResponse);
								break;
							
							}
	 	        		}
	 	        	}
	           }

	       	}
	       );
	       try{
	    	   serverThread.start();
	       }catch(Exception e){
	    	   serverThread.run();
	       }
	       
	}
	/**
	 * Run each component being tied to MeasurementPlane
	 * @throws IOException
	 */
	public static void runComponents() throws IOException{
		measurementPoint.main();
		qoETracker.main();
	}
	/**
	 * Get the current authenticated Users's MAC Addresses from QoExperiments
	 * @return String
	 */
	public static String getCurrentUserMACFromQOE() {
		// TODO Auto-generated method stub
		return qoETracker.getCurrentUserMACs().toString();
	}
	/**
	 * Get the critical Average Delay of clients
	 * @return String 
	 */
	public static String getAverageDelayState() {
		// TODO Auto-generated method stub
		int result= 0;
		try {
			result=qoETracker.averageDelay();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return ""+result+"";
	}
	/**
	 * 
	 * @return String
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	
	public static String getFirstCongestedUser() throws NumberFormatException, JSONException{
		return qoETracker.firstCongestedUser().toString();
	}
	/**
	 * Get the client id suffering from congestion at most
	 * @return String
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public static String getWorstCongestedUser() throws NumberFormatException, JSONException{
		return qoETracker.worstCongestedUser().toString();
	}
	/**
	 * Send corresponding message to the requestee Software 
	 * 	@param response
	 */
	public static void sendMessage(String response){
		JavaToJavaClient client = new JavaToJavaClient(clientPort);
		long QoSValue = client.send(response, ipAddress); 
		if(QoSValue > 0){	
			BlackBoard.writeConsole(className,"Message sent by MeasurementPoint, response \n" + response);
		} else{
			BlackBoard.writeConsole(className,"Message sent failed \n" + response);
		}
		//long QoSValue = mainClient.send(response, ipAddress); 
	}
	/**
	 * Get the current authenticated users' models
	 * @return String
	 */
	public static String getAuthenticatedUserModels(){
		String ourResponse=null;
		try {
			//ourResponse=measurementPoint.getAllUsers().toString();
			ourResponse=measurementPoint.getAllAuthenticatedUserModels().toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	
	/**
	 * Get all authenticated userIds
	 * @return String
	 */
	public static String getAuthenticatedUsers(){
		String ourResponse=null;
		//ourResponse=measurementPoint.getAllUsers().toString();
		ourResponse=measurementPoint.getAuthenticatedUsers().toString();
		return ourResponse;
	}
	/**
	 * Get the User Model of the given userId
	 * @param clientID
	 * @return String 
	 */
	private static String getModelOfKnownID(String userId) {
		String ourResponse=null;
		//ourResponse=measurementPoint.getAllUsers().toString();
		try {
			ourResponse=measurementPoint.getModelOfKnownId(userId).toString(); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	/**
	 * Get all known user IDs until now.
	 * @return String 
	 */

	private static String getAllKnownUsersID() {
		String ourResponse=null;
		ourResponse=measurementPoint.getAllUsersId().toString();
		return ourResponse;
	}
	/**
	 * Get all registered User Models 
	 * @return String
	 * FIXME: sending all users' models over the sockets can result in the socket exception.
	 */
	private static String getAllKnownUsersModels() {
		String ourResponse=null;
		try {
			ourResponse=measurementPoint.getAllUsersModel().toString();
			System.out.println(ourResponse); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	/**
	 * Get the count of all user Ids
	 * @return String
	 *  
	 */
	private static String getNumberOfKnownUsersID() {
		String ourResponse=null; 
		try {
			ourResponse= measurementPoint.getNumberOfKnownUsers().toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	/**
	 * Get the required Data by giving the required Parameter for all users.
	 * @param requiredParameter
	 * @return String
	 */
	//get a information for all users according to given parameter
	private static String getRequiredInfoForAllUser(String requiredParameter) {
		
		//get all users
		JSONArray allUsersId=measurementPoint.getAllUsersId();
		//JSON array 
		JSONArray allUsers= new JSONArray();
		JSONObject specUser= new JSONObject();
		
		for(int i=0;i<allUsersId.length();i++){
			try {
				specUser=new JSONObject(getRequiredInfoForKnownID(allUsersId.get(i).toString(),requiredParameter));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allUsers.put(specUser);
		}
		return allUsers.toString();
		
	}

	/**
	 * Get the required Data by giving the required Parameter for all authenticated users.
	 * @param requiredParameter
	 * @return String
	 */
	private static String getRequiredInfoForAllAuthenticatedUsers(String requiredParameter) {
		//users
		JSONArray authenticatedUsers=measurementPoint.getAuthenticatedUsers();
		JSONArray allAuthenticatedUser= new JSONArray();
		JSONObject specUser= new JSONObject();
		for(int i=0;i<authenticatedUsers.length();i++){
			try {
				specUser=new JSONObject(getRequiredInfoForKnownID(authenticatedUsers.get(i).toString(),requiredParameter));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allAuthenticatedUser.put(specUser);
		}
		return allAuthenticatedUser.toString();
	}
	
	/**
	 * Get the required Data by giving the required Parameter for a specific user Id
	 * @param userId
	 * @param requiredParameter
	 * @return String
	 */
	private static String getRequiredInfoForKnownID(String userId, String requiredParameter) {
		String ourResponse=null; 
		try {
			ourResponse= measurementPoint.getAnyRequiredInfoForUser(userId,requiredParameter).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	
	/**
	 * Get QoE Measurements for a known user Id
	 * @param userId
	 * @return String 
	 */
	private static String getQoEResultsForKnownID(String userId){
		
		String ourResponse=null;
		try {
			ourResponse=qoETracker.getQoEForKnownUserID(userId).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ourResponse;
	}
	/**
	 * 
	 */
	/*private static String getQoEResultsForAuthenticatedUser(){
		String ourResponse=null;
		ourResponse=qoETracker.getQoEsForAuthenticatedUsers().toString();
		return ourResponse;
	}
	
	private static String getQoEResultsForAllUser(){
		String ourResponse=null;
		ourResponse=qoETracker.getQoEsForAllUsers().toString();
		return ourResponse;
	}*/
	/**
	 * Get the required QoE Measurements for all users according to the given parameter.
	 * @param requiredParameter
	 * @return String 
	 */
	protected String getRequiredQoEValueForAllUser(String requiredParameter){
		
		//get all users
		JSONArray allUsersId=measurementPoint.getAllUsersId();
		//JSON array 
		JSONArray allUsers= new JSONArray();
		JSONObject specUser= new JSONObject();
		
		for(int i=0;i<allUsersId.length();i++){
			try {
				specUser=new JSONObject(getRequiredInfoForKnownID(allUsersId.get(i).toString(),requiredParameter));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allUsers.put(specUser);
		}
		return allUsers.toString();
	}
	/**
	 *  Get the required QoE Value for the authenticated users according to the given parameter.
	 * @param requiredParameter
	 * @return String 
	 */
	protected String getRequiredQoEValueForAuthenticatedUser(String requiredParameter){
		JSONArray authenticatedUsers=measurementPoint.getAuthenticatedUsers();
		JSONArray allAuthenticatedUser= new JSONArray();
		JSONObject specUser= new JSONObject();
		for(int i=0;i<authenticatedUsers.length();i++){
			try {
				specUser=new JSONObject(getRequiredQoEValueForKnownUserID(authenticatedUsers.get(i).toString(),requiredParameter));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allAuthenticatedUser.put(specUser);
		}
		return allAuthenticatedUser.toString();
	}
	/**
	 * Get the required QoE Value for the known User Id according to the given parameter.
	 * @param userId
	 * @param requiredParameter
	 * @return String
	 */
	protected String getRequiredQoEValueForKnownUserID(String userId,String requiredParameter){
		String ourResponse=null; 
		ourResponse= qoETracker.getAnyRequiredOoEValueForKnownUserID(userId, requiredParameter).toString();
		return ourResponse;
	}
	/**
	 * Get IP address of the localhost
	 * @return String
	 */
	public static String getIpAddress() {
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    return addr.getHostAddress(); 
		} catch (UnknownHostException e) {
			
		}
		return null;
	}
	
}
