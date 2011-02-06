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

//TODO: queue should know which packages were queued as a dependency and which were manually queued

public class Queue {
	
	public static ArrayList<Package> thequeue = new ArrayList<Package>();
	
	public static void readqueue()
	{
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();//won't do anything if it's not needed
		File queuefile = new File(appdir,"queue.lst");
		
		//thequeue = new ArrayList<Package>(); //clear it before loading - but only once we know the queue file exists
		if(thequeue != null && thequeue.size() > 0)
			return; //guess we don't want to reinit it...
		
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
				thequeue.add(p);
			}
			
			f3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void writequeue()
	{
		
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
	
	
	public static void queuePackage(Package p)
	{
		try {
			p.cache();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thequeue.add(p);
		writequeue();
		//TODO: should calculate dependencies, remove packages from queue that are conflicted, install everything depended upon, etc
	}
	public static void unqueuePackage(Package p)
	{
		thequeue.remove(p);
		writequeue();
		//TODO: stub
		//reverse of queuepackage
	}
	
	//TODO: all dependency calculation should be in separate functions, so that Commands can ask what will be done if it runs queuePackage or unqueuePackage
	
}
