package communicationPlatform;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Timer;

import json.JSONException;
import json.JSONObject;
/**
 * Java Server receives request from its clients and responses them in a similar way in a specific time Interval.
 * The only difference between JavaToJavaServer and this class is that this class holds a timeout while waiting
 * a message from client. There are a number of reasons for implementing such a function. The most crucial one is
 * that it is possible to encounter a server connection failure due to the fact that server crashed.
 *  
 * The incoming message format to server is shown below:
 * 
 * {
 * 		"ipClient":"X.X.X.X"
 * 		"receivedMessage": "Message" Remark: Message itself is a string in JSON format.
 * }
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */

public class JavaToJavaServerTimeOut {
	
	/*Port Number of the server*/
	private int port;
	/*The number of the clients which are connected */
	private int numberClients = 0;
	
	private String thisLine;
	/*Server Socket definition*/
	public ServerSocket serverSocket;
	
	public boolean firstLineRead = true; 
	
	public String className = "Server: "; 
	//private JTextArea textarea;

	/**
	 * Once this function called, it opens a TCP connection and wait for a message from java client in
	 * specific time Interval( with timeout attribute).
	 * All messages between client and server or server and the classes communicating with server
	 * are sent in JSON string format. 
	 * @return String in JSON format
	 */
	
	public String getMessageJSON(){
		String receivedMessage=""; 
		String ipOfTheClient=""; 
		try {
			serverSocket.setSoTimeout(10000);
			Socket client = serverSocket.accept();
			numberClients++;
			ipOfTheClient = client.getInetAddress().getHostAddress();
			writeConsole("IP address of this client : " + ipOfTheClient);
			BufferedReader incomingFlux = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			thisLine = incomingFlux.readLine();
			receivedMessage = thisLine + receivedMessage; 
			client.close();
		} 
		catch (SocketTimeoutException s) {
			return "timeout"; 
		}
		catch (IOException e) {
			System.out.println("Error with client number " + numberClients
					+ "\n" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		JSONObject messageObject = new JSONObject(); 
		try {
			messageObject.put("ipClient", ipOfTheClient);
			messageObject.put("receivedMessage", new JSONObject(receivedMessage)); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messageObject.toString(); 
	}
	/**
	 * FIXME: This function should be changed 
	 * Once this function called, it opens a TCP connection and wait for a message from java client in
	 * specific time Interval( with timeout attribute).
	 * All messages between client and server or server and the classes communicating with server
	 * are sent in JSON string format. 
	 * @return String in JSON format
	 */
	public String getMessage(){
		String receivedMessage=""; 
		String ipOfTheClient=""; 
		try {
			serverSocket.setSoTimeout(10000);
			Socket client = serverSocket.accept();
			ipOfTheClient = client.getInetAddress().getHostAddress();
			writeConsole("IP address of this client : " + ipOfTheClient);
			BufferedReader incomingFlux = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			System.out.println("Starting to read the line");
			thisLine = incomingFlux.readLine();
			receivedMessage = receivedMessage + thisLine;
			client.close();
		} 
		catch (SocketTimeoutException s) {
			return "timeout"; 
		}
		catch (IOException e) {
			System.out.println("Error with client number " + numberClients
					+ "\n" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		JSONObject messageObject = new JSONObject(); 
		try {
			messageObject.put("ipClient", ipOfTheClient);
			messageObject.put("receivedMessage",receivedMessage); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messageObject.toString(); 
	}
	
	/**
	 * Java Server Socket initiates by instantiating a new server socket object.
	 * @param portAddress
	 */
	public JavaToJavaServerTimeOut(int portAddress) {
		//this.textarea = textarea;
		port = portAddress; 
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			writeConsole("Error Message from Server :\n" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	};
	
	/**
	 * Adds a String to the JTextArea.
	 * @param towrite The String to add
	 */
	private void writeConsole(String towrite){
		System.out.println(className + towrite); 
	}

	
}
