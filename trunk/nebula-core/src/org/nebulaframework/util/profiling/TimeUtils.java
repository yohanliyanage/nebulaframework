package org.nebulaframework.util.profiling;


// TODO FixDoc

public class TimeUtils {

	public static String timeDifference(long start) {
		return timeDifference(start, System.currentTimeMillis());
	}
	public static String timeDifference(long start, long end) throws IllegalArgumentException {

		if (end < start) {
			throw new IllegalArgumentException("start time should be before end time");
		}
		
		long diff = end - start;
		
		// Days
		long days = diff / (1000 * 60 * 60 * 24);
		diff = diff % (1000 * 60 * 60 * 24);
		
		long hours = diff / (1000 * 60 * 60);
		diff = diff %  (1000 * 60 * 60);
		
		long mins = diff / (1000 * 60);
		diff = diff % (1000 * 60);
		
		long secs = diff / 1000;
		
		StringBuilder builder = new StringBuilder();
		if (days>0) {
			builder.append(days + " d : ");
		}
		builder.append(String.format("%02d h : ", hours));
		builder.append(String.format("%02d m : ", mins));
		builder.append(String.format("%02d s", secs));

		return builder.toString();
	}
}
