import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

//currently searches online repos EVERY time is loaded; maybe only search every 10 minutes?

public class Index {

	public static String[] mainrepos;
	public static final String defaultrepo = "file:///home/blendmaster/workspace/Patcher/testindex";
	
	public static HashMap<String, String> Sections = new HashMap<String, String>();
	
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
	
	public static boolean isin(String x, String[] y)
	{
		for (int i=0; i<y.length; i++)
		{
			if (x.equals(y[i]))
				return true;
		}
		return false;
	}
	
	//TODO: multiple, comma-separated minecraft versions - only read in the one for the current minecraft version
	//TODO: only read in latest version
	public static String[][] readRepoFromStream(BufferedReader in, String[] curKV, boolean cansection, boolean cansubrepo)
	{
		ArrayList<String[]> fieldcache = new ArrayList<String[]>();
		
		HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> loadeddata = new HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>();
		
		HashMap<String, ArrayList<String>> currentsection = null;
		
		String lastkey = null; //so we can append to last value with blocks
		
		final String[] SectionNames = new String [] {"Package", "Repository", "Section"};
		
		for(int i=0; i<SectionNames.length; i++)
		{
			loadeddata.put(SectionNames[i], new ArrayList<HashMap<String, ArrayList<String>>>());
		}
		
		while(curKV != null)
		{
			fieldcache.add(curKV);
			
			if(isin(curKV[0], SectionNames))
			{
				currentsection = new HashMap<String, ArrayList<String>>();
				ArrayList<String> curV = new ArrayList<String>();
				curV.add(curKV[1]);
				currentsection.put("Name", curV);
				lastkey = "Name";
				if(!loadeddata.containsKey(curKV[0]))
				{
					loadeddata.put(curKV[0], new ArrayList<HashMap<String, ArrayList<String>>>());
				}
				loadeddata.get(curKV[0]).add(currentsection);
			}
			else if(curKV[0].equals("Block") && currentsection != null && lastkey != null)
			{
				ArrayList<String> lastVs = currentsection.get(lastkey);
				String lastV = lastVs.get(lastVs.size()-1) + "\n" + curKV[1];
				lastVs.remove(lastVs.size()-1);
				lastVs.add(lastV);
			}
			else if(currentsection != null && !currentsection.containsKey(curKV[0]))
			{
				ArrayList<String> curV = new ArrayList<String>();
				curV.add(curKV[1]);
				currentsection.put(curKV[0], curV);
				lastkey = curKV[0];
			}
			else if(currentsection != null)
			{
				ArrayList<String> lastVs = currentsection.get(lastkey);
				lastVs.add(curKV[1]);
			}
			
			curKV = splitKV(Util.getNextLine(in));
		}
		/*
		for(int i=0; i<SectionNames.length; i++)
		{
			System.out.println("section type "+SectionNames[i]);
			ArrayList<HashMap<String, ArrayList<String>>> sstype = loadeddata.get(SectionNames[i]);
			for(int j=0; j<sstype.size(); j++)
			{
				currentsection = sstype.get(j);
				System.out.println("\tsection "+currentsection.get("Name").get(0));
				String[] sectionkeys = currentsection.keySet().toArray(new String[0]); 
				for(int k=0;k<sectionkeys.length;k++)
				{
					System.out.println("\t\tkey: "+sectionkeys[k]);
					if(!sectionkeys[k].equalsIgnoreCase("Name"))
					{
						ArrayList<String> curV = currentsection.get(sectionkeys[k]);
						for(int l=0; l<curV.size(); l++)
							System.out.println("\t\t\t"+curV.get(l)+"\n\t\t\t\tendV");
					}
				}
				System.out.println("\tsectionend");
			}
			System.out.println("sectiontypeend");
			
		}
		//*/
		///*
		//icky name is icky
		ArrayList<HashMap<String, ArrayList<String>>> Sectionsarr = loadeddata.get("Section");
		for(int i=0; i<Sectionsarr.size(); i++)
		{
			HashMap<String, ArrayList<String>> sdata = Sectionsarr.get(i);
			if((!Sections.containsKey(sdata.get("Name").get(0))) && (sdata.get("Description")!=null))
				Sections.put(sdata.get("Name").get(0),sdata.get("Description").get(0));
			else if(!Sections.containsKey(sdata.get("Name").get(0)))
				Sections.put(sdata.get("Name").get(0),null);
			//else
			//	throw new IllegalArgumentException("tried to add duplicate section "+sdata.get("Name").get(0));
		}
		ArrayList<HashMap<String, ArrayList<String>>> Packages = loadeddata.get("Package");
		for(int i=0; i<Packages.size(); i++)
		{
			//java.lang.IndexOutOfBoundsException
			HashMap<String, ArrayList<String>> pdata = Packages.get(i);
			Package p = new Package(pdata.get("Name").get(0));
			Package.Packages.put(p.Name, p);
			ArrayList<String> dataauthors = pdata.get("Author");
			String[] temp = dataauthors.get(0).split("[<>]");
			ArrayList<String[]> authors = new ArrayList<String[]>();
			authors.add(new String[] {temp[0], temp[1]});
			for(int j=1; j<dataauthors.size(); j++)
			{
				temp = dataauthors.get(j).split("[<>]");
				authors.add(new String[] {temp[0], temp[1]});
			}
			p.Authors=authors.toArray(new String[0][]);
			
			if(pdata.containsKey("Homepage"))
				p.Homepage = pdata.get("Homepage").get(0);
			
			String section = null;
			try{
				section = pdata.get("PackageSection").get(0);
			}catch (NullPointerException e) {
				//let's add some context, yes?
				throw new IllegalArgumentException("package "+p.Name+" missing PackageSection field");
			}
			if (!Sections.containsKey(section))
				throw new IllegalArgumentException("undeclared section "+section+" while reading package "+p.Name);
			p.Section = section;
			
			String[] Versions = pdata.get("Version").get(0).split(" ", 3);
			
			p.PackageURL = Versions[2];
			p.Version = Versions[1];
			p.MCVersion = Versions[0];
			
			if(pdata.containsKey("SingleChangelog"))
				p.Homepage = pdata.get("SingleChangelog").get(0);
			
			if(pdata.containsKey("ItemIDs"))
			{
				temp = pdata.get("ItemIDs").get(0).split(",");
				p.ItemIDs = new int[temp.length];
				for(int j=0; j<temp.length; j++)
				{
					p.ItemIDs[j]= new Integer(temp[j]);
				}
			}
			if(pdata.containsKey("BlockIDs"))
			{
				temp = pdata.get("BlockIDs").get(0).split(",");
				p.BlockIDs = new int[temp.length];
				for(int j=0; j<temp.length; j++)
				{
					p.BlockIDs[j]= new Integer(temp[j]);
				}
			}
			p.Depends = PackageList(pdata.get("Depends"));
			p.InstallBefore = PackageList(pdata.get("InstallBefore"));
			p.InstallAfter = PackageList(pdata.get("InstallAfter"));
			p.Recommends = PackageList(pdata.get("Recommends"));
			p.Suggests = PackageList(pdata.get("Suggests"));
			p.Enhances = PackageList(pdata.get("Enhances"));
			p.Conflicts = PackageList(pdata.get("Conflicts"));
			p.Provides = PackageList(pdata.get("Provides"));
			
			p.FullDescription = pdata.get("Description").get(0);
			p.ShortDescription = p.FullDescription.indexOf("\n") != -1 ? p.FullDescription.substring(0, p.FullDescription.indexOf("\n")) : "";
		}
		//*/
		return fieldcache.toArray(new String[0][]);
	}
	
	public static PackageCompare[] PackageList(ArrayList<String> _list)
	{
		if(_list == null)
			return null;
		String list = _list.get(0);
		ArrayList<PackageCompare> comparers = new ArrayList<PackageCompare>();
		if(list == null)
			return new PackageCompare[0];
		String[] comparisons = list.split(" *, *");
		
		for(int i=0; i<comparisons.length; i++)
		{
			comparers.add(new PackageCompare(comparisons[i]));
		}
		
		return comparers.toArray(new PackageCompare[0]);
	}
	
	public static void cacherepo(String[][] KVs, File output)
	{
		try {
			FileOutputStream fo = new FileOutputStream(output);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i< KVs.length;i++)
			{
				writer.write(KVs[i][0]);
				writer.write(": ");
				writer.write(KVs[i][1]);
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
	
	public static void loadrepo(String repourl, boolean cansection, boolean cansubrepo )
	{ //TODO: will be slow as hell when there is no connection or 404 or such ..
		try {
			String cachehash = MD5Checksum.strmd5(repourl);
			
			File cache = new File(Util.getAppDir("mcpkg")+"/repocache/");
			cache.mkdirs();//won't do anything if it's not needed
			File thiscache = new File(cache,cachehash);
			
			
			BufferedReader in;
			
			in = new BufferedReader(new InputStreamReader(Util.readURL(repourl)));
		

			String[] inputLine = splitKV(Util.getNextLine(in));
			if(inputLine != null && inputLine[0].equals("IndexVersion") && thiscache.exists())
			{
				//check cached copy; if it's IndexVersion matches just-read indexversion, no need to update it
				FileInputStream f1 = new FileInputStream(thiscache);
				InputStreamReader f2 = new InputStreamReader(f1);
				BufferedReader f3 = new BufferedReader(f2);
				String[] firstset = splitKV(Util.getNextLine(f3));
				if(firstset != null && firstset[0].equals("IndexVersion") && firstset[1].equals(inputLine[1]))
				{
					readRepoFromStream(f3, firstset, cansection, cansubrepo);
					return;
				}
			}

			cacherepo(readRepoFromStream(in, inputLine, cansection, cansubrepo), thiscache);
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadrepos()
	{
		readrepolist();
		//TODO: needs some kind of rate limiting .. don't check more often than every 10 minutes or something 
		for(int i=0; i<mainrepos.length; i++)
		{
			loadrepo(mainrepos[i], true, true);
		}
	}
	public static String repolisthash = "";
	public static void readrepolist()
	{
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		appdir.mkdirs();//won't do anything if it's not needed
		File repolist = new File(appdir,"repos.lst");
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		//String currepolisthash = null;
		try {
			f1 = new FileInputStream(repolist); //TODO: can't we just seek to beginning?
			//currepolisthash = MD5Checksum.make(new FileInputStream(repolist));
		} catch (FileNotFoundException e) {
			initrepolistfile();
			
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//wait, how is this method of hashing more efficient than doing the normal loading in?
		/*
		if(repolisthash.equals(currepolisthash))
			return;
		repolisthash=currepolisthash;*/
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader f3 = new BufferedReader(f2);
		
		ArrayList<String> listbuilder = new ArrayList<String>(); 
		
		try {
			String line = null;
			while ((line = Util.getNextLine(f3)) != null)
			{
				listbuilder.add(line);
			}
			
			f3.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mainrepos = listbuilder.toArray(new String[0]);
		//should check for old default repos
	}
	
	public static void initrepolistfile()
	{
		try {
			File appdir = new File(Util.getAppDir("mcpkg")+"/");
			appdir.mkdirs();//won't do anything if it's not needed
			File repolist = new File(appdir,"repos.lst");
			
			FileOutputStream fo = new FileOutputStream(repolist);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			writer.write("#default repository\n"+defaultrepo);
			
			writer.close();
			mainrepos = new String[]{defaultrepo};
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
