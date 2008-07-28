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
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;

public class MandelbrotJob implements SplitAggregateGridJob<MandelbrotResult, Serializable> {

	private static final long serialVersionUID = -1022185419447718663L;

    private int xsize;      // dimensions of window
    private int ysize;       
    private int ymid;
    public static final int MAX_LINES = 10;  // # of scan lines per task ( must be > MIN_LINES)
    public static final int MIN_LINES = 1;  // # of scan lines per task ( must be > 1)
    
    // initial region for which Mandelbrot is being computed
    private double x1 = -2.25;
    private double x2 =  3.0;
    private double y1 = -1.8;
    private double y2 =  3.3;
    
    
	public MandelbrotJob(int width, int height) {
		super();
		this.xsize = width;
		this.ysize = height;
		ymid = ysize / 2;
	}

	public Serializable aggregate(List<? extends Serializable> results) {
		System.out.println("Mandelbrot : Aggregate Method");
		return null;
	}

	public List<MandelbrotTask> split() {
		
        List<MandelbrotTask> tasks = new ArrayList<MandelbrotTask>();
 
        int lines = 0;
        
        for (int i = 0; i < ysize; i += lines) {
        	double deviation = calculateDeviation(i);
        	lines = (int)((MAX_LINES-MIN_LINES) * deviation + MIN_LINES);
            MandelbrotTask task = new MandelbrotTask(x1,y1,x2,y2,i,xsize, ysize, lines);
            tasks.add(task);
        }
		
		return tasks;
	}

	private double calculateDeviation(int i) {
		return Math.abs((double)(i - ymid)) / ymid;
	}

}
