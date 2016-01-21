package databaseConnection;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import utils.BlackBoard;
import utils.CommonFunctions;
import utils.Definitions;

import json.JSONException;
import json.JSONObject;

/**
 * 
 * Data Access Object(DAO) allows to store and fetch the User Information such as user Model in file or database. 
 *@author Cem Akpolat & Mursel Yildiz
 *
 */
public class DAO {//Data Access Object
 
   	/*
	 *The following variable values are used to setup
      the Connection object 
	 */
   static String className="DAO";
   static final String URL = Definitions.DAOURL;
   static final String USER = Definitions.DAOUSER;
   static final String PASSWORD = Definitions.DAOPASSWORD;
   static final String DRIVER = Definitions.DAODRIVER;
   static String userModelFolderName=Definitions.DOAUserModelFolderName;
   static String qoEFolderName=Definitions.DOAQoEFolderName;
   
   /**
    * This method is used to create a connection using 
      the values listed above. Notice the throws clause 
      in the method signature. This allows the calling method 
      to deal with the exception rather than catching it in 
      both places. The ClassNotFoundException must be caught 
      because the forName method requires it.
    * @return Connection
    * @throws SQLException
    */
   public Connection getConnection() throws SQLException {
      Connection con = null;
      try {
         Class.forName(DRIVER); 
         con = DriverManager.getConnection(URL, USER, PASSWORD);      
      }
      catch(ClassNotFoundException e) {
         System.out.println(e.getMessage());
         System.exit(-1);
      }
      return con;
   }
 
   /**
    * Get All user Ids in the file/database.
    * @return ArrayList
    */
   public ArrayList<String> getAllUserIDs(){
       File dir = new File( "Users" );
       String[] files = dir.list();
       ArrayList<String> userList = new ArrayList<String>();
       
       for(int i=0;i<files.length;i++){
    	   String user=(files[i].split(".txt"))[0];
    	   userList.add(user);
       }
       return userList;
   }
   /**
    * Get User Model From File by using userId.
    * If any file exists for the given user Id, firstly a new file is created with the name of user Id 
    * and a generic empty user Profile is assigned to the user.
    * @param userId
    * @return JSONObject
    * @throws JSONException
    */
	public JSONObject getUserModelFromFile(String userId) throws JSONException {
		JSONObject userModel = null;
		int loopCount=0;
		try {
			String str;
			File f = new File(userModelFolderName + userId + ".txt");
			if (!f.exists()) {
				BlackBoard.writeConsole(className, "no file for user: "
						+ userId);
				str = "{\"TCPAverage\":0,\"UDPAverage\":0,\"lastDepartureTime\":\"0\",\"dailyAuthenticationNumber\":0,\"dailyVFProbability\":0,\"sessionsPerMin\":[],\"authenticationNumber\":0,\"bandwidthAverage\":0,\"userId\":\"\",\"sessionAverage\":\"00:00:00:0\"}";
				userModel = new JSONObject(str);
			} else {

				FileInputStream in = new FileInputStream(f);
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						in));

				try {
					FileChannel channel = new RandomAccessFile(f, "rw")
							.getChannel();
					final FileLock lock = channel.lock();

					try {
						int i = 0;
						while ((str = buf.readLine()) != null) {
							if (i == 0) {
								userModel = new JSONObject(str);
							}
							i++;
						}
					} catch (OverlappingFileLockException e) {
						e.printStackTrace();
						CommonFunctions.randomBackOfftime(5);
						if(loopCount<2){
							getUserModelFromFile(userId);
						}
						loopCount++;
					}

					finally {
						lock.release();
						channel.close();
					}
				} finally {
					in.close();
				}
			}
		} catch (IOException e) {
		}
		if (userModel==null){
			getUserModelFromFile(userId);
			BlackBoard.writeConsole("user Model returns null interestingl, not expectebehaviour");
		}
		return userModel;
	}
	/**
	 * Store the userModel with regard to the userId
	 * @param userModel
	 * @param userId
	 * @throws JSONException
	 */
	public void storeUserModelInFile(JSONObject userModel, String userId)
			throws JSONException {
		int loopCount=0;
		try { // Get a file channel for the file

			File file = new File(userModelFolderName + userId + ".txt");
			final FileWriter fw = new FileWriter(file, true);
			FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
			// Try acquiring the lock without blocking. This method returns
			// null or throws an exception if the file is already locked.
			FileLock lock = channel.lock();
			try {
				fw.write(userModel.toString());

			} catch (OverlappingFileLockException e) {
				e.printStackTrace();
				BlackBoard.writeConsole(className,
						"storing stuff is being repeated due to overlapping");
				CommonFunctions.randomBackOfftime(5);
				if(loopCount<2){
				storeUserModelInFile(userModel, userId);
				}
				loopCount++;
			}

			finally {
				lock.release(); // Close the file
				
				channel.close();
				fw.flush();
				fw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	/**
	 * Store QoE Result according to the userId.
	 * @param time the time that the QoE result is received.
	 * @param jobj
	 * @param userId 
	 */
	public void storeQoEInfoInFile(String time, JSONObject jobj, String userId) {
		// TODO Auto-generated method stub
		int loopCount=0;
		JSONObject object = null;
		try {

			File f = new File(qoEFolderName + userId + ".txt");
			final FileWriter fw = new FileWriter(f, true);
			FileChannel channel = new RandomAccessFile(f, "rw").getChannel();
			FileLock lock = channel.lock();
			try {
				object = new JSONObject();
				object.put("Time", time);
				object.put("QoE", jobj);
				fw.write(object.toString());
			} catch (OverlappingFileLockException e) {
				e.printStackTrace();
				BlackBoard.writeConsole(className,
						"storing stuff is being repeated due to overlapping");
				CommonFunctions.randomBackOfftime(5);
				if(loopCount<2){				
					storeQoEInfoInFile(time, jobj, userId);
				}
				loopCount++;
			}

			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.release(); // Close the file
				channel.close();
				fw.flush();
				fw.close();
			}

		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	/**
	 * Get User QoE From File according to the userId.	
	 * @param userId
	 * @return JSONObject userQoE
	 * @throws JSONException
	 */
	public JSONObject getUserQoEFromFile(String userId) throws JSONException {
		JSONObject userQoE = null;
		JSONObject storedObject = null;
		int loopCount=0;
		try {
			String str;
			final File f = new File(qoEFolderName + userId + ".txt");
			if (!f.exists()) {
				BlackBoard.writeConsole(className, "no file for user: "
						+ userId);

			} else {

				FileInputStream in = new FileInputStream(f);
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						in));

				try {
					FileChannel channel = new RandomAccessFile(f, "rw")
							.getChannel();
					final FileLock lock = channel.lock();

					try {
						while ((str = buf.readLine()) != null) {
							storedObject = new JSONObject(str);// get the last QoE
						}
						userQoE = storedObject.getJSONObject("QoE");
					} catch (OverlappingFileLockException e) {
						e.printStackTrace();
						CommonFunctions.randomBackOfftime(5);
						if(loopCount<2){
							getUserQoEFromFile(userId);
						}
						loopCount++;
					}

					finally {
						lock.release();
						channel.close();
					}
				} finally {
					in.close();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return userQoE;
	}
	 
	/**
	 * Get User Model from the database with the help of the userId. 
	 * @param userId
	 * @return JSONObject
	 * @throws JSONException
	 */
	  public JSONObject getUserModel(String userId) throws JSONException{

		BlackBoard.writeConsole(className,
				"user model is being fetching from DB");
		String userModel = null;
		ResultSet rs = null;

		try {

			// Statement s = getConnection().createStatement();
			// rs =
			// s.executeQuery("SELECT * FROM UserModelTable where userID ="+userId);

			String query = "SELECT * FROM userModels.model where model.userID =?";
			PreparedStatement preparedStmt = getConnection().prepareStatement(
					query);
			preparedStmt.setString(1, userId);
			rs = preparedStmt.executeQuery();

			getConnection().close();

			while (rs.next()) {// rs count ought to be 1.
				// String id = rs.getString("userId");
				userModel = rs.getString("model");
			}

			getConnection().close();
		} catch (SQLException e) {

			BlackBoard.writeConsole(className, e.getMessage());
			System.exit(-1);
		} finally {
			try {
				getConnection().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return (new JSONObject(userModel));
	  }
	   
	   
	  /**
	   * Store the user Model in database according to user Id
	   * @param userId
	   * @param newModel
	   */
	public void storeUserModelintoDB(String userId, JSONObject newModel) {
		BlackBoard.writeConsole(className, "User Model is being storing...");
		try {

			String query = "UPDATE userModels.model SET model.model= ? where model.userID = ?";
			PreparedStatement preparedStmt = getConnection().prepareStatement(
					query);

			preparedStmt.setString(1, newModel.toString());
			preparedStmt.setString(2, userId);
			preparedStmt.executeUpdate();

			getConnection().close();
		} catch (SQLException e) {

			BlackBoard.writeConsole(className, e.getMessage());
			System.exit(-1);
		} finally {
			try {
				getConnection().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}