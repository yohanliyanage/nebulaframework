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
package org.nebulaframework.util.log4j;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom Log4J Appender which appends the given log message
 * to a JTextPane.
 * <p>
 * This was implemented following the basic concept provided by
 * <a href="http://textareaappender.zcage.com/">Eric Elfner</a>
 * regarding appending Log4J output to a JTextArea.
 * <p>
 * This implementation enhances the above implementation
 * to support a JTextPane along with colored display of 
 * different log levels, and support for auto-scrolling.
 */
public class JTextPaneAppender extends WriterAppender {

	/*
	 * Log Level Colors
	 */
	public static final Color TRACE = Color.BLUE;
	public static final Color DEBUG = Color.GREEN;
	public static final Color INFO = Color.WHITE;
	public static final Color WARN = Color.YELLOW;
	public static final Color ERROR = Color.RED;
	public static final Color FATAL = Color.RED;
	
	private static JTextPane textPane = null;
	private static boolean autoScroll = true;
	
	/**
	 * Sets the target JTextPane
	 * @param textPane target
	 */
	public static void setTextPane(JTextPane textPane) {
		JTextPaneAppender.textPane = textPane;
		
		initialize();
	}

	/**
	 * Initializes the target JTextPane.
	 */
	private static void initialize() {
		if (textPane==null) return;
		
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		// Style Definitions
		Style def = textPane.addStyle("default", null);
		StyleConstants.setFontFamily(def, Font.MONOSPACED);
		
		Style trace = textPane.addStyle("trace", def);
		StyleConstants.setForeground(trace, TRACE);
		
		Style debug = textPane.addStyle("debug", def);
		StyleConstants.setForeground(debug, DEBUG);
		
		Style info = textPane.addStyle("info", def);
		StyleConstants.setForeground(info, INFO);
		
		Style warn = textPane.addStyle("warn", def);
		StyleConstants.setForeground(warn, WARN);
		
		Style error = textPane.addStyle("error", def);
		StyleConstants.setForeground(error, ERROR);
		
		Style fatal = textPane.addStyle("fatal", def);
		StyleConstants.setForeground(fatal, FATAL);
		
		
	}

	/**
	 * Invoked by Log4J to notify about a LoggingEvent.
	 * @param event logging event
	 */
	public void append(LoggingEvent event) {
		
		if (textPane==null) return;
		final String message = this.layout.format(event);
		final String style = getStyleFor(event);
		
		// Update TextPane
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				Document doc = textPane.getDocument();
				int oldPosition = textPane.getCaretPosition();
			
				try {
					doc.insertString(doc.getLength(), message, textPane.getStyle(style));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
				textPane.setDocument(doc);
				
				if (! autoScroll) {
					textPane.setCaretPosition(oldPosition);
				}
				else {
					textPane.setCaretPosition(doc.getLength());
				}
			}
		});
	}

	/**
	 * Enables / disables auto-scroll facility.
	 * 
	 * @param autoScroll
	 */
	public static void setAutoScroll(boolean autoScroll) {
		JTextPaneAppender.autoScroll = autoScroll;
	}


	/**
	 * Returns the proper style for given log event.
	 * 
	 * @param event logging event
	 * @return Style name
	 */
	private String getStyleFor(LoggingEvent event)  {
		
		Level l = event.getLevel();
		
		if (l.equals(Level.TRACE)) {
			return "trace";
		}
		else if (l.equals(Level.DEBUG)) {
			return "debug";
		}
		else if (l.equals(Level.INFO)) {
			return "info";
		}
		else if (l.equals(Level.WARN)) {
			return "warn";
		}
		else if (l.equals(Level.ERROR)) {
			return "error";
		}
		else if (l.equals(Level.FATAL)) {
			return "fatal";
		}
		else {
			return "info";
		}
		
	}
	
	
}
