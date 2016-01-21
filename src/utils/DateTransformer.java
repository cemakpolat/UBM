package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DateTransformer is responsible for converting the given string date format
 * in millisecond format or vice versa.
 * 
 * @author Cem Akpolat & Mursel Yildiz
 * 
 */
public class DateTransformer {
	public static String className = "DateTransformer";

	/**
	 * Convert the given millisecond in the string date format.
	 * 
	 * @param milisecond
	 * @return
	 */
	public static String transformMiliSecondToString(long milisecond) {

		// System.out.println("Date to String Milisecond:" + milisecond);
		Date date = new Date(milisecond);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
		String timestampToParse = sdf.format(date);
		// System.out.println("Date to String TimeStamp:" + timestampToParse);
		return timestampToParse;

	}

	/**
	 * Convert the received string time in millisecond
	 * 
	 * @param timestamp
	 * @return
	 */
	public static long transformStrigToMiliSecond(String timestamp) {
		BlackBoard.writeConsole(className, " transformStrigToMiliSecond: "
				+ timestamp);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		String[] str = timestamp.split(":");
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str[0]));
		cal.set(Calendar.MINUTE, Integer.parseInt(str[1]));
		cal.set(Calendar.SECOND, Integer.parseInt(str[2]));
		cal.set(Calendar.MILLISECOND, Integer.parseInt(str[3]));
		// System.out.println(" transformStrigToMiliSecond: "+cal.getTimeInMillis());
		return cal.getTimeInMillis();

	}

	/**
	 * Subtract two different long time and return the result of this
	 * subtraction in the long-millisecond format.
	 * 
	 * @param milisecond1
	 * @param milisecond2
	 * @return
	 */
	public static long dateSoustraction(long milisecond1, long milisecond2) {
		// System.out.println("soustraction\n");
		long diff = milisecond2 - milisecond1;
		return diff;
	}

	/**
	 * Subtract two different long time and return the result of this
	 * subtraction in the string date format.
	 * 
	 * @param milisecond1
	 * @param milisecond2
	 * @return
	 */
	public static String dateAverage(long milisecond1, long milisecond2) {
		// System.out.println("addition\n");
		long diff = (milisecond2 + milisecond1) / 2;
		return transformMiliSecondToString(diff);
	}

	/**
	 * 
	 * @param milisecond1
	 * @param milisecond2
	 * @param experience
	 * @return
	 */
	public static String dateAverageWithExperience(long milisecond1,
			long milisecond2, int experience) {

		long diff = (milisecond2 * (experience - 1) + milisecond1) / experience;
		return transformMiliSecondToString(diff);
	}

	/**
	 * Convert string date format in long millisecond.
	 * 
	 * @param time
	 * @return
	 */
	public static long convertStringToMilisecond(String time) {

		// System.out.println(" convertStringToMilisecond: "+time);
		String[] str = time.split(":");
		long milisecond = 0;
		milisecond = Integer.parseInt(str[0]) * 1000 * 60 * 60 + milisecond;
		milisecond = Integer.parseInt(str[1]) * 1000 * 60 + milisecond;
		milisecond = Integer.parseInt(str[2]) * 1000 + milisecond;
		milisecond = Integer.parseInt(str[3]) + milisecond;
		// System.out.println(" convertStringToMilisecond: "+milisecond);
		return milisecond;
	}

	/**
	 * Convert long millisecond in string date format.
	 * 
	 * @param milisecond
	 * @return
	 */
	public static String convertMiliSecondTimeToString(long milisecond) {

		long elapsedTime = milisecond / 1000;
		int hours = (int) elapsedTime / (60 * 60);
		int minutes = (int) (elapsedTime / (60)) % 60;
		int seconds = (int) (elapsedTime) % 60;
		int mili = (int) (milisecond) % (1000);
		// System.out.println("convertMiliSecondTimeToString: "+milisecond);
		// System.out.println("hour: "+hours + ":" + minutes + ":" + seconds +
		// ":" + mili);
		return hours + ":" + minutes + ":" + seconds + ":" + mili;
	}

}
