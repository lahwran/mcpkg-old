package mcpkg;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


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
	        return s1+"Library/Application Support/"+d+"/";
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

	
	public static String getMinecraftVersion()
	{//will be a bit of work to get, should cache results
		return "1.2_02"; //for testing purposes until I actually write this
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
		while(spath.startsWith("/"))
			spath=spath.substring(1);
		//System.out.println("cantouch '"+spath+"' "+isdir);
		if(!isdir)
		{
			for (int i=0; i<notouchiefile.length; i++)
				if(spath.startsWith(notouchiefile[i]+"/") || spath.equals(notouchiefile[i]))
				{
					System.out.println("cantouch false '"+spath+"' "+isdir);
					//System.out.println();
					return false;
				}
		} else
		{
			for (int i=0; i<notouchiedir.length; i++)
				if(spath.startsWith(notouchiedir[i]+"/") || spath.equals(notouchiedir[i]))
				{
					System.out.println("cantouch false '"+spath+"' "+isdir);
					//System.out.println();
					return false;
				}
		}
		//System.out.println("cantouch true");
		//System.out.println();
		return true;
	}

	public static boolean canTouch(File path, File root)
	{
		String spath=path.getPath().substring(root.getPath().length()).toLowerCase();
		return canTouch(spath, path.isDirectory());
	}
}
