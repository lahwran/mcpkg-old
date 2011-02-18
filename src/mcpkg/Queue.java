package mcpkg;
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
import java.util.Arrays;
import java.util.HashMap;

import mcpkg.Index.subrepo;
import mcpkg.errors.dependency.UnsolvableConflict;

//TODO: queue should know which packages were queued as a dependency and which were manually queued

public class Queue {
	
	public static ArrayList<Package> thequeue;
	
	public static void readqueue()
	{
		String[] curKV = null;
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();//won't do anything if it's not needed
		File queuefile = new File(appdir,"queue.lst");
		//thequeue = new ArrayList<Package>(); //clear it before loading - but only once we know the queue file exists
		if(thequeue != null && thequeue.size() > 0 && isclean())
			return; //guess we don't want to reinit it...
		thequeue = new ArrayList<Package>();
		
		if(!queuefile.exists())
			return;
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		try {
			f1 = new FileInputStream(queuefile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader in = new BufferedReader(f2);
		
		
		
		HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> loadeddata = new HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>();
		
		HashMap<String, ArrayList<String>> currentsection = null;
		
		String lastkey = null; //so we can append to last value with blocks
		
		final String[] SectionNames = new String [] {"Package"};
		
		for(int i=0; i<SectionNames.length; i++)
		{
			loadeddata.put(SectionNames[i], new ArrayList<HashMap<String, ArrayList<String>>>());
		}

		curKV = Util.splitKV(Util.getNextLine(in));
		while(curKV != null)
		{
			
			if(Util.isin(curKV[0], SectionNames))
			{
				currentsection = new HashMap<String, ArrayList<String>>();
				ArrayList<String> curV = new ArrayList<String>();
				curV.add(curKV[1]);
				currentsection.put("Name", curV);
				lastkey = "Name";
				if(!loadeddata.containsKey(curKV[0]))
				{
					loadeddata.put(curKV[0], new ArrayList<HashMap<String, ArrayList<String>>>());
				}
				loadeddata.get(curKV[0]).add(currentsection);
			}
			else if(curKV[0].equals("Block") && currentsection != null && lastkey != null)
			{
				ArrayList<String> lastVs = currentsection.get(lastkey);
				String lastV = lastVs.get(lastVs.size()-1) + "\n" + curKV[1];
				lastVs.remove(lastVs.size()-1);
				lastVs.add(lastV);
			}
			else if(currentsection != null && !currentsection.containsKey(curKV[0]))
			{
				ArrayList<String> curV = new ArrayList<String>();
				curV.add(curKV[1]);
				currentsection.put(curKV[0], curV);
				lastkey = curKV[0];
			}
			else if(currentsection != null)
			{
				ArrayList<String> lastVs = currentsection.get(lastkey);
				lastVs.add(curKV[1]);
			}
			
			curKV = Util.splitKV(Util.getNextLine(in));
		}
		/*
		for(int i=0; i<SectionNames.length; i++)
		{
			System.out.println("section type "+SectionNames[i]);
			ArrayList<HashMap<String, ArrayList<String>>> sstype = loadeddata.get(SectionNames[i]);
			for(int j=0; j<sstype.size(); j++)
			{
				currentsection = sstype.get(j);
				System.out.println("\tsection "+currentsection.get("Name").get(0));
				String[] sectionkeys = currentsection.keySet().toArray(new String[0]); 
				for(int k=0;k<sectionkeys.length;k++)
				{
					System.out.println("\t\tkey: "+sectionkeys[k]);
					if(!sectionkeys[k].equalsIgnoreCase("Name"))
					{
						ArrayList<String> curV = currentsection.get(sectionkeys[k]);
						for(int l=0; l<curV.size(); l++)
							System.out.println("\t\t\t"+curV.get(l)+"\n\t\t\t\tendV");
					}
				}
				System.out.println("\tsectionend");
			}
			System.out.println("sectiontypeend");
			
		}
		//*/
		///*
		//icky name is icky
		ArrayList<HashMap<String, ArrayList<String>>> Packages = loadeddata.get("Package");
		for(int i=0; i<Packages.size(); i++)
		{
			//java.lang.IndexOutOfBoundsException
			HashMap<String, ArrayList<String>> pdata = Packages.get(i);
			
			String Cachename = pdata.get("Cachename").get(0);
			Package p = null;
			if(Package.CacheNames.get(Cachename) != null)
			{
				p = Package.CacheNames.get(Cachename);
			}
			else
			{
				String[] Versions = pdata.get("Version").get(0).split(" ", 3);
				p = new Package(pdata.get("Name").get(0),Versions[0],Versions[1]);
				
				Package.CacheNames.put(p.getCachename(), p);
				p.PackageURL = Versions[2];
				
				ArrayList<String> dataauthors = pdata.get("Author");
				String[] temp = dataauthors.get(0).split("[<>]");
				ArrayList<String[]> authors = new ArrayList<String[]>();
				authors.add(new String[] {temp[0], temp[1]});
				for(int j=1; j<dataauthors.size(); j++)
				{
					temp = dataauthors.get(j).split("[<>]");
					authors.add(new String[] {temp[0], temp[1]});
				}
				p.Authors=authors.toArray(new String[0][]);
				
				if(pdata.containsKey("Homepage"))
					p.Homepage = pdata.get("Homepage").get(0);
				
				String section = null;
				try{
					section = pdata.get("PackageSection").get(0);
				}catch (NullPointerException e) {
					//let's add some context, yes?
					throw new IllegalArgumentException("package "+p.Name+" missing PackageSection field");
				}
				if (!Index.Sections.containsKey(section))
					Index.Sections.put(section, null);
				p.Section = section;
				
				
				
				//if(pdata.containsKey("SingleChangelog"))
				//	p.Homepage = pdata.get("SingleChangelog").get(0);
				
				/*if(pdata.containsKey("ItemIDs"))
				{
					temp = pdata.get("ItemIDs").get(0).split(",");
					p.ItemIDs = new int[temp.length];
					for(int j=0; j<temp.length; j++)
					{
						p.ItemIDs[j]= new Integer(temp[j]);
					}
				}
				if(pdata.containsKey("BlockIDs"))
				{
					temp = pdata.get("BlockIDs").get(0).split(",");
					p.BlockIDs = new int[temp.length];
					for(int j=0; j<temp.length; j++)
					{
						p.BlockIDs[j]= new Integer(temp[j]);
					}
				}*/
				
				p.FullDescription = pdata.get("Description").get(0);
				p.ShortDescription = p.FullDescription.indexOf("\n") != -1 ? p.FullDescription.substring(0, p.FullDescription.indexOf("\n")) : "";
			}
			thequeue.add(p);
			p.isQueued = true;
		}
		//*/
	}
	
	public static void writequeue()
	{
		if(thequeue == null)
			return;
		try {
			File appdir = new File(Util.getAppDir("mcpkg")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File queuefile = new File(appdir,"queue.lst");
			
			FileOutputStream fo = new FileOutputStream(queuefile);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<thequeue.size();i++)
			{
				Package p = thequeue.get(i);
				writer.write("Package: "+p.Name+"\n");
				writer.write("Cachename: "+p.getCachename()+"\n");
				for(int authornum = 0; authornum < p.Authors.length; authornum++)
				{
					writer.write("Author: "+p.Authors[authornum][0]+"<"+p.Authors[authornum][1]+">\n");
				}
				if(p.Homepage != null)
					writer.write("Homepage: "+p.Homepage+"\n");
				writer.write("PackageSection: "+p.Section+"\n");
				writer.write("Version: "+p.MCVersion+" "+p.Version+" "+p.PackageURL+"\n");
				writer.write("Description: "+p.FullDescription+"\n");
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
	
	public static void read_queue()
	{
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();//won't do anything if it's not needed
		File queuefile = new File(appdir,"queue.lst");
		//thequeue = new ArrayList<Package>(); //clear it before loading - but only once we know the queue file exists
		if(thequeue != null && thequeue.size() > 0 && isclean())
			return; //guess we don't want to reinit it...
		thequeue = new ArrayList<Package>();
		
		if(!queuefile.exists())
			return;
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		try {
			f1 = new FileInputStream(queuefile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader f3 = new BufferedReader(f2);
		
		
		/*Package[] packages=Package.CacheNames.values().toArray(new Package[0]);
		for(int i=0; i<packages.length; i++)
		{
			packages[i].getCachename(); //generate cache names that haven't been
		}*/ 
		
		try {
			String line = null;
			while ((line = Util.getNextLine(f3)) != null)
			{
				Package p = Package.CacheNames.get(line);
				if (p == null)
				{//this package has not been loaded
					//p=Package.readFile(new File(cachedir,line));
					continue; //skip it
				}
				p.isQueued = true;
				thequeue.add(p);
			}
			
			f3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//deduplicateQueue();
	}
	
	public static boolean isclean()
	{
		for(int i=0; i<thequeue.size(); i++)
		{
			if(thequeue.get(i).isCorrupt)
				return false;
		}
		return true;
	}
	
	public static void write_queue()
	{
		if(thequeue == null)
			return;
		try {
			File appdir = new File(Util.getAppDir("mcpkg")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File queuefile = new File(appdir,"queue.lst");
			
			FileOutputStream fo = new FileOutputStream(queuefile);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<thequeue.size();i++)
			{
				Package p = thequeue.get(i);
				writer.write(p.getCachename());
				if(i<thequeue.size()-1)
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
	
	
	public static void queuePackage(Package p) throws IOException
	{
		//p=Package.revive(p);
		//writequeue();
		//readqueue(); //will clean the queue if there are corrupt packages
		
		if(!isclean())
			throw new IllegalArgumentException("unclean!");
		
		ArrayList<Package> toremove = new ArrayList<Package>();
		ArrayList<Package> toinstall = new ArrayList<Package>();
		//toinstall.add()
		
		try {
			Dependency.resolve(p, thequeue, toinstall, toremove);
		} catch (Throwable e) {
			Messaging.message("ERROR: "+e.getMessage());
			e.printStackTrace();
			return;
		}
		StringBuilder s = new StringBuilder();
		for(int i=0; i<toinstall.size(); i++)
		{
			Package x = toinstall.get(i);
			if(x.isCorrupt)
				throw new IllegalArgumentException("corrupt package "+x.Name+"!");
			boolean shouldcont = false;
			System.out.println("consider "+x.Name);
			for(int j=0; j<thequeue.size(); j++)
			{
				if(thequeue.get(j).equals(x))
				{
					toinstall.remove(x);
					System.out.println("not adding "+x.Name);
					shouldcont = true;
				}
			}
			if(i>0)
				s.append(" ");
			else
				s.append("Packages to be queued:\n");
			if (shouldcont)
				continue;
			else
				System.out.println("adding "+x.Name);
			s.append(x.Name);
			if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}
		}
		for(int i=0; i<toremove.size(); i++)
		{
			Package x = toremove.get(i);

			if(x.isCorrupt)
				throw new IllegalArgumentException("corrupt package!");
			boolean shouldcont = false;
			for(int j=0; j<thequeue.size(); j++)
			{
				if(thequeue.get(j).equals(x))
				{
					toremove.remove(x);
					shouldcont = true;
				}
			}
			if (shouldcont)
				continue;
			if(i==0)
				s.append("\nPackages to be removed:");
			if(i>0)
				s.append(" ");
			s.append(x.Name);
			if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}
		}
		if(Messaging.confirm(s.toString()))
		{
			for(int i=0; i<toinstall.size(); i++)
			{
				Package x = toinstall.get(i);
				boolean shouldcont = false;
				for(int j=0; j<thequeue.size(); j++)
				{
					if(thequeue.get(j).equals(x))
					{
						toinstall.remove(x);
						shouldcont = true;
					}
				}
				if (shouldcont)
					continue;
				thequeue.add(x);
				x.isQueued = true;
				x.cache();
			}
			for(int i=0; i<toremove.size(); i++)
			{
				Package x = toremove.get(i);
				
				if(thequeue.contains(x))
					thequeue.remove(x);
				x.isQueued = false;
			}

			//deduplicateQueue();
			writequeue();
		}
		else
		{
			Messaging.message("canceled.");
		}
		
	}
	public static void unqueuePackage(Package p)
	{
		writequeue();
		readqueue(); //will clean the queue if there are corrupt packages
		
		ArrayList<Package> toremove = new ArrayList<Package>();
		Dependency.remove(p, thequeue, toremove);

		StringBuilder s = new StringBuilder("Packages to be removed:\n");
		for(int i=0; i<toremove.size(); i++)
		{
			Package x = toremove.get(i);
			if(i>0)
				s.append(" ");
			s.append(x.Name);
			if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}
		}
		if(Messaging.confirm(s.toString()))
		{
			for(int i=0; i<toremove.size(); i++)
			{
				Package x = toremove.get(i);
				if(thequeue.contains(x))
					thequeue.remove(x);
				x.isQueued = false;
			}
			//deduplicateQueue();
			writequeue();
		}
		else
		{
			Messaging.message("canceled.");
		}
	}
	
	public static void updatePackage(Package p) throws IOException {
		ArrayList<Package> toremove = new ArrayList<Package>();
		ArrayList<Package> toinstall = new ArrayList<Package>();
		try {
			Dependency.replace(p, p.getLatest(), thequeue, toinstall, toremove);
		} catch (Throwable e) {
			Messaging.message("ERROR: "+e.getMessage());
			e.printStackTrace();
			return;
		}
		ArrayList<String> toupgrade = new ArrayList<String>();
		for(int i=0; i<toremove.size(); i++)
		{
			if(toinstall.contains(toremove.get(i).getLatest()))
			{
				toupgrade.add(toremove.get(i).Name);

				if(toremove.get(i).isCorrupt)
					throw new IllegalArgumentException("corrupt package!");
			}
		}
		StringBuilder s = new StringBuilder();
		for(int i=0; i<toinstall.size(); i++)
		{
			Package x = toinstall.get(i);
			if(x.isCorrupt)
				throw new IllegalArgumentException("corrupt package "+x.Name+"!");
			if(toupgrade.contains(x.Name))
				continue; //TODO: unfinished conversion
			boolean shouldcont = false;
			System.out.println("consider "+x.Name);
			for(int j=0; j<thequeue.size(); j++)
			{
				if(thequeue.get(j).equals(x))
				{
					toinstall.remove(x);
					System.out.println("not adding "+x.Name);
					shouldcont = true;
				}
			}
			if(i>0)
				s.append(" ");
			else
				s.append("Packages to be queued:\n");
			if (shouldcont)
				continue;
			else
				System.out.println("adding "+x.Name);
			s.append(x.Name);
			/*if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}*/
		}
		for(int i=0; i<toremove.size(); i++)
		{
			Package x = toremove.get(i);

			if(toupgrade.contains(x.Name))
				continue;
			if(x.isCorrupt)
				throw new IllegalArgumentException("corrupt package!");
			boolean shouldcont = false;
			for(int j=0; j<thequeue.size(); j++)
			{
				if(thequeue.get(j).equals(x))
				{
					toremove.remove(x);
					shouldcont = true;
				}
			}
			if (shouldcont)
				continue;
			if(i==0)
				s.append("\nPackages to be removed:");
			if(i>0)
				s.append(" ");
			s.append(x.Name);
			/*if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}*/
		}

		for(int i=0; i<toupgrade.size(); i++)
		{
			String x = toupgrade.get(i);

			if(i==0)
				s.append("\nPackages to be upgraded:");
			if(i>0)
				s.append(" ");
			s.append(x);
			/*if(!x.Version.equals(".") && false)
			{
				s.append("==");
				s.append(x.Version);
			}*/
		}
		if(Messaging.confirm(s.toString()))
		{
			for(int i=0; i<toinstall.size(); i++)
			{
				Package x = toinstall.get(i);
				boolean shouldcont = false;
				for(int j=0; j<thequeue.size(); j++)
				{
					if(thequeue.get(j).equals(x))
					{
						toinstall.remove(x);
						shouldcont = true;
					}
				}
				if (shouldcont)
					continue;
				thequeue.add(x);
				x.isQueued = true;
				x.cache();
			}
			for(int i=0; i<toremove.size(); i++)
			{
				Package x = toremove.get(i);
				
				if(thequeue.contains(x))
					thequeue.remove(x);
				x.isQueued = false;
			}

			//deduplicateQueue();
			writequeue();
		}
		else
		{
			Messaging.message("canceled.");
		}
	}
}
