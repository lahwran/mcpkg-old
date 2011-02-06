import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


public class Util {

	public static String getAppDir(String d)
	{
		String s1 = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
	    if(os.contains("linux") || os.contains("unix"))
	    {
	        return new StringBuilder().append(s1).append("/."+d+"/").toString();
	    }
	    else if(os.contains("windows"))
	    {
	        String s2 = System.getenv("APPDATA");
	        if(s2 != null)
	        {
	        	return new StringBuilder().append(s2).append("/."+d+"/").toString();
	        } else
	        {
	        	return new StringBuilder().append(s1).append("/."+d+"/").toString();
	        }
	    }
	    else if (os.contains("mac"))
	    {
	        return s1+"Library/Application Support/"+d+"/";
	    }
	    
	    else
	    {
	        return s1+"/"+d+"/";
	    }
	}

	public static String getNextLine(BufferedReader in) //eats comment and blank lines -- "omnomnomnom" says guipsp
	{
		String r=null;
		try {
			while((r = in.readLine()) != null)
			{
				if(!r.startsWith("#") && !r.equals(""))
				{
					return r;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public static String getMinecraftVersion()
	{//will be a bit of work to get, should cache results
		return "1.2_02"; //for testing purposes until I actually write this
	}
	
	public static InputStream readURL(String u)
	{
		InputStream inputstream = null;
		try {
			if (u.startsWith("http://") || u.startsWith("file:")) {
				URL url = new URL(u);
				inputstream = url.openStream();
			} 
			else 
			{
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				URL url = new URL(u);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setSSLSocketFactory(sslsocketfactory);
				inputstream = conn.getInputStream();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputstream;
	}
}
