package mcpkg;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import mcpkg.targetting.IArchive;
import mcpkg.targetting.IEntry;
import mcpkg.targetting.ZipArchive;


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
	        return s1+"/Library/Application Support/"+d+"/";
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

	public static String mcversioncache = null;
	public static String getCachedMinecraftVersion()
	{//will be a bit of work to get, should cache results
		//return "1.2_02"; //for testing purposes until I actually write this
		if(mcversioncache == null)
		{
			String s = getMinecraftVersion();
			return s;
		}
		else
			return mcversioncache;
	}
	public static Pattern pattern = Pattern.compile("Minecraft[:][^0-9]*([0-9._]*)");
	public static String getMinecraftVersion()
	{
		try {
			
			File appdir = new File(Util.getAppDir("minecraft")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File mcjar = new File(appdir, "/bin/minecraft.jar");
			IArchive jarreader = new ZipArchive(mcjar);
			
			IEntry[][] entries =  Patcher.readZip(jarreader);
			
			for(int i=0; i<entries[0].length; i++)
			{
				IEntry in = entries[0][i];
				if(in.getName().endsWith(".class"))
				{
					int inSize = (int)in.getSize();
					byte[] inBytes = new byte[inSize];
					InputStream sourceStream = jarreader.getInputStream(in);
					
					//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
					for (int erg = sourceStream.read(inBytes); erg < inBytes.length; erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
					sourceStream.close();
					String filestring = new String(inBytes);
					
					Matcher matcher = pattern.matcher(filestring);
					if(matcher.find())
					{ //we found it
						mcversioncache = matcher.group(1);
						break;
					}
				}
			}
			if(mcversioncache == null)
			{
				throw new Exception("did not find error report file - you may need to redownload mcpkg!");
			}
			System.out.println("Extracted version: "+mcversioncache);
			return mcversioncache;
		}catch(Throwable e)
		{
			e.printStackTrace();
			Messaging.message(e.getClass().getSimpleName()+": "+e.getMessage());
			System.out.println("error");
			return "1:error";
		}
		
		
	}
	
	public static String latestMCVersionCache = null;
	public static Pattern tktechpattern = Pattern.compile("Version: ([0-9_.]*) \\[([0-9]*)\\]");
	public static String[] getTktechVersion() throws Throwable
	{
		try{
			if(latestMCVersionCache == null)
			{
				String theurl = "http://cia.vc/stats/project/mc-ver/.rss?ver=2&medium=plaintext&limit=1";
				StringBuilder out = new StringBuilder();
				InputStream fin = null;
				byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
				int bytesRead;
				try {
					//open the files for input and output
					fin = Util.readURL(theurl);
					//while bytesRead indicates a successful read, lets write...
					while ((bytesRead = fin.read(buffer)) >= 0) {
						out.append(new String(buffer),0,bytesRead);
					}
				} catch (IOException e) { //Error copying file... 
					IOException wrapper = new IOException("copyFiles: Unable to download file " + 
							theurl + ".");
					wrapper.initCause(e);
					wrapper.setStackTrace(e.getStackTrace());
					throw wrapper;
				} finally { //Ensure that the files are closed (if they were open).
					if (fin != null) { fin.close(); }
				}
				
				latestMCVersionCache = out.toString();
			}
			Matcher matcher = tktechpattern.matcher(latestMCVersionCache);
			//System.out.println("~~"+latestMCVersionCache+">>");
			if(matcher.find())
			{
				//System.out.println(matcher.group(2));
				
				//System.out.println(matcher.group(1));
				return new String[]{matcher.group(1), matcher.group(2)};
			}
			else
			{
				throw new Exception("tktech's version feed does not match regex - will probably require a mcpkg update to fix");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			Messaging.message(e.getClass().getSimpleName()+": "+e.getMessage());
			throw e;
		}
	}
	public static String getLatestMinecraftVersion() throws Throwable
	{
		//.* .*
		return getTktechVersion()[0];
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

	public static boolean deleteDir(File dir)
	{
		return Util.deleteDir(dir, dir);
	}

	public static boolean deleteDir(File dir, File root) {
		if (!Util.canTouch(dir, root))
			return true;
		
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]), root);
	            if (!success) {
	                return false;
	            }
	        }
	    }
	
	    // The directory is now empty so delete it
	    if(dir.isDirectory() && dir.list().length != 0)
	    	return true;
	    else
	    	return dir.delete();
	}

	//ignores canTouch completely
	public static boolean deleteDirMean(File dir) {		
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDirMean(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	
	    // The directory is now empty so delete it
	    return dir.delete();
	}

	/**
	 * copied from http://www.dreamincode.net/code/snippet1443.htm
	 * found with google
	 * 
	 * This function will copy files or directories from one location to another.
	 * note that the source and the destination must be mutually exclusive. This 
	 * function can not be used to copy a directory to a sub directory of itself.
	 * The function will also have problems if the destination files already exist.
	 * @param src -- A File object that represents the source for the copy
	 * @param dest -- A File object that represnts the destination for the copy.
	 * @throws IOException if unable to copy.
	 */
	
	
	public static void copyFiles(File src, File dest) throws IOException {
		Util.copyFiles(src, dest, src);
	}
	public static void copyFiles(File src, File dest, File srcroot) throws IOException {
		if(!Util.canTouch(src,srcroot))
			return;
		
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath()+".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath()+".");
		}
		//is this a directory copy?
		if (src.isDirectory()) 	{
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//copy all the files in the list.
			for (int i = 0; i < list.length; i++)
			{
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1 , dest1, srcroot);
			}
		} else { 
			//This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			
			try {
				//open the files for input and output
				fin =  new FileInputStream(src);
				fout = new FileOutputStream (dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer,0,bytesRead);
				}
			} catch (IOException e) { //Error copying file... 
				IOException wrapper = new IOException("copyFiles: Unable to copy file: " + 
							src.getAbsolutePath() + "to" + dest.getAbsolutePath()+".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) { fin.close(); }
				if (fout != null) { fout.close(); }
			}
		}
	}
	//duplicate code much?
	public static void copyFilesMean(File src, File dest) throws IOException {
		
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath()+".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath()+".");
		}
		//is this a directory copy?
		if (src.isDirectory()) 	{
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//copy all the files in the list.
			for (int i = 0; i < list.length; i++)
			{
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFilesMean(src1 , dest1);
			}
		} else { 
			//This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			if(dest.exists())
				if(!dest.delete())
					throw new IOException("copyFiles: cannot delete file to be overwritten "+dest.getAbsolutePath());
			try {
				//open the files for input and output
				fin =  new FileInputStream(src);
				fout = new FileOutputStream (dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer,0,bytesRead);
				}
			} catch (IOException e) { //Error copying file... 
				IOException wrapper = new IOException("copyFiles: Unable to copy file: " + 
							src.getAbsolutePath() + "to" + dest.getAbsolutePath()+".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) { fin.close(); }
				if (fout != null) { fout.close(); }
			}
		}
	}
	/*//oops, don't need this after all?
	//nice is a nondescriptive name
	//this obeys the rules on what can be touched
	public static void copyFilesNice(File src, File dest) throws IOException {
		if(!canTouch(dest.getPath()))
			return;
		
		//Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath()+".");
		} else if (!src.canRead()) { //check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath()+".");
		}
		//is this a directory copy?
		if (src.isDirectory()) 	{
			if (!dest.exists()) { //does the destination already exist?
				//if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			//get a listing of files...
			String list[] = src.list();
			//copy all the files in the list.
			for (int i = 0; i < list.length; i++)
			{
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1 , dest1);
			}
		} else { 
			//This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				//open the files for input and output
				fin =  new FileInputStream(src);
				fout = new FileOutputStream (dest);
				//while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer,0,bytesRead);
				}
			} catch (IOException e) { //Error copying file... 
				IOException wrapper = new IOException("copyFiles: Unable to copy file: " + 
							src.getAbsolutePath() + "to" + dest.getAbsolutePath()+".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { //Ensure that the files are closed (if they were open).
				if (fin != null) { fin.close(); }
				if (fout != null) { fout.close(); }
			}
		}
	}*/

	public static boolean canTouch(String spath, boolean isdir)
	{
		//String spath=path.getPath().substring(root.getPath().length()).toLowerCase();
		String[] notouchiedir = new String[] {"mods","saves", "screenshots"} ;
		String[] notouchiefile = new String[] {"bin/version", "options.txt", "lastlogin"};
		spath = spath.replace('\\', '/');
		while(spath.startsWith("/"))
			spath=spath.substring(1);
		System.out.println("cantouch '"+spath+"' "+isdir);
		if(!isdir)
		{
			for (int i=0; i<notouchiefile.length; i++)
				if(spath.startsWith(notouchiefile[i]+"/") || spath.equals(notouchiefile[i]))
				{
					System.out.println("cantouch false '"+spath+"' "+isdir);
					System.out.println();
					return false;
				}
		} else
		{
			for (int i=0; i<notouchiedir.length; i++)
				if(spath.startsWith(notouchiedir[i]+"/") || spath.equals(notouchiedir[i]))
				{
					System.out.println("cantouch false '"+spath+"' "+isdir);
					System.out.println();
					return false;
				}
		}
		System.out.println("cantouch true");
		System.out.println();
		return true;
	}

	public static boolean canTouch(File path, File root)
	{
		String spath=path.getPath().substring(root.getPath().length()).toLowerCase();
		return canTouch(spath, path.isDirectory());
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
}
