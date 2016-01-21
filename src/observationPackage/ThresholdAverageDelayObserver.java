package observationPackage;
import java.net.InetAddress;
import java.net.UnknownHostException;

import communicationPlatform.JavaToJavaClient;
import communicationPlatform.JavaToJavaServer;

import utils.BlackBoard;
import utils.CommunicationLanguage;
import utils.Definitions;




import json.JSONException;
import json.JSONObject;

public class ThresholdAverageDelayObserver {

	/**
	 * @param args
	 */
	public static int serverPort=Definitions.ObservationServerPortNumber;
	public static int clientPort=Definitions.ObservationClientPortNumber;
	public static JavaToJavaServer mainServer = new JavaToJavaServer(serverPort);
	public static JavaToJavaClient mainClient=new JavaToJavaClient(clientPort);
	public static String ipAddress; 
	public static int threshold=100;
	public static String className="ThAvDelayObserver";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BlackBoard.writeConsole(className, "Observer Started");
	}
	
	public static boolean areThereCongestedClients(){
		ipAddress = getIpAddress(); 
		JSONObject messageJSON = new JSONObject();
		JSONObject incomingMessage=null;
		try {
			messageJSON.put("serverSocketNumber",""+ serverPort+"");
			messageJSON.put("option",""+CommunicationLanguage.GetQoEAverageDelayStatus+"");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long QoSValue=mainClient.send(messageJSON.toString(), ipAddress);	        		
		try {
			incomingMessage = new JSONObject(mainServer.getMessage());
			String temp = incomingMessage.get("receivedMessage").toString(); 
			BlackBoard.writeConsole(className, "Congested Statu: "+ temp);
			if( temp.contentEquals("true") ){
				return true; 
			}else{
				return false; 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false; 
	}
	public static void sleep(int duration){
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//return IP Address of host
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
