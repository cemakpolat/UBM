package userProfiling;
/**
 *	Once the user is camped on the Access Point, a new GlobalUserThreadListObject for the related user will 
 *be started. New initiated object encompass thread id,user's MAC address, thread object which
 *enables to start user's monitoring and to check thread state whether its finalized or not.
 
 *@author Cem Akpolat & Mursel Yildiz
 */
public class GlobalUserThreadListObject {
	/*identification number of generated thread*/
	public long threadId;
	/*show whether thread finished or not*/
	boolean finish;
	/*thread object started by authenticated user*/
	Thread thread;
	/*authenticated user's MAC address*/
	String mac;
	public GlobalUserThreadListObject(String userMac,long threadId,boolean finish,Thread thread) {
		this.mac=userMac;
		this.threadId=threadId;
		this.finish=finish;
		this.thread=thread;
	}
}
