package test.buddhabrot.twotask;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

public class BuddhabrotApp extends JFrame {

	private final int WIDTH = 600;
	private final int HEIGHT = 600;
	
	private static final long serialVersionUID = -3962671014114993755L;
	private static Log log = LogFactory.getLog(BuddhabrotApp.class);
	
	// Image Buffer
	BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	
	
	public BuddhabrotApp(final GridNode node) throws HeadlessException {
		super();
		setSize(WIDTH, HEIGHT);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				node.shutdown(true,false);
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
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				image.setRGB(x, y, rgb[x][y]);
			}
		}
		repaint();
	}
	
	public static void main(String[] args) {

		try {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();

			ApplicationContext ctx = new ClassPathXmlApplicationContext(
					"org/nebulaframework/core/grid/cluster/node/grid-node.xml");
			GridNode node = (GridNode) ctx.getBean("localNode");

			log.info("GridNode ID : " + node.getId());

			node.getNodeRegistrationService().register();
			log.info("Registered in Cluster : "
					+ node.getNodeRegistrationService().getRegistration()
							.getClusterId());

			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis()
					+ " ms]");

			// Create App Instance
			final BuddhabrotApp app = new BuddhabrotApp(node);

			app.requestFocus();
			
			// Create Mandelbrot Job
			BuddhabrotJob buddhabrotJob = new BuddhabrotJob(app.getWidth(), app.getHeight());
			
			// Start Job Submission
			sw.start();
			
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
			
			// Block till job finishes
			future.getResult();

			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis()
					+ " ms");

			log.debug("Press any key to unregister GridNode and terminate");
			System.in.read();
			node.getNodeRegistrationService().unregister();

			log.info("Unregistered, Terminating...");
			System.exit(0);

		} catch (RegistrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
