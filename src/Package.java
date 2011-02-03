import java.io.File;
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
	
	public PackageCompare[] ProvidedBy; //reverse of provides - this is actually used in dependency lookups
	public PackageCompare[] Provides;
	
	public String FullDescription;
	public String ShortDescription;
	
	public static HashMap<String, Package> Packages = new HashMap<String, Package>();
	public static HashMap<String, Package> CacheNames = new HashMap<String, Package>();
	
	public Package(String _name)
	{
		//Packages.put(_name, this);
		System.out.println("new package: "+_name);
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
			//TODO: does it make sense to include the package url in the hash? what if the url changes but the version does not?
			CacheName = MD5Checksum.strmd5(Name+PackageURL+MCVersion+Version);
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
	
	
	public static void cachePackage(Package p)
	{
		//TODO: implement package download function
	}
}
