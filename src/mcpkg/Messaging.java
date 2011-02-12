package mcpkg;
import java.util.ArrayDeque;


public class Messaging {

	public static float progress = 0.0f; //progress percentage, used for progress bars
	public static String status = ""; //general status, such as "downloading modloader-1.2_02v4.mcpkg"
	public static String status2 = ""; //detail status, such as "353.2KB/s"
	public static ArrayDeque<String> messages = new ArrayDeque<String>(); //messages, such as "connecting ..."
	public static ArrayDeque<Confirmation> confirmations = new ArrayDeque<Confirmation>();
	
	public synchronized static String message(String inmessage)
	{
		if(inmessage == null)
		{
			if(messages.size() > 0)
				return messages.removeFirst();
		}
		else
		{
			messages.add(inmessage);
		}
		return null;
	}
	public synchronized static Confirmation qconfirm(String message)
	{
		if(message == null)
		{
			if(confirmations.size() > 0)
				return confirmations.removeFirst();
		}
		else
		{
			Confirmation c = new Confirmation();
			c.question=message;
			c.isanswered = false;
			c.isconfirmed = false;
			confirmations.add(c);
			return c;
		}
		return null;
	}
	
	public static boolean confirm(String message)
	{
		Confirmation c = qconfirm(message);
		while(!c.isanswered)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Messaging.message("interrupt in confirmation");
				return false;
			}
		}
		return c.isconfirmed;
	}
	
}
