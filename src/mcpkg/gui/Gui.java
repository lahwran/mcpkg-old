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
				Boolean.class, Object.class, Object.class, Object.class, Object.class, Object.class
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

		@Override
		public Object getValueAt(int packagenum, int fieldnum) {
			// TODO Auto-generated method stub
			Package p = packageListValues[packagenum];
			if(p.isCorrupt) //should never happen with the new read prevention
				calcList();
			switch (fieldnum)
			{
			case 0:
				return p.getQueuedVersion() != null;
			case 1:
				return p.Name;
			case 2:
				Package queuedVersion =  p.getQueuedVersion();
				if (queuedVersion == null)
					return "";
				else
					return queuedVersion.MCVersion+"/"+queuedVersion.Version;
			case 3:
				Package p1 = p.getLatest();
				return p1.MCVersion+"/"+p1.Version;
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
