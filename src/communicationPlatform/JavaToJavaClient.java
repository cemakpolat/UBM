package communicationPlatform;


import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
/**
 * Any method desiring to communicate with a server socket and to send data should utilize this class in order to
 * establish a reliable TCP connection with timeout option between server and client. Time out option should not be 
 * perceived as a TCP option, this is an extra option which is utilized in case of a communication failure between
 * server and client such as server can not responses by virtue of the congestion or short term crashing  while client 
 * is attempting to send a message.
 * 
 * @author Cem Akpolat & Mursel Yildiz

 *
 */


public class JavaToJavaClient {

	public String className = "Client: "; 

	private static String serverIpAddress;
	//private boolean connected = false;
	private int  portAddress; 

	/**
	 * Creates a Client for the specified context.
	 * 
	 * @param cont
	 *            The Context of the Client.
	 */
	public JavaToJavaClient(int PORT) {
		portAddress = PORT; 
	}

	/**
	 * Sends a string to the server with a Pop-up to ask the IP address of the
	 * server.
	 * 
	 * @param tosend
	 *            The String to be sent.
	 */
	public long send(final String tosend, final String serverIP) {
		JavaToJavaClient.serverIpAddress = serverIP;
		return serverContact(tosend);
	}


	/**
	 * Sends a String to the server.
	 * 
	 * @param tosend
	 *            The String to be sent.
	 */
	private long serverContact(String tosend) {
		try {
			InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
			Socket socket = new Socket(serverAddr, portAddress ); 
			socket.setSoTimeout(5000);
			long after = 0, before = 0;
			before = System.currentTimeMillis();
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())), true);
			out.println(tosend);
			after = System.currentTimeMillis();
			socket.close();
			System.out.println(className + "client QoS values in RTT (round trip time): " + String.valueOf(after - before));
			return (after - before); 
		} catch (SocketTimeoutException s) {
			return 1; 
		}
		catch (Exception e) {
			System.out.println(className + "Connection error " + e.getMessage());
			return -1; 
		}
	}

}
