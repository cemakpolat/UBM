package userProfiling;

import java.io.IOException;

import communicationPlatform.Server;


import json.JSONException;
import json.JSONObject;
/**
 * @author Cem Akpolat & Mursel Yildiz
 * */
public class ConntrackRestarter implements Runnable {
	public int PortNumber = 5012;// default port

	// Message Types and values should be defined
	// You should also add some code lines into conntrack.c in order to receive
	// failures
	// new function callErrorConveyor() added to the approproate place.
	/**
	 * */
	public ConntrackRestarter() {

	}

	public static Server ourServer;

	@Override
	/**
	 * */
	public void run() {

		// TODO Auto-generated method stub

		System.out.println("ConntrackThread listens errors...");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				System.out.println("Shutdown hook ran!");
				try {
					ourServer.closesocket();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		try {
			ourServer = new Server(PortNumber);
			ourServer.connect();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.toString());
			e1.printStackTrace();
		}
		while (true) {
			try {
				System.out
						.println("\n\n\n\n\n\n\n Inside Loop Conntrack Restarter \n\n\n\n\n\n\n\n");
				JSONObject jsonObject = this.getNewPacket();

				if (jsonObject.getJSONObject("error").get("type").toString()
						.equalsIgnoreCase("RESTART")) {
					System.out.println("Conntrack should be started again");
					// Thread.sleep(3000);
					restartConntrack();

				}
				ourServer.closesocket();
				ourServer.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// messages types and functions
	/**
	 * */
	public boolean messageControlCenter() {
		boolean state = false;

		return state;
	}
	/**
	 * */
	private void restartConntrack() throws IOException {
		System.out.println("Conntrack restarting");

		// Runtime.getRuntime().exec(new String[] { "killall", "conntrack" });
		 Runtime.getRuntime().exec(
		 new String[] { "./conntrack-tools-1.0.0/src/conntrack", "-E","-o", "xml" });

		//Runtime.getRuntime().exec(new String[] {"./../../autonomous/conntrack-tools-1.0.0/src/conntrack",
		//				"-E ", "-o", "xml" });
		System.out.println("Conntrack restarted");
	}
	/**
	 * */
	public JSONObject getNewPacket() throws IOException {
		System.out.println("Message received from conntrack");
		JSONObject obj = new JSONObject();
		int[] value = new int[1];
		int packetSize = ourServer.recv_PacketSize(value, 1);
		char[] packet = new char[packetSize];
		System.out.println("packetsize:" + packet.length);
		// json object could be a problem here
		obj = ourServer.recv_packet(packet, packet.length);

		return obj;
	}
}
