package org.nebulaframework.ui.swing.GridNode;

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
import org.nebulaframework.util.log4j.JLabelAppender;
import org.nebulaframework.util.log4j.JTextPaneAppender;
import org.nebulaframework.util.net.NetUtils;
import org.nebulaframework.util.profiling.TimeUtils;

public class NodeMainUI extends JFrame {

	private static final long serialVersionUID = 1574154489795768861L;

	private static final Log log = LogFactory.getLog(NodeMainUI.class);
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 575;
	
	private static Map<String, JComponent> components = new HashMap<String, JComponent>();
	
	private JobHistoryListModel historyList = new JobHistoryListModel();
	
	private long executionTime = 0L;
	private boolean autodiscover = true;
	
	private Long lastDiscoveryAttempt = System.currentTimeMillis();
	
	private String activeJobId = null;
	
	private TrayIcon trayIcon;
	
	private Image idleIcon;
	private Image activeIcon;
	
	public NodeMainUI() throws HeadlessException {
		super();
		setupUI();
	}

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

	private void setupTrayIcon(final JFrame frame) {
		
		idleIcon = Toolkit.getDefaultToolkit()
			.getImage(ClassLoader.getSystemResource("META-INF/resources/node_inactive.png"));
		
		activeIcon = Toolkit.getDefaultToolkit()
			.getImage(ClassLoader.getSystemResource("META-INF/resources/node_active.png"));
		
		frame.setIconImage(idleIcon);
		
		if (SystemTray.isSupported()) {
			trayIcon = new TrayIcon(idleIcon,"Nebula Grid Node", createTrayPopup());
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!frame.isVisible()) {
						frame.setVisible(true);
						frame.setExtendedState(JFrame.NORMAL);
					}
					frame.requestFocus();
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

	private PopupMenu createTrayPopup() {
		PopupMenu trayPopup = new PopupMenu();
		
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showAbout();
			}
			
		});
		trayPopup.add(aboutItem);
		
		trayPopup.addSeparator();
		
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

	private void showBusyIcon() {
		if (trayIcon!=null) trayIcon.setImage(activeIcon);
	}
	
	private void showIdleIcon() {
		if (trayIcon!=null) trayIcon.setImage(idleIcon);
	}


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
	
	protected void setStatus(String status) {
		((JLabel)getUIElement("general.stats.status")).setText(status);
	}
	
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


	private void updateGridInfo() {
		
		JLabel nodeId = getUIElement("general.stats.nodeid");
		JLabel nodeIp = getUIElement("general.stats.nodeip");
		JLabel clusterId = getUIElement("general.stats.clusterid");
		JLabel clusterService = getUIElement("general.stats.clusterservice");
		
		if (Grid.isNode()) {
			
			GridNode instance = GridNode.getInstance();
			
			nodeId.setText(instance.getId().toString());
			nodeIp.setText(NetUtils.getLocalHostAddress());
			clusterId.setText(instance.getClusterId().toString());
			clusterService.setText(instance.getClusterUrl());
			
			getUIElement("menu.node.discover").setEnabled(false);
			
			registerHooks(instance);
			

			
		}
		else {
			nodeId.setText("Not Connected");
			nodeIp.setText("Not Connected");
			clusterId.setText("Not Connected");
			clusterService.setText("Not Connected");
			getUIElement("menu.node.discover").setEnabled(true);
		}
	}
	
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
		
		
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			@Override
			public void onServiceEvent(ServiceMessage message) {
				
				final String jobId = message.getMessage();
				final String jobName = GridNode.getInstance()
									.getJobExecutionService()
									.getJobName(jobId);
				
				final long timeStart = System.currentTimeMillis();
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						
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
								} catch (Exception e) {
									log.warn("[UI] Exception ",e);
								}
								
								
							}
							
						});
					}
					
				}, jobId, ServiceMessageType.LOCAL_JOBFINISHED);
			}
			
		}, ServiceMessageType.LOCAL_JOBSTARTED);
		

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
		
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						
						try {
							
							JLabel execTimeLabel = getUIElement("general.stats.exectime");
							executionTime += Long.parseLong(message.getMessage());
							
							execTimeLabel.setText(TimeUtils.buildTimeString(executionTime));		
							
						} catch (Exception e) {
							log.warn("[UI] Exception ",e);
						}
					}
				});
			}
		}, ServiceMessageType.LOCAL_TASKEXEC);
		
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(final ServiceMessage message) {
				
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
		}, GridNode.getInstance().getId().toString(),ServiceMessageType.NODE_BANNED);
		
		
	}

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

	
	@SuppressWarnings("unchecked")
	protected <T extends JComponent> T getUIElement(String identifier) throws IllegalArgumentException, ClassCastException {
		if (! components.containsKey(identifier)) throw new IllegalArgumentException("Invalid Identifier");
		return (T) components.get(identifier);
	}
	
	
	/* -- ACTIONS --*/

	protected void doShutdownNode() {
		
		int result = JOptionPane.showConfirmDialog(this, "Are You Sure to Shutdown Node ?","Nebula Grid", JOptionPane.YES_NO_OPTION);
		
		// If user chose no, abort shutdown
		if (result==JOptionPane.NO_OPTION) return;
		
		// TODO Implement
		System.exit(0);
	}
	


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
	
	protected void showAbout() {
		// TODO Auto-generated method stub
		
	}

	protected void showHelp() {
		// TODO Auto-generated method stub
		
	}

	protected void showConfiguration() {
		// TODO Auto-generated method stub
		
	}

	protected void onShutdown() {
		showBusyIcon();
		doShutdownNode();
		showIdleIcon();
	}
	
	
	protected static class JobHistoryElement {
		
		private String jobName;
		private String jobId;
		private String startTime;
		private String duration;
		private int tasks;
		private int failures;
		
		
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

		public String getJobName() {
			return jobName;
		}
		
		public String getJobId() {
			return jobId;
		}
		
		public String getStartTime() {
			return startTime;
		}
		
		public String getDuration() {
			return duration;
		}
		
		public int getTasks() {
			return tasks;
		}
		
		public int getFailures() {
			return failures;
		}
		
		
		@Override
		public boolean equals(Object obj) {
			if (!( obj instanceof JobHistoryElement)) return false;
			
			JobHistoryElement element = (JobHistoryElement) obj;
			return this.jobId.equals(element.getJobId());
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return super.hashCode();
		}

		@Override
		public String toString() {
			return "[" + this.startTime + "] " + this.jobName;
		}

	}
	
	protected static class JobHistoryListModel extends AbstractListModel {

		private static final long serialVersionUID = 4872462645102776860L;
		
		
		private List<JobHistoryElement> elements = new ArrayList<JobHistoryElement>();
		
		public void addJobHistoryElement(JobHistoryElement element) {
			elements.add(element);
		}
		
		@Override
		public Object getElementAt(int index) {
			return elements.get(index);
		}

		@Override
		public int getSize() {
			return elements.size();
		}
		
	}
	
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
