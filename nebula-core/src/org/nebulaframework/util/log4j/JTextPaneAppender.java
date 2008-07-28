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

// TODO FixDoc
// Ref : http://textareaappender.zcage.com/ Copyright 2006-2007 Eric Elfner

public class JTextPaneAppender extends WriterAppender {

	public static final Color TRACE = Color.BLUE;
	public static final Color DEBUG = Color.GREEN;
	public static final Color INFO = Color.WHITE;
	public static final Color WARN = Color.YELLOW;
	public static final Color ERROR = Color.RED;
	public static final Color FATAL = Color.RED;
	
	private static JTextPane textPane = null;
	private static boolean autoScroll = true;
	
	public static void setTextPane(JTextPane textPane) {
		JTextPaneAppender.textPane = textPane;
		
		initialize();
	}


	private static void initialize() {
		if (textPane==null) return;
		
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
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


	public void append(LoggingEvent event) {
		
		if (textPane==null) return;
		final String message = this.layout.format(event);
		final String style = getStyleFor(event);
		
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

	public static void setAutoScroll(boolean autoScroll) {
		JTextPaneAppender.autoScroll = autoScroll;
	}


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
