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
package org.nebulaframework.ui.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;

import com.Ostermiller.util.Browser;

/**
 * Displays the About dialog box for Nebula Swing UIs.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class AboutDialog extends JDialog {

	private static final Log log = LogFactory.getLog(AboutDialog.class);
	
	private static final long serialVersionUID = -5236469264640287797L;

	/**
	 * No-args Constructor constructs and displays 
	 * the about dialog as a modal dialog of the
	 * specified owner frame.
	 * 
	 * @param owner Owner frame
	 */
	public AboutDialog(Frame owner) {
		super(owner, true);
		
		setTitle("About Nebula Framework " + Grid.VERSION);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setSize(320, 500);
		this.setLayout(new BorderLayout());
		
		this.setLocationRelativeTo(this);	// Center on Main UI
		
		/* -- Logo Image Section -- */
		JLabel lbl = new JLabel(new ImageIcon(ClassLoader.getSystemResource("META-INF/resources/nebula-about.png")));
		this.add(lbl, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.Y_AXIS));
		
		this.add(southPanel, BorderLayout.SOUTH);
		
		/* -- Information Section -- */
		JPanel information = new JPanel();
		information.setBorder(BorderFactory.createTitledBorder("Information"));
		information.setLayout(new BorderLayout());
		
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		information.add(westPanel, BorderLayout.WEST);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		information.add(centerPanel, BorderLayout.CENTER);
		
		
		westPanel.add(new JLabel("Version : "));
		centerPanel.add(new JLabel(Grid.VERSION));
		
		westPanel.add(new JLabel("Project Site :   "));
		final String site_link = "http://code.google.com/p/nebulaframework";
		JLabel siteLabel = new JLabel("<html><body><a href=\""+site_link+"\">"+site_link+"</a></body></html>");
		centerPanel.add(siteLabel);
		siteLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					Browser.displayURL(site_link);
				} catch (IOException e) {
					log.warn("[UI] Unable to invoke Browser",e);
				}
			}
		});
		
		westPanel.add(new JLabel("Project Blog :   "));
		final String blog_link = "http://nebulaframework.blogspot.com";
		JLabel blogLabel = new JLabel("<html><body><a href=\""+blog_link+"\">"+blog_link+"</a></body></html>");
		centerPanel.add(blogLabel);
		blogLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					Browser.displayURL(blog_link);
				} catch (IOException e) {
					log.warn("[UI] Unable to invoke Browser",e);
				}
			}
			
		});
		
		westPanel.add(new JLabel("License : "));
		centerPanel.add(new JLabel("Apache 2.0 License"));

		
		southPanel.add(information);
		
		/* -- Copyright Section -- */
		
		JPanel developedBy = new JPanel();
		developedBy.setBorder(BorderFactory.createTitledBorder("Developer / Copyright"));
		developedBy.setLayout(new BorderLayout());
		developedBy.add(new JLabel("Copyright (C) 2008 Yohan Liyanage", JLabel.CENTER), BorderLayout.CENTER);
		southPanel.add(developedBy);
		
		// Resize to fit to size
		this.pack();
		this.setResizable(false);
		
		// Initialize Browser
		Browser.init();
		
		// Show 
		this.setVisible(true);
	}
	
}
