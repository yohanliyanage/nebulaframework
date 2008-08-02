package org.nebulaframework.util.profiling;

import java.text.SimpleDateFormat;
import java.util.Date;


// TODO FixDoc

public class TimeUtils {

	public static String timeDifference(long start) {
		return timeDifference(start, System.currentTimeMillis());
	}
	public static String timeDifference(long start, long end) throws IllegalArgumentException {

		if (end < start) {
			throw new IllegalArgumentException("start time should be before end time");
		}
		
		return buildTimeString (end - start);
		

	}
	
	public static String buildTimeString(long time) {
		
		// Days
		long days = time / (1000 * 60 * 60 * 24);
		time = time % (1000 * 60 * 60 * 24);
		
		long hours = time / (1000 * 60 * 60);
		time = time %  (1000 * 60 * 60);
		
		long mins = time / (1000 * 60);
		time = time % (1000 * 60);
		
		long secs = time / 1000;
		
		StringBuilder builder = new StringBuilder();
		if (days>0) {
			builder.append(days + " d : ");
		}
		builder.append(String.format("%02d h : ", hours));
		builder.append(String.format("%02d m : ", mins));
		builder.append(String.format("%02d s", secs));

		return builder.toString();
	}
	
	/**
	 * Formats and returns a given Date
	 * 
	 * @param date Date to be formatted
	 * @return Formatted Date as String
	 */
	public static String formatDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.format(new Date(date));
	}
	
	
}
