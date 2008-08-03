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

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom Log4J Appender which displays log messages in a given
 * JLabel.
 * <p>
 * This was implemented following the basic concept provided by
 * <a href="http://textareaappender.zcage.com/">Eric Elfner</a>
 * regarding appending Log4J output to a JTextArea.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class JLabelAppender extends WriterAppender {

	// Label to display messages
	private static JLabel label = null;

	/**
	 * Sets the target JLabel
	 * @param label target
	 */
	public static void setLabel(JLabel label) {
		JLabelAppender.label = label;
	}

	/**
	 * Invoked by Log4J to notify about a LoggingEvent.
	 * @param event logging event
	 */
	public void append(LoggingEvent event) {
		
		// Return if label not set
		if (label==null) return;
		
		// Format log message
		final String message = this.layout.format(event);
		
		// Display on Label
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setText(message);
			}
		});
	}
	

	
}
