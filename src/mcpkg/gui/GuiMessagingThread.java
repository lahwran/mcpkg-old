package mcpkg.gui;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

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

	public JLabel lblStatus;
	public GuiMessagingThread(JLabel _lblStatus) {
		// TODO Auto-generated constructor stub
		super("GuiMessaging");
		lblStatus=_lblStatus;
		start();
	}
	
	public void run()
	{
		//TODO: doesn't time out the status
		String m = Messaging.message(null);
		Confirmation c = Messaging.qconfirm(null);
		if(m == null && c == null)
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else 
		{
			if (m != null)
			{
				SwingUtilities.invokeLater(new statusUpdate(m));
			}
			if (c != null)
			{
				//TODO
			}
		}
	}

	
}
