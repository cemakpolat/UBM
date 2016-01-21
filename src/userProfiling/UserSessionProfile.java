package userProfiling;

import utils.BlackBoard;
import json.JSONException;
import json.JSONObject;


/**
 * The aim of User Session Profile Class is to keep user's data in an object, while his/her
 * data are continuing to be processed. This class offers two possibilities. First, if 
 * the authenticated user visits the concerning AP first time, a generic user profile is assigned
 * to the user. Otherwise user's profile is fetched from file/database and the processing is performed
 * on it. In case User deauthenticated, his/her data is collected and processed on this object,finally
 * this object is stored into the file/database.
 * Simple User Profile is shown above in JSON format. All User Profiling processes is done
 * with the help of JSON objects. 
 * 
 * /*
 * JSONModel Sample For UserProfiling\n\n

	{\n
				"userId":"", \n
				"bandwidthAverage":,\n
				"authenticationNumber":,\n
				"sessionAverageTime":"",\n
				"dailyAuthenticationNumber":,\n
				"dailyVFProbability":"",\n
				"visitingFrequency":"",\n
				"lastDepartureTime":,\n
				"sessionsPerMin":[ {"visitingFrequency":"","bandwidth":},\n
							 	   {"visitingFrequency":"","bandwidth":},\n
														....\n
								 ]\n
	} \n
 * 
 * @author Cem Akpolat & Mursel Yildiz
 */

public class UserSessionProfile {
	
	public String className;
	/*user id*/
	public String userId;
	/*UserBandwidth object contains the bandwidth information of user*/
	public UserBandwidth bandwidth;	
	/*UserSession object includes the session times  */
	public UserSession session;
	/*time Interval for repeated actions*/
	public int timeInterval;//ten minutes //Time Interval -<User Specific 

	/**
	 * Constructor initiates bandwidth and session objects
	 * */
	UserSessionProfile(){
		className=this.getClass().getName();
		bandwidth=new UserBandwidth();
		session=new UserSession();
		
	} 
	/***
	 * Initiation of bandwidth and session objects and getting user information 
	 * if the given user Model is not null.
	 * @param userModel :
	 * @param userId
	 * @param timeInterval
	 */
	UserSessionProfile(JSONObject userModel,String userId,long timeInterval){
		bandwidth=new UserBandwidth();
		session=new UserSession();
		this.userId=userId;
		this.timeInterval=(int)timeInterval/(1000*60);//used as minutes in this context
		try {
			this.getUserModelInfo(userModel);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	/***
	 * JSONModel information is transferred to the related functions so as to be put in concerning objects
	 * @param userModel
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public void getUserModelInfo(JSONObject userModel) throws NumberFormatException, JSONException{
		firstUserSessionsDeployement(userModel);
		this.bandwidth.getParametersFromUserModel(userModel);
		this.session.getParametersFromUserModel(userModel);
	}
	
	/**
	 * First user Model creation is performed by means of this function.
	 * Not:time Interval parameter in case of need  could be adjusted according to
	 * the user decision in the future version.This explanation is not valid for the current
	 * user JSON model.
	 * @param userModel
	 * @throws JSONException
	 */
	private void firstUserSessionsDeployement(JSONObject userModel) throws JSONException{
		if(userModel!=null){
		
		if(userModel.getString("userId").toString().equalsIgnoreCase("")){
			int size=24*60/this.timeInterval;

			for(int i=0;i<size;i++){
				JSONObject jsonobject = new JSONObject();
				jsonobject.put("visitingFrequency",0);
				jsonobject.put("bandwidth",0);
				userModel.getJSONArray("sessionsPerMin").put(jsonobject);
			}
		}	}
		else{
			BlackBoard.writeConsole("user model returns null");
		}
	}
	/***
	 * This function is called when user model is needed to be stored in file/database.
	 * Generally, this could be called periodically by timer function or end of the user 
	 * authentication. 
	 * @param userModel
	 */
	public void transferUserProfileInfoIntoUserModel(JSONObject userModel){
		session.setParametersToUserModel(userModel,this);
		bandwidth.setParametersToUserModel(userModel,this);
		try {
			userModel.put("userId",this.userId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
			
}


