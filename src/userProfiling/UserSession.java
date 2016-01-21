package userProfiling;



import java.util.ArrayList;
import java.util.Date;

import utils.BlackBoard;
import utils.DateTransformer;

import json.JSONException;
import json.JSONObject;
/***
 * User Session Class is a collection of time depended information. 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */

public class UserSession {
	
	public String className;
	/* 
	 * This number indicates how many time a user authenticated after the first time authentication
	 * At each authentication this number will be increased by 1. This number is referred to as
	 * also "age number". The bigger authentication number, the accurate session or bandwidth average
	 * could be obtained.
	 * */
	public int 		authenticationNumber;
	/*The beginning of the authentication time of a user at each involving in a network in ms*/
	public long 	arrivalTime;
	/*The departure time of a authenticated user in ms*/
	public long 	departureTime;
	/*Parameter checks whether user is authenticated or not*/
	public boolean 	isTimeAssigned;
	/*Total session time of a user in ms*/
	public long 	sessionTime;
	/*Session time average for a user in ms*/
	public long 	sessionAverage;
	/*it contains sessionobject*/
	public ArrayList<SessionObject> sessionList;
	/* 
	 * Visiting Frequency informs us about the frequency of visiting in a network of a user.
	 * this is also the answer of how many times user have visited the current network in 
	 * a day. At each new authentication in a day, this number is incremented by 1.
	 * */
	public double 	visitingFrequency;
	/*
	 *Show the number of 10 minutes which were visited until now
	 * */
	public double 	dailyVFProbability;//
	/*
	 * Show daily auth. number being increased only one time in a day.
	 * */
	public int 		dailyAuthenticationNumber;//
	/*The departure time from the last session*/
	public long 	lastDepartureTime; 
	
	UserSession(){
		className=this.getClass().getName();
		this.isTimeAssigned=false;
		sessionList=new ArrayList<SessionObject>();
	}
	/***
	 * Calculate session time of a user
	 */
	//time should be re-checked.
	public void calculateSessionTime(){
		sessionTime=departureTime-arrivalTime;
		//System.out.println("Session Time: "+sessionTime);
	}
	/***
	 * Calculate new session Average on the basis of current parameters.
	 */
	//
	public void calculateNewSessionAverage(){
		calculateSessionTime();
		this.sessionAverage=(this.sessionAverage*(this.authenticationNumber-1)+this.sessionTime)/this.authenticationNumber;//+(this.sessionAverage*(this.authenticationNumber-1)+this.sessionTime)%this.authenticationNumber;
	}
	/***
	 * Calculate visiting frequency of the current user.
	 * 
	 */
	//FIXME:Check this function
	public void calculateVisitingFrequency(int place,JSONObject userModel) throws JSONException{
			System.out.println("dailyVFProbability "+dailyVFProbability);
			if(dailyVFProbability>1 && this.dailyAuthenticationNumber>1){//after first time this function will be often utilized.
				for(int j=0;j<=place;j++){//calculate only until the current point, the rest is not yet visited. 
					this.visitingFrequency = userModel.getJSONArray("sessionsPerMin").getJSONObject(j).getDouble("visitingFrequency");
					
					if(visitingFrequency!=0 && j<place){
						double oldVisitingFrequency=(this.visitingFrequency*this.dailyAuthenticationNumber-1/(dailyVFProbability-1))/(this.dailyAuthenticationNumber-1);
						this.visitingFrequency=(oldVisitingFrequency*(this.dailyAuthenticationNumber-1)+1/dailyVFProbability)/this.dailyAuthenticationNumber;
						userModel.getJSONArray("sessionsPerMin").getJSONObject(j).put("visitingFrequency",visitingFrequency);
					}
					else if(j==place && visitingFrequency!=0 && visitingFrequency!=1){//daha onceden ugramis(dun)
						double oldVisitingFrequency=(this.visitingFrequency*this.dailyAuthenticationNumber-1/(dailyVFProbability-1))/(this.dailyAuthenticationNumber-1);
						this.visitingFrequency=(oldVisitingFrequency*(this.dailyAuthenticationNumber-1)+1/dailyVFProbability)/this.dailyAuthenticationNumber;
						userModel.getJSONArray("sessionsPerMin").getJSONObject(j).put("visitingFrequency",visitingFrequency);
					}
					else if(j==place && visitingFrequency==0){//ilk defa bu tike ugruyor
						this.visitingFrequency=(visitingFrequency*(this.dailyAuthenticationNumber-1)+1/dailyVFProbability)/this.dailyAuthenticationNumber;
						userModel.getJSONArray("sessionsPerMin").getJSONObject(j).put("visitingFrequency",this.visitingFrequency);
					}
					else if(j==place && visitingFrequency==1){//ayni zaman diliminde gelirse
						this.visitingFrequency=1;
					} 
				}
			}//first time authentication in other words first establishment of user profile file. 
			else if(dailyVFProbability>1 && this.dailyAuthenticationNumber==1){
				for(int j=0;j<=place;j++){//calculate only until the current point, the rest is not yet visited. 
					this.visitingFrequency = userModel.getJSONArray("sessionsPerMin").getJSONObject(j).getDouble("visitingFrequency");
					
					if(visitingFrequency!=0 && j<place){
						BlackBoard.writeConsole(className, "dailyVFProbability "+dailyVFProbability);
						this.visitingFrequency=(1/dailyVFProbability);
						BlackBoard.writeConsole(className, "visitingFrequency: "+visitingFrequency);
						userModel.getJSONArray("sessionsPerMin").getJSONObject(j).put("visitingFrequency",visitingFrequency);
					}
					else if(j==place && visitingFrequency==0){//for the last item
						BlackBoard.writeConsole(className, "dailyVFProbability "+dailyVFProbability);
						this.visitingFrequency=(1/dailyVFProbability);
						BlackBoard.writeConsole(className, "visitingFrequency In 0: "+this.visitingFrequency);
						userModel.getJSONArray("sessionsPerMin").getJSONObject(j).put("visitingFrequency",this.visitingFrequency);
					}
					else if(j==place && visitingFrequency==1){//ayni anda birden cok gelebilir
						//System.out.println("SECOND in Time VF: "+visitingFrequency);
						this.visitingFrequency=1;
					} 
				}
			}
			
			//first time in a day
			else if(dailyVFProbability==1 && this.dailyAuthenticationNumber>1){ 
				this.visitingFrequency = userModel.getJSONArray("sessionsPerMin").getJSONObject(place).getDouble("visitingFrequency");
				this.visitingFrequency=(this.visitingFrequency*(this.dailyAuthenticationNumber-1)+1/dailyVFProbability)/this.dailyAuthenticationNumber;
				userModel.getJSONArray("sessionsPerMin").getJSONObject(place).put("visitingFrequency",this.visitingFrequency);
		
			}//first time visiting for first time authentication until now
			else if(dailyVFProbability==1 && this.dailyAuthenticationNumber==1){
				this.visitingFrequency=1;
				//System.out.println("FIRST Time VF: "+visitingFrequency);
				userModel.getJSONArray("sessionsPerMin").getJSONObject(place).put("visitingFrequency",this.visitingFrequency);
			}
		}
	
	/***
	 * After the storing request for user's data, this function is called in order to prepare 
	 * session data and bandwidth data with conjunction with UserBandwidth object.  
	 * Periodically invoked it period based on time interval. 
	 * General time Interval is 10 minutes.
	 * @param userModel
	 * @param uprofile
	 * @throws JSONException
	 */
		
	public void addListToJSON(JSONObject userModel, UserSessionProfile uprofile)
			throws JSONException {
			this.dailyVFProbability=this.dailyVFProbability+1;//new probability
			String[] startTime;
			//FIXME: No need to use an arrayList for session due to the fact that sessionlist
			//size will be always 1.
			for (int i = 0; i < sessionList.size(); i++) {
				startTime = this.sessionList.get(i).startTime.split(":");
				int placeOfTime = Integer.parseInt(startTime[0]) * 6
						+ (Integer.parseInt(startTime[1]) / uprofile.timeInterval);

				// calculate new bandwidth
				uprofile.bandwidth.sessionBandwidthAveragePerMin = userModel.getJSONArray("sessionsPerMin").getJSONObject(placeOfTime).getDouble("bandwidth");
				uprofile.bandwidth.calculateSessionBandwidthAveragePerMin(uprofile);
				userModel.getJSONArray("sessionsPerMin").getJSONObject(placeOfTime).put("bandwidth",uprofile.bandwidth.sessionBandwidthAveragePerMin);
				
				//calculate new frequencies 
				calculateVisitingFrequency(placeOfTime,userModel);
				
				userModel.put("dailyVFProbability",this.dailyVFProbability);//add new daily VFP
			}		
			sessionList.clear();
	}
		
	/***
	 * Session Samples are collected periodically with the help of this function. Stores the consumed bandwidth in
	 * the specific time interval, then added new sample into the sessionList.
	 * @param uprofile
	 * @param timeInterval
	 */
	
		//check whether I ought to give a timeInterval parameter again, uprofile has already this variable. 
	public void addSessionSample(UserSessionProfile uprofile,int timeInterval){
			//Every 10 minutes this function is invoked by Timer
		
			long sessionTime;
			sessionTime=new Date().getTime();
			String[] time=(DateTransformer.transformMiliSecondToString(sessionTime)).split(":");

			String newStartTime=getAdequateTime(time,timeInterval);
			//System.out.println("newStartTime: "+newStartTime);
			uprofile.bandwidth.calculateTotalBandwidth();
			sessionList.add(new SessionObject(newStartTime,"",uprofile.bandwidth.totalBW));
			
	}

	/***
	 * Get an adequate time based on the offered timeInterval.
	 * @param time
	 * @param timeInterval
	 * @return
	 */
	private String getAdequateTime(String[] time,int timeInterval) {

			int hour=Integer.parseInt(time[0]);
			int min=Integer.parseInt(time[1]);
			
			int rest=min%timeInterval;
			int division=min/timeInterval;
			
			if(rest==0){//23:30

				if((hour!=0)){
					if(division==0){//12:00
						time[0]=""+Integer.toString(hour-1)+"";//decline one hour
						time[1]="50"; // 
					}	
					else{//12:10
						time[1]=""+((division*10-timeInterval)) +"";
					}
				}
				else{
					if(division==0){//00:00
						time[0]="23";//decrease one hour
						time[1]="50"; // set minute to 00
					}	
					else{//00:40
						time[1]=""+((division*10-timeInterval)) +"";
					}
				}
			
			}else if(rest>0)//23:32
			{
				time[1]=Integer.toString(division*10);
			}
			String newStartTime=time[0]+":"+time[1]+":00"+":0";
			return newStartTime;
			
	}
	/***
	 * Get Departure Time of the user for a given arrival time.
	 * @param arrivalTime
	 * @param timeInterval
	 * @return
	 */
	public String getDepartureTimeForGivenArrivalTime(String arrivalTime,int timeInterval){
		String[] time=arrivalTime.split(":");
		if((Integer.parseInt(time[0])<24)){
			if((Integer.parseInt(time[1])/timeInterval)==5){
				time[0]=""+(Integer.parseInt(time[0])+1)+"";//increase one hour
				time[1]="00"; // set minute to 00
				
				if(time[0].equalsIgnoreCase("24")){
					time[0]="00"; // set minute to 00	
				}
			}
			else{
				time[1]=""+((Integer.parseInt(time[1])+timeInterval)) +""; //just set minute at the rate of timeInterval
			}
			
		}
		String newEndTime=time[0]+":"+time[1]+":00"+":0";
		return newEndTime;
	}
	/***
	 * Set the current Session Parameters to the given user JSON Model
	 * @param userModel
	 * @param uprofile
	 */
	public void setParametersToUserModel(JSONObject userModel,UserSessionProfile uprofile){
			
			try {
				userModel.put("authenticationNumber",authenticationNumber);
				addListToJSON(userModel, uprofile);
				//checkDailyAuthNum(new Date().getTime(),this.lastDepartureTime);
				if(this.isTimeAssigned){
					calculateNewSessionAverage();
					userModel.put("sessionAverage",DateTransformer.convertMiliSecondTimeToString(this.sessionAverage));
					userModel.put("dailyAuthenticationNumber", this.dailyAuthenticationNumber);
					userModel.put("lastDepartureTime", ""+new Date().getTime()+"");
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}
	/***
	 * Get the user information based on the given user JSON Model
	 * @param userModel
	 */
	public void getParametersFromUserModel(JSONObject userModel){
		
		try {
			this.authenticationNumber=userModel.getInt("authenticationNumber");

			
			this.dailyVFProbability=userModel.getDouble("dailyVFProbability");
			this.dailyAuthenticationNumber=userModel.getInt("dailyAuthenticationNumber");
			this.lastDepartureTime=Long.parseLong(userModel.getString("lastDepartureTime"));

			//check here again due to the lastDepartureTime
			checkDailyAuthNum(new Date().getTime(),this.lastDepartureTime);
			
			this.sessionAverage=DateTransformer.convertStringToMilisecond(((String)userModel.get("sessionAverage")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/***
	 * Check the given two times whether they are the same day or not
	 * @param now
	 * @param lastDepart
	 */
	@SuppressWarnings("deprecation")
	public void checkDailyAuthNum(long now,long lastDepart){
		
		//String day1=DateTransformer.transformMiliSecondToString1(now);
		//String day2=DateTransformer.transformMiliSecondToString1(ms);
		
		int day1=(new Date(now)).getDate();
		int day2=(new Date(lastDepart)).getDate();
		
		if(this.dailyAuthenticationNumber==0 && this.lastDepartureTime==0){
			this.dailyAuthenticationNumber=this.dailyAuthenticationNumber+1;
			this.dailyVFProbability=0;
		}
		else if(day1!=day2){
			this.dailyAuthenticationNumber=this.dailyAuthenticationNumber+1;
			this.dailyVFProbability=0;
			 System.out.println("days are not equal days: "+now+" "+this.lastDepartureTime);
		}
		else{
			 System.out.println("days are  equal days: "+day1+" "+day2);
		}
		
	}
	public int getAuthenticationNumber() {
		return authenticationNumber;
	}

	public void setAuthenticationNumber(int authenticationNumber) {
		this.authenticationNumber = authenticationNumber;
	}

	public boolean isTimeAssigned() {
		return isTimeAssigned;
	}

	public void setTimeAssigned(boolean isTimeAssigned) {
		this.isTimeAssigned = isTimeAssigned;
	}

	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public void setDepartureTime(long departureTime) {
		this.departureTime = departureTime;
	}
		
}
/***
 * Session Object Class is implemented to cover the data related to a session.
 */
class SessionObject{
	public String startTime;
	public String endTime;
	public double bandwidth;
	SessionObject(String sT,String eT,double bw){
		this.startTime=sT;
		this.endTime=eT;
		this.bandwidth=bw;
	}	
}
