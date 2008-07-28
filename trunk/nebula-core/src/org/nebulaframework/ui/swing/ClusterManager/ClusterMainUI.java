package org.nebulaframework.ui.swing.ClusterManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.GridJobStateListener;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.UnboundedGridJob;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.node.delegate.GridNodeDelegate;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.log4j.JLabelAppender;
import org.nebulaframework.util.log4j.JTextPaneAppender;
import org.nebulaframework.util.profiling.TimeUtils;
import org.springframework.util.StringUtils;

// TODO Implement System Tray Support
// TODO FixDoc
public class ClusterMainUI extends JFrame {

	private static Log log = LogFactory.getLog(ClusterMainUI.class);
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 475;

	private static final long serialVersionUID = 8992643609753054554L;
	private static Map<String, JComponent> components = new HashMap<String, JComponent>();

	public ClusterMainUI() throws HeadlessException, IllegalStateException {
		super();
		
		if (!Grid.isClusterManager()) {
			throw new IllegalStateException("ClusterManager is not running");
		}
		setupUI();
		showClusterInfo();
	}

	
	private void setupUI() {

		setTitle("Nebula Grid - Cluster Manager");
		setSize(WIDTH, HEIGHT);

		/* -- Menu Bar -- */
		setJMenuBar(setupMenu());

		/* -- Content -- */

		setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		
		add(centerPanel, BorderLayout.CENTER);
		

		/* -- Setup Tabs -- */
		centerPanel.setLayout(new BorderLayout());
		JTabbedPane tabs = new JTabbedPane();
		centerPanel.add(tabs);
		addUIElement("tabs",tabs);	// Add to components map
		
		// General Tab
		tabs.addTab("General", setupGeneralTab());
		
		// Create Job Start Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(final ServiceMessage message) {
				createJobTab(message.getMessage());
			}
		},ServiceMessageType.JOB_START);
	}


	private JMenuBar setupMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* -- Cluster Menu -- */
		JMenu clusterMenu = new JMenu("Cluster");
		clusterMenu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(clusterMenu);

		
		// Discover Submenu
		JMenu clusterDiscoverMenu = new JMenu("Disover Peers");
		clusterDiscoverMenu.setMnemonic(KeyEvent.VK_D);
		clusterMenu.add(clusterDiscoverMenu);
		
		// Discover -> Multicast
		JMenuItem clusterDiscoverMulticast = new JMenuItem("Multicast");
		clusterDiscoverMulticast.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));
		clusterDiscoverMulticast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDiscoverMulticast();
			}
		});
		clusterDiscoverMenu.add(clusterDiscoverMulticast);
		addUIElement("menu.cluster.discover.multicast", clusterDiscoverMulticast);	// Add to components map
		
		// Discover -> WS
		JMenuItem clusterDiscoverWS = new JMenuItem("Colombus Web Service");
		clusterDiscoverWS.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,0));
		clusterDiscoverWS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDiscoverWS();
			}
		});
		clusterDiscoverMenu.add(clusterDiscoverWS);
		addUIElement("menu.cluster.discover.ws",clusterDiscoverWS);	// Add to components map
		
		clusterMenu.addSeparator();

		// Cluster-> Shutdown
		JMenuItem clusterShutdownItem = new JMenuItem("Shutdown", 'u');
		clusterShutdownItem.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_F6, 0));
		clusterShutdownItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doShutdownCluster();
			}
		});
		clusterMenu.add(clusterShutdownItem);
		addUIElement("menu.cluster.shutdown",clusterShutdownItem);	// Add to components map
		
		
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



	private JPanel setupGeneralTab() {

		JPanel generalTab = new JPanel();
		generalTab.setLayout(new BorderLayout());

		/* -- Create Main Panels -- */

		JPanel centerPanel = new JPanel();
		JPanel northPanel = new JPanel();
		JPanel southPanel = new JPanel();
		
		generalTab.add(centerPanel, BorderLayout.CENTER);
		generalTab.add(northPanel, BorderLayout.NORTH);

		/* -- Create Center Contents -- */

		// Statistics Panel
		JPanel statsPanel = setupStatsPanel();

		// Log Panel
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		logPanel.setBorder(BorderFactory.createTitledBorder("Log Output"));
		JTextPane logTextPane = new JTextPane();
		logTextPane.setEditable(false);
		logTextPane.setBackground(Color.BLACK);
		logTextPane.setForeground(Color.WHITE);
		logTextPane.setAutoscrolls(true);
		
		logPanel.add(new JScrollPane(logTextPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
						BorderLayout.CENTER);
		addUIElement("general.log", logTextPane);	// Add to component map
		
		// Enable Logging
		JTextPaneAppender.setTextPane(logTextPane);
		
		centerPanel.setLayout(new BorderLayout(10, 10));
		centerPanel.add(statsPanel, BorderLayout.NORTH);
		centerPanel.add(logPanel, BorderLayout.CENTER);

		/* -- Create Buttons (South) -- */
		
		generalTab.add(southPanel, BorderLayout.SOUTH);

		JButton shutdownButton = new JButton("Shutdown");

		shutdownButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doShutdownCluster();
			}

		});

		southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		southPanel.add(shutdownButton);
		addUIElement("general.shutdown", shutdownButton);	// Add to components map
		
		return generalTab;
	}

	private JPanel setupStatsPanel() {

		JPanel statPanel = new JPanel();
		statPanel.setLayout(new GridLayout(0, 2, 10, 10));

		JPanel clusterInfoPanel = new JPanel();
		clusterInfoPanel.setLayout(new GridLayout(0, 2, 10, 10));
		clusterInfoPanel.setBorder(BorderFactory
				.createTitledBorder("Cluster Information"));
		statPanel.add(clusterInfoPanel);

		JPanel gridInfoPanel = new JPanel();
		gridInfoPanel.setLayout(new GridLayout(0, 2, 10, 10));
		gridInfoPanel.setBorder(BorderFactory
				.createTitledBorder("Grid Information"));
		statPanel.add(gridInfoPanel);

		/* -- Cluster Information -- */

		// ClusterID
		JLabel clusterIDLabel = new JLabel("Cluster ID :");
		clusterInfoPanel.add(clusterIDLabel);
		JLabel clusterID = new JLabel("#clusterId#");
		clusterInfoPanel.add(clusterID);
		addUIElement("general.stats.clusterid", clusterID);	// Add to components map
		
		// Host Information (ex. localhost:61616)
		JLabel hostInfoLabel = new JLabel("Host Information :");
		clusterInfoPanel.add(hostInfoLabel);
		JLabel hostInfo = new JLabel("#hostInfo#");
		clusterInfoPanel.add(hostInfo);
		addUIElement("general.stats.hostinfo", hostInfo);	// Add to components map
		
		// Protocol Information
		JLabel protocolsLabel = new JLabel("Protocols :");
		clusterInfoPanel.add(protocolsLabel);
		JLabel protocols = new JLabel("#protocols#");
		clusterInfoPanel.add(protocols);
		addUIElement("general.stats.protocols", protocols);	// Add to components map
		
		// Cluster Up Time
		JLabel upTimeLabel = new JLabel("Cluster Up Time :");
		clusterInfoPanel.add(upTimeLabel);
		JLabel upTime = new JLabel("#upTime#");
		clusterInfoPanel.add(upTime);
		addUIElement("general.stats.uptime", upTime);	// Add to components map
		
		/* -- Grid Information -- */

		// Peer Cluster Count
		JLabel peerClustersLabel = new JLabel("Peer Clusters :");
		gridInfoPanel.add(peerClustersLabel);
		JLabel peerClusters = new JLabel("#peerClusters#");
		gridInfoPanel.add(peerClusters);
		addUIElement("general.stats.peerclusters", peerClusters);	// Add to components map
		
		// Node Count
		JLabel nodesLabel = new JLabel("Nodes in Cluster :");
		gridInfoPanel.add(nodesLabel);
		JLabel nodes = new JLabel("#nodes#");
		gridInfoPanel.add(nodes);
		addUIElement("general.stats.nodes", nodes);	// Add to components map
		
		// Jobs Done Count
		JLabel jobsDoneLabel = new JLabel("Executed Jobs :");
		gridInfoPanel.add(jobsDoneLabel);
		JLabel jobsDone = new JLabel("#jobsdone#");
		gridInfoPanel.add(jobsDone);
		addUIElement("general.stats.jobsdone", jobsDone);	// Add to components map
		
		// Active Jobs
		JLabel activeJobsLabel = new JLabel("Active Jobs :");
		gridInfoPanel.add(activeJobsLabel);
		JLabel activeJobs = new JLabel("#activeJobs#");
		gridInfoPanel.add(activeJobs);
		addUIElement("general.stats.activejobs", activeJobs);	// Add to components map
		
		return statPanel;
	}

	protected void createJobTab(final String jobId) {
		
		
		final InternalClusterJobService jobService = ClusterManager.getInstance().getJobService();
		final GridJobProfile profile = jobService.getProfile(jobId);
		
		final long startTime = System.currentTimeMillis();
		
		final JPanel jobPanel = new JPanel();
		jobPanel.setLayout(new BorderLayout(10,10));
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout(10,10));
		progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		jobPanel.add(progressPanel, BorderLayout.NORTH);
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar, BorderLayout.CENTER);
		addUIElement("jobs."+jobId+".progress", progressBar);	// Add to components map
		
		JPanel buttonsPanel = new JPanel();
		jobPanel.add(buttonsPanel, BorderLayout.SOUTH);
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		// Terminate Button
		JButton terminateButton = new JButton("Terminate");
		terminateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					public void run() {

						String name = getClassName(profile.getJob().getClass().getName());
						int option = JOptionPane.showConfirmDialog(ClusterMainUI.this, 
						                                                  "Are you sure to terminate GridJob " 
						                                           + name + "?",
						                                           "Nebula - Terminate GridJob",
						                                           JOptionPane.YES_NO_OPTION);
						
						if (option == JOptionPane.NO_OPTION) return;
						
						// Attempt Cancel
						boolean result = profile.getFuture().cancel();
						
						if (result) {
							JOptionPane.showMessageDialog(ClusterMainUI.this, "Grid Job '" + 
							                              getClassName(profile.getJob().getClass().getName()) +
							                              "terminated successfully.", "Nebula - Job Terminated", 
							                              JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(ClusterMainUI.this, "Failed to terminate Grid Job '" + 
							                              name
							                              , "Nebula - Job Termination Failed", 
							                              JOptionPane.WARNING_MESSAGE);
						}						
					}
					
				}).start();
			}
		});
		buttonsPanel.add(terminateButton);
		addUIElement("jobs."+jobId+".terminate", terminateButton);	// Add to components map
		
		// Close Tab Button
		JButton closeButton = new JButton("Close Tab");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						removeJobTab(jobId);
					}
				});
			}
		});
		closeButton.setEnabled(false);
		
		buttonsPanel.add(closeButton);
		addUIElement("jobs."+jobId+".closetab", closeButton);	// Add to components map
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridBagLayout());
		jobPanel.add(centerPanel, BorderLayout.CENTER);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weightx = 1.0;
		
		/* -- Job Information -- */
		
		JPanel jobInfoPanel = new JPanel();
		jobInfoPanel.setBorder(BorderFactory.createTitledBorder("Job Information"));
		jobInfoPanel.setLayout(new GridBagLayout());
		c.gridy = 0;
		c.ipady = 30;
		centerPanel.add(jobInfoPanel, c);
		
		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.BOTH;
		c1.weightx = 1;
		c1.weighty = 1;
		
		// Name
		jobInfoPanel.add(new JLabel("Name :"),c1);
		JLabel jobNameLabel = new JLabel();
		jobInfoPanel.add(jobNameLabel,c1);
		jobNameLabel.setText(getClassName(profile.getJob().getClass().getName()));
		addUIElement("jobs."+jobId+".job.name", jobNameLabel);	// Add to components map
		
		// Gap
		jobInfoPanel.add(new JLabel(),c1);
		
		// Type
		jobInfoPanel.add(new JLabel("Type :"),c1);
		JLabel jobType = new JLabel();
		jobType.setText(getJobType(profile.getJob()));
		jobInfoPanel.add(jobType,c1);
		addUIElement("jobs."+jobId+".job.type", jobType );	// Add to components map
		
		// Job Class Name
		c1.gridy = 1;
		c1.gridwidth = 1;
		jobInfoPanel.add(new JLabel("GridJob Class :"),c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		JLabel jobClassLabel = new JLabel();
		jobClassLabel.setText(profile.getJob().getClass().getName());
		jobInfoPanel.add(jobClassLabel,c1);
		addUIElement("jobs."+jobId+".job.class", jobClassLabel);	// Add to components map
		
		
		
		/* -- Execution Information -- */
		
		JPanel executionInfoPanel = new JPanel();
		executionInfoPanel.setBorder(BorderFactory.createTitledBorder("Execution Statistics"));
		executionInfoPanel.setLayout(new GridBagLayout());
		c.gridy = 1;
		c.ipady = 30;
		centerPanel.add(executionInfoPanel, c);
		
		GridBagConstraints c3 = new GridBagConstraints();
		c3.weightx = 1;
		c3.weighty = 1;
		c3.fill = GridBagConstraints.BOTH;
		
		// Start Time
		executionInfoPanel.add(new JLabel("Job Status :"),c3);
		final JLabel statusLabel = new JLabel("Initializing");
		executionInfoPanel.add(statusLabel,c3);
		addUIElement("jobs."+jobId+".execution.status", statusLabel);	// Add to components map
		
		// Status Update Listener
		profile.getFuture().addGridJobStateListener(new GridJobStateListener() {
			public void stateChanged(final GridJobState newState) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						statusLabel.setText(StringUtils.capitalize(newState.toString().toLowerCase()));
					}
				});
			}
		});
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Percent Complete
		executionInfoPanel.add(new JLabel("Completed % :"),c3);
		final JLabel percentLabel = new JLabel("-N/A-");
		executionInfoPanel.add(percentLabel,c3);
		addUIElement("jobs."+jobId+".execution.percentage", percentLabel);	// Add to components map
		
		c3.gridy = 1;
		
		// Start Time
		executionInfoPanel.add(new JLabel("Start Time :"),c3);
		JLabel startTimeLabel = new JLabel(DateFormat.getInstance().format(new Date(startTime)));
		executionInfoPanel.add(startTimeLabel,c3);
		addUIElement("jobs."+jobId+".execution.starttime", startTimeLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Elapsed Time
		executionInfoPanel.add(new JLabel("Elapsed Time :"),c3);
		JLabel elapsedTimeLabel = new JLabel("-N/A-");
		executionInfoPanel.add(elapsedTimeLabel,c3);
		addUIElement("jobs."+jobId+".execution.elapsedtime", elapsedTimeLabel);	// Add to components map
		
		c3.gridy = 2;
		
		// Tasks Deployed (Count)
		executionInfoPanel.add(new JLabel("Tasks Deployed :"),c3);
		JLabel tasksDeployedLabel = new JLabel("-N/A-");
		executionInfoPanel.add(tasksDeployedLabel,c3);
		addUIElement("jobs."+jobId+".execution.tasks", tasksDeployedLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Results Collected (Count)
		executionInfoPanel.add(new JLabel("Results Collected :"),c3);
		JLabel resultsCollectedLabel = new JLabel("-N/A-");
		executionInfoPanel.add(resultsCollectedLabel,c3);
		addUIElement("jobs."+jobId+".execution.results", resultsCollectedLabel);	// Add to components map
		
		c3.gridy = 3;
		
		// Remaining Tasks (Count)
		executionInfoPanel.add(new JLabel("Remaining Tasks :"),c3);
		JLabel remainingTasksLabel = new JLabel("-N/A-");
		executionInfoPanel.add(remainingTasksLabel,c3);
		addUIElement("jobs."+jobId+".execution.remaining", remainingTasksLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Failed Tasks (Count)
		executionInfoPanel.add(new JLabel("Failed Tasks :"),c3);
		JLabel failedTasksLabel = new JLabel("-N/A-");
		executionInfoPanel.add(failedTasksLabel,c3);
		addUIElement("jobs."+jobId+".execution.failed", failedTasksLabel);	// Add to components map
		
		/* -- Submitter Information -- */
		UUID ownerId = profile.getOwner();
		GridNodeDelegate owner = ClusterManager.getInstance().getClusterRegistrationService().getGridNodeDelegate(ownerId);
		
		JPanel ownerInfoPanel = new JPanel();
		ownerInfoPanel.setBorder(BorderFactory.createTitledBorder("Owner Information"));
		ownerInfoPanel.setLayout(new GridBagLayout());
		c.gridy = 2;
		c.ipady = 10;
		centerPanel.add(ownerInfoPanel, c);
		
		GridBagConstraints c2 = new GridBagConstraints();
		
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1;
		c2.weighty = 1;

		// Host Name
		ownerInfoPanel.add(new JLabel("Host Name :"),c2);
		JLabel hostNameLabel = new JLabel(owner.getProfile().getName());
		ownerInfoPanel.add(hostNameLabel, c2);
		addUIElement("jobs."+jobId+".owner.hostname", hostNameLabel);	// Add to components map
		
		// Gap
		ownerInfoPanel.add(new JLabel(), c2);
		
		// Host IP Address
		ownerInfoPanel.add(new JLabel("Host IP :"), c2);
		JLabel hostIPLabel = new JLabel(owner.getProfile().getIpAddress());
		ownerInfoPanel.add(hostIPLabel,c2);
		addUIElement("jobs."+jobId+".owner.hostip", hostIPLabel);	// Add to components map
		
		// Owner UUID
		c2.gridy = 1;
		c2.gridx = 0;
		ownerInfoPanel.add(new JLabel("Owner ID :"),c2);
		JLabel ownerIdLabel = new JLabel(profile.getOwner().toString());
		c2.gridx = 1;
		c2.gridwidth = 4;
		ownerInfoPanel.add(ownerIdLabel,c2);
		addUIElement("jobs."+jobId+".owner.id", ownerIdLabel);	// Add to components map
		

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Create Tab
				addUIElement("jobs." + jobId, jobPanel);
				JTabbedPane tabs = getUIElement("tabs");
				tabs.addTab(getClassName(profile.getJob().getClass().getName()), jobPanel);
				tabs.revalidate();
			}
		});

		
		
		// Execution Information Updater Thread
		new Thread(new Runnable() {
			
			boolean initialized = false;
			boolean unbounded = false;
			
			public void run() {

				// Unbounded, No Progress Supported
				if ((!initialized) && profile.getJob() instanceof UnboundedGridJob<?>) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							progressBar.setIndeterminate(true);
							progressBar.setStringPainted(false);
							percentLabel.setText("< Unbounded >");
							
							
						}
					});
					initialized = true;
					unbounded = true;
				}
				
				// Progress Update Supported
				while(true) {
					
					try {
						// 500ms Interval
						Thread.sleep(500);
						
					} catch (InterruptedException e) {
						log.warn("Interrupted Progress Updater Thread",e);
					}
					
					final int totalCount = profile.getTotalTasks();
					final int tasksRem = profile.getTaskCount();
					final int resCount = profile.getResultCount();
					final int failCount = profile.getFailedCount();
					
					// Task Information
					JLabel totalTaskLabel = getUIElement("jobs."+jobId+".execution.tasks");
					totalTaskLabel.setText(String.valueOf(totalCount));
					
					// Result Count
					JLabel resCountLabel = getUIElement("jobs."+jobId+".execution.results");
					resCountLabel.setText(String.valueOf(resCount));
					
					// Remaining Task Count
					JLabel remLabel = getUIElement("jobs."+jobId+".execution.remaining");
					remLabel.setText(String.valueOf(tasksRem));
					
					// Failed Task Count
					JLabel failedLabel = getUIElement("jobs."+jobId+".execution.failed");
					failedLabel.setText(String.valueOf(failCount));
					
					// Elapsed Time
					JLabel elapsedLabel = getUIElement("jobs."+jobId+".execution.elapsedtime");
					elapsedLabel.setText(TimeUtils.timeDifference(startTime));
					
					// If not in Executing Mode
					if ((!profile.getFuture().isJobFinished())&&profile.getFuture().getState()!=GridJobState.EXECUTING) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								progressBar.setIndeterminate(true);
								progressBar.setStringPainted(false);
								String state = profile.getFuture().getState().toString();
								percentLabel.setText(StringUtils.capitalize(state.toLowerCase()));
							}
						});
					}
					else { // Executing Mode : Progress Information
						
						if (!unbounded) {
							
							final int percentage = (int) (profile.percentage() * 100);
							
							//final int failCount = profile.get
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									// If ProgressBar is in indeterminate
									if (progressBar.isIndeterminate()) {
										progressBar.setIndeterminate(false);
										progressBar.setStringPainted(true);
									}
									
									// Update Progress Bar / Percent Label
									progressBar.setValue(percentage);
									percentLabel.setText(percentage + " %");
									
								}
							});
						}
					}
					
					// Job Finished, Stop
					if (profile.getFuture().isJobFinished()) {
						return;
					}
					
				}
			}
		}).start();
		
		// Job End Hook to Execute Job End Actions
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

			public void onServiceEvent(ServiceMessage event) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						
						JButton close = getUIElement("jobs."+jobId+".closetab");
						JButton terminate = getUIElement("jobs."+jobId+".terminate");
						terminate.setEnabled(false);
						close.setEnabled(true);
						
						JProgressBar progress = getUIElement("jobs."+jobId+".progress");
						JLabel percentage = getUIElement("jobs."+jobId+".execution.percentage");
						
						if (progress.isIndeterminate()) {
							progress.setIndeterminate(false);
							progress.setStringPainted(false);
							percentage.setText("< Canceled >");
						}
					}
				});
			}
			
		}, jobId, ServiceMessageType.JOB_CANCEL, ServiceMessageType.JOB_END, ServiceMessageType.CLUSTER_SHUTDOWN);
	}

	private String getJobType(GridJob<?, ?> job) {
		if (job instanceof SplitAggregateGridJob<?, ?>) {
			return "Split-Aggregate";
		}
		else if (job instanceof UnboundedGridJob<?>) {
			return "Unbounded";
		}
		else {
			return "Unknown";
		}
	}


	private String getClassName(String clsName) {
		String[] tokens = clsName.split("\\.");
		return tokens[tokens.length-1];
	}


	protected void removeJobTab(String jobId) {
		
		// Detach Tab
		JTabbedPane tabs = getUIElement("tabs");
		tabs.remove(getUIElement("jobs." + jobId));
		tabs.revalidate();
		
		// Remove UI Elements
		removeUIElement("jobs."+jobId+".progress");
		removeUIElement("jobs."+jobId+".terminate");
		removeUIElement("jobs."+jobId+".closetab");
		removeUIElement("jobs."+jobId+".job.name");
		removeUIElement("jobs."+jobId+".job.type");
		removeUIElement("jobs."+jobId+".job.class");
		removeUIElement("jobs."+jobId+".execution.status");
		removeUIElement("jobs."+jobId+".execution.percentage");
		removeUIElement("jobs."+jobId+".execution.starttime");
		removeUIElement("jobs."+jobId+".execution.elapsedtime");
		removeUIElement("jobs."+jobId+".execution.tasks");
		removeUIElement("jobs."+jobId+".execution.results");
		removeUIElement("jobs."+jobId+".execution.remaining");
		removeUIElement("jobs."+jobId+".execution.failed");
		removeUIElement("jobs."+jobId+".owner.hostname");
		removeUIElement("jobs."+jobId+".owner.hostip");
		removeUIElement("jobs."+jobId+".owner.id");
		removeUIElement("jobs." + jobId);
	}
	
	protected void doShutdownCluster() {

		// Consider different messages when there are active jobs
		String message = "Are you sure to shutdown the cluster ?";

		int response = JOptionPane
				.showConfirmDialog(this, message, "Shutdown Cluster",
									JOptionPane.YES_NO_OPTION);
		
		// User chose 'No'
		if (response == JOptionPane.NO_OPTION) return;
		
		// Shutdown
		onShutdown();
	}

	protected void showAbout() {
		final JWindow window = new JWindow(this);
		JLabel lbl = new JLabel(new ImageIcon(ClassLoader.getSystemResource("META-INF/resources/about.png")));
		lbl.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				hideAbout(window);
			}
		});
		
		window.setSize(400, 200);
		window.setLayout(new BorderLayout());
		window.add(lbl, BorderLayout.CENTER);
		window.setVisible(true);
		window.setLocationRelativeTo(this);	// Center on Main UI
		
		// Auto Close About after 8 Seconds
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					log.warn("Interrupted Exception in About Close Handler", e);
				}
				
				hideAbout(window);
			}
		}).start();
	}
	
	protected void hideAbout(JWindow window) {
		if (window.isVisible()) {
			window.setVisible(false);
			window.dispose();
		}
	}

	protected void showHelp() {
		// TODO Auto-generated method stub
		
	}

	protected void doDiscoverWS() {
		// TODO Auto-generated method stub
		
	}

	protected void doDiscoverMulticast() {
		// TODO Auto-generated method stub
		
	}

	protected void showConfiguration() {
		// TODO Auto-generated method stub
		
	}
	
	public void onShutdown() {

		ClusterManager.getInstance().shutdown(true);
		// Clean up UI
		// clear stats
		// remove tabs related to jobs
		
	}

	private void showClusterInfo() {
		ClusterManager mgr = ClusterManager.getInstance();

		// ClusterID
		JLabel clusterId = getUIElement("general.stats.clusterid");
		clusterId.setText(mgr.getClusterId().toString());
		
		// HostInfo
		JLabel hostInfo = getUIElement("general.stats.hostinfo");
		hostInfo.setText(mgr.getClusterInfo().getHostInfo());
		
		// Protocols
		JLabel protocols = getUIElement("general.stats.protocols");
		protocols.setText(mgr.getClusterInfo().getProtocolInfo());
		
		// Uptime Initial Value
		JLabel upTime = getUIElement("general.stats.uptime");
		upTime.setText("");
		
		// Peer Clusters
		final JLabel clusters = getUIElement("general.stats.peerclusters");
		clusters.setText("0");
		
		// Peer Clusters Update Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int peers = ClusterManager.getInstance().getPeerService().getPeerCount();
						clusters.setText(String.valueOf(peers));
					}
				});
			}
		}, ServiceMessageType.PEER_CONNECTION, ServiceMessageType.PEER_DISCONNECTION);
		
		// Local Nodes
		final JLabel nodes = getUIElement("general.stats.nodes");
		nodes.setText("0");
		
		
		// Local Nodes Update Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int count = ClusterManager.getInstance().getClusterRegistrationService().getNodeCount();
						nodes.setText(String.valueOf(count));
					}
				});
			}
		}, ServiceMessageType.NODE_REGISTERED, ServiceMessageType.NODE_UNREGISTERED);
		
		// Completed Jobs
		final JLabel jobsdone = getUIElement("general.stats.jobsdone");
		jobsdone.setText("0");
		
		// Completed Jobs Update Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int count = ClusterManager.getInstance().getJobService().getFinishedJobCount();
						jobsdone.setText(String.valueOf(count));
					}
				});
			}
		}, ServiceMessageType.JOB_END, ServiceMessageType.JOB_CANCEL);
		
		
		// Active Jobs
		final JLabel activejobs = getUIElement("general.stats.activejobs");
		activejobs.setText("0");
		
		// Active Jobs Update Hook
		ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int count = ClusterManager.getInstance().getJobService().getActiveJobCount();
						activejobs.setText(String.valueOf(count));
					}
				});
			}
		}, ServiceMessageType.JOB_START, ServiceMessageType.JOB_CANCEL, ServiceMessageType.JOB_END);
		
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
		splash.setAlwaysOnTop(true);
		splash.setLocationRelativeTo(null);
		
		return splash;
	}
	
	public static ClusterMainUI create() {
		final ClusterMainUI ui = new ClusterMainUI();
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
