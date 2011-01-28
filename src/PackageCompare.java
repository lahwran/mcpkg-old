
public class PackageCompare {
	
	//these are kinda premature optimization ...
	public static final int equals = 0;
	public static final int notequals = 1;
	public static final int atleast = 2;
	public static final int atmost = 3;
	public static final int above = 4;
	public static final int below = 5;
	
	public int comparison;
	public String version;
	public String name;
	
	public PackageCompare(String compstring)
	{
		if(!compstring.matches("^[^=>!<]*[=>!<][=><][^=>!<]*$"))
		{
			throw new IllegalArgumentException("invalid comparison string '"+compstring+"'");
		}
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
			throw new IllegalArgumentException("invalid comparison: "+comparison);
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
	
}
