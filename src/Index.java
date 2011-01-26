import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;


public class Index {

	public static String[] repos;
	
	public static String getMcDir()
	{
		String s1 = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("linux") || os.contains("unix"))
        {
            return new StringBuilder().append(s1).append("/.minecraft/").toString();
        }
        else if(os.contains("windows"))
        {
            String s2 = System.getenv("APPDATA");
            if(s2 != null)
            {
            	return new StringBuilder().append(s2).append("/.minecraft/").toString();
            } else
            {
            	return new StringBuilder().append(s1).append("/.minecraft/").toString();
            }
        }
        else if (os.contains("mac"))
        {
            return s1+"Library/Application Support/minecraft/";
        }
        
        else
        {
            return s1+"/minecraft/";
        }
	}
	
	public static String[] getRepolist()
	{
		ArrayList<String> repolist = new ArrayList<String>();
		BufferedReader inputStream;
		try {
			inputStream = new BufferedReader(new FileReader("repos.lst"));
		
			String l = "";
			while((l = inputStream.readLine()) != null)
			{
				if(!l.matches("^#") && !l.matches("^$"))
				{
					repolist.add(l);
				}
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return repolist.toArray(new String[0]);
	}
	
	public static String getNextLine(BufferedReader in) //eats comment and blank lines
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
	
	public static String[] splitKV(String whole)
	{
		//spec states that keys may not contain spaces
		//consider it part of a block
		if(whole.matches("^[a-zA-Z_]+: "))
		{
			String[] spl = whole.split(": ", 2);
			return spl;
		}
		return new String[]{"Block", whole};
	}
	
	public static void readRepoFromStream(BufferedReader in)
	{
		
	}
	
	public static void readrepo(String repourl)
	{
		try {
			String cachehash = MD5Checksum.strmd5(repourl);
			
			File cache = new File("")
			
			URL reporeader = new URL(repourl);
			BufferedReader in;
			
			in = new BufferedReader(new InputStreamReader(reporeader.openStream()));
		

			String inputLine = getNextLine(in);
			if(inputLine.startsWith("IndexVersion"))
			{
				//check cached copy; if it's IndexVersion matches just-read indexversion, no need to update it
				
			}
			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readrepos()
	{
		
	}
	
	public static void main(String[] args)
	{
		
	}
}
