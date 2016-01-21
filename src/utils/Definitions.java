package utils;
/**
 * Definitions Class, as its name implies, covers all crucial static definitions/parameters with the aim of easing
 * their usability. Instead of searching static elements in the whole code, this class allows us with the 
 * capability of a global change.
 * In other words, this class could be also referred to as configuration class.
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class Definitions {
	//Package ActionPackage
	
		/*Actions default Port Numbers*/
		public static int ActionServerPortNumber=14500;
		public static int ActionClientPortNumber=14131;
	
	//Package ObservationPackage
		
		/*Observation default Port Numbers*/
		public static int ObservationServerPortNumber=14400;
		public static int ObservationClientPortNumber=14131;

	//Package MeasurementPlane
		
		/*MeasurementPlane default port Numbers*/
		 public static int MPServerPortNumber=14131;
		 public static int MPClientPortNumber=14132;
		 
	//Package userProfiling
		 
		 /*Packet Buffer Server Port Number*/
		 public static int PacketBufferServerPortNumber=5010;
		 
		 /*Authentication and Association Server Port Number*/
		 public static int AssoAuthThreadServerPortNumber=5011;
		
		 /*Time Interval For Storing User Profile Sample in File/Database (default 10 min.)*/
		 public static long TimeIntervalForStroingUserProfile=1000*60*10;//10 min.

		 
	//Package qoETracker 	
		 
		 /*Quality of Experience Server Port Number */
		 public static int QoEServerPortNumber=13132;
		 /*ITG Timer Duration for restarting*/
		 public static long ITGTimerDuration=100000; 
	 												 
		 // General Measurement Thresholds:
		 /*Average Delay Time Threshold in Milliseconds*/
		 public static double averageDelayTimeThreshold = 100;
		 /*QoE Experience Shift Time*/
		 public static int qoEExperienceShiftTime = 20000;
		 
	 //Package dataBaseConnection

		 /*DAO File Path for Users*/
		 public static  String DOAUserModelFolderName="Users/";
		 /*DAO File Path For Quality of Experiences*/
		 public static  String DOAQoEFolderName="QoExperience/";
		 
		 /*Database Configurations*/
		 public static  String DAOURL = "jdbc:mysql://localhost:3306/userModels";
		 public static  String DAOUSER = "root";
		 public static  String DAOPASSWORD = "akpolat";
		 public static  String DAODRIVER = "com.mysql.jdbc.Driver";
		 

		 /*ARP File Path*/
		 public static String ARPFILEPATH="/proc/net/arp";
		 /*DHCP File Path*/
		 public static String DHCPFILEPATH="/var/lib/dhcp/dhcpd.leases";
	 
}
