package qoETracker;


import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import communicationPlatform.JavaToJavaServer;
import communicationPlatform.JavaToJavaServerTimeOut;

import databaseConnection.DAO;

import userProfiling.GlobalUserStateListObject;
import userProfiling.UPMain;
import utils.BlackBoard;
import utils.Definitions;


import json.JSONException;
import json.JSONObject;
/***
 * QoETracker Thread is in charge of collecting user experiments in a small sized buffer which 
 * is accomplished  by DITG Tool (on the Client side) with the conjunction of ITGChecker Class (on the server side). 
 * Apart from putting transmitted data over sockets in a buffer, these experiments are also 
 * separately stored in files according to the user identifier. 
 *
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class QoETrackerThread implements Runnable {
	/**/
	UPMain upmain;
	/*QoE Tracker starter*/
	QoETracker qoeTracker;
	/*IP address of the related user*/
	public String userIPAddress;
	/*MAC Address of the related user*/
	public String userMac;
	/*Server Socket connector*/
	public JavaToJavaServerTimeOut ourServer;
	/**/
	public JSONObject jsonObject;
	/*the count of the element in the buffer*/
	public int bufferingNumber = 0 ; 
	/*last Quality of Experience Time which has been taken*/
	public long lastQoETime = 0; 
	/**/
	public String className;
	/***
	 * Constructor
	 */
	public QoETrackerThread(){
		upmain = UPMain.getInstance();
		qoeTracker=QoETracker.getInstance();
		className=this.getClass().getName();
		BlackBoard.writeConsole(className, "QoETracker initiated");
		
	}
	
	/***
	 * Receive DITG result from the authenticated users, store them in a list and also in a file
	 * specific to the user.
	 */
	@Override
	public void run() {
		BlackBoard.writeConsole(className, "QoETracker is started");
		// TODO Auto-generated method stub it seems needless
		Runtime.getRuntime().addShutdownHook(new Thread() {
			/***
			 * Shutdown hook is invoked in case of unsolicited interruption
			 * in order to manage the last tasks to do before leaving totally the system.
			 */
			@Override
			public void run() {
				System.out.println("Shutdown hook ran!");
				try {
					ourServer.serverSocket.close();
					storeInformationOfQoE();//in case of occurring interruption
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BlackBoard.writeConsole(className,"All required information is stored due to the unintented interruption!");
			}

		});
		
		BlackBoard.writeConsole(className, "run hook time initiated");
		Thread QoEShifterThread = new Thread (new Runnable() {
			/***
			 * In case of any data received from users for 20 seconds, it supposed that
			 * there is any experiment that has been taken, as a result, QoEList shifts to left and 
			 * current place is left blank.
			 * This thread checks every 20 minutes whether any experiment during 20 seconds.
			 * Once an experiment is carried our, its timer set to 0. 
			 */
 	       	@Override
			public void run() {
 	       		try {
 	       			while(true){		
 	       				Thread.sleep(Definitions.qoEExperienceShiftTime);
 	       				long currentTime = System.currentTimeMillis(); 
 	       				if ( ( currentTime - lastQoETime ) > 19000 ){
 	       				BlackBoard.writeConsole(className,"Shifting to the left, no QoE for 20 seconds ");
 	       					shiftQoEFromClientsToLeft("","","","",new JSONObject("{}"));
 	       				}
 	       			}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} 
    		}
	    }
		);
	    try{
	    	QoEShifterThread.start(); 
	   	}catch(Exception e){ 			
	   		QoEShifterThread.run(); 
	    }
	    BlackBoard.writeConsole(className, "Shifter initiated");
	    try {
	    	ourServer = new JavaToJavaServerTimeOut(Definitions.QoEServerPortNumber);
	    	BlackBoard.writeConsole(this.getClass().getName(), "QoE Thread socket accepts connections");
			ourServer.serverSocket.accept();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}
	    /*
	     * Socket is opened and wait with the aim of receiving user's experiments.
	     * */
		while (true) {// we do not need to get always QoE data
			try {
				//accept connection request
				//Socket socket=ourServer.serverSocket.accept();
				BlackBoard.writeConsole(this.getClass().getName(),"While loop started");
				if(getAuthenticatedUserCount()!=0){
				// start DITG Receive
					//jsonObject = getQoEMeasurementFromClients();
					String receivedMessage = getQoEMeasurementFromClientsInString();
					if (receivedMessage.equalsIgnoreCase("timeout")) {
						BlackBoard.writeConsole(this.getClass().getName(),"Time out in collecting userQoEs ");
					}
					else{
						JSONObject obj = new JSONObject(receivedMessage);
						this.userIPAddress=obj.get("ipClient").toString();			
						jsonObject = obj.getJSONObject("receivedMessage");
				
						if (jsonObject != null) {
							BlackBoard.writeConsole(this.getClass().getName(),"USERIPADDRESS: "+userIPAddress);
							if (checkUserAuthenticated(userIPAddress) == true) {
								
								String userId = getUserName();
								BlackBoard.writeConsole(this.getClass().getName(), ""+userId+" is still authenticated");
								shiftQoEFromClientsToLeft(userId, userIPAddress,userMac,
										getCurrentTime("yyyy-MM-dd HH:mm:ss.S"),
										jsonObject);
								lastQoETime = System.currentTimeMillis(); 
							}
						} else {
							// wait few seconds for receiving packet
							randomBackOfftime();
							BlackBoard.writeConsole(className, "False Packet and waiting for RBT");
						}
					}
				}else{
					randomBackOfftime();
					BlackBoard.writeConsole(className, "Authenticated user number is 0 and waiting for RBT");
				}
				//socket.close();
				/*ourServer.serverSocket.close();
				ourServer = new JavaToJavaServerTimeOut(Definitions.QoEServerPortNumber);
		    	BlackBoard.writeConsole(className, "QoE Thread socket accepts connections");
				ourServer.serverSocket.accept(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);*/
			}  catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}

		}
	}
	
	/***
	 * Wait randomly few seconds 
	 */
	protected  void randomBackOfftime(){
		 Random randomNumberGenerator = new Random();
	     int randomInstance = randomNumberGenerator.nextInt(5);
	     randomInstance++; // randomInstance is between 1 and 6 seconds
	     BlackBoard.writeConsole(this.getClass().getName(), " in randombackoff time state: " + randomInstance);
	     try {
	         Thread.sleep(randomInstance * 1000);
	     } catch (InterruptedException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	     }
	}
	/***
	 * Get the current authenticated user count.
	 * @return integer
	 */
	public int getAuthenticatedUserCount(){
		int count=0;
		synchronized (UPMain.GlobalUserStateList) {
			count=UPMain.GlobalUserStateList.size();
		}
		return count;
	}
	
	/***
	 * Get the transmitted user's QoE experiment from server socket.
	 * @return receivedMessage in JSON Object
	 * @throws IOException
	 * @throws JSONException
	 */
	
	public JSONObject getQoEMeasurementFromClients() throws IOException, JSONException{
			String receivedMessage = ourServer.getMessageJSON(); 
			JSONObject obj = new JSONObject(receivedMessage);
			this.userIPAddress=obj.get("ipClient").toString();			
			return obj.getJSONObject("receivedMessage");
	}
	/***
	 * Get the transmitted user's QoE experiment from server socket.
	 * @return received Message in String
	 */
	public String getQoEMeasurementFromClientsInString() {
		String receivedMessage = ourServer.getMessageJSON(); 			
		return receivedMessage;
	}
	//shifting

	/***
	 * 
	 * Put QoE measurements and some crucial user data in an ArrayList 
	 * @param userId
	 * @param iP
	 * @param userMAC
	 * @param time
	 * @param jobj
	 */
	public void shiftQoEFromClientsToLeft(String userId,String iP,String userMAC,String time,JSONObject jobj){
		System.out.println("JSONPACKET \n" + jobj.toString());
		QoEListObject qoeObj= new QoEListObject(userId,iP,userMAC,time, jobj);
		synchronized (QoETracker.userQoEJSONList) {
			if(QoETracker.userQoEJSONList.size()<50){
				QoETracker.userQoEJSONList.add(qoeObj);
			}else{
				QoETracker.userQoEJSONList.remove(0);
				QoETracker.userQoEJSONList.add(qoeObj);
			}
			bufferingNumber = bufferingNumber + 1;
			if ( bufferingNumber == 50 ){ 
				bufferingNumber = 0; 
				storeInformationOfQoE(); 
			}
		}
		
	}
	/***
	 * Store client's QoE in file with the help of DAO
	 */
	public void storeInformationOfQoE(){
		DAO dao= new DAO();
		ArrayList<QoEListObject> list=new ArrayList<QoEListObject>();
		synchronized (QoETracker.userQoEJSONList) {
			for(QoEListObject item:QoETracker.userQoEJSONList){
				list.add(item);
			}
		}
		for(QoEListObject item:list){
			if(!item.qoEMeasurement.toString().equalsIgnoreCase("{}")){
				dao.storeQoEInfoInFile(item.Time,item.qoEMeasurement,item.userId);	
			}
		}
		
	}
	

	/***
	 * Get UserId by using userMAC address
	 * @return string value
	 */
	public String getUserName(){
		synchronized (UPMain.GlobalUserStateList) {
			for (GlobalUserStateListObject item : UPMain.GlobalUserStateList) {
				if (item.getMAC().equalsIgnoreCase(userMac)) {
					//userId = item.getUserId();
					return item.getUserId();
				}
			}
		}
		//System.out.println("gotten userID: " + userId);
		return "";
	}
	
	/**
	 * Check user whether is authenticated or not.
	 * 
	 * @param IP
	 * @return true or false
	 * */
	public   boolean checkUserAuthenticated(String IP) {
		boolean state = false;
		synchronized (UPMain.GlobalIPMACMatchList) {
			int index=0;																	
			while (index<UPMain.GlobalIPMACMatchList.size()) {
				BlackBoard.writeConsole(this.getClass().getName(),UPMain.GlobalIPMACMatchList.get(index).getIP());
				if (UPMain.GlobalIPMACMatchList.get(index).getIP().equalsIgnoreCase(IP)) {
					state = true;
					userMac=UPMain.GlobalIPMACMatchList.get(index).getMAC();
					break;
				}
			index++;
			}
		}
		BlackBoard.writeConsole(this.getClass().getName(),"is user autheticated? =>"+state);
		return state;
	}
	
	/***
	 * Check user existence in QoExperience List, if user in the list this function will return true.
	 * @param userId
	 * @return true/false
	 */
	public boolean checkUserExistence(String userId){
		boolean state=false;
		for(int i=0;i<QoETracker.userQoEJSONList.size();i++){
			if(QoETracker.userQoEJSONList.get(i).userId.equalsIgnoreCase(userId)){
				state=true;
				break;
			}
		}
		return state; 
	}

	/***
	 * Get the current time.
	 * @param dateFormat
	 * @return string 
	 */
	public static String getCurrentTime(String dateFormat) {
		  Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		    return sdf.format(cal.getTime());

	}
	
}

/***
 * Each recceived packet is in the form of JSON and provides a lot of data. Through this class 
 * we eliminate some attributes and select only required parameters, so that we cover all needed
 * data in one object.
 * @author 
 *
 */
class QoEListObject {
	/*User ID*/
	public String userId;
	/*User IP address*/
	public String IP;
	/* The time that the related packet was received.*/
	public String Time;
	/*User QoE packet*/
	public JSONObject qoEMeasurement;
	/*User MAC address*/
	public String MAC;
	QoEListObject(String userId,String iP,String MAC,String time,JSONObject qoe){
		this.userId=userId;
		this.IP=iP;
		this.MAC=MAC;
		this.Time=time;
		this.qoEMeasurement=qoe;
	}
}

