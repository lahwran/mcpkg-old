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

import mcpkg.errors.dependency.UnsolvableConflict;

//TODO: queue should know which packages were queued as a dependency and which were manually queued

public class Queue {
	
	public static ArrayList<Package> thequeue;
	
	public static void readqueue()
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
		
		
		Package[] packages=Package.Packages.values().toArray(new Package[0]);
		for(int i=0; i<packages.length; i++)
		{
			packages[i].getCachename(); //generate cache names that haven't been
		}
		
		try {
			String line = null;
			while ((line = Util.getNextLine(f3)) != null)
			{
				Package p = Package.CacheNames.get(line);
				if (p == null)
				{//this package has not been loaded
					p=Package.readFile(new File(cachedir,line));
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
		p=Package.revive(p);
		writequeue();
		readqueue(); //will clean the queue if there are corrupt packages
		
		if(!isclean())
			throw new IllegalArgumentException("unclean!");
		
		ArrayList<Package> toremove = new ArrayList<Package>();
		ArrayList<Package> toinstall = new ArrayList<Package>();
		
		try {
			Dependency.resolve(p, thequeue, toinstall, toremove);
		} catch (UnsolvableConflict e) {
			Messaging.message("ERROR: "+e.getMessage());
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
	
	public static void deduplicateQueue()
	{
		ArrayList<Package> tempqueue = thequeue;
		thequeue = new ArrayList<Package>();
		for(int x=0; x<thequeue.size(); x++)
		{
			boolean canadd = true;
			for(int y=0; y<thequeue.size(); y++)
			{
				if(y != x && thequeue.get(x).getCachename().equals(thequeue.get(y).getCachename()))
				{
					canadd = false;
				}
			}
			if(canadd)
				thequeue.add(thequeue.get(x));
		}
	}
	
}
