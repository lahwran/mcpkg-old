package mcpkg;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipException;

import mcpkg.errors.installer.ModConflict;
import mcpkg.errors.patcher.FormatError;


//proxy class - interfaces to the package manager (gui, commandline, etc) may only use this class


public class Commands extends Thread {

	
	public static String[] getRepos() throws FileNotFoundException, IOException
	{
		Index.readrepolist();
		return Index.mainrepos;
	}
	
	
	
	public static Package[] queryPackages(String query) throws FileNotFoundException, IOException
	{
		Index.loadrepos(false);
		HashMap<Package,Integer> matches = new HashMap<Package,Integer>();
		
		Package[] allpackages = Package.CacheNames.values().toArray(new Package[0]);
		for(int i=0; i<allpackages.length; i++)
		{
			if(!allpackages[i].checkLatest())
				continue;
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
			
			//System.out.println(allpackages[i].Name + " " + matchcount);
			
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
	
	public static String[][] getSections() throws FileNotFoundException, IOException
	{ //TODO: should have some kind of sorting ..
		Index.loadrepos(false);
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
	
	public static Package[] getPackages() throws FileNotFoundException, IOException
	{
		Index.loadrepos(false);
		ArrayList<Package> allpackages = new ArrayList<Package>(Package.CacheNames.values());
		ArrayList<Package> latestpackages = new ArrayList<Package>(allpackages.size());
		for(int i=0; i<allpackages.size(); i++)
		{
			if(allpackages.get(i).checkLatest())
				latestpackages.add(allpackages.get(i));
		}
		
		return latestpackages.toArray(new Package[0]);
	}
	
	public static Package getPackage(String name) throws FileNotFoundException, IOException
	{
		Index.loadrepos(false); //TODO: due to how often loadrepos is called (every time anything is needed, pretty much) it must nicely detect when everything is already loaded
		return new PackageCompare(name).get();
	}
	
	public static Package[] getQueue() throws FileNotFoundException, IOException
	{
		Index.loadrepos(false); 
		if(Queue.thequeue==null)
			return new Package[0];
		return Queue.thequeue.toArray(new Package[0]);
	}
	
	public static class runInstall implements Runnable
	{
		public void run()
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			} 
			Queue.readqueue();
			try {
				Installer.run();
			} catch (Throwable e) {
				e.printStackTrace();
				Messaging.message(e.getMessage());
				return;
			}
		}
	}
	
	public static class runMinecraft extends runInstall
	{
		@Override
		public void run()
		{
			try {
				Index.loadrepos(false);
				Queue.readqueue();
				
				if(!Installer.run())
				{
					Messaging.message("Canceled launch...");
					return;
				}
				
				Messaging.message("Getting Launcher...");
				File appdir = new File(Util.getAppDir("mcpkg")+"/");
				appdir.mkdirs();//won't do anything if it's not needed
				File mclauncher = new File(appdir,"Minecraft.jar");
				//http://www.minecraft.net/download/minecraft.jar?v=1297558339102
				String url = "http://www.minecraft.net/download/minecraft.jar";
				if(!mclauncher.exists())
				{
					InputStream fin = null;
					FileOutputStream fout = null;
					byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
					int bytesRead;
					try {
						//open the files for input and output
						fin = Util.readURL(url);
						fout = new FileOutputStream (mclauncher);
						//while bytesRead indicates a successful read, lets write...
						while ((bytesRead = fin.read(buffer)) >= 0) {
							fout.write(buffer,0,bytesRead);
						}
					
					} finally { //Ensure that the files are closed (if they were open).
						if (fin != null) { fin.close(); }
						if (fout != null) { fout.close(); }
					}
				}
				Messaging.message("Running Launcher...");
				Process ps = null;
				ps = Runtime.getRuntime().exec(new String[]{"java","-jar",mclauncher.getAbsolutePath()});
				
		        ps.waitFor();
		        java.io.InputStream is=ps.getInputStream();
		        byte b[] = null;
				b = new byte[is.available()];
		        is.read(b,0,b.length);
		        System.out.println(new String(b));
			} catch (Throwable e) {
				e.printStackTrace();
				Messaging.message(e.getClass().getSimpleName()+": "+e.getMessage());
				return;
			}
		}
	}
	
	public static class repoUpdate implements Runnable
	{
		public void run()
		{
			try {
				Index.loadrepos(true);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
		}
	}
	
	public static class queuePackage implements Runnable
	{
		public Package p;
		public queuePackage(String _id)
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=new PackageCompare(_id).get();
		}
		public queuePackage(Package _p)
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=_p;
		}

		public void run()
		{
			Queue.readqueue();
			try {
				Queue.queuePackage(p);
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
		}
	}
	
	public static class unqueuePackage implements Runnable
	{
		public Package p;
		public unqueuePackage(String _id)
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=new PackageCompare(_id).get();
			if(p == null)
				throw new IllegalArgumentException("package '"+_id+"' not found");
		}
		public unqueuePackage(Package _p)
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=_p;
		}
		public void run()
		{
			Queue.readqueue();
			Queue.unqueuePackage(p);
		}
	}
	
	public static class updatePackage implements Runnable {
		public Package p;
		public updatePackage(String _id)
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=new PackageCompare(_id).get();
			if(p == null)
				throw new IllegalArgumentException("package '"+_id+"' not found");
		}
		public updatePackage(Package _p) {
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			p=_p;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Queue.readqueue();
			try {
				Queue.updatePackage(p);
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				e.printStackTrace();
				return;
			}
		}

	}
	
	public static class delRepo implements Runnable
	{
		public String id;
		public delRepo(String _id)
		{
			id = _id;
		}
		public void run()
		{
			try {
				Index.readrepolist();
			} catch (FileNotFoundException e1) {
				Messaging.message(e1.getMessage());
				return;
			} catch (IOException e1) {
				Messaging.message(e1.getMessage());
				return;
			}
			ArrayList<String> lines = new ArrayList<String>();
			
			File appdir = new File(Util.getAppDir("mcpkg")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File repolist = new File(appdir,"repos.lst");
			//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
			FileInputStream f1 = null;
			try {
				f1 = new FileInputStream(repolist);
			} catch (FileNotFoundException e1) {
				Messaging.message(e1.getMessage());
				return;
			}
			InputStreamReader f2 = new InputStreamReader(f1);
			BufferedReader f3 = new BufferedReader(f2);
			String lookedup = null;
			try {
				lookedup = Index.mainrepos[new Integer(id)];
				Messaging.message("will search for '"+lookedup+"'");
			} catch (NumberFormatException e){}
			String line = null;
			int linenumber = 0;
			try {
				while ((line = f3.readLine()) != null)
				{
					if(line.equals(id) || (lookedup != null && line.equals(lookedup)))
					{
						lines.add("#"+line);
						Messaging.message("commented line "+linenumber);
					}
					else
						lines.add(line);
					linenumber++;
				}
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			try {
				f3.close();
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			
			FileOutputStream fo = null;
			try {
				fo = new FileOutputStream(repolist);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<lines.size();i++)
			{
				try {
					writer.write(lines.get(i));
				} catch (IOException e) {
					Messaging.message(e.getMessage());
					return;
				}
				if(i<lines.size()-1)
					try {
						writer.write("\n");
					} catch (IOException e) {
						Messaging.message(e.getMessage());
						return;
					}
			}
			
			try {
				writer.close();
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
		}
	}
	
	public static class addRepo implements Runnable
	{
		public String id;
		public addRepo(String _id)
		{
			id = _id;
		}
		public void run()
		{
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e1) {
				Messaging.message(e1.getMessage());
				return;
			} catch (IOException e1) {
				Messaging.message(e1.getMessage());
				return;
			}
			ArrayList<String> lines = new ArrayList<String>();
			
			File appdir = new File(Util.getAppDir("mcpkg")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File repolist = new File(appdir,"repos.lst");
			//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
			FileInputStream f1 = null;
			try {
				f1 = new FileInputStream(repolist);
			} catch (FileNotFoundException e1) {
				Messaging.message(e1.getMessage());
				return;
			}
			InputStreamReader f2 = new InputStreamReader(f1);
			BufferedReader f3 = new BufferedReader(f2);
			
			boolean handled = false;
			//if it gets uncommented, we don't want to add it again at the end
			
			try {
				String line = null;
				int linenumber = 0;
				while ((line = f3.readLine()) != null)
				{
					if(line.equals("#"+id))
					{
						lines.add(line.substring(1));
						handled = true;
						Messaging.message("uncommented line "+linenumber);
					}
					else
						lines.add(line);
					linenumber++;
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
			
			FileOutputStream fo;
			try {
				fo = new FileOutputStream(repolist);
			} catch (FileNotFoundException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			try {
				for(int i=0; i<lines.size();i++)
				{
					
						writer.write(lines.get(i));
					
					if(i<lines.size()-1)
						writer.write("\n");
				}
				writer.close();
			} catch (IOException e) {
				Messaging.message(e.getMessage());
				return;
			}
			
			
			try {
				Index.loadrepos(false);
			} catch (FileNotFoundException e) {

				Messaging.message(e.getMessage());
				return;
			} catch (IOException e) {

				Messaging.message(e.getMessage());
				return;
			}
		}
	}
	
	public static class cleanRepos implements Runnable
	{
		public void run()
		{
			File cache = new File(Util.getAppDir("mcpkg")+"/repocache/");
			cache.mkdirs();
			
			File[] files = cache.listFiles();
			for(int i=0; i<files.length; i++)
			{
				files[i].delete();
			} //notice how we DON'T reload the repos
		}
	}
	
	public static ArrayDeque<Runnable> commandQueue = new ArrayDeque<Runnable>();
	
	public static synchronized Runnable queue(Runnable toqueue)
	{
		if(toqueue == null)
		{
			if(commandQueue.size() > 0)
			{
				return commandQueue.removeFirst();
			}
		}
		else
		{
			clicanexit = false;
			commandQueue.add(toqueue);
		}
		return null;
	}
	
	public static boolean clicanexit = true;
	
	@Override
	public void run() {
		while(true)
		{
			Runnable r = queue(null);
			if (r == null)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {} //should we care about this?
			}
			else
			{
				System.out.println("command");
				r.run();
				clicanexit = true; //when this is set true, the command line version exits
			}
		}
	}
	
	public Commands()
	{
		super("Commands");
		start();
	}
	
	public static Commands threadinstance;
	public static void launchthread()
	{
		if(threadinstance == null)
		{
			threadinstance = new Commands();
			
		}
	}
	
}
