package mcpkg;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipException;

import mcpkg.errors.dependency.UnsolvableConflict;
import mcpkg.errors.installer.ModConflict;
import mcpkg.errors.patcher.FormatError;



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
			Commands.launchthread();
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
				Package[] results = null;
				try {
					results = Commands.queryPackages(search);
				} catch (FileNotFoundException e) {
					System.out.println(e.getMessage());
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
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
					Commands.queue(new Commands.addRepo(url));
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

					Commands.queue(new Commands.delRepo(url));
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
						String[] repos = null;
						try {
							repos = Commands.getRepos();
						} catch (FileNotFoundException e) {
							System.out.println(e.getMessage());
							return;
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
						for(int i=0; i<repos.length; i++)
						{
							System.out.println(""+i+" "+repos[i]);
						}
					}
					else if(args[1].equals("sections"))
					{
						String[][] sections = null;
						try {
							sections = Commands.getSections();
						} catch (FileNotFoundException e) {
							System.out.println(e.getMessage());
							return;
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
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
						Package[] packages = null;
						try {
							packages = Commands.getPackages();
						} catch (FileNotFoundException e) {
							System.out.println(e.getMessage());
							return;
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
						listPackages(packages);
					}
					else if(args[1].equals("queue"))
					{
						Package[] packages = null;
						try {
							packages = Commands.getQueue();
						} catch (FileNotFoundException e) {
							System.out.println(e.getMessage());
							return;
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
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
					Package p;
					try {
						p = Commands.getPackage(args[1]);
					} catch (FileNotFoundException e) {
						System.out.println(e.getMessage());
						return;
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					Messaging.message("Name: "+p.Name);
					for(int i=0; i<p.Authors.length; i++)
						Messaging.message("Author: "+p.Authors[i][0]+"<"+p.Authors[i][1]+">");
					if(p.Homepage != null)
						Messaging.message("Homepage: "+p.Homepage);
					Messaging.message("Section: "+p.Section);
					Messaging.message("MCVersion: "+p.MCVersion);
					Messaging.message("Version: "+p.Version);
					Messaging.message("PackageURL: "+p.PackageURL);
					if(p.LatestChangelog != null)
						Messaging.message("LatestChangeLog: "+p.LatestChangelog);
					Messaging.message("CacheName: "+p.getCachename());
					Messaging.message("Description: "+p.FullDescription);
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
						Commands.queue(new Commands.cleanRepos());
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
					Commands.queue(new Commands.queuePackage(args[1]));
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
					Commands.queue(new Commands.unqueuePackage(args[1]));
				}
				else
				{
					System.err.println("unqueue requires argument of package to unqueue");
				}
			}
			else if(args[0].equals("run"))
			{
				Commands.queue(new Commands.runInstall());
			}
		}
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		while(!Commands.clicanexit || Messaging.messages.size() > 0 || Messaging.confirmations.size() > 0)
		{
			String m = Messaging.message(null);
			Confirmation c = Messaging.qconfirm(null);
			if(m == null && c == null)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else 
			{
				if (m != null)
				{
					System.out.println(m);
				}
				if (c != null)
				{
					System.out.println(c.question);
					String response = "skip";
					while(true)
					{
						System.out.print("Confirm? "+(c.isconfirmed?"[Y/n]":"[y/N]")+" ");
						try {
							response = stdin.readLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(response.toLowerCase().startsWith("y"))
						{
							c.isconfirmed = true;
							c.isanswered = true;
							break;
						}
						else if (response.toLowerCase().startsWith("n"))
						{
							c.isconfirmed = false;
							c.isanswered = true;
							break;
						}
						else if (response.equals(""))
						{
							c.isanswered = true;
							break;
						}
					}
				}
			}
				
		}
		Runtime.getRuntime().exit(0);
	}
	public static void listPackages(Package[] list)
	{
		for(int i=0; i<list.length; i++)
		{
			Package p = list[i];
			Messaging.message(p.Name + " - " + p.ShortDescription + " ("+p.MCVersion+"/"+p.Version+")");
		}
	}

}
