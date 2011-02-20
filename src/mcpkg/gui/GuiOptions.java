package mcpkg.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public class GuiOptions extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiOptions frame = new GuiOptions();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GuiOptions() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelRepositories = new JPanel();
		tabbedPane.addTab("Repositories", null, panelRepositories, null);
		panelRepositories.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panelRepositories.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnSave = new JButton("Save");
		panel.add(btnSave);
		
		JButton button_1 = new JButton("New button");
		panel.add(button_1);
		
		JButton button_2 = new JButton("New button");
		button_2.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(button_2);
		
		JPanel bottompanel = new JPanel();
		contentPane.add(bottompanel, BorderLayout.SOUTH);
		
		JButton btnDone = new JButton("Done");
		bottompanel.add(btnDone);
	}

}
