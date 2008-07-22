package org.nebulaframework.ui.swing.ClusterManager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

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
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.util.net.NetUtils;

// TODO FixDoc
public class ClusterMainUI extends JFrame {

	private static Log log = LogFactory.getLog(ClusterMainUI.class);
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 500;

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
		addComponent("tabs",tabs);	// Add to components map
		
		// General Tab
		tabs.addTab("General", setupGeneralTab());

		tabs.addTab("Job XYZ",createJobTab("XYZ"));
	}


	private JMenuBar setupMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* -- Cluster Menu -- */
		JMenu clusterMenu = new JMenu("Cluster");
		clusterMenu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(clusterMenu);

		// Cluster-> Start
		JMenuItem clusterStartItem = new JMenuItem("Start Cluster", 'S');
		clusterStartItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
																0));
		clusterStartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doStartCluster();
			}
		});
		clusterMenu.add(clusterStartItem);
		addComponent("menu.cluster.start", clusterStartItem);
		

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
		addComponent("menu.cluster.shutdown",clusterShutdownItem);	// Add to components map
		
		clusterMenu.addSeparator();
		
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
		addComponent("menu.cluster.discover.multicast", clusterDiscoverMulticast);	// Add to components map
		
		// Discover -> WS
		JMenuItem clusterDiscoverWS = new JMenuItem("Colombus Web Service");
		clusterDiscoverWS.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,0));
		clusterDiscoverWS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDiscoverWS();
			}
		});
		clusterDiscoverMenu.add(clusterDiscoverWS);
		addComponent("menu.cluster.discover.ws",clusterDiscoverWS);	// Add to components map
		
		// Exit
		JMenuItem clusterExitItem = new JMenuItem("Exit", 'x');
		clusterExitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitApplication();
			}
		});
		clusterMenu.add(clusterExitItem);
		
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
		JTextArea logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		logPanel.add(new JScrollPane(logTextArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
						BorderLayout.CENTER);
		addComponent("general.log", logTextArea);	// Add to component map
		
		centerPanel.setLayout(new BorderLayout(10, 10));
		centerPanel.add(statsPanel, BorderLayout.NORTH);
		centerPanel.add(logPanel, BorderLayout.CENTER);

		/* -- Create Buttons (South) -- */
		
		generalTab.add(southPanel, BorderLayout.SOUTH);

		JButton startButton = new JButton("Start");
		JButton shutdownButton = new JButton("Shutdown");

		startButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doStartCluster();
			}

		});

		shutdownButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doShutdownCluster();
			}

		});

		southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		southPanel.add(startButton);
		addComponent("general.start", startButton);	// Add to components map
		southPanel.add(shutdownButton);
		addComponent("general.shutdown", startButton);	// Add to components map
		
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
		addComponent("general.stats.clusterid", clusterID);	// Add to components map
		
		// Host Information (ex. localhost:61616)
		JLabel hostInfoLabel = new JLabel("Host Information :");
		clusterInfoPanel.add(hostInfoLabel);
		JLabel hostInfo = new JLabel("#hostInfo#");
		clusterInfoPanel.add(hostInfo);
		addComponent("general.stats.hostinfo", hostInfo);	// Add to components map
		
		// Protocol Information
		JLabel protocolsLabel = new JLabel("Protocols :");
		clusterInfoPanel.add(protocolsLabel);
		JLabel protocols = new JLabel("#protocols#");
		clusterInfoPanel.add(protocols);
		addComponent("general.stats.protocols", protocols);	// Add to components map
		
		// Cluster Up Time
		JLabel upTimeLabel = new JLabel("Cluster Up Time :");
		clusterInfoPanel.add(upTimeLabel);
		JLabel upTime = new JLabel("#upTime#");
		clusterInfoPanel.add(upTime);
		addComponent("general.stats.uptime", upTime);	// Add to components map
		
		/* -- Grid Information -- */

		// Peer Cluster Count
		JLabel peerClustersLabel = new JLabel("Peer Clusters :");
		gridInfoPanel.add(peerClustersLabel);
		JLabel peerClusters = new JLabel("#peerClusters#");
		gridInfoPanel.add(peerClusters);
		addComponent("general.stats.peerclusters", peerClusters);	// Add to components map
		
		// Node Count
		JLabel nodesLabel = new JLabel("Nodes in Cluster :");
		gridInfoPanel.add(nodesLabel);
		JLabel nodes = new JLabel("#nodes#");
		gridInfoPanel.add(nodes);
		addComponent("general.stats.nodes", nodes);	// Add to components map
		
		// Jobs Done Count
		JLabel jobsDoneLabel = new JLabel("Completed Jobs :");
		gridInfoPanel.add(jobsDoneLabel);
		JLabel jobsDone = new JLabel("#jobsdone#");
		gridInfoPanel.add(jobsDone);
		addComponent("general.stats.jobsdone", jobsDone);	// Add to components map
		
		// Active Jobs
		JLabel activeJobsLabel = new JLabel("Active Jobs :");
		gridInfoPanel.add(activeJobsLabel);
		JLabel activeJobs = new JLabel("#activeJobs#");
		gridInfoPanel.add(activeJobs);
		addComponent("general.stats.activejobs", activeJobs);	// Add to components map
		
		return statPanel;
	}

	protected JPanel createJobTab(final String jobId) {
		JPanel jobPanel = new JPanel();
		jobPanel.setLayout(new BorderLayout(10,10));
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout(10,10));
		progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		jobPanel.add(progressPanel, BorderLayout.NORTH);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar, BorderLayout.CENTER);
		addComponent("jobs."+jobId+".progress", progressBar);	// Add to components map
		
		JPanel buttonsPanel = new JPanel();
		jobPanel.add(buttonsPanel, BorderLayout.SOUTH);
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		// Terminate Button
		JButton terminateButton = new JButton("Terminate");
		terminateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTerminateJob(jobId);
			}
		});
		buttonsPanel.add(terminateButton);
		addComponent("jobs."+jobId+".terminate", terminateButton);	// Add to components map
		
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
		JLabel jobNameLabel = new JLabel("#name#");
		jobInfoPanel.add(jobNameLabel,c1);
		addComponent("jobs."+jobId+".job.name", jobNameLabel);	// Add to components map
		
		// Gap
		jobInfoPanel.add(new JLabel(),c1);
		
		// Type
		jobInfoPanel.add(new JLabel("Type :"),c1);
		JLabel jobType = new JLabel("#type#");
		jobInfoPanel.add(jobType,c1);
		addComponent("jobs."+jobId+".job.type", jobType);	// Add to components map
		
		// JobId
		c1.gridy = 1;
		jobInfoPanel.add(new JLabel("Job Id :"),c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		JLabel jobIdLabel = new JLabel(jobId);
		jobInfoPanel.add(jobIdLabel,c1);
		addComponent("jobs."+jobId+".job.id", jobIdLabel);	// Add to components map
		
		// Job Class Name
		c1.gridy = 2;
		c1.gridwidth = 1;
		jobInfoPanel.add(new JLabel("GridJob Class :"),c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		JLabel jobClassLabel = new JLabel("#jobclass#");
		jobInfoPanel.add(jobClassLabel,c1);
		addComponent("jobs."+jobId+".job.class", jobClassLabel);	// Add to components map
		
		// JobId
		c1.gridy = 3;
		c1.gridwidth = 1;
		jobInfoPanel.add(new JLabel("GridTask Class :"),c1);
		c1.gridwidth =  GridBagConstraints.REMAINDER;
		JLabel jobTaskClassLabel = new JLabel("#taskclass#");
		jobInfoPanel.add(jobTaskClassLabel,c1);
		addComponent("jobs."+jobId+".job.taskclass", jobTaskClassLabel);	// Add to components map
		
		
		
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
		JLabel statusLabel = new JLabel("#status#");
		executionInfoPanel.add(statusLabel,c3);
		addComponent("jobs."+jobId+".execution.status", statusLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Percent Complete
		executionInfoPanel.add(new JLabel("Done :"),c3);
		JLabel percentLabel = new JLabel("#percentage#");
		executionInfoPanel.add(percentLabel,c3);
		addComponent("jobs."+jobId+".execution.percentage", percentLabel);	// Add to components map
		
		c3.gridy = 1;
		
		// Start Time
		executionInfoPanel.add(new JLabel("Start Time :"),c3);
		JLabel startTimeLabel = new JLabel("#starttime#");
		executionInfoPanel.add(startTimeLabel,c3);
		addComponent("jobs."+jobId+".execution.starttime", startTimeLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Elapsed Time
		executionInfoPanel.add(new JLabel("Elapsed Time :"),c3);
		JLabel elapsedTimeLabel = new JLabel("#elapsedtime#");
		executionInfoPanel.add(elapsedTimeLabel,c3);
		addComponent("jobs."+jobId+".execution.elapsedtime", elapsedTimeLabel);	// Add to components map
		
		c3.gridy = 2;
		
		// Tasks Deployed (Count)
		executionInfoPanel.add(new JLabel("Tasks Deployed :"),c3);
		JLabel tasksDeployedLabel = new JLabel("#taskcount#");
		executionInfoPanel.add(tasksDeployedLabel,c3);
		addComponent("jobs."+jobId+".execution.tasks", tasksDeployedLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Results Collected (Count)
		executionInfoPanel.add(new JLabel("Results Collected :"),c3);
		JLabel resultsCollectedLabel = new JLabel("#resultcount#");
		executionInfoPanel.add(resultsCollectedLabel,c3);
		addComponent("jobs."+jobId+".execution.results", resultsCollectedLabel);	// Add to components map
		
		c3.gridy = 3;
		
		// Remaining Tasks (Count)
		executionInfoPanel.add(new JLabel("Remaining Tasks :"),c3);
		JLabel remainingTasksLabel = new JLabel("#remaningcount#");
		executionInfoPanel.add(remainingTasksLabel,c3);
		addComponent("jobs."+jobId+".execution.remaining", remainingTasksLabel);	// Add to components map
		
		executionInfoPanel.add(new JLabel(),c3); // Space Holder
		
		// Failed Tasks (Count)
		executionInfoPanel.add(new JLabel("Failed Tasks :"),c3);
		JLabel failedTasksLabel = new JLabel("#failedcount#");
		executionInfoPanel.add(failedTasksLabel,c3);
		addComponent("jobs."+jobId+".execution.failed", failedTasksLabel);	// Add to components map
		
		/* -- Submitter Information -- */
		
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
		JLabel hostNameLabel = new JLabel("#hostname#");
		ownerInfoPanel.add(hostNameLabel, c2);
		addComponent("jobs."+jobId+".owner.hostname", hostNameLabel);	// Add to components map
		
		// Gap
		ownerInfoPanel.add(new JLabel(), c2);
		
		// Host IP Address
		ownerInfoPanel.add(new JLabel("Host IP :"), c2);
		JLabel hostIPLabel = new JLabel("#ipaddress#");
		ownerInfoPanel.add(hostIPLabel,c2);
		addComponent("jobs."+jobId+".owner.hostip", hostIPLabel);	// Add to components map
		
		// Owner UUID
		c2.gridy = 1;
		c2.gridx = 0;
		ownerInfoPanel.add(new JLabel("Owner ID :"),c2);
		JLabel ownerIdLabel = new JLabel("#ownerid#");
		c2.gridx = 1;
		c2.gridwidth = 4;
		ownerInfoPanel.add(ownerIdLabel,c2);
		addComponent("jobs."+jobId+".owner.id", ownerIdLabel);	// Add to components map
		
		return jobPanel;
	}
	
	protected void doTerminateJob(String jobId) {
		// TODO Auto-generated method stub
		
	}

	protected void doStartCluster() {
		JOptionPane.showMessageDialog(this, "Cluster Start");
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
		JOptionPane.showMessageDialog(this, "Cluster Shutdown");
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
		window.setLocationRelativeTo(this);
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(5000);
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
		// Clean up UI
		// clear stats
		// remove tabs related to jobs
	}
	
	protected void exitApplication() {
		// TODO Implement
		System.exit(0);
	}

	private void showClusterInfo() {
		ClusterManager mgr = ClusterManager.getInstance();

		// ClusterID
		JLabel clusterId = getUIElement("general.stats.clusterid");
		clusterId.setText(mgr.getClusterId().toString());
		
		// HostInfo
		JLabel hostInfo = getUIElement("general.stats.hostinfo");
		hostInfo.setText(NetUtils.getHostName(mgr.getBrokerUrl()) + ":" + NetUtils.getHostPort(mgr.getBrokerUrl()));
		
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.warn("Interrupted Exception in Up Time Thread",e);
					}
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							JLabel upTime = getUIElement("general.stats.uptime");
							// FIXME Format TIme and Show
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
	protected void addComponent(String identifier, JComponent component) {
		components.put(identifier, component);
	}

	@SuppressWarnings("unchecked")
	protected <T extends JComponent> T getUIElement(String identifier) throws IllegalArgumentException, ClassCastException {
		if (! components.containsKey(identifier)) throw new IllegalArgumentException("Invalid Identifier");
		return (T) components.get(identifier);
	}

	public static ClusterMainUI create() {
		ClusterMainUI ui = new ClusterMainUI();
		ui.setLocationRelativeTo(null);
		ui.setVisible(true);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		return ui;
	}


}
