package org.nebulaframework.ui.swing;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.Ostermiller.util.Browser;

public class UISupport {

	private static final Log log = LogFactory.getLog(UISupport.class);;
	
	public static void displayHelp(JFrame owner) {

		String filePath = "help/index.html";
		
		// If Path Info Available
		if (System.getProperty("nebula.home")!=null) {
			filePath = System.getProperty("nebula.home") + "/" + filePath;
		}
		
		File helpFile = new File(filePath);
		
		if (!helpFile.exists()) {
			JOptionPane.showMessageDialog(owner, "Unable to locate Help Files");
			log.warn("[UI] Help files missing : " + filePath);
			return;
		}
		
		// Initialize Browser
		Browser.init();
		try {
			Browser.displayURL(helpFile.toURI().toURL().toString());
		} catch (IOException e) {
			log.warn("[UI] Unable to display Help",e);
			JOptionPane.showMessageDialog(owner, "Unable to display Help Contents");
		}
	}
	
	/**
	 * Returns the current active window of application
	 * or null if no such window exists.
	 * 
	 * @return active window if found, or null
	 */
	public static Window activeWindow() {
		return getActiveWindow(Frame.getFrames());
	}
	
	/**
	 * Returns the current active window of application
	 * or null if no such window exists.
	 * <p>
	 * Reference:
	 * http://www.rojotek.com/blog/rob/archives/000060.html
	 * 
	 * @param windows windows to be searched
	 * @return active window if found, or null
	 */
	private static Window getActiveWindow(Window[] windows) {
	    for (int i = 0; i < windows.length; i++) {
	        Window window = windows[i];
	        if (window.isActive()) {
	            return window;
	        } else {
	            Window[] ownedWindows = window.getOwnedWindows();
	            if (ownedWindows != null) {
	            	return getActiveWindow(ownedWindows);
	            }
	        }
	    }
	    return null;
	}
}
