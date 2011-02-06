import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

//proxy class - interfaces to the package manager (gui, commandline, etc) may only use this class


public class Commands {

	public static String[] getRepos()
	{
		Index.readrepolist();
		return Index.mainrepos;
	}
	
	public static void disableRepo(String id)
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		appdir.mkdirs();//won't do anything if it's not needed
		File repolist = new File(appdir,"repos.lst");
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		try {
			f1 = new FileInputStream(repolist);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader f3 = new BufferedReader(f2);
		String lookedup = null;
		try {
			lookedup = Index.mainrepos[new Integer(id)];
		} catch (NumberFormatException e){}
		try {
			String line = null;
			while ((line = f3.readLine()) != null)
			{
				if(line.equals(id) || (lookedup != null && line.equals(lookedup)))
					lines.add("#"+line);
				else
					lines.add(line);
			}
			
			f3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			FileOutputStream fo = new FileOutputStream(repolist);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<lines.size();i++)
			{
				writer.write(lines.get(i));
				if(i<lines.size()-1)
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
		Index.loadrepos();
	}
	
	public static void addRepo(String id)
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		appdir.mkdirs();//won't do anything if it's not needed
		File repolist = new File(appdir,"repos.lst");
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		try {
			f1 = new FileInputStream(repolist);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader f3 = new BufferedReader(f2);
		
		boolean handled = false;
		//if it gets uncommented, we don't want to add it again at the end
		
		try {
			String line = null;
			while ((line = f3.readLine()) != null)
			{
				if(line.equals("#"+id))
				{
					lines.add(line.substring(1, line.length()));
					handled = true;
				}
				else
					lines.add(line);
			}
			
			f3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!handled)
		{
			lines.add(id);
		}
		
		try {
			FileOutputStream fo = new FileOutputStream(repolist);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<lines.size();i++)
			{
				writer.write(lines.get(i));
				if(i<lines.size()-1)
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
		Index.loadrepos();
	}
	
	public static void cleanRepos()
	{
		File cache = new File(Util.getAppDir("mcpkg")+"/repocache/");
		cache.mkdirs();
		
		File[] files = cache.listFiles();
		for(int i=0; i<files.length; i++)
		{
			files[i].delete();
		} //notice how we DON'T reload the repos
	}
	
	public static Package[] queryPackages(String query)
	{
		Index.loadrepos();
		HashMap<Package,Integer> matches = new HashMap<Package,Integer>();
		
		Package[] allpackages = Package.Packages.values().toArray(new Package[0]);
		for(int i=0; i<allpackages.length; i++)
		{
			int matchcount=0;
			
			//split returns the number of fragments, which is the number of matches + 1
			int splits=0;
			
			
			splits = allpackages[i].Name.split(query).length;
			if(splits == 0)
				splits = 2;
			matchcount += (splits-1)*30; //such a hack ...
			

			splits = allpackages[i].ShortDescription.split(query).length;
			if(splits == 0)
				splits = 2;
			matchcount += (splits-1)*15;
			

			splits = allpackages[i].FullDescription.split(query).length;
			if(splits == 0)
				splits = 2;
			matchcount += (splits-1)*10;
			
			System.out.println(allpackages[i].Name + " " + matchcount);
			
			//matchcount /= 10; //why bother?
			if (matchcount > 0)
				matches.put(allpackages[i], matchcount);
		}
		ArrayList<Package> keys = new ArrayList<Package>(matches.keySet());
		SearchComparer s = new SearchComparer();
		s.matches = matches;
		Collections.sort(keys, s);
		
		return keys.toArray(new Package[0]);
	}
	
	public static String[][] getSections()
	{ //TODO: should have some kind of sorting ..
		Index.loadrepos();
		String[][] Sections = new String[Index.Sections.size()][];
		String[] keys = Index.Sections.keySet().toArray(new String[0]);
		for(int i=0; i<keys.length; i++)
		{
			Sections[i] = new String[] 
			     {keys[i], 
					Index.Sections.get(keys[i])};
		}
		return Sections;
	}
	
	public static Package[] getPackages()
	{
		Index.loadrepos();
		return Package.Packages.values().toArray(new Package[0]);
	}
	
	public static Package getPackage(String name)
	{
		Index.loadrepos(); //TODO: due to how often loadrepos is called (every time anything is needed, pretty much) it must nicely detect when everything is already loaded
		return Package.Packages.get(name);
	}
	
	public static void queuePackage(String id)
	{
		Index.loadrepos(); 
		Queue.readqueue();
		Queue.queuePackage(new PackageCompare(id).get());
	}
	
	
	
	public static void run()
	{
		Index.loadrepos(); 
		Queue.readqueue();
		Installer.run();
	}
}
