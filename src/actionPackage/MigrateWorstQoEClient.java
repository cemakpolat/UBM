package actionPackage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import communicationPlatform.JavaToJavaClient;
import communicationPlatform.JavaToJavaServer;

import utils.BlackBoard;
import utils.CommunicationLanguage;
import utils.Definitions;




import json.JSONException;
import json.JSONObject;


// Note that this action can be only performed on the same local host with the measurement Plane

public class MigrateWorstQoEClient {

	/**
	 * @param args
	 */
	public static String apSSID = "wpaclient_ufit-left.conf";
	public static String otherSSID = "wpaclient_ufit-right.conf";
	
	public static int serverPort=Definitions.ActionServerPortNumber;
	public static int measurementPointClientPort=Definitions.ActionClientPortNumber;
	public static int clientPortOfUser=13131;
	public static JavaToJavaServer mainServer = new JavaToJavaServer(serverPort);
	public static JavaToJavaClient mainClient=new JavaToJavaClient(measurementPointClientPort);
	public static String className="MigrateWorstQoEClient";
	
	public static String ipAddress; 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BlackBoard.writeConsole(className, "Action Started");
	}
	
	public static void migrateWorstCongestedClient(){
		JSONObject messageJSON = new JSONObject();
		JSONObject incomingMessage=null;
		ipAddress = getIpAddress(); 
		try {
			messageJSON.put("serverSocketNumber",""+ serverPort+"");
			messageJSON.put("option",""+CommunicationLanguage.GetWorstCongestedUser+"");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long QoSValue=mainClient.send(messageJSON.toString(), ipAddress);
		String userIP="0.0.0.0";
		try {
			String message = mainServer.getMessageJSON();
			incomingMessage = new JSONObject(message);						
			JSONObject obj=incomingMessage.getJSONObject("receivedMessage");
			userIP=obj.getString("congestedUserIP");
			BlackBoard.writeConsole(className, "congested User : "+userIP);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//send Command user so as to force client to change AP
		JavaToJavaClient client=new JavaToJavaClient(clientPortOfUser);
		String command = otherSSID;
		client.send(command, userIP);
	}
	
	public static void sleep(int duration){
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// send Message through socket
	public static void sendMessage(String response) {

		long QoSValue = mainClient.send(response, ipAddress);
		BlackBoard.writeConsole(className,"Message sent by MeasurementPoint, response \n"
				+ response);
		// long QoSValue = mainClient.send(response, ipAddress);
	}

	// return IP Address of host
	public static String getIpAddress() {
		// TODO Auto-generated method stub
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {

		}
		return null;
	}

	
}
