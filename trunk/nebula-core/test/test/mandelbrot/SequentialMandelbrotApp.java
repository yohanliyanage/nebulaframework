package test.mandelbrot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

public class SequentialMandelbrotApp extends JFrame {

	public static final int MAX_LINES = 10; // # of scan lines per task ( must
											// be > MIN_LINES)
	public static final int MIN_LINES = 1; // # of scan lines per task ( must
											// be > 1)

	// initial region for which Mandelbrot is being computed
	private double x1 = -2.25;
	private double x2 = 3.0;
	private double y1 = -1.8;
	private double y2 = 3.3;

	// dimensions of window
	private static final int WIDTH = 300;
	private static final int HEIGHT = 300;

	// dimensions of window (used for computation)
	private int ymid = HEIGHT / 2;

	private static final long serialVersionUID = -2070429091106618345L;
	private static Log log = LogFactory.getLog(SequentialMandelbrotApp.class);

	// off-screen buffer and graphics
	private Image offscreen;
	private Graphics offg;

	private boolean done = false;
	private int progress;

	public SequentialMandelbrotApp() throws HeadlessException {
		super();
		this.setSize(WIDTH, HEIGHT);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

		log.info("Starting Sequential Mandelbrot Application");
		// Create App Instance
		SequentialMandelbrotApp app = new SequentialMandelbrotApp();

		app.requestFocus();

		StopWatch sw = new StopWatch();
		log.info("Starting Computation...");
		sw.start();

		// Start Rendering
		app.startRendering();
		
		sw.stop();
		
		log.info("Computation Complete... " + sw.getLastTaskTimeMillis() + "ms");
		//System.exit(0);

	}

	private void startRendering() {
		new Thread(new Runnable() {

			public void run() {
				
				doStart();

			}

		}).start();
		
		
		// Block until done
		try {
			log.debug("Waiting");
			synchronized (this) {
				SequentialMandelbrotApp.this.wait();
			}
			log.debug("Notified");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	protected void doStart() {
		int lines = 0;

		for (int i = 0; i < HEIGHT; i += lines) {
			double deviation = calculateDeviation(i);
			lines = (int) ((MAX_LINES - MIN_LINES) * deviation + MIN_LINES);
			processMandelbrot(i, lines);
		}
		
		synchronized (this) {
			this.notify();
		}
		
	}
	
	private double calculateDeviation(int i) {
		return Math.abs((double)(i - ymid)) / ymid;
	}
	
	
	private void processMandelbrot(int start, int lines) {
	       	double x, y, xx, a, b;
	        int end = start + lines;

	        double da = x2/WIDTH;
	        double db = y2/HEIGHT;

	        b = y1;

	        long[][] points = new long[WIDTH][lines];

	        for (int i = 0; i < start; i++) {
	            b = b + db;
	        }

	        int k = 0;

	        for (int i = start; i < end; i++, k++) {
	            a = x1;
	            for (int j = 0; j < WIDTH; j++) {
	                long n = 0;
	                x = 0.0;
	                y = 0.0;
	                while ( (n < 1000000L) && ( (x*x)+(y*y) < 4.0) ) {
	                    xx = x * x - y * y + a;
	                    y = 2 * x * y + b;
	                    x = xx;
	                    n++;
	                }
	                points[j][k] = n;
	                a = a + da;
	            }
	            b = b + db;
	        }
	        displayResult(points, start, lines);
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

	private void displayResult(long[][] points, int start, int lines) {

		this.setTitle("Rendering...");

		int j = 0;
		for (int l = start; j < lines; j++, l++) {
			for (int k = 0; k < WIDTH; k++) {
				long pixel = points[k][j];
				Color pixelColor = getPixelColor(pixel);
				offg.setColor(pixelColor);
				offg.fillRect(k, l, 1, 1);
			}
		}
		repaint();
	}

}
