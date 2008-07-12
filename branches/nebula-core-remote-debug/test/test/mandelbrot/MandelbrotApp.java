package test.mandelbrot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

public class MandelbrotApp extends JFrame {

	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;

	private static final long serialVersionUID = -2070429091106618345L;
	private static Log log = LogFactory.getLog(MandelbrotApp.class);

	
	// off-screen buffer and graphics
	private Image offscreen;
	private Graphics offg;

	private boolean done = false;
	private int progress;

	public MandelbrotApp(final GridNode nodeRef) throws HeadlessException {
		super();
		this.setSize(WIDTH, HEIGHT);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				
				setTitle("Shutting Down Node... Wait");
				System.out.println("Shutting Down Node... Wait");
				
				nodeRef.shutdown(true, false);

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
		offscreen = createImage(WIDTH, HEIGHT);
		offg = offscreen.getGraphics();
		offg.setColor(Color.black);
		offg.fillRect(0, 0, WIDTH, HEIGHT);
		repaint();
		setTitle("Wait...");
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
			final MandelbrotApp app = new MandelbrotApp(node);

			app.requestFocus();
			
			// Create Mandelbrot Job
			MandelbrotJob mandelbrotJob = new MandelbrotJob(app.getWidth(), app
					.getHeight());
			
			// Start Job Submission
			sw.start();
			
			GridJobFuture future = node.getJobSubmissionService()
					.submitJob(mandelbrotJob, new ResultCallback() {

						public void onResult(Serializable result) {
							
							MandelbrotResult mResult = (MandelbrotResult) result;
							app.displayResult(mResult);
							app.setProgress(app.getProgress()
									+ mResult.getLines());
						}

					});
			
			// Block till job finishes
			future.getResult();
			app.setDone(true);

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

	@Override
	public void paint(Graphics g) {
		if (done) {
			g.drawImage(offscreen, 0, 0, this);
			this.setTitle("Done");
		} else {
			g.drawImage(offscreen, 0, 0, this);
			g.setColor(Color.white);
			g.drawRect(WIDTH / 4, 10, WIDTH / 2, 5);
			g.fillRect(WIDTH / 4, 11, (progress * (WIDTH / 2)) / HEIGHT, 4);
		}
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	private Color getPixelColor(long pixel) {

		Color color;
		
		int val = (int) pixel % 101;
		
		if (val == 100)
			color = new Color(0, 0, 0);
		else if (val > 90)
			color = new Color(val * 2, 0, (val - 90) * 25);
		else if (val > 80)
			color = new Color(val * 2, 0, 0);
		else if (val > 60)
			color = new Color(val * 3, 0, val);
		else if (val > 20)
			color = new Color(val * 4, 0, val * 2);
		else if (val > 10)
			color = new Color(val * 5, 0, val * 10);
		else
			color = new Color(0, 0, val * 20);

		return color;
	}

	private void displayResult(MandelbrotResult result) {
		
		this.setTitle("Rendering...");
		
		int j = 0;
		for (int l = result.getStart(); j < result.getLines(); j++, l++) {
			for (int k = 0; k < WIDTH; k++) {
				long pixel = result.getPoints()[k][j];
				Color pixelColor = getPixelColor(pixel);
				offg.setColor(pixelColor);
				offg.fillRect(k, l, 1, 1);
			}
		}
		repaint();
	}

}
