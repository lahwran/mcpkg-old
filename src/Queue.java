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


public class Queue {
	
	public static ArrayList<Package> thequeue = new ArrayList<Package>();
	
	public static void readqueue()
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();//won't do anything if it's not needed
		File queuefile = new File(appdir,"queue.lst");
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
		
		
		Package[] packages=Commands.getPackages();
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
	
}
