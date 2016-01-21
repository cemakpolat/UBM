package observationPackage;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import communicationPlatform.JavaToJavaClient;
import communicationPlatform.JavaToJavaServer;


import utils.CommunicationLanguage;
import utils.ConsoleMessageInterpreter;

import json.JSONException;
import json.JSONObject;
/**
 * This class is implemented as an example so as to show how to interact and communicate with MaasurementPlane.
 * It is possible to enrich the CommunicationLanguage and the corresponding functions in the MeasurementPlane
 * in case of a specific request. 
 *  
 * 
 * 
 * @author Cem Akpolat & Mursel Yildiz
 */ 
public class ExampleObserver {
	public static int serverPort=14132;
	public static int clientPort=14131; 
	public static JavaToJavaServer MessageServer=new JavaToJavaServer(serverPort);;
	public static JavaToJavaClient mainClient = new JavaToJavaClient(clientPort);
	public static String ipAddress; 
	public static void main(String[] args) throws IOException {
	
		/*
		 * ConsoleMessageInterpreter intpret=new ConsoleMessageInterpreter(args);
		 * if(!intpret.exitSignal){ // clientPort=intpret.clientPortNumber;
		 * if(intpret.serverPortNumber!=0){ serverPort=intpret.serverPortNumber;
		 * } MessageServer = new JavaToJavaServer(serverPort); }
		 * else{System.exit(0);}
		 */
		
		/*
		 *Once this thread starts, it will show the option list for observer, then 
		 *check the selected option by user. If the option value is properly written, 
		 *it will send the desired request to MeasurementPlane. Finally the response sent by MeasurementPlane
		 *will be print out on the console.
		 */
		Thread serverThread = new Thread (new Runnable() {
	           @Override
			public void run() {
	        	   CommunicationLanguage requests= new CommunicationLanguage();
	       			requests.showListContent();
	        	   while(true){
	        			//Get the options to be selected
	        			Scanner scanner = new Scanner(System.in);
	        			//receive user request from Console
	        			System.out.println("\n");
	        			System.out.println("REQUEST");
	        			
	        			System.out.print("Please, select an option above: ");
	    				String option = scanner.nextLine();
	    				JSONObject messageJSON = new JSONObject();

	    				//New Options for closing JVM
	    				if(option.equalsIgnoreCase("exit")){
	    					System.exit(0);
	    				}
	    				
	    				//check option and fetch selected option number.
	    				int  opInt=checkOption(option);
	    				
	        			try {
	        				//transmit firstly server socket number
	        				messageJSON.put("serverSocketNumber",Integer.toString(serverPort));
	        				//add selected options
	        				messageJSON.put("option", Integer.toString(opInt));
						
							//user specific Requests
							
							if((opInt>CommunicationLanguage.generalCommands.size()-1) && 
									(opInt<CommunicationLanguage.generalCommands.size()+CommunicationLanguage.userSpecificCommands.size()))
							{
								System.out.println("\n");
								System.out.println("----- All Authenticated  Users----");
								mainClient.send("0", ipAddress);//for listing authenticated users
			        			String message = MessageServer.getMessage();
			        			System.out.println(message); 
			        			System.out.println("\n");
			        			System.out.println("If you would like to see also unauthenticated users\n, please utilize 'General Options'");
								
			        			
			        			System.out.println("Enter the user ID:");
								String userId = scanner.nextLine();
								messageJSON.put("userId", userId);//For each user specific request, userId ought to be provided.
							}
							
							
							long QoSValue=mainClient.send(messageJSON.toString(), ipAddress);
			        		String message = MessageServer.getMessage();
							JSONObject jmessage=new JSONObject(message);
			        		System.out.println("\nRESPONSE:\n");
			        			//System.out.println(message);
			        		System.out.println(jmessage.get("receivedMessage"));
			        			//We need here a message interpreter
			        			//2 Types message can come either JSONArray or JSONObject
		        			} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        			requests.showListContent();
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
	 * Check the inserted option value whether the selected number 
	 * is localized in the current option list or not. 
	 * @param option
	 * @return integer
	 */
	public static int checkOption(String option){
		int opInt=999999;
		int totalCommandsCount=0;
		try {
			    opInt = Integer.parseInt(option);
			    totalCommandsCount=CommunicationLanguage.generalCommands.size()+CommunicationLanguage.userSpecificCommands.size();
			    if(opInt>(totalCommandsCount-1)){
			    	System.out.println("Selected number is not a member of the offered list!");
			}
		}
		catch(NumberFormatException nFE) {
			    System.out.println("Not an Integer, please give a number in the list between 0 and "+(totalCommandsCount-1)+"");
		}
		return opInt;
	}
	
	//list options
	/**
	 * Show the options provided by CommunicationLanguage
	 */
	public static void showRequestsMenu(){
		CommunicationLanguage requests= new CommunicationLanguage();
		requests.showListContent();
	}
	//return IP Address of host
	/**
	 * Get Ip address of the local host
	 * @return String 
	 */
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
//Communication Language was implemented to define the communication rules between MeasurementPoint or Observer
//In other words, the below defined rules are recognized both of them.
//


