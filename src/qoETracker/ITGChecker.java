package qoETracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import utils.BlackBoard;
import utils.Definitions;

/***
 *  This class is responsible for maintaining the execution of ITGRecv tool. 
 *  For more information about DITG Tool please check the following link.
 *  
 * @see http://www.grid.unina.it/software/ITG/ 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */

public class ITGChecker {


	/**/
	static ProcessBuilder pb;
	/*indicates whether a process continue or not*/
	public static boolean continueHandling = false ; 
	/**/
	static Process pbProcess; 
	/**/
	//static Thread itgReceiverCheck;
	/**/
	static BufferedReader br; 
	/*shows whether ITG started or not*/
	public static boolean itgReceiverStarted; 
	/**/
	public static String className; 
	/*wait time for restarting ITG 180 seconds */
	public int waitTimeForRestartingITG = 180; 
	/***
	 * 
	 */
	public void main() {
		// TODO Auto-generated method stub
		className=this.getClass().getName();	
		continueHandling = true; 
		pb = new ProcessBuilder("./D-ITG-2.7.0-Beta2/bin/ITGRecv");
		pb.redirectErrorStream(true);
		
		while(true){
			try {
				pbProcess = pb.start();
				Thread.sleep(waitTimeForRestartingITG *1000);
				pbProcess.destroy();
				BlackBoard.writeConsole(className, "Restarting the ITG again");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//handleITGRecv(); 
	}
		
	/***
	 * Restart ITG 
	 */
	public  void reStartITGRecv(){
		System.out.println("Restarting the ITGRecv");
		pbProcess.destroy();
		continueHandling = false; 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		continueHandling = true; 
		handleITGRecv(); 
		
	}
	/***
	 * Start ITGRecv and wait for the request of ITGSend. 
	 * In case of receiving Finish Message, test would be thought as successful.
	 */
	public  void handleITGRecv(){
		try {
			pb = new ProcessBuilder("./D-ITG-2.7.0-Beta2/bin/ITGRecv");
			pb.redirectErrorStream(true);
			pbProcess = pb.start();
			br = new BufferedReader(new InputStreamReader(pbProcess.getInputStream())); 
			
			/*Two timer and their tasks are defined 
			 *for listening ITGSend messages which are initiated by Users */
			
			Timer timerFirst= new Timer();
			Timer timerSecond= new Timer();
			
			TimerTask restartTask1,restartTask2 ;
	    			
			while(continueHandling){
				String line = "";
				// schedule the general timer here.
				randomBackOfftime();
				timerFirst.schedule( restartTask1 = new TimerTask() {
					@Override
					public void run(){
						reStartITGRecv();
		    		}
		    	}, Definitions.ITGTimerDuration);
				
				
				while (  ((line = br.readLine()) != null) ) {
					
					BlackBoard.writeConsole(className, line);
					
					if (line.contains("Listen") ) {// a flow is started
						BlackBoard.writeConsole(className, "starting the timer for waiting finished");
						// schedule the smaller timer here 
						timerSecond.schedule(restartTask2 = new TimerTask() {
							@Override
							public void run(){
								reStartITGRecv();
				    		}
				    	}, Definitions.ITGTimerDuration);
						//timerFirst.cancel();
						restartTask1.cancel();

						while (  ((line = br.readLine()) != null)  ) {
							BlackBoard.writeConsole(className, line);
							if ( line.contains("Finish") ){
								BlackBoard.writeConsole(className, line);
								//timerSecond.cancel(); 
								restartTask2.cancel();
								break; 
							}
						}
					}
					else if ( line.contains("Finish") ){
						BlackBoard.writeConsole(className, line);
						//timerFirst.cancel();
						restartTask1.cancel();
						BlackBoard.writeConsole(className, "canceling general timer");
						continue;
					} 
					else if ( line.contains("Press CTRL")) { 
						BlackBoard.writeConsole(className, line);
						//timerFirst.cancel();
						restartTask1.cancel();
						BlackBoard.writeConsole(className, "canceling general timer");
						continue; 
					}
				}
				//System.gc();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} 
		randomBackOfftime();
		BlackBoard.writeConsole(className, "HandleITGRecv started again");
		handleITGRecv(); 
	}
	/***
	 * Wait randomly an amount of time.
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

}
