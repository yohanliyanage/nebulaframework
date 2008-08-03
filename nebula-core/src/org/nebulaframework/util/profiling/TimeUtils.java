/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.util.profiling;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides utility routines for calculating time differences
 * and also formatting the time into String format.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class TimeUtils {

	/**
	 * Calculates the time difference from the specified time 
	 * to the current time and returns the result as a formatted
	 * String.
	 * 
	 * @param start start time
	 * @return String representation of time difference
	 */
	public static String timeDifference(long start) {
		return timeDifference(start, System.currentTimeMillis());
	}
	
	/**
	 * Calculates the time difference from the {@code start} time 
	 * to the {@code end} time and returns the result as a formatted
	 * String.
	 * 
	 * @param start start time
	 * @param end end time
	 * 
	 * @return String representation of time difference
	 * 
	 * @throws IllegalArgumentException if start time is later than end time
	 */
	public static String timeDifference(long start, long end) throws IllegalArgumentException {

		if (end < start) {
			throw new IllegalArgumentException("start time should be before end time");
		}
		
		return buildTimeString (end - start);
		

	}
	
	/**
	 * Builds the time string for a given time difference.
	 * The time string is in format,
	 * <p>
	 * <code><i>days</i> d : <i>hours</i> h : <i>minutes</i> m : <i>seconds</i> s</code>
	 * </p>
	 * @param time time difference to format
	 * 
	 * @return String representation of time difference
	 */
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
