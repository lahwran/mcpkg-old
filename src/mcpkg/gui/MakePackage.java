package mcpkg.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import mcpkg.Messaging;
import mcpkg.Patcher;
import mcpkg.Util;
import mcpkg.targetting.DirArchive;
import mcpkg.targetting.IArchive;
import mcpkg.targetting.IDirOutputStream;
import mcpkg.targetting.ZipArchive;
import mcpkg.targetting.ZipDirOutputStream;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class MakePackage extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private JTextField fieldMainOrig;
	private JTextField fieldMainPatch;
	private JTextField fieldMcjarOrig;
	private JTextField fieldMcjarPatch;
	private JTextField fieldOutput;
	private JButton okButton;
	private JButton cancelButton;
	public Gui maingui;
	private JButton browseOutput;
	private JButton browseMcjarPatch;
	private JButton browseMcjarOrig;
	private JButton browseMainPatch;
	private JButton browseMainOrig;


	/**
	 * Create the dialog.
	 * @param gui 
	 * @param fileChooser 
	 */
	public MakePackage(Gui _gui) {
		maingui=_gui;
		setModal(true);
		setTitle("Make a Package");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				JLabel lblInput = new JLabel("Input");
				panel.add(lblInput);
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				panel_1.setLayout(new GridLayout(4, 1, 0, 0));
				{
					JPanel main_orig = new JPanel();
					panel_1.add(main_orig);
					main_orig.setLayout(new BoxLayout(main_orig, BoxLayout.X_AXIS));
					{
						JLabel lblminecraftOriginal = new JLabel(".minecraft original:  ");
						main_orig.add(lblminecraftOriginal);
					}
					{
						fieldMainOrig = new JTextField();
						fieldMainOrig.setMaximumSize(new Dimension(2147483647, 28));
						fieldMainOrig.setMinimumSize(new Dimension(200, 28));
						fieldMainOrig.setPreferredSize(new Dimension(150, 28));
						main_orig.add(fieldMainOrig);
						fieldMainOrig.setColumns(10);
					}
					{
						browseMainOrig = new JButton("Browse..");
						browseMainOrig.addActionListener(this);
						main_orig.add(browseMainOrig);
					}
				}
				{
					JPanel main_patch = new JPanel();
					panel_1.add(main_patch);
					main_patch.setLayout(new BoxLayout(main_patch, BoxLayout.X_AXIS));
					{
						JLabel lbloptionalminecraftPatch = new JLabel(".minecraft patch (optional):  ");
						main_patch.add(lbloptionalminecraftPatch);
					}
					{
						fieldMainPatch = new JTextField();
						fieldMainPatch.setMaximumSize(new Dimension(2147483647, 28));
						main_patch.add(fieldMainPatch);
						fieldMainPatch.setColumns(10);
					}
					{
						browseMainPatch = new JButton("Browse..");
						browseMainPatch.addActionListener(this);
						main_patch.add(browseMainPatch);
					}
				}
				{
					JPanel mcjar_orig = new JPanel();
					panel_1.add(mcjar_orig);
					mcjar_orig.setLayout(new BoxLayout(mcjar_orig, BoxLayout.X_AXIS));
					{
						JLabel lblMinecraftjarOriginal = new JLabel("minecraft.jar original:  ");
						mcjar_orig.add(lblMinecraftjarOriginal);
					}
					{
						fieldMcjarOrig = new JTextField();
						fieldMcjarOrig.setMaximumSize(new Dimension(2147483647, 28));
						mcjar_orig.add(fieldMcjarOrig);
						fieldMcjarOrig.setColumns(10);
					}
					{
						browseMcjarOrig = new JButton("Browse..");
						browseMcjarOrig.addActionListener(this);
						mcjar_orig.add(browseMcjarOrig);
					}
				}
				{
					JPanel mcjar_patch = new JPanel();
					panel_1.add(mcjar_patch);
					mcjar_patch.setLayout(new BoxLayout(mcjar_patch, BoxLayout.X_AXIS));
					{
						JLabel lbloptionalMinecraftjarPatch = new JLabel("minecraft.jar patch (optional):  ");
						mcjar_patch.add(lbloptionalMinecraftjarPatch);
					}
					{
						fieldMcjarPatch = new JTextField();
						fieldMcjarPatch.setMaximumSize(new Dimension(200000000, 28));
						mcjar_patch.add(fieldMcjarPatch);
						fieldMcjarPatch.setColumns(10);
					}
					{
						browseMcjarPatch = new JButton("Browse..");
						browseMcjarPatch.addActionListener(this);
						mcjar_patch.add(browseMcjarPatch);
					}
				}
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				JLabel lblOutput = new JLabel("Output");
				panel.add(lblOutput);
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
				{
					JLabel lblOutputPackage = new JLabel("Output Package:");
					panel_1.add(lblOutputPackage);
				}
				{
					fieldOutput = new JTextField();
					fieldOutput.setMaximumSize(new Dimension(2147483647, 28));
					panel_1.add(fieldOutput);
					fieldOutput.setColumns(10);
				}
				{
					browseOutput = new JButton("Browse..");
					browseOutput.addActionListener(this);
					panel_1.add(browseOutput);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new GridLayout(0, 2, 0, 0));
			{
				okButton = new JButton("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
				getRootPane().setDefaultButton(cancelButton);
			}
		}
		fieldMainOrig.setText(Util.getAppDir("minecraft"));
		fieldMcjarOrig.setText(Util.getAppDir("minecraft")+"bin/minecraft.jar");
	}

	@Override
	public void setVisible(boolean flag)
	{
		super.setVisible(flag);
			if(fieldMainOrig.getText().equals(""))
			{
				fieldMainOrig.setText(Util.getAppDir("minecraft"));
			}
			if(fieldMcjarOrig.getText().equals(""))
			{
				fieldMcjarOrig.setText(Util.getAppDir("minecraft")+"bin/minecraft.jar");
			}
	}

	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		HashMap<JButton,JTextField> s=new HashMap<JButton,JTextField>();
		s.put(browseMainOrig, fieldMainOrig);
		s.put(browseMainPatch, fieldMainPatch);
		s.put(browseMcjarOrig, fieldMcjarOrig);
		s.put(browseMcjarPatch, fieldMcjarPatch);
		s.put(browseOutput, fieldOutput);
		if(arg0.getSource() == cancelButton)
		{
			setVisible(false);
		}
		else if(arg0.getSource() == okButton)
		{
			runIt();
		}
		else if(s.containsKey(arg0.getSource()))
		{
			int rVal;
			if(arg0.getSource() == browseOutput)
			{
				rVal = maingui.fileChooser.showSaveDialog(maingui.frmMcpkg);
			}
			else
			{
				rVal = maingui.fileChooser.showOpenDialog(maingui.frmMcpkg);
			}
			if (rVal == JFileChooser.APPROVE_OPTION) 
			{
				s.get(arg0.getSource()).setText(maingui.fileChooser.getSelectedFile().getAbsolutePath());
				//dir.setText(c.getCurrentDirectory().toString());
			}
		}
	}
	
	public void runIt()
	{
		try{
			File minecraftdir = new File(fieldMainOrig.getText());
			
			File mainPatch = null;
			if (!fieldMainPatch.getText().equals(""))
				mainPatch = new File(fieldMainPatch.getText());
			
			File mcjarPatch = null;
			if (!fieldMcjarPatch.getText().equals(""))
				mcjarPatch = new File(fieldMcjarPatch.getText());
			
			IArchive patchreader = null;
			
			
			File altered = new File(fieldOutput.getText());
			IDirOutputStream outwriter;
			outwriter = new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(altered)));
			
			IArchive inreader = new DirArchive(minecraftdir);
			
			
			if(mainPatch != null)
			{
				if(mainPatch.isFile())
					patchreader = new ZipArchive(mainPatch);
				else
					patchreader = new DirArchive(mainPatch);
				Patcher.makepatch("main",inreader, patchreader, outwriter, false);
				patchreader.close();
			}
			
			
			if(mcjarPatch != null)
			{
				if(mcjarPatch.isFile())
					patchreader = new ZipArchive(mcjarPatch);
				else
					patchreader = new DirArchive(mcjarPatch);
				Patcher.makepatch("mcjar",new ZipArchive(new File(fieldMcjarOrig.getText())), patchreader, outwriter, false);
				patchreader.close();
			}
			outwriter.close();
			inreader.close();
			Messaging.message("Packaged file at "+fieldOutput.getText());
			setVisible(false);
		}catch(Throwable e)
		{
			e.printStackTrace();
			Messaging.message(e.getClass().getSimpleName()+": "+e.getMessage());
			return;
		}
	}

}
