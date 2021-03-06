package org.penemunxt.projects.penemunxtexplorer.pc;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.VolatileImage;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.penemunxt.communication.*;
import org.penemunxt.graphics.pc.Icons;
import org.penemunxt.projects.penemunxtexplorer.*;
import org.penemunxt.projects.penemunxtexplorer.pc.connection.*;
import org.penemunxt.projects.penemunxtexplorer.pc.map.*;
import org.penemunxt.projects.penemunxtexplorer.pc.map.file.MapFileUtilities;
import org.penemunxt.projects.penemunxtexplorer.pc.map.processing.*;
import org.penemunxt.projects.penemunxtexplorer.pc.map.timeline.MapTimeline;
import org.penemunxt.windows.pc.ComponentSpacer;
import org.penemunxt.windows.pc.DataTableWindow;

public class PenemuNXTExplorerControl extends JPanel implements Runnable,
		ActionListener, WindowListener, ComponentListener, ChangeListener,
		MouseListener {

	/*
	 * To do list: - Send commands to Robot - Clear Area: Use Hotspots instead
	 * of all values. Possible fix: Instead of public MapPositionPoints(int
	 * points, int x, int y), make it public MapPositionPoints(int points, int
	 * index), where index is the frame with corresponding coordinates. This
	 * means all data we have will be available everywhere. - Should DataShare
	 * be merged for server and client. They are similar and DSclient has
	 * several methods which could be useful both for the client and the server.
	 */

	// Constants
	private static final long serialVersionUID = 1L;

	// // Application
	final static String APPLICATION_NAME = "PenemuNXT - Explorer control";
	final static ImageIcon APPLICATION_ICON = Icons.PENEMUNXT_CIRCLE_LOGO_ICON_16_X_16_ICON;
	final static ImageIcon APPLICATION_LOGO = Icons.PENEMUNXT_LOGO_LANDSCAPE_ICON;
	final static Boolean APPLICATION_START_FULLSCREEN = true;
	final static Boolean APPLICATION_SHOW_ABOUT_DIALOG = false;
	final static String APPLICATION_ABOUT_DIALOG_TITLE = "About PenemuNXT";
	final static String APPLICATION_ABOUT_DIALOG_TEXT = "This is a preview of the PC app for the project PenemuNXT.\nPlease report any bugs to:\nhttp://code.google.com/p/penemunxt/\n\nRead more about the project:\nhttp://penemunxt.blogspot.com/";

	final static String APPLICATION_START_DIALOG_TITLE = "PenemuNXT";
	final static String APPLICATION_START_DIALOG_TEXT = "Start by opening one of the sample maps included in the preview download.";

	// // Maps

	// None
	// final static String DEFAULT_FOLDER_PATH = "";
	// final static String PRELOAD_PENEMUNXT_MAP_PATH = "";

	// PeterF-01
	// final static String DEFAULT_FOLDER_PATH =
	// "C:\\Users\\Peter\\Desktop\\Maps\\";
	// final static String PRELOAD_PENEMUNXT_MAP_PATH =
	// "C:\\Users\\Peter\\Desktop\\Maps\\Sample_3.penemunxtmap";

	// PeterF-04
	//final static String DEFAULT_FOLDER_PATH = "C:\\Users\\Peter\\Documents\\Projects\\PenemuNXT\\Data\\Maps\\";
	//final static String PRELOAD_PENEMUNXT_MAP_PATH = "C:\\Users\\Peter\\Documents\\Projects\\PenemuNXT\\Data\\Maps\\Josef\\100211_2.penemunxtmap";

	//Presentation
	final static String DEFAULT_FOLDER_PATH = "C:\\Users\\Peter\\Desktop\\Maps";
	final static String PRELOAD_PENEMUNXT_MAP_PATH = "";
	
	
	// // Scale
	final static int MAP_INIT_SCALE = 50;

	// // UI
	final static Color DEFAULT_PANEL_BACKGROUND_COLOR = new Color(197, 209, 215);
	final static Color VIEW_PANEL_BACKGROUND_COLOR = DEFAULT_PANEL_BACKGROUND_COLOR;
	final static Color LEFT_PANEL_BACKGROUND_COLOR = DEFAULT_PANEL_BACKGROUND_COLOR;
	final static Color BOTTOM_PANEL_BACKGROUND_COLOR = DEFAULT_PANEL_BACKGROUND_COLOR;

	final static Color MAP_PANEL_BACKGROUND_COLOR = Color.GRAY;
	final static Color MAP_PANEL_BORDER_COLOR = Color.BLACK;
	final static int MAP_PANEL_BORDER_WIDTH = 2;

	final static int PANEL_MARGIN = 15;
	final static int LOGO_MARGIN_BOTTOM = 15;

	// // Connection
	final static NXTConnectionModes[] CONNECTION_MODES = {
			NXTConnectionModes.USB, NXTConnectionModes.Bluetooth };
	final static String[] CONNECTION_MODES_NAMES = { "USB", "Bluetooth" };
	final static int CONNECTION_MODES_INIT_SELECTED = 1;

	final static String CONNECT_TO_NAME_DEFAULT = "NXT";
	final static String CONNECT_TO_ADDRESS_DEFAULT = "0016530A9000";

	// Variables

	// // Frame
	JFrame mainWindowFrame;

	// // Menu
	JMenuBar mnuMainBar;

	JMenuItem mnuFileOpenDataViewButton;
	JMenuItem mnuFileOpenMapProcessorsButton;
	JMenuItem mnuFileOpenButton;
	JMenuItem mnuFileSaveButton;
	JMenuItem mnuFileExportMapAsImageButton;
	JMenuItem mnuFileExitButton;
	JMenuItem mnuAboutAuthors;

	// // Map menu
	JMenuItem mnuMapClearButton;
	JMenu mnuMapProcessors;

	// // Buttons
	JButton btnConnectAndStart;
	JButton btnDisconnectAndStop;
	JButton btnSendTargetData;

	// // Panels
	Panel controlPanel;

	// // Labels
	Label lblRDX;
	Label lblRDY;
	Label lblRDRobotHeading;
	Label lblRDHeadDistance;
	Label lblRDHeadHeading;
	Label lblRDUssDistance;
	Label lblCompassValues;
	Label lblTargetPosX;
	Label lblTargetPosY;
	Label lblBatteryLevel;

	Label lblTimelineCurrentFrame;

	// // Comboboxes
	JComboBox cboConnectionTypes;

	// // Sliders
	JSlider sldMapScale;
	JSlider sldMapRotate;
	JSlider sldAlgorithmsSensitivityFilter;

	// // Textboxes
	JTextField txtConnectToName;
	JTextField txtConnectToAddress;
	JTextField txtTargetPosX;
	JTextField txtTargetPosY;

	// // Map
	MapVisulaisation mapVisulaisation;
	MapProcessors mapProcessors;
	MapProcessorsList MapProcessorsListView;
	MapTimeline mapTimeline;

	// // Misc
	boolean AppActive;
	NXTCommunication NXTC;
	DataShare DS;
	RobotConnection RC;
	VolatileImage OSI;
	PenemuNXTExplorerDataViewer DataView;

	// Functions

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public PenemuNXTExplorerControl(JFrame frame) {
		AppActive = true;

		// Timeline
		mapTimeline = new MapTimeline();
		this.mainWindowFrame = frame;

		this.setLayout(new BorderLayout());
		this.add(getContentPanel(), BorderLayout.CENTER);

		Thread t = new Thread(this);
		t.start();
	}

	private static void createAndShowGUI() {
		JFrame mainFrame = new JFrame(APPLICATION_NAME);

		PenemuNXTExplorerControl PCCT = new PenemuNXTExplorerControl(mainFrame);

		mainFrame.addWindowListener(PCCT);
		mainFrame.add(PCCT);

		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		mainFrame.setJMenuBar(PCCT.getMenuBar());

		mainFrame.setIconImage(APPLICATION_ICON.getImage());
		mainFrame.setBackground(Color.WHITE);

		Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setSize((int) (ScreenSize.width * 0.85),
				(int) (ScreenSize.height * 0.85));

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if (APPLICATION_START_FULLSCREEN) {
			mainFrame.setUndecorated(true);
			mainFrame.pack();
			mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
		mainFrame.setVisible(true);

		if (APPLICATION_SHOW_ABOUT_DIALOG) {
			JOptionPane.showMessageDialog(mainFrame,
					APPLICATION_ABOUT_DIALOG_TEXT,
					APPLICATION_ABOUT_DIALOG_TITLE,
					JOptionPane.INFORMATION_MESSAGE);
			JOptionPane.showMessageDialog(mainFrame,
					APPLICATION_START_DIALOG_TEXT,
					APPLICATION_START_DIALOG_TITLE,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public JMenu getMapMenu() {
		JMenu mnuMapMenu = new JMenu("Map");
		mnuMapProcessors = new JMenu("Processors");

		// Map menu items
		mnuMapClearButton = new JMenuItem("Clear");
		mnuMapClearButton.addActionListener(this);

		mnuMapMenu.add(mnuMapClearButton);
		mnuMapMenu.add(new JSeparator());
		mnuMapMenu.add(mnuMapProcessors);

		return mnuMapMenu;
	}

	public JMenuBar getMenuBar() {
		// Menu bar
		JMenuBar mnuMainBar = new JMenuBar();

		// Menus
		JMenu mnuFileMenu = new JMenu("File");

		// File menu
		// mnuFileMenu.setIcon(APPLICATION_ICON);

		mnuFileOpenDataViewButton = new JMenuItem("Show data...");
		mnuFileOpenDataViewButton.addActionListener(this);
		mnuFileOpenMapProcessorsButton = new JMenuItem("Show map processors...");
		mnuFileOpenMapProcessorsButton.addActionListener(this);
		mnuFileOpenButton = new JMenuItem("Open Map...");
		mnuFileOpenButton.addActionListener(this);
		mnuFileSaveButton = new JMenuItem("Save map As...");
		mnuFileSaveButton.addActionListener(this);
		mnuFileExportMapAsImageButton = new JMenuItem("Export rendered map...");
		mnuFileExportMapAsImageButton.addActionListener(this);
		mnuFileExitButton = new JMenuItem("Exit");
		mnuFileExitButton.addActionListener(this);

		// Menus
		JMenu mnuAboutMenu = new JMenu("About");

		// File menu
		mnuAboutMenu.setIcon(APPLICATION_ICON);

		mnuAboutAuthors = new JMenuItem("About PenemuNXT...");
		mnuAboutAuthors.addActionListener(this);

		// Add everything
		mnuMainBar.add(mnuFileMenu);
		mnuMainBar.add(getMapMenu());
		mnuMainBar.add(mnuAboutMenu);

		mnuFileMenu.add(mnuFileOpenButton);
		mnuFileMenu.add(mnuFileSaveButton);
		mnuFileMenu.add(mnuFileExportMapAsImageButton);
		mnuFileMenu.add(new JSeparator());
		mnuFileMenu.add(mnuFileOpenDataViewButton);
		mnuFileMenu.add(mnuFileOpenMapProcessorsButton);
		mnuFileMenu.add(new JSeparator());
		mnuFileMenu.add(mnuFileExitButton);

		setupMapProcessorsMenu();

		mnuAboutMenu.add(mnuAboutAuthors);

		return mnuMainBar;
	}

	public Panel getContentPanel() {
		// Panels
		Panel mainPanel = new Panel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel viewPanel = new JPanel();

		controlPanel = new Panel();

		// Fonts
		Font fntSectionHeader = new Font("Arial", Font.BOLD, 14);
		Font fntLabelHeader = new Font("Arial", Font.BOLD, 12);

		// Map
		setupMapProcessors();
		mapVisulaisation = new MapVisulaisation(MAP_INIT_SCALE, mapProcessors,
				true);
		mapVisulaisation.mapCenterChanged.add(this);
		mapVisulaisation.mapScaleChanged.add(this);
		mapVisulaisation.mapMouseClick.add(this);

		// Image logo
		JPanel logoPanel = new JPanel(new BorderLayout());
		JLabel lblLogo = new JLabel("", APPLICATION_LOGO, SwingConstants.CENTER);

		logoPanel.setBackground(LEFT_PANEL_BACKGROUND_COLOR);
		logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0,
				LOGO_MARGIN_BOTTOM, 0));
		logoPanel.add(lblLogo, BorderLayout.CENTER);

		// Connect
		Label lblConnectionHeader = new Label("Connection");
		lblConnectionHeader.setFont(fntSectionHeader);

		Label lblConnectionMode = new Label("Mode:");
		lblConnectionMode.setFont(fntLabelHeader);

		Label lblConnectionNXTName = new Label("NXT name:");
		lblConnectionNXTName.setFont(fntLabelHeader);

		Label lblConnectionNXTAddress = new Label("NXT address:");
		lblConnectionNXTAddress.setFont(fntLabelHeader);

		cboConnectionTypes = new JComboBox(CONNECTION_MODES_NAMES);
		cboConnectionTypes.setSelectedIndex(CONNECTION_MODES_INIT_SELECTED);

		txtConnectToName = new JTextField(CONNECT_TO_NAME_DEFAULT, 15);
		txtConnectToAddress = new JTextField(CONNECT_TO_ADDRESS_DEFAULT, 15);

		btnConnectAndStart = new JButton("Connect and Start");
		btnConnectAndStart.addActionListener(this);
		btnConnectAndStart.setEnabled(true);

		btnDisconnectAndStop = new JButton("Disconnect and Stop");
		btnDisconnectAndStop.addActionListener(this);
		btnDisconnectAndStop.setEnabled(false);

		Panel pnlConnection = new Panel(new GridBagLayout());

		GridBagConstraints CBC = new GridBagConstraints();
		CBC.fill = GridBagConstraints.HORIZONTAL;
		CBC.weightx = 0.0;
		CBC.weighty = 0.0;

		CBC.gridx = 0;
		CBC.gridy = 0;
		pnlConnection.add(lblConnectionMode, CBC);
		CBC.gridx = 1;
		CBC.gridy = 0;
		pnlConnection.add(cboConnectionTypes, CBC);
		CBC.gridx = 0;
		CBC.gridy = 1;
		pnlConnection.add(lblConnectionNXTName, CBC);
		CBC.gridx = 1;
		CBC.gridy = 1;
		pnlConnection.add(txtConnectToName, CBC);
		CBC.gridx = 0;
		CBC.gridy = 2;
		pnlConnection.add(lblConnectionNXTAddress, CBC);
		CBC.gridx = 1;
		CBC.gridy = 2;
		pnlConnection.add(txtConnectToAddress, CBC);
		CBC.gridx = 0;
		CBC.gridy = 3;
		CBC.gridwidth = 2;
		pnlConnection.add(btnConnectAndStart, CBC);
		CBC.gridx = 0;
		CBC.gridy = 4;
		CBC.gridwidth = 2;
		pnlConnection.add(btnDisconnectAndStop, CBC);

		// Map scale
		Label lblMapScalesHeader = new Label("Map scale");
		lblMapScalesHeader.setFont(fntSectionHeader);

		sldMapScale = new JSlider(SwingConstants.HORIZONTAL,
				MapVisulaisation.MAP_MIN_SCALE, MapVisulaisation.MAP_MAX_SCALE,
				MAP_INIT_SCALE);
		sldMapScale.setMajorTickSpacing(10);
		sldMapScale.setMinorTickSpacing(5);
		sldMapScale.setPaintTicks(true);
		sldMapScale.setPaintLabels(true);

		Hashtable<Integer, JLabel> mapScaleLabelTable = new Hashtable<Integer, JLabel>();
		mapScaleLabelTable.put(new Integer(MapVisulaisation.MAP_MIN_SCALE),
				new JLabel(MapVisulaisation.MAP_MIN_SCALE + "%"));

		mapScaleLabelTable
				.put(
						new Integer(
								(MapVisulaisation.MAP_MAX_SCALE - MapVisulaisation.MAP_MAX_SCALE / 2)),
						new JLabel(
								(MapVisulaisation.MAP_MAX_SCALE - MapVisulaisation.MAP_MAX_SCALE / 2)
										+ "%"));

		mapScaleLabelTable.put(new Integer(MapVisulaisation.MAP_MAX_SCALE),
				new JLabel(MapVisulaisation.MAP_MAX_SCALE + "%"));
		sldMapScale.setLabelTable(mapScaleLabelTable);
		sldMapScale.setBackground(LEFT_PANEL_BACKGROUND_COLOR);
		sldMapScale.addChangeListener(this);

		// Map rotate
		Label lblMapRotateHeader = new Label("Map orientation");
		lblMapRotateHeader.setFont(fntSectionHeader);

		sldMapRotate = new JSlider(SwingConstants.HORIZONTAL, 0, 360, 0);
		sldMapRotate.setMajorTickSpacing(36);
		sldMapRotate.setMinorTickSpacing(18);
		sldMapRotate.setPaintTicks(true);
		sldMapRotate.setPaintLabels(true);

		Hashtable<Integer, JLabel> mapRotateLabelTable = new Hashtable<Integer, JLabel>();
		mapRotateLabelTable.put(new Integer(0), new JLabel("0�"));
		mapRotateLabelTable.put(new Integer(180), new JLabel("180�"));
		mapRotateLabelTable.put(new Integer(360), new JLabel("360�"));

		sldMapRotate.setLabelTable(mapRotateLabelTable);
		sldMapRotate.setBackground(LEFT_PANEL_BACKGROUND_COLOR);
		sldMapRotate.addChangeListener(this);

		// Current data
		Label lblCurrentDataHeader = new Label("Current");
		lblCurrentDataHeader.setFont(fntSectionHeader);
		Panel pnlCurrentData = new Panel(new GridLayout(0, 2));
		JScrollPane pnlCurrentDataScroll = new JScrollPane(pnlCurrentData,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		lblRDX = new Label();
		lblRDY = new Label();
		lblRDRobotHeading = new Label();
		lblRDHeadDistance = new Label();
		lblRDHeadHeading = new Label();
		lblRDUssDistance = new Label();
		lblCompassValues = new Label();
		lblTargetPosX = new Label();
		lblTargetPosY = new Label();
		lblBatteryLevel = new Label();
		lblTimelineCurrentFrame = new Label();

		Label lblRDXHeader = new Label("X:");
		Label lblRDYHeader = new Label("Y:");
		Label lblRDRobotHeadingHeader = new Label("Robot heading:");
		Label lblRDHeadDistanceHeader = new Label("Head distance:");
		Label lblRDHeadHeadingHeader = new Label("Head heading:");
		Label lblTimelineCurrentFrameHeader = new Label("Frame:");

		Label lblRDUssDistanceHeader = new Label("Distance ahead:");
		Label lblCompassValuesHeader = new Label("Compass:");
		Label lblTargetPosXHeader = new Label("Target X:");
		Label lblTargetPosYHeader = new Label("Target Y:");
		Label lblBatteryLevelHeader = new Label("Battery voltage:");

		lblRDXHeader.setFont(fntLabelHeader);
		lblRDYHeader.setFont(fntLabelHeader);
		lblRDRobotHeadingHeader.setFont(fntLabelHeader);
		lblRDHeadDistanceHeader.setFont(fntLabelHeader);
		lblRDHeadHeadingHeader.setFont(fntLabelHeader);
		lblTimelineCurrentFrameHeader.setFont(fntLabelHeader);
		lblRDUssDistanceHeader.setFont(fntLabelHeader);
		lblCompassValuesHeader.setFont(fntLabelHeader);
		lblTargetPosXHeader.setFont(fntLabelHeader);
		lblTargetPosYHeader.setFont(fntLabelHeader);
		lblBatteryLevelHeader.setFont(fntLabelHeader);

		pnlCurrentData.add(lblRDXHeader);
		pnlCurrentData.add(lblRDX);
		pnlCurrentData.add(lblRDYHeader);
		pnlCurrentData.add(lblRDY);
		pnlCurrentData.add(lblRDRobotHeadingHeader);
		pnlCurrentData.add(lblRDRobotHeading);
		pnlCurrentData.add(lblRDHeadDistanceHeader);
		pnlCurrentData.add(lblRDHeadDistance);
		pnlCurrentData.add(lblRDHeadHeadingHeader);
		pnlCurrentData.add(lblRDHeadHeading);
		pnlCurrentData.add(lblRDUssDistanceHeader);
		pnlCurrentData.add(lblRDUssDistance);
		pnlCurrentData.add(lblCompassValuesHeader);
		pnlCurrentData.add(lblCompassValues);
		pnlCurrentData.add(lblTargetPosXHeader);
		pnlCurrentData.add(lblTargetPosX);
		pnlCurrentData.add(lblTargetPosYHeader);
		pnlCurrentData.add(lblTargetPosY);
		pnlCurrentData.add(lblBatteryLevelHeader);
		pnlCurrentData.add(lblBatteryLevel);
		pnlCurrentData.add(lblTimelineCurrentFrameHeader);
		pnlCurrentData.add(lblTimelineCurrentFrame);

		// Send data
		Label lblSendDataHeader = new Label("Send data");
		lblSendDataHeader.setFont(fntSectionHeader);
		JPanel pnlSendData = new JPanel(new FlowLayout());
		pnlSendData.setBackground(LEFT_PANEL_BACKGROUND_COLOR);
		btnSendTargetData = new JButton("Send data");
		btnSendTargetData.addActionListener(this);

		// //Target pos
		JPanel pnlTargetPos = new JPanel(new FlowLayout());
		pnlTargetPos.setBackground(LEFT_PANEL_BACKGROUND_COLOR);

		txtTargetPosX = new JTextField("0", 5);
		txtTargetPosY = new JTextField("0", 5);

		pnlTargetPos.add(new JLabel("X: "));
		pnlTargetPos.add(txtTargetPosX);
		pnlTargetPos.add(new JLabel("Y: "));
		pnlTargetPos.add(txtTargetPosY);

		// Send data

		pnlSendData.add(pnlTargetPos);
		pnlSendData.add(btnSendTargetData);

		// Control panel
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(logoPanel);

		controlPanel.add(lblConnectionHeader);
		controlPanel.add(pnlConnection);

		controlPanel.add(lblMapScalesHeader);
		controlPanel.add(sldMapScale);

		controlPanel.add(lblMapRotateHeader);
		controlPanel.add(sldMapRotate);

		controlPanel.add(lblCurrentDataHeader);
		controlPanel.add(pnlCurrentDataScroll);

		controlPanel.add(lblSendDataHeader);
		controlPanel.add(pnlSendData);

		// Timeline
		mapTimeline.addFrameChangeListeners(this);
		mapTimeline.setBgColor(BOTTOM_PANEL_BACKGROUND_COLOR);

		// Bottom panel
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, PANEL_MARGIN,
				PANEL_MARGIN, PANEL_MARGIN));
		bottomPanel.setBackground(BOTTOM_PANEL_BACKGROUND_COLOR);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(mapTimeline, BorderLayout.CENTER);

		// Left panel
		leftPanel.setBorder(BorderFactory.createEmptyBorder(PANEL_MARGIN,
				PANEL_MARGIN, PANEL_MARGIN, 0));
		leftPanel.setBackground(LEFT_PANEL_BACKGROUND_COLOR);
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftPanel.add(controlPanel);

		// Right panel
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(viewPanel, BorderLayout.CENTER);
		rightPanel.add(bottomPanel, BorderLayout.SOUTH);

		// Menu
		// mnuMainBar = getMenuBar();

		// Main panel
		mainPanel.setLayout(new BorderLayout());
		// mainPanel.add(mnuMainBar, BorderLayout.NORTH);
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.CENTER);

		// View panel
		viewPanel.setLayout(new BorderLayout());
		viewPanel.add(new ComponentSpacer(mapVisulaisation, PANEL_MARGIN,
				VIEW_PANEL_BACKGROUND_COLOR, MAP_PANEL_BORDER_WIDTH,
				MAP_PANEL_BORDER_COLOR, MAP_PANEL_BACKGROUND_COLOR),
				BorderLayout.CENTER);

		return mainPanel;
	}

	private void setupMapProcessors() {
		mapProcessors = new MapProcessors(PenemuNXTDefaultMapProcessors
				.getDefaultProcessors());
	}

	private void setupMapProcessorsMenu() {
		mnuMapProcessors.removeAll();
		for (IMapProcessor.MapProcessorType processorType : IMapProcessor.MapProcessorType
				.values()) {
			JMenu mnuCategory = new JMenu(processorType.name());
			for (final IMapProcessor mapProcessor : mapProcessors.getList()) {
				if (mapProcessor.getType() == processorType) {
					final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(
							mapProcessor.getName(), mapProcessor.isEnabled());
					menuItem.setForeground(mapProcessor.getColor());
					menuItem.setToolTipText(mapProcessor.getDescription());

					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							mapProcessor.setEnabled(menuItem.getState());
							if (MapProcessorsListView != null) {
								MapProcessorsListView.refresh();
							}
							refreshMap();
						}
					});
					mnuCategory.add(menuItem);
				}
			}
			mnuMapProcessors.add(mnuCategory);
		}
	}

	public void refreshLatestData() {
		RobotData LatestData = null;
		if (DS != null && DS.NXTRobotData != null) {
			LatestData = MapUtilities.getLatestData(DS.NXTRobotData,
					mapTimeline.getCurrentFrame() - 1);
		}

		if (DS != null && DS.NXTRobotData != null && LatestData != null) {
			lblRDX.setText(String.valueOf(LatestData.getPosX()));
			lblRDY.setText(String.valueOf(LatestData.getPosY()));
			lblRDRobotHeading.setText(String.valueOf(LatestData
					.getRobotHeading()));
			lblRDHeadDistance.setText(String.valueOf(LatestData
					.getHeadDistance()));
			lblRDHeadHeading.setText(String
					.valueOf(LatestData.getHeadHeading()));
			lblTimelineCurrentFrame.setText(String.valueOf(mapTimeline
					.getCurrentFrame()));

			lblRDUssDistance.setText(String
					.valueOf(LatestData.getUssDistance()));
			lblCompassValues.setText(String.valueOf(LatestData
					.getCompassValues()));
			lblTargetPosX.setText(String.valueOf(LatestData.getTargetPosX()));
			lblTargetPosY.setText(String.valueOf(LatestData.getTargetPosY()));
			lblBatteryLevel.setText(String
					.valueOf(LatestData.getBatteryLevel() / 1000f));
		} else {
			lblRDHeadDistance.setText("");
			lblRDHeadHeading.setText("");
			lblRDRobotHeading.setText("");
			lblRDX.setText("");
			lblRDY.setText("");
			lblTimelineCurrentFrame.setText("");

			lblRDUssDistance.setText("");
			lblCompassValues.setText("");
			lblTargetPosX.setText("");
			lblTargetPosY.setText("");
			lblBatteryLevel.setText("");
		}
	}

	public void refreshMap() {
		if (mapVisulaisation != null) {
			mapVisulaisation.setMapCurrentFrame(mapTimeline.getCurrentFrame());
			mapVisulaisation.refresh();
		}
	}

	@Override
	public void run() {
		// Object to share data internal
		DS = new DataShare();

		mapVisulaisation.setDS(DS);
		mapTimeline.setDS(DS);
		mapTimeline.setEnabled(false);
		mapTimeline.setMapThumbnail(MAP_INIT_SCALE, mapProcessors, DS);

		// Auto open
		if (PRELOAD_PENEMUNXT_MAP_PATH.length() > 0) {
			MapFileUtilities.openData(PRELOAD_PENEMUNXT_MAP_PATH, DS);
		}

		while (AppActive) {
			if (mapVisulaisation != null) {
				sldMapScale.setValue(mapVisulaisation.getMapScale());
				sldMapRotate.setValue((int) Math.toDegrees(mapVisulaisation
						.getMapRotate()));
			}

			mapTimeline.refresh();

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}

		System.exit(0);
	}

	private void connectRobot() {
		if (RC != null) {
			RC.disconnect();
		}
		RC = new RobotConnection(NXTC, DS, CONNECTION_MODES[cboConnectionTypes
				.getSelectedIndex()], txtConnectToName.getText(),
				txtConnectToAddress.getText());
		RC.start();
	}

	private void sendTargetData(int x, int y, boolean updateTextBoxes) {
		if (updateTextBoxes) {
			txtTargetPosX.setText(String.valueOf(x));
			txtTargetPosY.setText(String.valueOf(y));
		}
		sendTargetData(x, y);
	}

	private void sendTargetData(int x, int y) {
		if (RC != null && RC.NXTC != null) {
			RC.NXTC.sendData(new ServerData(x, y, 0));
		}
	}

	private void sendTargetData() {
		if (RC != null && RC.NXTC != null) {
			try {
				int x = Integer.parseInt(txtTargetPosX.getText());
				int y = Integer.parseInt(txtTargetPosY.getText());
				sendTargetData(x, y);
			} catch (NumberFormatException e) {
			}
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == mnuFileExitButton) {
			exitApp();
		} else if (ae.getSource() == btnConnectAndStart) {
			connectAndStart();
		} else if (ae.getSource() == btnDisconnectAndStop) {
			disconnectAndStop();
		} else if (ae.getSource() == mnuFileOpenButton) {
			MapFileUtilities.openData(DEFAULT_FOLDER_PATH, this, DS,
					mapProcessors);
		} else if (ae.getSource() == mnuFileSaveButton) {
			MapFileUtilities.saveData(DEFAULT_FOLDER_PATH, this, DS);
		} else if (ae.getSource() == mnuFileExportMapAsImageButton) {
			if (mapVisulaisation != null) {
				MapFileUtilities.saveRenderedMap(DEFAULT_FOLDER_PATH, this,
						mapVisulaisation);
			}
		} else if (ae.getSource() == mnuFileOpenDataViewButton) {
			openDataView();
		} else if (ae.getSource() == mnuFileOpenMapProcessorsButton) {
			openMapProcessorsListView();
		} else if (ae.getSource() == mnuMapClearButton) {
			clearMap();
		} else if (ae.getSource() == mnuAboutAuthors) {
			showAbout();
		} else if (ae.getSource() == btnSendTargetData) {
			sendTargetData();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == mapVisulaisation) {
			if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					sendTargetData(e.getX(), e.getY(), true);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					mapVisulaisation.reset();
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		if (ce.getSource() == sldMapScale) {
			if (mapVisulaisation != null) {
				mapVisulaisation.setMapScale(sldMapScale.getValue(), false);
			}
			refreshMap();
		} else if (ce.getSource() == sldMapRotate) {
			if (mapVisulaisation != null) {
				mapVisulaisation.setMapRotate(Math.toRadians(sldMapRotate
						.getValue()));
			}
			refreshMap();
		} else if (ce.getSource() == MapProcessorsListView) {
			refreshMap();
			setupMapProcessorsMenu();
		} else if (ce.getSource() == mapVisulaisation) {
			refreshMap();
		} else if (ce.getSource() == DataView) {
			if (DataView != null && mapTimeline.isEnabled()
					&& DataView.getSelectedFrame() >= 0) {
				mapTimeline.setCurrentFrame(DataView.getSelectedFrame());
			}
		} else if (ce.getSource() == mapTimeline) {
			timelineChanged();
		}
	}

	private void timelineChanged() {
		refreshMap();
		refreshLatestData();
		// RobotData LatestData = null;
		// if (DS != null && DS.NXTRobotData != null) {
		// LatestData = MapUtilities.getLatestData(DS.NXTRobotData,
		// 0);
		// }
		// if(LatestData!=null){
		// mapVisulaisation.setMapRotate(Math
		// .toRadians(LatestData.getCompassValues()));
		// }

		if (DataView != null
				&& DataView.getWindowState() == DataTableWindow.WINDOW_STATE_OPEN) {
			DataView.selectFrame(mapTimeline.getCurrentFrame());
			DataView.focus();
		}
	}

	private void openDataView() {
		if (DataView != null
				&& DataView.getWindowState() == DataTableWindow.WINDOW_STATE_OPEN) {
			DataView.refresh(mapTimeline.getCurrentFrame(), true);
		} else {
			DataView = new PenemuNXTExplorerDataViewer(DS.NXTRobotData,
					APPLICATION_NAME + " - Data view", APPLICATION_ICON
							.getImage());
			DataView.setSelectedFrameChanged(this);
			DataView.open();
		}
	}

	private void openMapProcessorsListView() {
		if (MapProcessorsListView != null
				&& MapProcessorsListView.getWindowState() == DataTableWindow.WINDOW_STATE_OPEN) {
			MapProcessorsListView.refresh(true);
		} else {
			MapProcessorsListView = new MapProcessorsList(mapProcessors, DS,
					APPLICATION_NAME + " - Map Processors", APPLICATION_ICON
							.getImage());
			MapProcessorsListView.setDataChanged(this);
			MapProcessorsListView.open();
		}
	}

	private void exitApp() {
		AppActive = false;
	}

	private void showAbout() {
		JOptionPane
				.showMessageDialog(null, APPLICATION_ABOUT_DIALOG_TEXT,
						APPLICATION_ABOUT_DIALOG_TITLE,
						JOptionPane.INFORMATION_MESSAGE);
	}

	private void clearMap() {
		if (DS != null) {
			DS.NXTRobotData.clear();
		}

		refreshLatestData();
		refreshMap();
	}

	private void disconnectAndStop() {
		if (RC != null) {
			RC.disconnect();
		}

		btnConnectAndStart.setEnabled(true);
		btnDisconnectAndStop.setEnabled(false);
		cboConnectionTypes.setEnabled(true);
		txtConnectToName.setEnabled(true);
		txtConnectToAddress.setEnabled(true);
	}

	private void connectAndStart() {
		btnConnectAndStart.setEnabled(false);
		btnDisconnectAndStop.setEnabled(true);
		cboConnectionTypes.setEnabled(false);
		txtConnectToName.setEnabled(false);
		txtConnectToAddress.setEnabled(false);

		connectRobot();
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (RC != null && RC.isConnectionActive()) {
			disconnectAndStop();
			exitApp();
		} else {
			System.exit(0);
		}
	}

	public void windowActivated(WindowEvent arg0) {
		refreshMap();
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		refreshMap();
	}

	public void windowOpened(WindowEvent arg0) {
		refreshMap();
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}