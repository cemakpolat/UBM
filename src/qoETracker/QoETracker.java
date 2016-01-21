package qoETracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import databaseConnection.DAO;
import userProfiling.UPMain;
import utils.BlackBoard;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
/***
 * QoE Tracker is responsible for collecting QoE measurements from the clients. 
 * This class provides measurements including Average Delay times, Jitter, etc.   
 * 
 * @see http://www.grid.unina.it/software/ITG/ 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class QoETracker  {
	
	UPMain upmain;
	/*QoE List for holding user experiments obtained from user side*/
	public static List<QoEListObject> userQoEJSONList;
	
	private static QoETracker instance = new QoETracker();
	public QoETracker(){
		upmain=UPMain.getInstance();
	}
	
	public static QoETracker getInstance(){
		return instance;
	}
	/***
	 * Create quality of experiment list in order to hold user's experiments
	 * Start a thread which is in the charge of running ITG
	 * Start QoETracker in order to obtain the experiments from the authenticated users.
	 */
	public void main(){
		//create List
		userQoEJSONList = Collections.synchronizedList(new ArrayList<QoEListObject>());
		
		//start ITG
		Thread thread = new Thread(new Runnable(){
			public void run(){
				new ITGChecker().main();		
			}
		});
		thread.start();
		
		//run thread for collecting experience results from users
		Runnable qoeTracker = new QoETrackerThread();
		Thread threadQoeCollector = new Thread(qoeTracker);
		threadQoeCollector.start();
		
		// Create one directory for test results		
    	String strDirectoy ="QoExperience";
		boolean success = (new File(strDirectoy)).mkdirs();
		if (success) {
			  System.out.println("Directory: " + strDirectoy + " created");
		}
	}		



	/***
	 * Get MAC addresses of the current authenticated users. 
	 * @return
	 */
	public JSONArray getCurrentUserMACs(){
		JSONArray MACListArray=new JSONArray();
		
		synchronized (userQoEJSONList) {
			for(QoEListObject qoeResult: userQoEJSONList){
			/*  JSONObject obj=new JSONObject();
				try {
					obj.put(qoeResult.userId,qoeResult.MAC);
				} catch ( e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MACListArray.put(obj);
			*/				 
				MACListArray.put(qoeResult.MAC);
			} 
		}
		return MACListArray;
	}
	

	/***
	 * Get the critical Average Delay of the current users.
	 * @return
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public int averageDelay() throws NumberFormatException, JSONException{
		//double averageDelay= Definitions.averageDelayTimeThreshold;//milisecond
		//Hashtable<String, String> table= new Hashtable<String, String>();
		//table.put("averageDelay", "20000");
		double maximumAverageDelay = 0;
		synchronized (userQoEJSONList) {
			if ((userQoEJSONList.size() > 8) ) {
				for(int i=userQoEJSONList.size()-1;i>=(userQoEJSONList.size()-8);i--){
					JSONObject obj=userQoEJSONList.get(i).qoEMeasurement;
					if(!obj.toString().equalsIgnoreCase("{}")){
						JSONObject obj2=obj.getJSONArray("Flow Result").getJSONObject(0);
						double temp = Double.parseDouble(obj2.getString("Average Delay"))*1000;
						BlackBoard.writeConsole(this.getClass().getName(),""+temp+"");
						if( temp > maximumAverageDelay){
							BlackBoard.writeConsole(this.getClass().getName(),"Congestion Occured" ); 
							maximumAverageDelay = temp;
						}
					}	
					
				}
			}
			
		}
		int tempInt = (int) maximumAverageDelay; 
		return tempInt;
	} 

	/***
	 * Detect the congested user/users
	 * @return
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public JSONObject firstCongestedUser() throws NumberFormatException, JSONException{
		double averageDelay=20;
		JSONObject congestedUser=new JSONObject();
		synchronized (userQoEJSONList) {
			for(int i=userQoEJSONList.size()-1;i>=0;i--){
				JSONObject obj=userQoEJSONList.get(i).qoEMeasurement;
				if(!obj.toString().equalsIgnoreCase("{}")){
					JSONObject obj2=obj.getJSONArray("Flow Result").getJSONObject(0);
					if( (Double.parseDouble(obj2.getString("Average Delay"))*1000) > averageDelay){
						return congestedUser.put("congestedUserIP", userQoEJSONList.get(i).IP);//send first congested user
					}
				}	
			}
			
		}
		return null;
	}
	/***
	 * Detect the worst congested User
	 * @return
	 * @throws JSONException
	 */
	public JSONObject worstCongestedUser() throws JSONException{
		double temp=0;
		JSONObject congestedUser=new JSONObject();
		synchronized (userQoEJSONList) {
			int placeOfWorstAvDelay=0;
			for(int i=userQoEJSONList.size()-1;i>=0;i--){
				JSONObject obj=userQoEJSONList.get(i).qoEMeasurement;
				if(!obj.toString().equalsIgnoreCase("{}")){
					JSONObject obj2=obj.getJSONArray("Flow Result").getJSONObject(0);
					if( (Double.parseDouble(obj2.getString("Average Delay"))*1000) > temp){
						temp=Double.parseDouble(obj2.getString("Average Delay"))*1000;
						placeOfWorstAvDelay=i;
					}
				}	
			}
			//send a client IP whose average delay is worst than other
			if ( userQoEJSONList.size() > 0 ){ 
				return congestedUser.put("congestedUserIP", userQoEJSONList.get(placeOfWorstAvDelay).IP);
			}
		}
		return null;
	}

	/***
	 * 
	 * @return
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public JSONObject congestedUsers() throws NumberFormatException, JSONException{
		double averageDelay=200;
		JSONArray congestedUsers=new JSONArray();
		
		synchronized (userQoEJSONList) {
			for(int i=userQoEJSONList.size();i<userQoEJSONList.size();i--){
				JSONObject obj=userQoEJSONList.get(i).qoEMeasurement;
				if(!obj.toString().equalsIgnoreCase("{}")){
					JSONObject obj2=obj.getJSONArray("Flow Result").getJSONObject(0);
					if(Double.parseDouble(obj2.getString("Average Delay"))*1000>averageDelay){
						congestedUsers.put(userQoEJSONList.get(i).IP);//send first congested user
					}
				}	
			}
			
		}
		JSONObject obj= new JSONObject();
		obj.put("congestedUsers",congestedUsers);
		return obj;
	}
	
/*	
	public JSONArray getQoEsForAllUsers(){
		JSONArray arr= new JSONArray();
		DAO db= new DAO();
		ArrayList<String> users=db.getAllUserIDs();//this func is valid for UBM but it could be used
		for(int j=0;j<users.size();j++){
			System.out.println(users.get(j));
			  try {
				arr.put( db.getUserQoEFromFile(users.get(j)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return arr;
	}

	
	public JSONArray getQoEsForAuthenticatedUsers(){
		JSONArray arr= new JSONArray();
		DAO db= new DAO();
		synchronized (UPMain.GlobalUserStateList) {
			int index=0;
			while (index<UPMain.GlobalUserStateList.size()) {
				 try {
					arr.put( db.getUserModelFromFile(UPMain.GlobalUserStateList.get(index).getUserId()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				index++;
			}
		}
		return arr;
	}
*/
	/***
	 * FIXME: NEED TO BE COMPLETED
	 * @param userId
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getQoEForKnownUserID(String userId) throws JSONException{
		JSONObject qoExprience= new JSONObject();
		DAO db= new DAO();
		qoExprience=db.getUserQoEFromFile(userId);
		return qoExprience;
	}
	
	/***
	 * FIXME: NEED TO BE COMPLETED
	 * @param userId
	 * @param requiredParamater
	 * @return
	 */
	public JSONObject getAnyRequiredOoEValueForKnownUserID(String userId,String requiredParamater){
		
		String value=null;
		JSONObject qoExperience= new JSONObject();
		JSONObject paramWillBeSent= new JSONObject();
		DAO db= new DAO();
		return qoExperience;
	}
	
}
