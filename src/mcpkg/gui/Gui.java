package mcpkg.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JToggleButton;

import mcpkg.*;
import mcpkg.Package;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Gui implements ActionListener{

	public final class PackageTableModel implements TableModel {
		public Package[] packageListValues = null;
		

		public void set(Package[] mp) {
			Package prevselected = null;
			ListSelectionModel lsm = packageTable.getSelectionModel();
			if(getRowCount() > 0 && !lsm.isSelectionEmpty())
			{
				prevselected = packageListValues[lsm.getMinSelectionIndex()];
			}
			lsm.clearSelection();
			packageListValues = mp;
			//may throw
			TableModelEvent event = new TableModelEvent(this);
			for(int i=0; i<listeners.size(); i++)
			{
				TableModelListener l = listeners.get(i);
				l.tableChanged(event);
			}
			for(int i=0; i<mp.length; i++)
			{
				if (mp[i] == prevselected)
				{
					lsm.setSelectionInterval(i, i);
				}
			}
			packageTable.repaint();
		}

		ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
		@Override
		public void addTableModelListener(TableModelListener arg0) {
			// TODO Auto-generated method stub
			listeners.add(arg0);
		}

		@Override
		public void removeTableModelListener(TableModelListener arg0) {
			// TODO Auto-generated method stub
			listeners.remove(arg0);
		}
		
		String[] columnnames = new String[] {
				"", "ID", "Queued", "Avail.", "Section", "Description"
			};
		Class[] columnTypes = new Class[] {
				Color.class, Object.class, Object.class, Object.class, Object.class, Object.class
			};
		
		@Override
		public Class<?> getColumnClass(int arg0) {
			// TODO Auto-generated method stub
			return columnTypes[arg0];
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnTypes.length;
		}

		@Override
		public String getColumnName(int arg0) {
			// TODO Auto-generated method stub
			return columnnames[arg0];
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return packageListValues != null ? packageListValues.length : 0;
		}

		//extremely long name warning in these constants ...
		public final Color colorSafeToInstall = new Color(0xffffff); //0
		public final Color colorInstalled = new Color(0x1bdc1b); //7
		public final Color colorBroken = new Color(0xd42020); //3
		
		public final Color colorIncompatMCUpgrade = new Color(0x11e7d0); //1
		public final Color colorRequireMCUpgrade = new Color(0xf4aa28); //2
		
		
		public final Color colorPackageUpdateRequired = new Color(0xffd284); //4
		public final Color colorNoPackageUpdate = new Color(0xffd284); //5
		
		public final Color colorPackageUpdateAvail = new Color(0x1624de); //6
		

		public final Color colorNotSafeToInstall = new Color(0xff8484);
		public final Color colorNotInstalledIncompatMCUpgrade = new Color(0xbcfff8);
		public final Color colorNotInstalledRequireMCUpgrade = new Color(0xffe3b3);
		public final Color colorUpgradeAvailWillBreak = new Color(0xe5ec1f);
		public final Color colorMCAndPackageUpgrade = new Color(0xec621f);
		public final Color colorMCUpgradePackageUpgradeCurrentMCVersion = new Color(0x16a8de);
		public final Color colorPackageBrokenUpgradeBoth = new Color(0xff5400);
		
		
		//not installed, safe to install - _0
		//not installed, not safe to install - ff8484
		//not installed, can install but will not work if minecraft is upgraded - bcfff8
		//not installed, will not work unless minecraft is upgraded before install - ffe3b3
		//everything is up to date and it's queued - _7
		//everything is up to date but conflicts with minecraft - _3
		//package is up to date, will not work if minecraft is updated, no upgrade available - _1
		//package is up to date, but needs an upgrade of minecraft to be used - _2
		//package is up to date but conflicts with minecraft - _3
		//package upgrade is available, will work fine - _6
		//package upgrade is available but will break package - e5ec1f
		//upgrade is available that will fix current version incompatibility - _4
		//conflicts - _3
		//minecraft upgrade is available and package upgrade is available, must upgrade both - ec621f
		//minecraft upgrade is available, package upgrade is available but is for current version - 16a8de
		//package upgrade will break package - minecraft upgrade is available - _5
		//minecraft upgrade is required to use package - package upgrade is available - _2
		//upgrade of package will make it compatible with current version, but make it incompatible with latest minecraft - _3
		//upgrade will break package - minecraft upgrade is available but doesn't matter - _5
		//package is currently broken, but upgrading both minecraft and the package will fix this - ff5400
		//package is currently broken, but upgrading will make it compatible with current minecraft - _4
		//package is broken, upgrade is available that will make no difference - minecraft upgrade available - _3

		//_0 clean
		//_1 generally, will not work if minecraft is updated - 11e7d0
		//_2 generally, will only work if minecraft is updated - f4aa28
		//_3 generally, will not work - d42020

		//_4 generally, will only work if package is updated - ecd11f
		//_5 generally, will not work if package is updated - c81fec
		//_6 generally, package upgrade is available - 1624de
		//_7 generally, things are good - 1bdc1b
		
		
		//@Override
		public Object getValueAt(int packagenum, int fieldnum) {
			// TODO Auto-generated method stub
			Package p = packageListValues[packagenum]; //don't make any assumptions about what version we have here
			if(p.isCorrupt) //should never happen with the new read prevention
				calcList();
			Package queuedVersion = p.getQueuedVersion();
			Package latestVersion = p.getLatest();
			switch (fieldnum)
			{
			case 0:
				String mcvers = Util.getCachedMinecraftVersion();
				String latestmcvers = null;
				try{
					latestmcvers = Util.getLatestMinecraftVersion();
				}catch (Throwable t)
				{
					Messaging.message(t.getMessage());
					t.printStackTrace();
					latestmcvers = mcvers;
				}
				boolean mcislatest = latestmcvers.equals(mcvers);
				if(queuedVersion == null)
				{
					if(mcislatest)
					{
						if(latestVersion.MCVersion.equals(mcvers))
						{
							//not installed, safe to install
							return colorSafeToInstall;
						}
						else
						{
							//not installed, not safe to install
							return colorNotSafeToInstall;
						}
					}
					else
					{
						if(latestVersion.MCVersion.equals(mcvers))
						{
							//not installed, can install but will not work if minecraft is upgraded
							return colorNotInstalledIncompatMCUpgrade;
						}
						else if(latestVersion.MCVersion.equals(latestmcvers))
						{
							//not installed, will not work unless minecraft is upgraded before install
							return colorNotInstalledRequireMCUpgrade;
						}
						else
						{
							//not installed, not safe to install
							return colorNotSafeToInstall;
						}
					}
				}
				else
				{
					if(queuedVersion == latestVersion)
					{
						if (mcislatest)
						{
							if(queuedVersion.MCVersion.equals(mcvers))
							{
								//everything is up to date and it's queued
								return colorInstalled;
							}
							else
							{
								//everything is up to date but conflicts with minecraft
								return colorBroken;
							}
						}
						else
						{
							if(queuedVersion.MCVersion.equals(mcvers))
							{
								//package is up to date, will not work if minecraft is updated, no upgrade available
								return colorIncompatMCUpgrade;
							}
							else if (queuedVersion.MCVersion.equals(latestmcvers))
							{
								//package is up to date, but needs an upgrade of minecraft to be used
								return colorRequireMCUpgrade;
							}
							else
							{
								//package is up to date but conflicts with minecraft
								return colorBroken;
							}
						}
					}
					else
					{
						if(mcislatest)
						{
							if(queuedVersion.MCVersion.equals(mcvers) && latestVersion.MCVersion.equals(mcvers))
							{
								//package upgrade is available, will work fine
								return colorPackageUpdateAvail;
							}
							else if(queuedVersion.MCVersion.equals(mcvers))
							{
								//package upgrade is available but will break package
								return colorUpgradeAvailWillBreak;
							}
							else if(latestVersion.MCVersion.equals(mcvers))
							{
								//upgrade is available that will fix current version incompatibility
								return colorPackageUpdateRequired;
							}
							else
							{
								//conflicts
								return colorBroken;
							}
						}
						else
						{
							if(queuedVersion.MCVersion.equals(mcvers))
							{
								if(latestVersion.MCVersion.equals(latestmcvers))
								{
									//minecraft upgrade is available and package upgrade is available, must upgrade both
									return colorMCAndPackageUpgrade;
								}
								else if(latestVersion.MCVersion.equals(mcvers))
								{
									//minecraft upgrade is available, package upgrade is available but is for current version
									return colorMCUpgradePackageUpgradeCurrentMCVersion;
								}
								else
								{
									//package upgrade will break package - minecraft upgrade is available
									return colorNoPackageUpdate;
								}
							}
							else if(queuedVersion.MCVersion.equals(latestmcvers))
							{
								if(latestVersion.MCVersion.equals(latestmcvers))
								{
									//minecraft upgrade is required to use package - package upgrade is available
									return colorRequireMCUpgrade;
								}
								else if (latestVersion.MCVersion.equals(mcvers))
								{
									//upgrade of package will make it compatible with current version, but make it incompatible with latest minecraft
									return colorBroken;
								}
								else
								{
									//upgrade will break package - minecraft upgrade is available but doesn't matter
									return colorNoPackageUpdate;
								}
							}
							else
							{
								if(latestVersion.MCVersion.equals(latestmcvers))
								{
									//package is currently broken, but upgrading both minecraft and the package will fix this
									return colorPackageBrokenUpgradeBoth;
								}
								else if (latestVersion.MCVersion.equals(mcvers))
								{
									//package is currently broken, but upgrading will make it compatible with current minecraft
									return colorPackageUpdateRequired;
								}
								else
								{
									//package is broken, upgrade is available that will make no difference - minecraft upgrade available
									return colorBroken;
								}
							}
						}
					}
				}
			case 1:
				return p.Name;
			case 2:
				if (queuedVersion == null)
					return "";
				else
					return queuedVersion.MCVersion+"/"+queuedVersion.Version;
			case 3:
				return latestVersion.MCVersion+"/"+latestVersion.Version;
			case 4:
				return p.Section;
			case 5:
				return p.ShortDescription;
			default:
				return "unmapped field";
			}
		}

		@Override
		public boolean isCellEditable(int x, int y) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setValueAt(Object arg0, int arg1, int arg2) {
			// TODO: do we need this?
		}
	}

	public JFrame frmMcpkg;
	public JTextField textField;
	public JTextField searchBox;

	public JComboBox sectionBox;
	public JButton btnRunMinecraft;
	public JToggleButton tglbtnShowQueue;
	public JButton btnQueueOrUnqueue;
	public JLabel lblPackageName;
	public JButton btnMakePackage;
	public JButton btnOptions;
	public JLabel lblStatus;
	public JTextPane txtpnPackageDescription;
	public MakePackage dlgMakePackage;
	public JFileChooser fileChooser;
	public PackageTableModel packageTableModel;
	
	public void calcList()
	{
		//ArrayList<Package> templist = new ArrayList<Package>(); //arraylists are great array builders
		Package[] mp = null;
		
		if(tglbtnShowQueue.isSelected())
		{
			try {
				mp = Commands.getQueue();
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
		}
		else
		{
			String search = searchBox.getText();
			if(search != null && search.length() > 0)
			{
				try {
					mp = Commands.queryPackages(search);
				} catch (FileNotFoundException e) {
					Messaging.message(e.getMessage());
					return;
				} catch (IOException e) {
					Messaging.message(e.getMessage());
					return;
				}
			}
			else
			{
				try {
					mp = Commands.getPackages();
				} catch (FileNotFoundException e) {
					Messaging.message(e.getMessage());
				} catch (IOException e) {
					Messaging.message(e.getMessage());
				}
			}
			String section = (String) sectionBox.getSelectedItem();
			if(section != null && !section.equals("") && !section.equals("all"))
			{
				ArrayList<Package> templist = new ArrayList<Package>();
				for(int i=0; i<mp.length; i++)
				{
					if(mp[i].Section.equals(section))
					{
						templist.add(mp[i]);
					}
				}
				mp = templist.toArray(new Package[0]);
			}
			else if (section.equals("") || section.equals("all"))
			{
				ArrayList<Package> templist = new ArrayList<Package>();
				for(int i=0; i<mp.length; i++)
				{
					if(!mp[i].Section.equals("libraries"))
					{
						templist.add(mp[i]);
					}
				}
				mp = templist.toArray(new Package[0]);
			}
		}
		packageTableModel.set(mp);
		//for(int )
		
		//if()
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frmMcpkg.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			System.out.println("lf2");
		} catch (Throwable ex2) {
		}
		frmMcpkg = new JFrame();
		frmMcpkg.setTitle("mcpkg");
		frmMcpkg.setBounds(100, 100, 822, 560);
		frmMcpkg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMcpkg.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel minecraftButtons = new JPanel();
		minecraftButtons.setLayout(new BorderLayout(0, 0));
		btnRunMinecraft = new JButton("Run Minecraft (doesn't update)");
		btnRunMinecraft.addActionListener(this);
		minecraftButtons.add(btnRunMinecraft, BorderLayout.NORTH);
		frmMcpkg.getContentPane().add(minecraftButtons, BorderLayout.NORTH);
		
		btnUpdateMinecraft = new JButton("Update Minecraft (doesn't run)");
		minecraftButtons.add(btnUpdateMinecraft, BorderLayout.SOUTH);
		
		JPanel statusPane = new JPanel();
		frmMcpkg.getContentPane().add(statusPane, BorderLayout.SOUTH);
		statusPane.setLayout(new BorderLayout(0, 0));
		
		lblStatus = new JLabel("Status");
		statusPane.add(lblStatus, BorderLayout.WEST);
		
		btnMakePackage = new JButton("Make Package ..");
		btnMakePackage.addActionListener(this);
		statusPane.add(btnMakePackage, BorderLayout.EAST);
		
		JPanel panel = new JPanel();
		statusPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		btnOptions = new JButton("Options...");
		btnOptions.setEnabled(false);
		panel.add(btnOptions, BorderLayout.EAST);
		
		JSplitPane selectionPane = new JSplitPane();
		selectionPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frmMcpkg.getContentPane().add(selectionPane, BorderLayout.CENTER);
		
		JScrollPane packageDescScroller = new JScrollPane();
		selectionPane.setRightComponent(packageDescScroller);
		
		JPanel packageDescHeader = new JPanel();
		packageDescScroller.setColumnHeaderView(packageDescHeader);
		packageDescHeader.setLayout(new BorderLayout(0, 0));
		
		lblPackageName = new JLabel("No package is selected.");
		packageDescHeader.add(lblPackageName, BorderLayout.WEST);
		
		btnQueueOrUnqueue = new JButton("Queue or Unqueue");
		JPanel packageButtons = new JPanel();
		packageButtons.setLayout(new BorderLayout(0, 0));
		packageButtons.add(btnQueueOrUnqueue, BorderLayout.EAST);
		packageDescHeader.add(packageButtons, BorderLayout.EAST);
		
		btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(this);
		btnUpdate.setVisible(false);
		packageButtons.add(btnUpdate, BorderLayout.WEST);
		
		txtpnPackageDescription = new JTextPane();
		txtpnPackageDescription.setEditable(false);
		btnQueueOrUnqueue.setVisible(false);
		btnQueueOrUnqueue.addActionListener(this);
		packageDescScroller.setViewportView(txtpnPackageDescription);
		
		JPanel pkgListPanel = new JPanel();
		selectionPane.setLeftComponent(pkgListPanel);
		pkgListPanel.setLayout(new BorderLayout(0, 0));
		
		/*JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		textField = new JTextField();
		panel_1.add(textField);
		textField.setColumns(10);
		
		JComboBox comboBox = new JComboBox();
		panel_1.add(comboBox);*/
		packageTableModel = new PackageTableModel();
		//System.out.println(packageList.getSelectedIndex());
		JScrollPane packageScrollPane = new JScrollPane();
		// Or in two steps:
		
		pkgListPanel.add(packageScrollPane, BorderLayout.CENTER);
		
		packageTable = new JTable();
		packageTable.setModel(packageTableModel);
		//packageTable.
		packageTable.getColumnModel().getColumn(0).setResizable(false);
		packageTable.getColumnModel().getColumn(0).setPreferredWidth(21);
		packageTable.getColumnModel().getColumn(0).setMinWidth(21);
		packageTable.getColumnModel().getColumn(0).setMaxWidth(21);
		packageTable.getColumnModel().getColumn(1).setPreferredWidth(0);
		packageTable.getColumnModel().getColumn(2).setPreferredWidth(0);
		packageTable.getColumnModel().getColumn(3).setPreferredWidth(0);
		packageTable.getColumnModel().getColumn(4).setPreferredWidth(0);
		packageTable.getColumnModel().getColumn(5).setPreferredWidth(217);
		packageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		packageTable.setFillsViewportHeight(true);
		packageTable.setDefaultRenderer(Color.class, new CellColorRenderer(true));
		
		ListSelectionModel rowSM = packageTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages.
				if (e.getValueIsAdjusting()) return;
				updatePackageView();
			}
		});
		
		packageScrollPane.setViewportView(packageTable);
		
		JPanel pkglistHeader = new JPanel();
		pkgListPanel.add(pkglistHeader, BorderLayout.NORTH);
		pkglistHeader.setLayout(new BorderLayout(0, 0));
		
		JPanel searchPanel = new JPanel();
		pkglistHeader.add(searchPanel, BorderLayout.WEST);
		searchPanel.setLayout(new BorderLayout(0, 0));
		
		sectionBox = new JComboBox();
		String[][] sections = null;
		try {
			sections = Commands.getSections();
		} catch (FileNotFoundException e) {
			Messaging.message(e.getMessage());
		} catch (IOException e) {
			Messaging.message(e.getMessage());
		}
		String[] sectionsList = new String[sections.length+1];
		sectionsList[0] = "all";
		for(int i=0; i<sections.length; i++)
		{
			sectionsList[i+1] = sections[i][0];
		}
		
		sectionBox.setModel(new DefaultComboBoxModel(sectionsList));
		sectionBox.setSize(new Dimension(0, 28));
		sectionBox.setPreferredSize(new Dimension(200, 28));
		sectionBox.addActionListener(this);
		searchPanel.add(sectionBox, BorderLayout.SOUTH);
		
		JLabel lblSection = new JLabel("Section");
		searchPanel.add(lblSection, BorderLayout.NORTH);
		
		JPanel panel_3 = new JPanel();
		pkglistHeader.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblSearch = new JLabel("Search");
		panel_3.add(lblSearch, BorderLayout.NORTH);
		
		searchBox = new JTextField();
		searchBox.setSize(new Dimension(0, 28));
		searchBox.setPreferredSize(new Dimension(4, 28));
		searchBox.setMaximumSize(new Dimension(2147483647, 28));
		searchBox.setMinimumSize(new Dimension(4, 28));
		searchBox.addActionListener(this);
		panel_3.add(searchBox, BorderLayout.SOUTH);
		searchBox.setColumns(10);
		
		tglbtnShowQueue = new JToggleButton("Show Queue");
		tglbtnShowQueue.addActionListener(this);
		pkglistHeader.add(tglbtnShowQueue, BorderLayout.EAST);
		
		fileChooser = new JFileChooser();
		dlgMakePackage = new MakePackage(this);
		
		
		
		calcList();
		GuiMessagingThread g = new GuiMessagingThread(lblStatus, frmMcpkg, this);
		Commands.launchthread();
		

		selectionPane.setDividerLocation(250);
		btnUpdateMinecraft.setVisible(false);
		
		/*JList list = new JList();
		list.setModel(new AbstractListModel() {
			String[] values = new String[] {"package1", "package2", "package3"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		panel.add(list, BorderLayout.SOUTH);*/
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == searchBox || arg0.getSource() == tglbtnShowQueue || arg0.getSource() == sectionBox)
		{
			calcList();
		}
		else if(arg0.getSource() == btnQueueOrUnqueue)
		{

			if(selectedPackage.isCorrupt)
			{
				calcList();
				updatePackageView();
			} else if(selectedPackage.isQueued)
			{
				Commands.queue(new Commands.unqueuePackage(selectedPackage));
			}
			else
			{
				Commands.queue(new Commands.queuePackage(selectedPackage));
			}
		}
		else if(arg0.getSource() == btnUpdate)
		{
			if(selectedPackage.isCorrupt)
			{
				calcList();
				updatePackageView();
			} else
			{
				Commands.queue(new Commands.updatePackage(selectedPackage.getQueuedVersion()));
			}
		}
		else if(arg0.getSource() == btnRunMinecraft)
		{
			Commands.queue(new Commands.runMinecraft());
		}
		else if(arg0.getSource() == btnMakePackage)
		{
			
			
		      /*int rVal = c.showSaveDialog(FileChooserTest.this);
		      if (rVal == JFileChooser.APPROVE_OPTION) {
		        filename.setText(c.getSelectedFile().getName());
		        dir.setText(c.getCurrentDirectory().toString());
		      }
		      if (rVal == JFileChooser.CANCEL_OPTION) {
		        filename.setText("You pressed cancel");
		        dir.setText("");
		      }*/
			
			dlgMakePackage.setVisible(true);
		}
			
	}
	public Package selectedPackage = null;
	private JTable packageTable;
	private JButton btnUpdateMinecraft;
	private JButton btnUpdate;
	public void updatePackageView()
	{
		ListSelectionModel lsm = packageTable.getSelectionModel();
		//packageList.getSelectedIndices();
		
		if(lsm.getMinSelectionIndex() < 0 || packageTableModel.packageListValues == null ||  lsm.getMinSelectionIndex()>=packageTableModel.packageListValues.length)
		{
			selectedPackage = null;
			lblPackageName.setText("No package is selected.");
			txtpnPackageDescription.setText("");
			btnQueueOrUnqueue.setVisible(false);
			btnUpdate.setVisible(false);
			return;
		}
		else
		{
			selectedPackage = packageTableModel.packageListValues[lsm.getMinSelectionIndex()];
			lblPackageName.setText(selectedPackage.Name);
			String subdesc = selectedPackage.FullDescription;
			subdesc = "\t"+selectedPackage.ShortDescription +"\n\n" + subdesc.substring(subdesc.indexOf("\n"));
			
			txtpnPackageDescription.setText(subdesc);
			btnQueueOrUnqueue.setVisible(true);
			//System.out.println(selectedPackage.isQueued);
			if(selectedPackage.getQueuedVersion() != null)
			{
				btnQueueOrUnqueue.setText("Unqueue (remove)");
				if(selectedPackage.getQueuedVersion() != selectedPackage.getLatest())
				{
					btnUpdate.setVisible(true);
				}
				else
				{
					btnUpdate.setVisible(false);
				}
			}
			else
			{
				btnQueueOrUnqueue.setText("Queue (install)");
			}
		}
		if(selectedPackage.isCorrupt)
		{
			calcList();
			updatePackageView();
		}
	}
	
	

}
