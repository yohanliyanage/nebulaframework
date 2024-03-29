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

package test.mandelbrot;

import java.io.Serializable;

public class MandelbrotResult implements Serializable {

	private static final long serialVersionUID = -83408493802551375L;

	private int start;
	private int lines;
	
	private long[][] points;
	
	public MandelbrotResult(int start, int lines, long[][] points) {
		super();
		this.start = start;
		this.lines = lines;
		this.points = points.clone();
	}
	
	public int getStart() {
		return start;
	}
	public long[][] getPoints() {
		return points.clone();
	}

	public int getLines() {
		return lines;
	}
	
	
	
}
