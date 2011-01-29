import java.util.HashMap;


public class Package {
	public String Name;
	public String[][] Authors;
	public String Homepage;
	public String Section;
	public String MCVersion;
	public String Version;
	public String PackageURL;
	public String LatestChangelog; //if url is used, is read on load
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
	
	public Package(String _name)
	{
		Packages.put(_name, this);
		Name = _name;
	}
	
}
