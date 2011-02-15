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

public class Gui implements ActionListener, ListSelectionListener {

	public final class PackageListModel implements ListModel {
		public Package[] packageListValues = null;
		public int getSize() {
			return packageListValues != null ? packageListValues.length : 0;
		}
		
		public Object getElementAt(int index) {
			Package p = packageListValues[index];
			if(p.isCorrupt)
				calcList();
			return (p.isQueued?"-":" ")+" "+p.Name+" - "+p.ShortDescription + " ("+p.MCVersion+"/"+p.Version+")";
		}
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
		@Override
		public void addListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
			listeners.add(arg0);
		}

		@Override
		public void removeListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
			listeners.remove(arg0);
			
		}

		public void set(Package[] mp) {
			Package prevselected = null;
			if(getSize() > 0 && !packageList.isSelectionEmpty())
			{
				prevselected = packageListValues[packageList.getSelectedIndex()];
			}
			packageList.clearSelection();
			packageListValues = mp;
			//may throw
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, mp.length);
			for(int i=0; i<listeners.size(); i++)
			{
				ListDataListener l = listeners.get(i);
				l.contentsChanged(event);
			}
			for(int i=0; i<mp.length; i++)
			{
				if (mp[i] == prevselected)
				{
					packageList.setSelectedIndex(i);
				}
			}
		}
	}

	public JFrame frmMcpkg;
	public JTextField textField;
	public JTextField searchBox;

	public PackageListModel packageListModel;
	public JComboBox sectionBox;
	public JButton btnRunMinecraft;
	public JToggleButton tglbtnShowQueue;
	public JButton btnQueueOrUnqueue;
	public JLabel lblPackageName;
	public JButton btnMakePackage;
	public JButton btnOptions;
	public JLabel lblStatus;
	public JTextPane txtpnPackageDescription;
	public JList packageList;
	public MakePackage dlgMakePackage;
	public JFileChooser fileChooser;
	
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
		}
		packageListModel.set(mp);
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
		
		btnRunMinecraft = new JButton("Run Minecraft");
		btnRunMinecraft.addActionListener(this);
		frmMcpkg.getContentPane().add(btnRunMinecraft, BorderLayout.NORTH);
		
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
		selectionPane.setDividerLocation(1.0);
		selectionPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frmMcpkg.getContentPane().add(selectionPane, BorderLayout.CENTER);
		
		JScrollPane packageDescScroller = new JScrollPane();
		selectionPane.setRightComponent(packageDescScroller);
		
		JPanel packageDescHeader = new JPanel();
		packageDescScroller.setColumnHeaderView(packageDescHeader);
		packageDescHeader.setLayout(new BorderLayout(0, 0));
		
		lblPackageName = new JLabel("No package is selected.");
		packageDescHeader.add(lblPackageName, BorderLayout.WEST);
		
		btnQueueOrUnqueue = new JButton("Queue");
		packageDescHeader.add(btnQueueOrUnqueue, BorderLayout.EAST);
		
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
		packageListModel = new PackageListModel();
		packageList = new JList();
		packageList.addListSelectionListener(this);
		packageList.setModel(packageListModel);
		//System.out.println(packageList.getSelectedIndex());
		JScrollPane packageScrollPane = new JScrollPane(packageList);
		// Or in two steps:
		
		pkgListPanel.add(packageScrollPane, BorderLayout.CENTER);
		
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
			}
			if(selectedPackage.isQueued)
			{
				Commands.queue(new Commands.unqueuePackage(selectedPackage));
			}
			else
			{
				Commands.queue(new Commands.queuePackage(selectedPackage));
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
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		updatePackageView();
		
		
	}
	public void updatePackageView()
	{
		//packageList.getSelectedIndices();
		if(packageList.getSelectedIndex() < 0 || packageListModel.packageListValues == null ||  packageList.getSelectedIndex()>=packageListModel.packageListValues.length)
		{
			selectedPackage = null;
			lblPackageName.setText("No package is selected.");
			txtpnPackageDescription.setText("");
			btnQueueOrUnqueue.setVisible(false);
			return;
		}
		else
		{
			selectedPackage = packageListModel.packageListValues[packageList.getSelectedIndex()];
			lblPackageName.setText(selectedPackage.Name);
			String subdesc = selectedPackage.FullDescription;
			subdesc = "\t"+selectedPackage.ShortDescription +"\n\n" + subdesc.substring(subdesc.indexOf("\n"));
			
			txtpnPackageDescription.setText(subdesc);
			btnQueueOrUnqueue.setVisible(true);
			//System.out.println(selectedPackage.isQueued);
			btnQueueOrUnqueue.setText(selectedPackage.isQueued ? "Unqueue (remove)" : "Queue (install)");
		}
		if(selectedPackage.isCorrupt)
		{
			calcList();
			updatePackageView();
		}
	}
	
	

}
