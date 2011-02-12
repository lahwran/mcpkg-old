package mcpkg.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridLayout;

public class MakePackage extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MakePackage dialog = new MakePackage();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MakePackage() {
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
						textField = new JTextField();
						textField.setMaximumSize(new Dimension(2147483647, 28));
						textField.setMinimumSize(new Dimension(200, 28));
						textField.setPreferredSize(new Dimension(150, 28));
						main_orig.add(textField);
						textField.setColumns(10);
					}
					{
						JButton btnBrowse = new JButton("Browse..");
						main_orig.add(btnBrowse);
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
						textField_1 = new JTextField();
						textField_1.setMaximumSize(new Dimension(2147483647, 28));
						main_patch.add(textField_1);
						textField_1.setColumns(10);
					}
					{
						JButton btnBrowse_1 = new JButton("Browse..");
						main_patch.add(btnBrowse_1);
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
						textField_2 = new JTextField();
						textField_2.setMaximumSize(new Dimension(2147483647, 28));
						mcjar_orig.add(textField_2);
						textField_2.setColumns(10);
					}
					{
						JButton btnBrowse_2 = new JButton("Browse..");
						mcjar_orig.add(btnBrowse_2);
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
						textField_3 = new JTextField();
						textField_3.setMaximumSize(new Dimension(200000000, 28));
						mcjar_patch.add(textField_3);
						textField_3.setColumns(10);
					}
					{
						JButton btnBrowse_3 = new JButton("Browse..");
						mcjar_patch.add(btnBrowse_3);
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
					textField_4 = new JTextField();
					textField_4.setMaximumSize(new Dimension(2147483647, 28));
					panel_1.add(textField_4);
					textField_4.setColumns(10);
				}
				{
					JButton btnBrowse_4 = new JButton("Browse..");
					panel_1.add(btnBrowse_4);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new GridLayout(0, 2, 0, 0));
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
