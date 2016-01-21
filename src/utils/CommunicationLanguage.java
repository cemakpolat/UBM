package utils;

import java.util.ArrayList;
/**
 * A flexible and understandable messaging for for the communication between MeasurementPlane
 * and other frameworks is provided by this class. Once a platform sends a request by using this class,
 * MeasurementPlane can easily detect what request is needed. In other words, MeasurementPlane can provide
 * its data collected from users to the other components with the help of Communication Language. 
 * The language set offered below would be extended with respect to the types of the request being sent
 * by external components.
 *
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class CommunicationLanguage{
	 
	//General Options
	 
	 public static final   	int AuthenticatedUsers = 				0;
	 public static final  	int NumberOfKnownUsersID = 				1;
	 public static final  	int AuthenticatedUserModels = 			2;
	 public static final 	int GetAllKnownUsersID = 				3; 
	 public static final 	int AuthenticatedUserQoEs=				4;
	 public static final	int AllUserQoEs=						5;
	 public static final int 	GetQoEAverageDelayStatus = 			6; 
	
	 public static final int GetUserMACAddressFromQOE = 			7;

	 //User Specific Options
	 
	 public static final 	int GetModelOfKnownID = 				100;
	 public static final 	int GetTotalConsumedByteOfKnownID=		101;
	 public static final 	int GetQoEResultForKnownUserId=			102;
	 public static final 	int GetFirstCongestedUser=				103;
	 public static final 	int GetWorstCongestedUser=				104;
	
	 /*	public static final 	int
	 	public static final 	int 
	 	public static final 	int 
	 	public static final 	int 
	 */
	
	 
	 public static ArrayList<CommunicationLanguageObject> generalCommands=new ArrayList<CommunicationLanguageObject>();
	 public static ArrayList<CommunicationLanguageObject> userSpecificCommands=new ArrayList<CommunicationLanguageObject>(); 
	/**
	 * 
	 */
	 public CommunicationLanguage(){		 
		 
		 //General Information
		 
		 generalCommands.add(new CommunicationLanguageObject(AuthenticatedUsers,"Get All Authenticated Users"));
		 generalCommands.add(new CommunicationLanguageObject(NumberOfKnownUsersID,"Get Number of Known Users "));
		 generalCommands.add(new CommunicationLanguageObject(AuthenticatedUserModels,"Get All Authenticated Users' Models"));
		 generalCommands.add(new CommunicationLanguageObject(GetAllKnownUsersID,"Get All Known Users ID"));
		 //generalCommands.add(new CommunicationLanguageObject(GetQoEAverageDelayStatu,"Get QoE Congested Statu"));
		 
		 
		 //User Specific Information
		 userSpecificCommands.add(new CommunicationLanguageObject(GetModelOfKnownID,"Get Model of Known ID"));//give authenticated and unauthenticated users' names
		 userSpecificCommands.add(new CommunicationLanguageObject(GetTotalConsumedByteOfKnownID, "Get Total Downloaded  / Uploaded Bytes per 10 min "));
		 //userSpecificCommands.add(new CommunicationLanguageObject(GetCongestedUser, "Get Congested User"));
		 //userSpecificCommands.add(new CommunicationLanguageObject(GetTotalUploadedPer10MinOfKnownID, "[5] Get Total Downloaded  / Downloaded Bytes per 10 min "));
		 //userSpecificCommands.add(new CommunicationLanguageObject(GetTotalDownloadedPer10OfKnownID, "[5] Get Total Uploaded  / Uploaded Bytes per 10 min "));
}
	
	 /**
	  * 
	  */
	public void showListContent(){
		System.out.println("[exit] close Observer");
		System.out.println("------------------------------------");
		System.out.println("	General Options			");
		System.out.println("------------------------------------");
		int i;
		for(i=0;i<generalCommands.size();i++){
			System.out.println("["+i+"]"+generalCommands.get(i).message);
		}
		System.out.println("------------------------------------");
		System.out.println("	User Specific Options		");
		System.out.println("------------------------------------");
		
		for(int j=0;j<userSpecificCommands.size();j++){
			int res=i+j;
			System.out.println("["+res+"]"+userSpecificCommands.get(j).message);
		}
	}

	
}
/**
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
class CommunicationLanguageObject{
	public int type;
	public String message;
	public String name;
	public CommunicationLanguageObject(int type,String mes){
		this.type=type;
		this.message=mes;
	}
	public CommunicationLanguageObject(String name,String mes){
		this.name=name;
		this.message=mes;
	}
}

