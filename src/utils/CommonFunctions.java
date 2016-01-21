package utils;

import java.util.Random;
/**
 * CommonFunctions contains some functions being mostly utilized by other classes.
 * 
 * @author Cem Akpolat & Mursel Yildiz
 *
 */
public class CommonFunctions {
/**
 * In case of calling this functions with an integer value which is the maximum random value.
 * Caller of this method will sleep for a while according to the generated random value. 
 * @param requestedTimeInterval
 */
	public static void randomBackOfftime(int requestedTimeInterval){
		 Random randomNumberGenerator = new Random();
	     int randomInstance = randomNumberGenerator.nextInt(requestedTimeInterval);
	     randomInstance++;
	     try {
	         Thread.sleep(randomInstance * 500);
	     } catch (InterruptedException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	     }
	}
}
