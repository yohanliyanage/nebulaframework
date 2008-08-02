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

package test.buddhabrot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Date;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.cluster.registration.RegistrationException;
import org.springframework.util.StopWatch;

public class BuddhabrotApp extends JFrame {

	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	
	private static final long serialVersionUID = -3962671014114993755L;
	private static Log log = LogFactory.getLog(BuddhabrotApp.class);
	
	private int plots = 0;
	
	private GridJobFuture future;
	
	// Image Buffer
	BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	
	
	public BuddhabrotApp(final GridNode node) throws HeadlessException {
		super();
		setSize(WIDTH, HEIGHT);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				if (future!=null) {
					log.info("Cancelling Job... wait");
					setTitle("Cancelling Job - WAIT");
					if (!future.cancel()) {
						log.warn("Cancel Failed");
					}
				}
				
				node.shutdown();
				// Give time to send termination message
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				System.exit(0);
			}
			
		});

		setVisible(true);
		
		// create off-screen buffer
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		repaint();		
	}

	@Override
	public synchronized void paint(Graphics g) {
		g.drawImage(image, 0, 0, this);
	}
	
	public synchronized void onResult(int[][] rgb) {
		System.err.println(rgb.length + ","+ rgb[0].length);
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				image.setRGB(x, y, rgb[x][y]);
			}
		}
		setTitle("Rendering + " + (++plots) + " Plots");
		repaint();
	}
	
	
	public void setFuture(GridJobFuture future) {
		this.future = future;
	}

	public static void main(String[] args) {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();

		
			GridNode node = Grid.startLightGridNode();

			log.info("GridNode ID : " + node.getId());

			// Register on Cluster
			try {
				node.getNodeRegistrationService().register();
			} catch (RegistrationException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			log.info("Registered in Cluster : "
					+ node.getNodeRegistrationService().getRegistration()
							.getClusterId());

			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis()
					+ " ms]");

			// Create App Instance
			final BuddhabrotApp app = new BuddhabrotApp(node);

			app.requestFocus();
			
			// Create Buddhabrot Job
			BuddhabrotJob buddhabrotJob = new BuddhabrotJob(WIDTH, HEIGHT);
			
			// Start Job Submission
			sw.start();
			
			System.err.println(new Date());
			
			GridJobFuture future = node.getJobSubmissionService()
					.submitJob(buddhabrotJob, new ResultCallback() {

						public void onResult(Serializable result) {
							
							log.debug("CALLBACK");
							
							if (result==null) return;
							if (result instanceof int[][]) {
								app.onResult((int[][]) result);
							}
						}

					});
			
			app.setFuture(future);

	}
}
