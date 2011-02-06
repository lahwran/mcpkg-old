
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length == 0)
			//launch gui
			System.out.println("in future versions, this will launch the gui. \nfor now, please refer to the instructions as to usage.");
		else if(args.length >= 1) //what else would it be at this point?
		{
			if(args[0].equals("search"))
			{
				String search = ""; //should have a way to search for keywords instead of regexes
				for (int i=1; i<args.length; i++)
				{
					//check if it's an argument here
					if(i>1)
						search += " ";
					search += args[i];
				}
				Package[] results = Commands.queryPackages(search);
				listPackages(results);
			}
			else if(args[0].equals("addrepo"))
			{
				if(args.length >= 2)
				{
					String url = "";
					for (int i=1; i<args.length; i++)
					{
						//check if it's an argument here
						if(i>1)
							url += " ";
						url += args[i];
					}
					Commands.addRepo(url);
				}
				else
				{
					System.err.println("addrepo requires argument of repo to add");
				}
			}
			else if(args[0].equals("delrepo"))
			{
				if(args.length >= 2)
				{
					String url = "";
					for (int i=1; i<args.length; i++)
					{
						//check if it's an argument here
						if(i>1)
							url += " ";
						url += args[i];
					}
					Commands.disableRepo(url);
				}
				else
				{
					System.err.println("delrepo requires argument of repo to delete");
				}
			}
			else if(args[0].equals("list"))
			{
				if(args.length >= 2)
				{
					if(args[1].equals("repos"))
					{
						String[] repos = Commands.getRepos();
						for(int i=0; i<repos.length; i++)
						{
							System.out.println(""+i+" "+repos[i]);
						}
					}
					else if(args[1].equals("sections"))
					{
						String[][] sections = Commands.getSections();
						for(int i=0; i<sections.length; i++)
						{
							System.out.print(sections[i][0]);
							if(sections[i][1]!=null)
							{
								System.out.print(" - "+sections[i][1]);
							}
							System.out.println();
						}
					}
					else if(args[1].equals("packages"))
					{
						Package[] packages = Commands.getPackages();
						listPackages(packages);
					}
					else if(args[1].equals("queue"))
					{
						Package[] packages = Commands.getQueue();
						listPackages(packages);
					}
				}
				else
				{
					System.err.println("list requires argument of value to list");
				}
			}
			else if(args[0].equals("show"))
			{
				if(args.length >= 2)
				{
					Package p =  Commands.getPackage(args[1]);
					System.out.println("Name: "+p.Name);
					for(int i=0; i<p.Authors.length; i++)
						System.out.println("Author: "+p.Authors[i][0]+"<"+p.Authors[i][1]+">");
					if(p.Homepage != null)
						System.out.println("Homepage: "+p.Homepage);
					System.out.println("Section: "+p.Section);
					System.out.println("MCVersion: "+p.MCVersion);
					System.out.println("Version: "+p.Version);
					System.out.println("PackageURL: "+p.PackageURL);
					if(p.LatestChangelog != null)
						System.out.println("LatestChangeLog: "+p.LatestChangelog);
					if(p.CacheName != null)
						System.out.println("CacheName: "+p.CacheName);
					System.out.println("Description: "+p.FullDescription);
					//TODO: uh, this doesn't list relationships and it should
				}
				else
				{
					System.err.println("show requires argument of package to show");
				}
			}
			else if(args[0].equals("clean"))
			{
				if(args.length >= 2)
				{
					if(args[1].equals("repos"))
					{
						Commands.cleanRepos();
					}
				}
				else
				{
					System.err.println("clean requires argument of storage to clean \n(though in the future clean will clean everything with no args)");
				}
			}
			else if(args[0].equals("queue"))
			{
				if(args.length >= 2)
				{
					Commands.queuePackage(args[1]);
				}
				else
				{
					System.err.println("queue requires argument of package to queue");
				}
			}
			else if(args[0].equals("unqueue"))
			{
				if(args.length >= 2)
				{
					Commands.unqueuePackage(args[1]);
				}
				else
				{
					System.err.println("unqueue requires argument of package to unqueue");
				}
			}
			else if(args[0].equals("run"))
			{
				Commands.run();
			}
		}
			
	}
	public static void listPackages(Package[] list)
	{
		for(int i=0; i<list.length; i++)
		{
			Package p = list[i];
			System.out.println(p.Name + " - " + p.ShortDescription);
		}
	}

}
