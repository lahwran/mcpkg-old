package mcpkg.gui;

import java.io.IOException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mcpkg.Commands;
import mcpkg.Confirmation;
import mcpkg.Messaging;

public class GuiMessagingThread extends Thread {
	public final class statusUpdate implements Runnable {
		public final String value;
		public statusUpdate(String v)
		{
			value = v;
		}
		public void run() {
			lblStatus.setText(value);
		}
	}
	public final class confirmationDialog implements Runnable {
		public final Confirmation conf;
		public confirmationDialog(Confirmation c)
		{
			conf = c;
		}
		public void run() {
			conf.isconfirmed = JOptionPane.showConfirmDialog(mainframe, (Object)conf.question, "Confirmation",JOptionPane.YES_NO_OPTION) == 0;
			//System.out.println(conf.isconfirmed);
			conf.isanswered = true;
		}
	}
	public final class refresh implements Runnable {
		public void run() {
			main.packageListModel.set(null);
			main.calcList();
			main.updatePackageView();
		}
	}

	public JLabel lblStatus;
	public JFrame mainframe; //lolatthename
	public Gui main;
	public GuiMessagingThread(JLabel _lblStatus, JFrame _mainframe, Gui _main) {
		// TODO Auto-generated constructor stub
		super("GuiMessaging");
		lblStatus=_lblStatus;
		mainframe = _mainframe;
		main = _main;
		start();
	}
	
	public void run()
	{
		//TODO: doesn't time out the status
		while(true)
		{
			String m = Messaging.message(null);
			Confirmation c = Messaging.qconfirm(null);
			if(Commands.clicanexit)
			{
				Commands.clicanexit = false;
				SwingUtilities.invokeLater(new statusUpdate(m));
			}
			//System.out.println("messaging "+new Date().getTime());
			if(m == null && c == null)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else 
			{
				//System.out.println("messaging info");
				if (m != null)
				{
					SwingUtilities.invokeLater(new statusUpdate(m));
					//System.out.println("messaging info2");
				}
				if (c != null)
				{
					SwingUtilities.invokeLater(new confirmationDialog(c));
					//System.out.println("messaging info3");
				}
			}
		}
	}

	
}
