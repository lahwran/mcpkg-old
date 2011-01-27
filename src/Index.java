import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;


public class Index {

	public static String[] repos;
	
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
		if(whole == null)
			return null;
		
		if(whole.matches("^[^ ]*: .*"))
		{
			String[] spl = whole.split(": ", 2);
			return spl;
		}
		return new String[]{"Block", whole};
	}
	
	public static String[][] readRepoFromStream(BufferedReader in, String[] curKV)
	{
		ArrayList<String[]> fieldcache = new ArrayList<String[]>();
		while(curKV != null)
		{
			fieldcache.add(curKV);
			
			
			
			curKV = splitKV(getNextLine(in));
		}
		return fieldcache.toArray(new String[0][]);
	}
	
	public static void cacherepo(String[][] KVs, File output)
	{
		try {
			FileOutputStream fo = new FileOutputStream(output);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do file.write()??? I mean seriously? 
			
			for(int i=0; i< KVs.length;i++)
			{
				writer.write(KVs[i][0]);
				writer.write(": ");
				writer.write(KVs[i][1]);
				writer.write("\n");
			}
			writer.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void readrepo(String repourl)
	{
		try {
			String cachehash = MD5Checksum.strmd5(repourl);
			
			File cache = new File(getAppDir("mcpkg")+"/repocache/");
			cache.mkdirs();//won't do anything if it's not needed
			File thiscache = new File(cache,cachehash);
			
			
			URL reporeader = new URL(repourl);
			BufferedReader in;
			
			in = new BufferedReader(new InputStreamReader(reporeader.openStream()));
		

			String[] inputLine = splitKV(getNextLine(in));
			if(inputLine != null && inputLine[0].equals("IndexVersion") && thiscache.exists())
			{
				//check cached copy; if it's IndexVersion matches just-read indexversion, no need to update it
				FileInputStream f1 = new FileInputStream(thiscache);
				InputStreamReader f2 = new InputStreamReader(f1);
				BufferedReader f3 = new BufferedReader(f2);
				String[] firstset = splitKV(getNextLine(f3));
				if(firstset != null && firstset[0].equals("IndexVersion") && firstset[1].equals(inputLine[1]))
				{
					readRepoFromStream(f3, firstset);
					return;
				}
			}

			cacherepo(readRepoFromStream(in, inputLine), thiscache);
			
			in.close();
		} catch (IOException e) {
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
