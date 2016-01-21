package communicationPlatform;

import java.io.*;
import java.net.*;

import utils.BlackBoard;
import utils.Definitions;

import json.JSONException;
import json.JSONObject;
import json.XML;

/**
 * This class acts as a Server socket and listens its clients. The aim of
 * creating this is to be able to receive messages and network packets from the
 * clients.On the other hand Server class acts as a provider for the other
 * classes. Once it receives packets, it offers them to be employed by other
 * classes. The usage area of this class is for the packets receiving from conntrack-tools and 
 * hostapd.
 * @see http://conntrack-tools.netfilter.org/
 * @see http://hostap.epitest.fi/hostapd/
 * @author Cem Akpolat & Mursel Yildiz
 * */

public class Server {
	public static String className;
	/*debugging output*/
	boolean VERBOSE = false; // turn on/off debugging output
	/*Buffer Size*/
	static int BUFFSIZE = 128000; 
	static int DOUBLE_SIZE = 8;
	static int INT_SIZE = 4;
	byte buff[];
	byte data[];
	int port;

	ServerSocket server;
	Socket sock;
	BufferedInputStream input;
	BufferedOutputStream output;

	/**
	 * Contructor is responsible to open a new Server socket.
	 * 
	 * @param p
	 *            Port number
	 * */
	public Server(int p) throws IOException {
		className=this.getClass().getName();
		port = p;

		try {
			server = new ServerSocket(port,100);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// amortize the buffer allocation
		buff = new byte[BUFFSIZE];
	}

	//
	/**
	 * Wait for somebody to connect on opened socket
	 * */
	public void connect() throws IOException {
		sock = server.accept();

		if (VERBOSE)
			System.out.println("Server: opening socket to "
					+ sock.getInetAddress().getHostName() + " on port " + port);

		input = new BufferedInputStream(sock.getInputStream(), BUFFSIZE);
		output = new BufferedOutputStream(sock.getOutputStream(), BUFFSIZE);

		// now wait for test
		byte ack[] = new byte[1];
		// if (VERBOSE)
		// //System.out.println("Waiting for test A...");
		input.read(ack);
		// if (VERBOSE)
		// //System.out.println("Test A recieved: " + ack[0]);
		// //System.out.println("\n");

	}

	/**
	 * Receive packet size from the client. Before sending the original packet,
	 * user ought to transmit the size of the related packet in order to
	 * allocate a resource for coming packet.
	 * 
	 * @param val
	 *            ,maxlen
	 * @return val[0] total character count for the next coming packet
	 * */
	public int recv_PacketSize(int[] val, int maxlen) throws IOException {

		int i;
		int numbytes;
		int totalbytes = 0;
		byte data[] = new byte[maxlen * INT_SIZE];
		/*
		 * for performance, we need to receive data as an array of bytes and
		 * then convert to an array of doubles
		 */

		//if (maxlen * 8 > BUFFSIZE)
			//System.out.println("Sending more doubles then will fit in buffer!");

		while (totalbytes < maxlen * INT_SIZE) {
			numbytes = input.read(data);
			// copy the bytes into the result buffer
			for (i = totalbytes; i < totalbytes + numbytes; i++)
				buff[i] = data[i - totalbytes];
			totalbytes += numbytes;
		}

		// now we must convert the array of bytes to an array of doubles
		ByteArrayInputStream bytestream_rev;
		DataInputStream instream_rev;
		byte flip_array[] = new byte[totalbytes];
		int j;

		for (i = 0; i < totalbytes / INT_SIZE; i++) {
			for (j = 0; j < INT_SIZE; j++) {
				flip_array[(i + 1) * INT_SIZE - j - 1] = buff[i * INT_SIZE + j];
			}

		}

		bytestream_rev = new ByteArrayInputStream(flip_array);
		instream_rev = new DataInputStream(bytestream_rev);

		for (i = 0; i < maxlen; i++)
			val[i] = instream_rev.readInt();

		// if (VERBOSE) {
		// //System.out.print("Server: received " + maxlen + " doubles: ");
		// for (i = 0; i < maxlen; i++)
		// //System.out.print(val[i] + " hey:" + i);
		// //System.out.println("");
		// }

		return val[0];

	}

	/**
	 * Once a packet received from the clients, as a response to it an "OK"
	 * packet is sent with the aim of informing the client that server received
	 * the concerning packet. This function is not activated for a while.
	 * */
	public void send_OKForPacketSize() throws IOException {
		String stringToConvert = "OK";

		byte[] theByteArray = stringToConvert.getBytes();
		for (byte o : theByteArray) {
			output.write(o);
		}
		// //System.out.println("The byte Array lenght:" + theByteArray.length);
		output.flush();
	}

	/**
	 * Once Server receives packet, it converts the char packet to JSONObject.
	 * 
	 * @param val
	 *            [](incoming packet in the form of character), maxlen
	 * @return jobject JSONObject
	 * */
	public JSONObject recv_packet(char val[], int maxlen) throws IOException {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i;
		int numbytes;
		int totalbytes = 0;
		byte data[] = new byte[maxlen];
		/*
		 * for performance, we need to receive data as an array of bytes and
		 * then convert to an array of doubles
		 */

		//if (maxlen * 1 > BUFFSIZE)
			//System.out.println("Sending more doubles then will fit in buffer!");

		while (totalbytes < maxlen) {
			numbytes = input.read(data);
			// copy the bytes into the result buffer
			for (i = totalbytes; i < totalbytes + numbytes; i++)
				buff[i] = data[i - totalbytes];
			totalbytes += numbytes;
		}

		// now we must convert the array of bytes to an array of char
		ByteArrayInputStream bytestream_rev;
		DataInputStream instream_rev;
		byte flip_array[] = new byte[totalbytes];
		int j;

		for (i = 0; i < totalbytes; i++) {
			for (j = 0; j < 1; j++) {
				flip_array[i - j] = buff[i + j];
			}

		}

		bytestream_rev = new ByteArrayInputStream(data);
		instream_rev = new DataInputStream(bytestream_rev);

		for (i = 0; i < maxlen; i++)
			val[i] = (char) instream_rev.readByte();

//		 if (VERBOSE) {
//			 System.out.print("Server: received " + maxlen + " doubles: ");
//			for (i = 0; i < maxlen; i++)
//				System.out.print(val[i]);
//			 	System.out.println("");
//		 	}
		
		JSONObject jobject = null;
		String r = new String(val);
		if (this.port == Definitions.AssoAuthThreadServerPortNumber) {

			BlackBoard.writeConsole(className, "======!NEW PACKET RECEIVED FROM HOSTAPD!======");
			try {
				jobject = new JSONObject(r);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			BlackBoard.writeConsole(className, "======!NEW PACKET RECEIVED FROM CONNTRACK!======");
			try {
				jobject = XML.toJSONObject(r);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jobject;
	}

	/**
	 * Shutdown the socket
	 * */
	public void closesocket() throws IOException {
		output.close();
		input.close();
		sock.close();
		if (VERBOSE)
			System.out.println("Server: closing socket");
	}

}
