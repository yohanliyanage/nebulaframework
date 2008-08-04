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
package org.nebulaframework.ui.swing.node;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.discovery.DiscoveryFailureException;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.ui.swing.AboutDialog;
import org.nebulaframework.ui.swing.UISupport;
import org.nebulaframework.util.log4j.JLabelAppender;
import org.nebulaframework.util.log4j.JTextPaneAppender;
import org.nebulaframework.util.net.NetUtils;
import org.nebulaframework.util.profiling.TimeUtils;

/**
 * The Swing UI for GridNode. This UI exposes basic functionality of 
 * GridNode, and allows users to easily manage the Node.
 * <p>
 * However, for more advanced uses, such as embedding
 * GridNodes, consider using the API.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class NodeMainUI extends JFrame {

	private static final long serialVersionUID = 1574154489795768861L;

	private static final Log log = LogFactory.getLog(NodeMainUI.class);
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 575;
	
	// Components
	private static Map<String, JComponent> components = new HashMap<String, JComponent>();
	
	// Job History List
	private JobHistoryListModel historyList = new JobHistoryListModel();
	
	// Total Execution Time
	private long executionTime = 0L;
	
	// Auto Discovery Enabled
	private boolean autodiscover = true;
	
	// Last Discovery Attempt Time stamp
	private Long lastDiscoveryAttempt = System.currentTimeMillis();
	
	// Currently Active JobId
	private String activeJobId = null;
	
	
	private TrayIcon trayIcon;
	
	private Image idleIcon;
	private Image activeIcon;
	
	/**
	 * Constructs a GridNode UI.
	 * 
	 * @throws HeadlessException if UI not supported.
	 */
	public NodeMainUI() throws HeadlessException {
		super();
		setupUI();
	}

	/**
	 * UI Setup operations.
	 */
	private void setupUI() {
		setTitle("Nebula Grid - Execution Node");
		setSize(WIDTH, HEIGHT);
		
		setJMenuBar(setupMenu());
		setLayout(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		
		tabs.addTab("Control Center", setupGeneral());
		tabs.addTab("History", setupHistory());
		
		resetActiveJobInfo();
		setStatus("Not Connected");
		updateGridInfo();
		updateExecutionTime();
		
		setupTrayIcon(this);
	}

	/**
	 * Setup System Tray Icon.
	 * 
	 * @param frame owner frame
	 */
	private void setupTrayIcon(final JFrame frame) {
		
		idleIcon = Toolkit.getDefaultToolkit()
			.getImage(ClassLoader.getSystemResource("META-INF/resources/node_inactive.png"));
		
		activeIcon = Toolkit.getDefaultToolkit()
			.getImage(ClassLoader.getSystemResource("META-INF/resources/node_active.png"));
		
		frame.setIconImage(idleIcon);
		
		// If system tray is supported by OS
		if (SystemTray.isSupported()) {
			trayIcon = new TrayIcon(idleIcon,"Nebula Grid Node", createTrayPopup());
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton()==MouseEvent.BUTTON1) {
						if (!frame.isVisible()) {
							frame.setVisible(true);
						}
						
						frame.setExtendedState(JFrame.NORMAL);
						frame.requestFocus();
						frame.toFront();
					}
				}
				
			});
			
			try {
				SystemTray.getSystemTray().add(trayIcon);
			} catch (AWTException ae) {
				log.debug("[UI] Unable to Initialize Tray Icon");
				return;
			}
			
			frame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowIconified(WindowEvent e) {
					// Hide (can be shown using tray icon)
					frame.setVisible(false);
				}
				
			});
		}
	
	}

	/**
	 * System Tray Icon Pop Up Menu
	 * 
	 * @return PopupMenu
	 */
	private PopupMenu createTrayPopup() {
		PopupMenu trayPopup = new PopupMenu();
		
		
		// About
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showAbout();
			}
			
		});
		trayPopup.add(aboutItem);
		
		trayPopup.addSeparator();
		
		// Shutdown Node
		MenuItem shutdownItem = new MenuItem("Shutdown");
		shutdownItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doShutdownNode();
			}
			
		});
		trayPopup.add(shutdownItem);
		
		return trayPopup;
	}

	/**
	 * Displays busy icon in System tray
	 */
	private void showBusyIcon() {
		if (trayIcon!=null) trayIcon.setImage(activeIcon);
	}
	
	/**
	 * Displays Idle icon in System tray
	 */
	private void showIdleIcon() {
		if (trayIcon!=null) trayIcon.setImage(idleIcon);
	}
	
	/**
	 * Removes System Tray icon.
	 */
	private void removeIcon() {
		if (SystemTray.isSupported()) {
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}

	/**
	 * Setup Menu Bar
	 * @return JMenu Bar
	 */
	private JMenuBar setupMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		/* -- GridNode Menu -- */
		
		JMenu gridNodeMenu = new JMenu("GridNode");
		gridNodeMenu.setMnemonic(KeyEvent.VK_N);
		menuBar.add(gridNodeMenu);
		
		// Discover 
		JMenuItem clusterDiscoverItem = new JMenuItem("Disover and Connect Clusters");
		clusterDiscoverItem.setMnemonic(KeyEvent.VK_D);
		clusterDiscoverItem.setAccelerator(KeyStroke
		                				.getKeyStroke(KeyEvent.VK_F2, 0));
		gridNodeMenu.add(clusterDiscoverItem);
		clusterDiscoverItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDiscover(false);
				((JCheckBoxMenuItem)getUIElement("menu.node.autodiscover")).setSelected(false);
			}
		});
		addUIElement("menu.node.discover", clusterDiscoverItem);	// Add to components map
		
		// Auto-Discovery
		final JCheckBoxMenuItem autodiscoveryItem = new JCheckBoxMenuItem("Auto Discover");
		autodiscoveryItem.setMnemonic(KeyEvent.VK_A);
		autodiscoveryItem.setSelected(true);
		gridNodeMenu.add(autodiscoveryItem);
		autodiscoveryItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autodiscover = autodiscoveryItem.isSelected();
			}
		});
		addUIElement("menu.node.autodiscover", autodiscoveryItem);	// Add to components map
		
		gridNodeMenu.addSeparator();

		// Cluster-> Shutdown
		JMenuItem nodeShutdownItem = new JMenuItem("Shutdown", 'u');
		nodeShutdownItem.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_F6, 0));
		nodeShutdownItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doShutdownNode();
			}
		});
		gridNodeMenu.add(nodeShutdownItem);
		addUIElement("menu.node.shutdown",nodeShutdownItem);	// Add to components map
		
		/* -- Options Menu -- */
		JMenu optionsMenu = new JMenu("Options");
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(optionsMenu);
		
		// Configuration
		JMenuItem optionsConfigItem = new JMenuItem("Configuration...", 'C');
		optionsConfigItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showConfiguration();
			}
		});
		optionsMenu.add(optionsConfigItem);
		optionsConfigItem.setEnabled(false);	// TODO Create Configuration Options
		
		
		/* -- Help Menu -- */
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);
		
		// Help Contents
		JMenuItem helpContentsItem = new JMenuItem("Help Contents", 'H');
		helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpContentsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHelp();
			}
		});
		helpMenu.add(helpContentsItem);
		
		helpMenu.addSeparator();
		
		JMenuItem helpAboutItem = new JMenuItem("About", 'A');
		helpAboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAbout();
			}
		});
		helpMenu.add(helpAboutItem);
		
		return menuBar;
	}


	/**
	 * Setup General (Control Center) Tab
	 * 
	 * @return JPanel for Control Center
	 */
	private JPanel setupGeneral() {
		
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new BorderLayout());

		/* -- Stats Panel -- */
		JPanel statsPanel = new JPanel();
		generalPanel.add(statsPanel, BorderLayout.NORTH);

		
		statsPanel.setLayout(new GridLayout(0, 2, 10, 10));
		
		JPanel eastPanel = new JPanel();
		statsPanel.add(eastPanel, BorderLayout.EAST);
		eastPanel.setLayout(new BorderLayout());
		
		JPanel westPanel = new JPanel();
		statsPanel.add(westPanel, BorderLayout.WEST);
		westPanel.setLayout(new BorderLayout());
		
		// Grid Information Panel
		JPanel gridInfoPanel = new JPanel();
		eastPanel.add(gridInfoPanel, BorderLayout.NORTH);
		
		gridInfoPanel.setBorder(BorderFactory.createTitledBorder("Grid Information"));
		gridInfoPanel.setLayout(new GridLayout(0, 2, 10, 10));
		
		JLabel nodeIdLabel = new JLabel("Node ID :");
		gridInfoPanel.add(nodeIdLabel);
		JLabel nodeId = new JLabel("#nodeid#");
		gridInfoPanel.add(nodeId);
		addUIElement("general.stats.nodeid", nodeId);	// Add to components map
		
		JLabel nodeIpLabel = new JLabel("Node IP :");
		gridInfoPanel.add(nodeIpLabel);
		JLabel nodeIp = new JLabel("#nodeip#");
		gridInfoPanel.add(nodeIp);
		addUIElement("general.stats.nodeip", nodeIp);	// Add to components map
		

		
		JLabel clusterIdLabel = new JLabel("Cluster ID :");
		gridInfoPanel.add(clusterIdLabel);
		JLabel clusterId = new JLabel("#clusterid#");
		gridInfoPanel.add(clusterId);
		addUIElement("general.stats.clusterid", clusterId);	// Add to components map
		
		JLabel clusterServiceLabel = new JLabel("Cluster Service :");
		gridInfoPanel.add(clusterServiceLabel);
		JLabel clusterService = new JLabel("#clusterservice#");
		gridInfoPanel.add(clusterService);
		addUIElement("general.stats.clusterservice", clusterService);	// Add to components map
		
		
		// Node Status Panel 
		JPanel nodeStatusPanel = new JPanel();
		eastPanel.add(nodeStatusPanel, BorderLayout.SOUTH);
		
		nodeStatusPanel.setBorder(BorderFactory.createTitledBorder("GridNode Status"));
		nodeStatusPanel.setLayout(new GridLayout(0, 2, 10, 10));
		
		JLabel statusLabel = new JLabel("Status :");
		nodeStatusPanel.add(statusLabel);
		JLabel status = new JLabel("#status#");
		nodeStatusPanel.add(status);
		addUIElement("general.stats.status", status);	// Add to components map
		
		JLabel uptimeLabel = new JLabel("Node Up Time :");
		nodeStatusPanel.add(uptimeLabel);
		JLabel uptime = new JLabel("#uptime#");
		nodeStatusPanel.add(uptime);
		addUIElement("general.stats.uptime", uptime);	// Add to components map
		
		JLabel execTimeLabel = new JLabel("Execution Time :");
		nodeStatusPanel.add(execTimeLabel);
		JLabel execTime = new JLabel("#exectime#");
		nodeStatusPanel.add(execTime);
		addUIElement("general.stats.exectime", execTime);	// Add to components map
		
		// Execution Statistics Panel
		JPanel execStatsPanel = new JPanel();
		westPanel.add(execStatsPanel, BorderLayout.NORTH);
		
		execStatsPanel.setLayout(new GridLayout(0, 2, 10, 10));
		execStatsPanel.setBorder(BorderFactory.createTitledBorder("Execution Statistics"));
		
		JLabel totalJobsLabel = new JLabel("Total Jobs :");
		execStatsPanel.add(totalJobsLabel);
		JLabel totalJobs = new JLabel("0");
		execStatsPanel.add(totalJobs);
		addUIElement("general.stats.totaljobs", totalJobs);	// Add to components map
		
		JLabel totalTasksLabel = new JLabel("Total Tasks :");
		execStatsPanel.add(totalTasksLabel);
		JLabel totalTasks = new JLabel("0");
		execStatsPanel.add(totalTasks);
		addUIElement("general.stats.totaltasks", totalTasks);	// Add to components map
		
		JLabel totalBansLabel = new JLabel("Banments :");
		execStatsPanel.add(totalBansLabel);
		JLabel totalBans = new JLabel("0");
		execStatsPanel.add(totalBans);
		addUIElement("general.stats.totalbans", totalBans);	// Add to components map
		
		// Execution Active Job Panel
		JPanel activeJobPanel = new JPanel();
		westPanel.add(activeJobPanel, BorderLayout.SOUTH);
		
		activeJobPanel.setLayout(new GridLayout(0, 2, 10, 10));
		activeJobPanel.setBorder(BorderFactory.createTitledBorder("Active Job"));
		
		JLabel jobNameLabel = new JLabel("GridJob Name :");
		activeJobPanel.add(jobNameLabel);
		JLabel jobName = new JLabel("#jobname#");
		activeJobPanel.add(jobName);
		addUIElement("general.stats.jobname", jobName);	// Add to components map
		
		JLabel durationLabel = new JLabel("Duration :");
		activeJobPanel.add(durationLabel);
		JLabel duration = new JLabel("#duration#");
		activeJobPanel.add(duration);
		addUIElement("general.stats.duration", duration);	// Add to components map
		
		JLabel tasksLabel = new JLabel("Tasks Executed :");
		activeJobPanel.add(tasksLabel);
		JLabel tasks = new JLabel("#xyz#");
		activeJobPanel.add(tasks);
		addUIElement("general.stats.tasks", tasks);	// Add to components map
		
		JLabel failuresLabel = new JLabel("Failures :");
		activeJobPanel.add(failuresLabel);
		JLabel failures = new JLabel("#failures#");
		activeJobPanel.add(failures);
		addUIElement("general.stats.failures", failures);	// Add to components map
		
		
		/* -- Log Panel -- */
		JPanel logPanel = new JPanel();
		generalPanel.add(logPanel, BorderLayout.CENTER);
		
		logPanel.setLayout(new BorderLayout());
		logPanel.setBorder(BorderFactory.createTitledBorder("Log Output"));
		JTextPane logTextPane = new JTextPane();
		logTextPane.setEditable(false);
		logTextPane.setBackground(Color.BLACK);
		logTextPane.setForeground(Color.WHITE);
		
		logPanel.add(new JScrollPane(logTextPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
						BorderLayout.CENTER);
		addUIElement("general.log", logTextPane);	// Add to component map
		
		JPanel logOptionsPanel = new JPanel();
		logPanel.add(logOptionsPanel, BorderLayout.SOUTH);
		logOptionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		final JCheckBox logScrollCheckbox = new JCheckBox("Auto-Scroll Log");
		logScrollCheckbox.setSelected(true);
		logScrollCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextPaneAppender.setAutoScroll(logScrollCheckbox.isSelected());
			}
			
		});
		logOptionsPanel.add(logScrollCheckbox);
		
		
		// Enable Logging
		JTextPaneAppender.setTextPane(logTextPane);
		

		/* -- Buttons Panel -- */
		JPanel buttonsPanel = new JPanel();
		generalPanel.add(buttonsPanel, BorderLayout.SOUTH);
		
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// Shutdown Button
		JButton shutdownButton = new JButton("Shutdown");
		buttonsPanel.add(shutdownButton);
		shutdownButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doShutdownNode();
			}
			
		});
		
		// Start Up time Thread
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				long start = System.currentTimeMillis();
				
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.warn("Interrupted Exception in Up Time Thread",e);
					}
					
					
					final String uptime = TimeUtils.timeDifference(start);
					
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							JLabel upTime = getUIElement("general.stats.uptime");
							upTime.setText(uptime);
						}
						
					});
				}
				
			}
		});
		t.setDaemon(true);
		t.start();
		
		// Auto-Discovery Thread
		Thread autoDiscovery = new Thread(new Runnable() {
			public void run() {
				
				
				while (true) {
					try {
						
						// Attempt every 30 seconds
						Thread.sleep(30000);
						
					} catch (InterruptedException e) {
						log.warn("Interrupted Exception in Up Time Thread",e);
					}
					
					if (autodiscover && (!Grid.isNode())) {
						// 30 Second Intervals
						doDiscover(true);
					}
				
				}
			}
			});
		autoDiscovery.setDaemon(true);
		autoDiscovery.start();
		
		return generalPanel;
	}

	/**
	 * Setup Job History Tab Pane
	 * 
	 * @return JPanel for History tab
	 */
	private JPanel setupHistory() {
		
		JPanel historyPanel = new JPanel();
		historyPanel.setLayout(new BorderLayout(10,10));

		
		/* -- Job List  -- */
		JPanel jobListPanel = new JPanel();
		historyPanel.add(jobListPanel, BorderLayout.CENTER);
		
		jobListPanel.setLayout(new BorderLayout(10,10));
		jobListPanel.setBorder(BorderFactory.createTitledBorder("Grid Jobs"));
		
		final JList jobList = new JList(historyList);
		JScrollPane jobListScroll = new JScrollPane(jobList);
		jobListPanel.add(jobListScroll,BorderLayout.CENTER);
		jobListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jobListScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		addUIElement("history.joblist", jobList);

		jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jobList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				// Ignore intermediate events
				if (e.getValueIsAdjusting()) return;
				
				displayJobInfo(jobList.getSelectedValue());
			}
			
		});
		
		
		JPanel jobInfoPanel = new JPanel();
		historyPanel.add(jobInfoPanel, BorderLayout.SOUTH);
		
		jobInfoPanel.setLayout(new BorderLayout(10,10));
		jobInfoPanel.setBorder(BorderFactory.createTitledBorder("Job Information"));
		
		JPanel centerPanel = new JPanel();
		
		jobInfoPanel.add(centerPanel, BorderLayout.CENTER);
		
		centerPanel.setLayout(new GridLayout(0,4, 10,10));
		
		JLabel jobIdLabel = new JLabel("GridJob ID :");
		centerPanel.add(jobIdLabel);
		JLabel jobId = new JLabel("N/A");
		centerPanel.add(jobId);
		addUIElement("history.jobid", jobId);	// Add to components map
		
		JLabel jobNameLabel = new JLabel("GridJob Name :");
		centerPanel.add(jobNameLabel);
		JLabel jobName = new JLabel("N/A");
		centerPanel.add(jobName);
		addUIElement("history.jobname", jobName);	// Add to components map
		
		JLabel startTimeLabel = new JLabel("Start Time :");
		centerPanel.add(startTimeLabel);
		JLabel startTime = new JLabel("N/A");
		centerPanel.add(startTime);
		addUIElement("history.starttime", startTime);	// Add to components map
		
		JLabel durationLabel = new JLabel("Duration :");
		centerPanel.add(durationLabel);
		JLabel duration = new JLabel("N/A");
		centerPanel.add(duration);
		addUIElement("history.duration", duration);	// Add to components map
		
		JLabel tasksLabel = new JLabel("Tasks Executed :");
		centerPanel.add(tasksLabel);
		JLabel tasks = new JLabel("N/A");
		centerPanel.add(tasks);
		addUIElement("history.tasks", tasks);	// Add to components map
		
		JLabel failuresLabel = new JLabel("Failures :");
		centerPanel.add(failuresLabel);
		JLabel failures = new JLabel("N/A");
		centerPanel.add(failures);
		addUIElement("history.failures", failures);	// Add to components map
		
		// Place Holders
		centerPanel.add(new JLabel());
		centerPanel.add(new JLabel());
		
		return historyPanel;
	}
	
	/**
	 * Resets the active Job Info fields
	 */
	protected void resetActiveJobInfo() {
		
		JLabel jobName = getUIElement("general.stats.jobname");
		JLabel duration = getUIElement("general.stats.duration");
		JLabel tasks = getUIElement("general.stats.tasks");
		JLabel failure = getUIElement("general.stats.failures");
		
		jobName.setText("N/A");
		duration.setText("N/A");
		tasks.setText("N/A");
		failure.setText("N/A");
		
	}
	
	/**
	 * Updates the GridNode Status to given status
	 * @param status status text
	 */
	protected void setStatus(String status) {
		((JLabel)getUIElement("general.stats.status")).setText(status);
	}
	
	/**
	 * Displays the Job info for given {@link JobHistoryElement} in the
	 * History Tab Pane's fields.
	 *  
	 * @param obj JobHistoryElement
	 */
	protected void displayJobInfo(Object obj) {
		
		if (!(obj instanceof JobHistoryElement)) return;
		JobHistoryElement element = (JobHistoryElement) obj;
		
		JLabel jobId = getUIElement("history.jobid");
		JLabel jobName = getUIElement("history.jobname");
		JLabel starttime = getUIElement("history.starttime");
		JLabel duration = getUIElement("history.duration");
		JLabel tasks = getUIElement("history.tasks");
		JLabel failures = getUIElement("history.failures");
		
		jobId.setText(element.getJobId());
		jobName.setText(element.getJobName());
		starttime.setText(element.getStartTime());
		duration.setText(element.getDuration());
		tasks.setText(String.valueOf(element.getTasks()));
		failures.setText(String.valueOf(element.getFailures()));
		
	}

	/**
	 * Updates GridInfo in Control Center
	 */
	private void updateGridInfo() {
		
		JLabel nodeId = getUIElement("general.stats.nodeid");
		JLabel nodeIp = getUIElement("general.stats.nodeip");
		JLabel clusterId = getUIElement("general.stats.clusterid");
		JLabel clusterService = getUIElement("general.stats.clusterservice");
		
		if (Grid.isNode()) {
			// If connected
			GridNode instance = GridNode.getInstance();
			
			nodeId.setText(instance.getId().toString());
			nodeIp.setText(NetUtils.getLocalHostAddress());
			clusterId.setText(instance.getClusterId().toString());
			clusterService.setText(instance.getClusterUrl());
			
			getUIElement("menu.node.discover").setEnabled(false);
			
			registerHooks(instance);
			setStatus("Idle");

			
		}
		else {
			// If not connected
			nodeId.setText("Not Connected");
			nodeIp.setText("Not Connected");
			clusterId.setText("Not Connected");
			clusterService.setText("Not Connected");
			setStatus("Not Connected");
			getUIElement("menu.node.discover").setEnabled(true);
		}
	}
	
	/**
	 * Registers Service Event Hooks to update fields, once
	 * connected
	 * 
	 * @param instance GridNode instance
	 */
	private void registerHooks(GridNode instance) {
		
		// Disconnection Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			@Override
			public void onServiceEvent(ServiceMessage message) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						updateGridInfo();
					}
					
				});
			}
			
		}, instance.getClusterId().toString(), ServiceMessageType.NODE_DISCONNECTED);
		
		// Job Start Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			@Override
			public void onServiceEvent(ServiceMessage message) {
				
				final String jobId = message.getMessage();
				final String jobName = GridNode.getInstance()
									.getJobExecutionService()
									.getJobName(jobId);
				
				activeJobId = jobId;
				
				final long timeStart = System.currentTimeMillis();
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						
						showBusyIcon();
						setStatus("Executing Job");
						
						((JLabel) getUIElement("general.stats.jobname"))
								.setText(jobName);

						((JLabel) getUIElement("general.stats.tasks")).setText("0");
						((JLabel) getUIElement("general.stats.failures")).setText("0");						
					}
					
				});
				
				
				// Duration Update Thread
				new Thread(new Runnable() {

					@Override
					public void run() {
						
						
						
						while (jobId.equals(activeJobId)) {
							
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									((JLabel) getUIElement("general.stats.duration"))
										.setText(TimeUtils.timeDifference(timeStart));
								}
								
							});
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								log.warn("Interrupted", e);
							}
							
						}
					}
					
				}).start();
				
				// Job End Hook
				ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

					@Override
					public void onServiceEvent(ServiceMessage message) {
						
						activeJobId = null;
						
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								
								try {
									
									JLabel durationLabel = getUIElement("general.stats.duration");
									JLabel jobnameLabel = getUIElement("general.stats.jobname");
									JLabel tasksLabel = getUIElement("general.stats.tasks");
									JLabel failuresLabel = getUIElement("general.stats.failures");
									JLabel totalJobsLabel = getUIElement("general.stats.totaljobs");
									
									int tasks = Integer.parseInt(tasksLabel.getText());
									int failures = Integer.parseInt(failuresLabel.getText());
									
									
									// Create and enter Job History Element
									JobHistoryElement element =
											new JobHistoryElement(jobName, 
											                      jobId, 
											                      TimeUtils.formatDate(timeStart), 
											                      durationLabel.getText(), 
											                      tasks, 
											                      failures);
									
									historyList.addJobHistoryElement(element);
									
									// Update Job Info Fields
									durationLabel.setText("N/A");
									jobnameLabel.setText("N/A");
									tasksLabel.setText("N/A");
									failuresLabel.setText("N/A");
									
									// Update Total Jobs Count
									int totalJobs = Integer.parseInt(totalJobsLabel.getText()) + 1;
									totalJobsLabel.setText(String.valueOf(totalJobs));
									
									showIdleIcon();
									setStatus("Idle");
									
								} catch (Exception e) {
									log.warn("[UI] Exception ",e);
								}
								
								
							}
							
						});
					}
					
				}, jobId, ServiceMessageType.LOCAL_JOBFINISHED);
			}
			
		}, ServiceMessageType.LOCAL_JOBSTARTED);
		

		// Task Executed Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						
						try {
							// Total Tasks
							JLabel totalTasksLabel = getUIElement("general.stats.totaltasks");
							int totalTasks = Integer.parseInt(totalTasksLabel.getText()) + 1;	
							totalTasksLabel.setText(String.valueOf(totalTasks));
							
							// If active job, update task count
							if (message.getMessage().equals(activeJobId)) {
								JLabel tasksLabel = getUIElement("general.stats.tasks");
								int tasks = Integer.parseInt(tasksLabel.getText()) + 1;	
								tasksLabel.setText(String.valueOf(tasks));		
							}
						} catch (Exception e) {
							log.warn("[UI] Exception ",e);
						}
					}
				});
			}
			
		}, ServiceMessageType.LOCAL_TASKDONE);
		
		// Task Failed Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						
						try {
							// If active job, update task count
							if (message.getMessage().equals(activeJobId)) {
								JLabel failuresLabel = getUIElement("general.stats.failures");
								int fails = Integer.parseInt(failuresLabel.getText()) + 1;	
								failuresLabel.setText(String.valueOf(fails));		
							}
						} catch (Exception e) {
							log.warn("[UI] Exception ",e);
						}
					}
				});
			}
		}, ServiceMessageType.LOCAL_TASKFAILED);
		
		// Task Execution Time Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
				try {
					executionTime += Long.parseLong(message.getMessage());
				} catch (Exception e) {
					log.warn("[UI] Exception ",e);
				}

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
							updateExecutionTime();
					}
				});
			}
		}, ServiceMessageType.LOCAL_TASKEXEC);
		
		// Node Banned Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
				// If not relevant to node, ignore
				if (!message.getMessage().startsWith(GridNode.getInstance().getId().toString())) {
					return;
				}
				
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						
						try {
							// If active job, update task count
							JLabel bansLabel = getUIElement("general.stats.totalbans");
							int bans = Integer.parseInt(bansLabel.getText()) + 1;	
							bansLabel.setText(String.valueOf(bans));		
						} catch (Exception e) {
							log.warn("[UI] Exception ",e);
						}
					}
				});
			}
		},ServiceMessageType.NODE_BANNED);
		
		
	}

	/**
	 * Updates the total execution time field
	 */
	private void updateExecutionTime() {
		((JLabel) getUIElement("general.stats.exectime"))
			.setText(TimeUtils.buildTimeString(executionTime));
	}
	
	/**
	 * Adds a Component to the components map of this object.
	 * 
	 * @param identifier Component Identifier
	 * @param component Component
	 */
	protected void addUIElement(String identifier, JComponent component) {
		components.put(identifier, component);
	}

	/**
	 * Removes a Component from the components map of this object.
	 * 
	 * @param identifier Component Identifier
	 */
	protected void removeUIElement(String identifier) {
		components.remove(identifier);
	}

	/**
	 * Returns the UI Element for given Identifier.
	 * 
	 * @param <T> Expected Type of UI Element
	 * @param identifier Element Identifier
	 * 
	 * @return UI Element Instance
	 * 
	 * @throws IllegalArgumentException if invalid identifier
	 * @throws ClassCastException if invalid type
	 */
	@SuppressWarnings("unchecked")
	protected <T extends JComponent> T getUIElement(String identifier) throws IllegalArgumentException, ClassCastException {
		if (! components.containsKey(identifier)) throw new IllegalArgumentException("Invalid Identifier");
		return (T) components.get(identifier);
	}
	
	
	/* -- ACTIONS --*/
	/**
	 * Shutdowns the GridNode
	 */
	protected void doShutdownNode() {
		
		int result = JOptionPane.showConfirmDialog(this, "Are You Sure to Shutdown Node ?","Nebula Grid", JOptionPane.YES_NO_OPTION);
		
		// If user chose no, abort shutdown
		if (result==JOptionPane.NO_OPTION) return;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					
					// If connected to Cluster, unregister
					if (Grid.isNode()) {
						GridNode.getInstance().shutdown();
					}
					removeIcon();
					System.exit(0);
				} catch (Exception e) {
					log.fatal("[GridNode] Exception while Shutting Down",e);
					System.exit(1);
				}
			}
			
		}).start();
	}
	

	/**
	 * Attempts to discover a cluster.
	 * 
	 * @param silent if silent, no message will be displayed
	 */
	protected void doDiscover(final boolean silent) {
		new Thread(new Runnable() {

			public void run() {
				
				synchronized (lastDiscoveryAttempt) {
					try {
						showBusyIcon();
						
						getUIElement("menu.node.discover").setEnabled(false);
						getUIElement("menu.node.autodiscover").setEnabled(false);
						
						// Attempt start GridNode
						Grid.startGridNode();
						
					} catch ( DiscoveryFailureException e) {
						if (!silent) {
							JOptionPane.showMessageDialog(NodeMainUI.this, "Unable to discover any Cluster");
						}
					} catch (Exception e) {
						if (!silent) {
							JOptionPane.showMessageDialog(NodeMainUI.this, "Exception while attempting Cluster Connection");
						}
						log.error("[GridNode] Exception while connecting", e);
					} finally {
						
						getUIElement("menu.node.discover").setEnabled(true);
						getUIElement("menu.node.autodiscover").setEnabled(true);
						
						updateGridInfo();
						
						showIdleIcon();
						
						lastDiscoveryAttempt = System.currentTimeMillis();
					}
				}
			}
			
		}).start();
	}
	
	/**
	 * Displays About Dialog
	 */
	protected void showAbout() {
		try {
			new AboutDialog(this);
		} catch (Exception e) {
			log.error(e);
			JOptionPane.showMessageDialog(this, "Unable to display About dialog");
		}
	}

	/**
	 * Displays Help Contents
	 */
	protected void showHelp() {
		UISupport.displayHelp(this);
	}

	/**
	 * Displays Configuration dialog
	 */
	protected void showConfiguration() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Invoked on Shutdown Event
	 */
	protected void onShutdown() {
		showBusyIcon();
		doShutdownNode();
		showIdleIcon();

	}
	


	/**
	 * Class which represents a Job History Element, inserted into the
	 * JobHistory List Model.
	 * 
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	protected static class JobHistoryElement {
		
		private String jobName;
		private String jobId;
		private String startTime;
		private String duration;
		private int tasks;
		private int failures;
		
		/**
		 * Constructs a JobHistoryElement with given field
		 * values.
		 * 
		 * @param jobName job name
		 * @param jobId job id
		 * @param startTime job start time
		 * @param duration job duration
		 * @param tasks tasks executed for job on this node
		 * @param failures total failures for job on this node
		 */
		public JobHistoryElement(String jobName, String jobId,
				String startTime, String duration, int tasks, int failures) {
			super();
			this.jobName = jobName;
			this.jobId = jobId;
			this.startTime = startTime;
			this.duration = duration;
			this.tasks = tasks;
			this.failures = failures;
		}

		/**
		 * Returns the user friendly name for Job
		 * 
		 * @return name
		 */
		public String getJobName() {
			return jobName;
		}
		
		/**
		 * Returns the JobId for Job
		 * @return job id
		 */
		public String getJobId() {
			return jobId;
		}
		
		/**
		 * Returns the start time for job
		 * @return start time
		 */
		public String getStartTime() {
			return startTime;
		}
		
		/**
		 * Returns the duration for job
		 * @return duration
		 */
		public String getDuration() {
			return duration;
		}
		
		/**
		 * Returns the number of tasks executed for job on this node
		 * @return tasks
		 */
		public int getTasks() {
			return tasks;
		}
		
		/**
		 * Returns the number of tasks failed for job on this node
		 * @return tasks
		 */
		public int getFailures() {
			return failures;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (!( obj instanceof JobHistoryElement)) return false;
			
			JobHistoryElement element = (JobHistoryElement) obj;
			return this.jobId.equals(element.getJobId());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return this.jobId.hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "[" + this.startTime + "] " + this.jobName;
		}

	}
	
	/**
	 * ListModel for JobHistory List.
	 * 
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	protected static class JobHistoryListModel extends AbstractListModel {

		private static final long serialVersionUID = 4872462645102776860L;
		
		// Job History Elements
		private List<JobHistoryElement> elements = new ArrayList<JobHistoryElement>();
		
		/**
		 * Adds a new JobHistoryElement.
		 * 
		 * @param element new element
		 */
		public void addJobHistoryElement(JobHistoryElement element) {
			elements.add(element);
			fireIntervalAdded(this, this.getSize()-1, this.getSize()-1);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getElementAt(int index) {
			return elements.get(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getSize() {
			return elements.size();
		}
		
	}
	
	/**
	 * Displays Splash Screen.
	 * 
	 * @return Splash Screen reference
	 */
	public static JWindow showSplash() {
		
		JWindow splash = new JWindow();
		splash.setSize(400, 250);
		splash.setLayout(null);
		
		JLabel status = new JLabel("Developed by Yohan Liyanage, 2008");
		JLabelAppender.setLabel(status);
		status.setFont(new Font("sansserif", Font.PLAIN, 10));
		status.setSize(350, 30);
		status.setLocation(10, 220);
		splash.add(status);
		
		JLabel lbl = new JLabel(new ImageIcon(ClassLoader.getSystemResource("META-INF/resources/nebula-startup.png")));
		lbl.setSize(400, 250);
		lbl.setLocation(0, 0);
		splash.add(lbl);
		
		splash.setVisible(true);
		splash.setLocationRelativeTo(null);
		
		return splash;
	}
	
	/**
	 * Creates a GridNode UI
	 * @return UI
	 */
	public static NodeMainUI create() {
		
		final NodeMainUI ui = new NodeMainUI();
		ui.setLocationRelativeTo(null);

		ui.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				ui.onShutdown();
			}
			
		});
		
		return ui;
	}

	
	
}
