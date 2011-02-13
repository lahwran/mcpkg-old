package mcpkg.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import mcpkg.Confirmation;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;

public class GuiConfirmation extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JButton cancelButton;
	public Confirmation conf;
	/*
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Confirmation c = new Confirmation();
			c.isanswered = false;
			c.isconfirmed = false;
			c.question = "is this a good test question?";
			GuiConfirmation dialog = new GuiConfirmation(c);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public GuiConfirmation(Confirmation c) {
		conf = c;
		setTitle("Confirmation");
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				JTextPane txtpnQuestion = new JTextPane();
				txtpnQuestion.setText(c.question);
				txtpnQuestion.setOpaque(false);
				txtpnQuestion.setEditable(false);
				scrollPane.setViewportView(txtpnQuestion);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				okButton.addActionListener(this);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == okButton)
		{
			conf.isconfirmed = true;
			conf.isanswered = true;
			
		}
	}

}
