package org.nebulaframework.util.log4j;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

// TODO FixDoc
public class JLabelAppender extends WriterAppender {

	private static JLabel label = null;

	public static void setLabel(JLabel label) {
		JLabelAppender.label = label;
	}

	public void append(LoggingEvent event) {
		
		if (label==null) return;
		
		final String message = this.layout.format(event);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setText(message);
			}
		});
	}
	

	
}
