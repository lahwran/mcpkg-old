package mcpkg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PackageCompare implements Comparator<Package> {
	
	//these are kinda premature optimization ...
	public static final int equals = 0;
	public static final int notequals = 1;
	public static final int atleast = 2;
	public static final int atmost = 3;
	public static final int above = 4;
	public static final int below = 5;
	public static final int nocomparison = 6;
	
	public int comparison=nocomparison;
	public String version=null;
	public String name;
	
	public PackageCompare(String compstring)
	{
		if(!compstring.matches("^[^=>!<]*[=>!<][=><][^=>!<]*$"))
		{
			name = compstring;
			//throw new IllegalArgumentException("invalid comparison string '"+compstring+"'");
		}
		else
		{
			if(compstring.matches("^.*[=][=].*$"))
			{
				comparison=equals;
			}
			else if(compstring.matches("^.*[!][=].*$"))
			{
				comparison=notequals;
			}
			else if(compstring.matches("^.*[>][=].*$"))
			{
				comparison=atleast;
			}
			else if(compstring.matches("^.*[<][=].*$"))
			{
				comparison=atmost;
			}
			else if(compstring.matches("^.*[>][>].*$"))
			{
				comparison=above;
			}
			else if(compstring.matches("^.*[<][<].*$"))
			{
				comparison=below;
			}
			String[] x=compstring.split("[=>!<][=>!<]");
			if(x.length != 2)
			{
				throw new IllegalArgumentException("invalid comparison string (needs to split to two) '"+compstring+"'");
			}
			name=x[0];
			version=x[1];
		}
	}
	public PackageCompare(String name, String comparison, String version)
	{
		this(name, getCompnum(comparison), version);
	}
	public PackageCompare(String _name, int _comparison, String _version)
	{
		comparison = _comparison;
		name=_name;
		version=_version;
	}
	
	public static int getCompnum(String comparison)
	{
		int compnum = -1; 
		if(comparison.equals("=="))
			compnum = equals;
		else if(comparison.equals("!="))
			compnum = notequals;
		else if(comparison.equals(">="))
			compnum = atleast;
		else if(comparison.equals("<="))
			compnum = atmost;
		else if(comparison.equals(">>"))
			compnum = above;
		else if(comparison.equals("<<"))
			compnum = below;
		else
			throw new IllegalArgumentException("invalid comparison: "+comparison+" - how the shit did you make this happen, anyway?");
		return compnum;
	}
	
	public boolean test(Package p)
	{
		return test(p.Name, p.Version);
	}
	
	public boolean test(String pkgname, String pkgversion)
	{
		if(!pkgname.equals(name))
			return false;
		else if(comparison == nocomparison)
			return true;
		
		//1 means A > B, -1 means A < B
		int res = CompareVersion.compare(pkgversion, version);
		switch(comparison)
		{
		case equals:
			return res == 0;
		case notequals:
			return res != 0;
		case atleast:
			return res >= 0;
		case atmost:
			return res <= 0;
		case above:
			return res > 0;
		case below:
			return res < 0;
		default:
			throw new IllegalArgumentException("invalid comparison number "+comparison);
		}
	}
	
	//-1 means second one is newer
	//0 means equals
	//1 means first one is newer
	//sort will be 0=lowest to length-1=highest
	@Override
	public int compare(Package arg0, Package arg1) {
		// TODO Auto-generated method stub
		return scompare(arg0, arg1);
	}
	
	public static int scompare(Package arg0, Package arg1) //scompare - static compare, because it's retarded to initialize an object
	{
		if(arg0 == arg1)
			return 0;
		if(arg0 == null)
			return -1;
		else if (arg1 == null)
			return 1;
		
		
		int mc=CompareVersion.compare(arg0.MCVersion, arg1.MCVersion);
		if(mc != 0)
			return mc;
		return CompareVersion.compare(arg0.Version, arg1.Version);
	}
	
	public Package[] all()
	{
		Package[] packages=Package.CacheNames.values().toArray(new Package[0]);
		ArrayList<Package> matches = new ArrayList<Package>();
		for(int i=0; i<packages.length; i++)
		{
			if(test(packages[i]))
			{
				matches.add(packages[i]);
			}
		}
		Collections.sort(matches, this);
		
		return matches.toArray(new Package[0]);
	}
	
	public Package get() //get latest
	{
		Package[] p = all();
		if(p.length == 0)
			return null;
		
		return p[p.length-1];
	}
}
