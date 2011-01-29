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
import java.util.HashMap;


public class Index {

	public static String[] mainrepos;
	
	
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
	
	public static boolean isin(String x, String[] y)
	{
		for (int i=0; i<y.length; i++)
		{
			if (x.equals(y[i]))
				return true;
		}
		return false;
	}
	
	public static String[][] readRepoFromStream(BufferedReader in, String[] curKV, boolean cansection, boolean cansubrepo)
	{
		ArrayList<String[]> fieldcache = new ArrayList<String[]>();
		
		HashMap<String, ArrayList<HashMap<String, String>>> loadeddata = new HashMap<String, ArrayList<HashMap<String, String>>>();
		
		HashMap<String, String> currentsection = null;
		
		String lastkey = null; //so we can append to last value with blocks
		
		final String[] SectionNames = new String [] {"Package", "Repository"};
		
		for(int i=0; i<SectionNames.length; i++)
		{
			loadeddata.put(SectionNames[i], new ArrayList<HashMap<String, String>>());
		}
		
		while(curKV != null)
		{
			fieldcache.add(curKV);
			
			if(isin(curKV[0], SectionNames))
			{
				currentsection = new HashMap<String, String>();
				currentsection.put("Name", curKV[1]);
				lastkey = "Name";
				if(!loadeddata.containsKey(curKV[0]))
				{
					loadeddata.put(curKV[0], new ArrayList<HashMap<String, String>>());
				}
				loadeddata.get(curKV[0]).add(currentsection);
			}
			else if(curKV[0].equals("Block") && currentsection != null && lastkey != null)
			{
				currentsection.put(lastkey, currentsection.get(lastkey) + "\n" + curKV[1]);
			}
			else if(currentsection != null)
			{
				currentsection.put(curKV[0], curKV[1]);
				lastkey = curKV[0];
			}
			
			curKV = splitKV(getNextLine(in));
		}
		for(int i=0; i<SectionNames.length; i++)
		{
			System.out.println("section type "+SectionNames[i]);
			ArrayList<HashMap<String, String>> sstype = loadeddata.get(SectionNames[i]);
			for(int j=0; j<sstype.size(); j++)
			{
				currentsection = sstype.get(j);
				System.out.println("\tsection "+currentsection.get("Name"));
				String[] sectionkeys = currentsection.keySet().toArray(new String[0]); 
				for(int k=0;k<sectionkeys.length;k++)
				{
					if(!sectionkeys[k].equalsIgnoreCase("Name"))
					{
						System.out.println("\t\t"+sectionkeys[k]+" "+currentsection.get(sectionkeys[k])+"\n\t\t\tendKV");
					}
				}
				System.out.println("\tsectionend");
			}
			System.out.println("sectiontypeend");
			
		}
		
		return fieldcache.toArray(new String[0][]);
	}
	
	public static void cacherepo(String[][] KVs, File output)
	{
		try {
			FileOutputStream fo = new FileOutputStream(output);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
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
	
	public static void readrepo(String repourl, boolean cansection, boolean cansubrepo )
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
					readRepoFromStream(f3, firstset, cansection, cansubrepo);
					return;
				}
			}

			cacherepo(readRepoFromStream(in, inputLine, cansection, cansubrepo), thiscache);
			
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
