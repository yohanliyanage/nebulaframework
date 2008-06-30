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
package test.nebulaframework.simpleTest;

import java.util.Date;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Executes a Sequential Job to Demonstrate Sequential Running Time.
 * @author Yohan Liyanage
 *
 */
public class SequentialRunner {

	private static Log log = LogFactory.getLog(SequentialRunner.class);
	public static void main(String[] args) {
		Date start = new Date();
		int sum = 0;
		log.info("Starting Job...");
		for(int i=0 ; i < 50; i++) {
			sum += execute();
		}
		Date end = new Date();
		log.info("Job Complete. RESULT : " + sum);
		log.info("Execution Time : " + (end.getTime() - start.getTime()) + " milliseconds");
	}
	
	private static int execute() {
		Integer val = new Random().nextInt(100);
		System.out.println("Got Random Value : " + val);
		TestRunner.sum += val;
		try {
			//Wait for 1 second, to simulate a large work unit
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return val;		
	}
}
