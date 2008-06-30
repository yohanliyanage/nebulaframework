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

import java.util.Random;

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTask;

/**
 * Task Implementation for Test Job.
 * @author Yohan Liyanage
 *
 */
public class TestTask implements GridTask<Integer>{

	private static final long serialVersionUID = -4826864297461445244L;

	@Override
	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer execute() throws GridExecutionException {
		Integer val = new Random().nextInt(100);
		System.out.println("Random Value : " + val);
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
