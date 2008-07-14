package test.buddhabrot.sequential;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class SwingRunner extends JFrame implements ResultListener {

	private static final long serialVersionUID = 3420048335602060583L;

	private int imageWidth;
	private int imageHeight;
	AtomicInteger counter = new AtomicInteger();

	private BufferedImage off;
	JLabel countLabel;

	private static int plots = 0;
	
	public SwingRunner(int width, int height) throws HeadlessException {
		super();
		this.imageWidth = width;
		this.imageHeight = height;

		off = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		setLayout(new FlowLayout());
		countLabel = new JLabel("Calculations : 0");
		countLabel.setForeground(Color.BLACK);

		setSize(width, height);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		startProcessing();
	}

	private void startProcessing() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				doStart();
			}

		});
	}

	protected void doStart() {

		// Make Black Image
		for (int x=0; x<imageWidth; x++) {
			for (int y=0; y<imageHeight; y++) {
				off.setRGB(x, y, Color.BLACK.getRGB());
			}
		}
		paint(this.getGraphics());
		
		// x = random(-2.0, 1.0);
		// y = random(-1.5, 1.5);
		
		
		Buddhabrot b = new Buddhabrot(imageWidth, imageHeight, this, imageWidth, 0, 0, imageHeight);
		//Buddhabrot b2 = new Buddhabrot(imageWidth, imageHeight, this, imageWidth, 0, 0, imageHeight);
		
		//Buddhabrot b1 = new Buddhabrot(imageWidth, imageHeight, this);
		//Buddhabrot b2 = new Buddhabrot(imageWidth, imageHeight, this);
		
		startProcess(b);
		//startProcess(b2);
		//startProcess(b1);
		//startProcess(b2);

	}

	private void startProcess(final Buddhabrot b) {
		new Thread(new Runnable() {

			public void run() {
				b.refresh();

				while (true) {
					b.plot(Buddhabrot.PLOT_SIZE);
				}				
			}
			
		}).start();

	}

	public synchronized void onResult(int[][] rgb) {

		setTitle("Rendering : " + (++plots) +" Plots");

		int startX = 0;
		int startY = 0;
		int width = rgb.length;
		int height = rgb[0].length;

		// pixel = rgbArray[(y-startY) + (x-startX)];

		for (int x = startX; x < width; x++) {
			for (int y = startY; y < height; y++) {
				if (rgb[x][y]!=Color.BLACK.getRGB()) {
					off.setRGB(x, y, rgb[x][y]);
				}
			}
		}

		// repaint(); // trigger a repaint
		paint(this.getGraphics());
		/*
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * public void run() {
		 *  }
		 * 
		 * });
		 */

	}

	// AWT STUFF
	/**
	 * Override update method to prevent flicker
	 */
	// public void update(Graphics g) {
	//		
	// }

	/**
	 * Just blit our image buffer to the screen
	 */
	public synchronized void paint(Graphics g) {

		g.drawImage(off, 0, 0, null);

	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			
			public void run() {
				System.err.println(new Date());
				new SwingRunner(150, 150);
			}

		});

	}

}
