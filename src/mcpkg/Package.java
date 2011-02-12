package mcpkg;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class Package {
	public String Name;
	public String[][] Authors; //upper array minimum 1; lower array {author, contact}
	public String Homepage; //nullable
	public String Section;
	public String MCVersion;
	public String Version;
	public String PackageURL;
	public String LatestChangelog; //nullable
	public String CacheName; //nullable - use getCacheName where a cachename is needed
	public int[] ItemIDs;
	public int[] BlockIDs;
	public PackageCompare[] Depends;
	public PackageCompare[] InstallBefore;
	public PackageCompare[] InstallAfter;
	public PackageCompare[] Recommends;
	public PackageCompare[] Suggests;
	public PackageCompare[] Enhances;
	public PackageCompare[] Conflicts;
	
	//public PackageCompare[] ProvidedBy; //reverse of provides - this is actually used in dependency lookups
	public PackageCompare[] Provides;
	
	public String FullDescription;
	public String ShortDescription;
	public boolean isQueued = false;
	
	public static HashMap<String, Package> Packages = new HashMap<String, Package>();
	public static HashMap<String, Package> CacheNames = new HashMap<String, Package>();
	
	public Package(String _name)
	{
		//Packages.put(_name, this);
		//System.out.println("new package object: "+_name);
		Name = _name;
	}
	
	public String getID()
	{ //returns ID. will be used as part of system to get package not in a repository
		return Name;
	}
	
	public String getCachename()
	{
		if(CacheName == null)
		{
			CacheName = MD5Checksum.strmd5(Name+MCVersion+Version);
			CacheNames.put(CacheName, this);
		}
		return CacheName;
	}
	
	public static Package readFile(String filename)
	{
		return readFile(new File(filename));
	}
	public static Package readFile(File f)
	{
		return null;
		//TODO: deal with this stub, it's a bit important
	}
	
	public void cache() throws IOException
	{ //TODO: could use progress indicator ...
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		File dest = new File(cachedir, getCachename());
		if(dest.exists())
			return;
		InputStream fin = null;
		FileOutputStream fout = null;
		byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
		int bytesRead;
		try {
			//open the files for input and output
			fin = Util.readURL(PackageURL);
			fout = new FileOutputStream (dest);
			//while bytesRead indicates a successful read, lets write...
			while ((bytesRead = fin.read(buffer)) >= 0) {
				fout.write(buffer,0,bytesRead);
			}
		} catch (IOException e) { //Error copying file... 
			IOException wrapper = new IOException("copyFiles: Unable to download file " + 
					PackageURL + " to " + dest.getAbsolutePath()+".");
			wrapper.initCause(e);
			wrapper.setStackTrace(e.getStackTrace());
			throw wrapper;
		} finally { //Ensure that the files are closed (if they were open).
			if (fin != null) { fin.close(); }
			if (fout != null) { fout.close(); }
		}
	}
}
