import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

import targetting.DirArchive;
import targetting.DirOutputStream;
import targetting.IArchive;
import targetting.IDirOutputStream;
import targetting.IEntry;
import targetting.ZipArchive;
import targetting.ZipDirOutputStream;

import com.nothome.delta.Delta;
import com.nothome.delta.DiffWriter;
import com.nothome.delta.GDiffPatcher;
import com.nothome.delta.GDiffWriter;

import errors.patcher.FormatError;
import errors.patcher.PatchConflict;


/*
 * /info.properties #mod name, mod version, depends, conflicts, provides, author, so on and so forth
	
	/$target/
	/$target/delete.properties #list of files to delete, along with hashes - all hashes would be checked before beginning deletion
	/$target/add/ #files to add - adding not begun until all hashes are checked
	/$target/patch/ #files to patch
	/$target/patch/patch.properties #list of files to patch, matched to source and dest hashes, along with patchfile names - 
	
	targets:
	main #stuff that should be altered in .minecraft - not allowed to have modify personal data, or touch minecraft.jar (lwjgl jars are allowed)
	mcjar #same as main but is applied to minecraft.jar instead
	svrjar #same as main and mcjar but applies to server. possibly mutually exclusive with mcjar and main? when operating on server, only svrjar is used.
	possibly in the future, have
 */

//TODO: incorrect error handling
public class Patcher {

	
	
	public static IEntry[][] readZip(IArchive f)
	{
		
		ArrayList<IEntry> files = new ArrayList<IEntry>();
		ArrayList<IEntry> directories = new ArrayList<IEntry>();

		
		
		
		for (Enumeration<IEntry> enumer = f.entries(); enumer.hasMoreElements(); )
		{
			IEntry entry = enumer.nextElement();
			if(!Util.canTouch(entry.getName(), entry.isDirectory()))
				continue;
			if(entry.isDirectory())
			{
				directories.add(entry);
				//System.out.println("read dir:  "+entry.getName());
			}
			else
			{
				files.add(entry);
				//System.out.println("read file: "+entry.getName());
			}
		}
		IEntry[] A = new IEntry[0];
		return new IEntry[][]{files.toArray(A),directories.toArray(A)};
	}
	
	public static boolean copyArchive(IArchive instream, IDirOutputStream outstream) throws IOException
	{
		System.out.println("getting input names ...");
		IEntry[][] incontent = readZip(instream);
		System.out.println("add old directories");
		for(int i=0; i<incontent[1].length; i++)
		{
			IEntry outputEntry = outstream.makeEntry(incontent[1][i].getName());
			outstream.putNextEntry(outputEntry);
		}
		for(int i=0; i<incontent[0].length; i++)
		{
			IEntry outputEntry = outstream.makeEntry(incontent[0][i].getName());
			outstream.putNextEntry(outputEntry);
			
			IEntry in =incontent[0][i];
			int inSize = (int)in.getSize();
			//System.out.println(incontent[0][i].getName()+" "+inSize);
			if(inSize > 0)
			{
				byte[] inBytes = new byte[inSize];
				InputStream sourceStream = instream.getInputStream(in);
				
				//System.out.println(""+(in==null)+" "+(inBytes==null));

				//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
				for (int erg = sourceStream.read(inBytes); 
				erg < inBytes.length;
				erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
				sourceStream.close();
				
				outstream.write(inBytes);
			}
		}
		return false;
		
	}
	
	public static String[] listPatch(String target, IArchive patchreader) throws FileNotFoundException, IOException
	{

		Properties deletions = new Properties();
		Properties patches = new Properties();
		System.out.println("getting patch names ...");
		IEntry[][] patchcontent = readZip(patchreader);
		
		ArrayList<String> files = new ArrayList<String>();
		
		try{
			deletions.load(patchreader.getInputStream((patchreader.getEntry(target+"/delete.properties"))));
			patches.load(patchreader.getInputStream((patchreader.getEntry(target+"/patch/patch.properties"))));
		} catch (NullPointerException e) { 
			return new String[0]; //if this is reached, then the most likely cause is that the target is missing
			                      //so just move on
			                      //one of the properties might also be missing; just ignore that (TODO: should we ignore that?)
		}

		String[] delkeys = deletions.keySet().toArray(new String[0]);
		String[] patchkeys = patches.keySet().toArray(new String[0]);
		for(int i=0; i<delkeys.length; i++)
			files.add("-"+delkeys[i]);
		for(int i=0; i<patchkeys.length; i++)
			files.add("~"+patchkeys[i]);
		for(int t=0; t<2; t++) //two iteration for loop ... whoopee ...
			for(int i=0; i<patchcontent[t].length; i++)
				if(patchcontent[t][i].getName().startsWith(target+"/add/"))
					files.add("+"+patchcontent[t][i].getName().substring((target+"/add/").length()));
		return files.toArray(new String[0]);
		
	}
	
	public static void applypatch(String target, IArchive originalreader, IArchive patchreader, IDirOutputStream output, boolean dodelete) throws FileNotFoundException, IOException, PatchConflict, FormatError
	{
		//try
		//{
			System.out.println(target);
			Properties deletions = new Properties();
			Properties patches = new Properties();
			try{
				
				deletions.load(patchreader.getInputStream((patchreader.getEntry(target+"/delete.properties"))));
				patches.load(patchreader.getInputStream((patchreader.getEntry(target+"/patch/patch.properties"))));
			} catch (NullPointerException e) { 
				System.out.println("missing target "+target+" - this is probably OK - doing flat copy instead");
				copyArchive(originalreader, output);
				return;
			}
			
			System.out.println("getting patch names ...");
			IEntry[][] patchcontent = readZip(patchreader);
	
			System.out.println("getting input names ...");
			IEntry[][] incontent = readZip(originalreader);
			
			
			
			InputStream is = null;
			
			
			
			
			System.out.println("checking all md5sums");
			System.out.println("delkeys");
			String[] delkeys = deletions.keySet().toArray(new String[0]);
			for(int i=0; i<delkeys.length; i++)
			{
				String sum = deletions.getProperty(delkeys[i]);
				if (sum.equals("directory")) continue; //should verify that it will be empty? maybe...
				try{
					is = originalreader.getInputStream(originalreader.getEntry(delkeys[i]));
				} catch (NullPointerException e)
				{
					System.out.println("NOTE: file to be deleted '"+delkeys[i]+"' appears to already have been removed");
					continue;
				}
				if(!MD5Checksum.check(is,sum))
				{
					throw new PatchConflict(delkeys[i], "File to be deleted '"+delkeys[i]+"' does not have md5 '"+sum+"' (target: "+target+")");
				}
			}
			System.out.println("patchkeys");
			String[] patchkeys = patches.keySet().toArray(new String[0]);
			for(int i=0; i<patchkeys.length; i++)
			{
				String[] inf = patches.getProperty(patchkeys[i]).split(" ");
				if(inf.length != 3)
				{
					throw new FormatError("Field count for patch for file '"+patchkeys[i]+"' is not three (target: "+target+")");
				}
				try{
					is = originalreader.getInputStream(originalreader.getEntry(patchkeys[i]));
				} catch (FileNotFoundException e) //rewrite the error to be a bit more useful
				{
					throw new FileNotFoundException("File to be patched '"+patchkeys[i]+"' appears to be missing (target: "+target+")");
				}
				String sum = MD5Checksum.make(is);
				if (sum.equals(inf[2])) //already matches target md5 - remove from list to be patches
				{
					patches.remove(patchkeys[i]);
				}
				else if(!sum.equals(inf[1])) //unclean - matches neither target nor source md5, abort
				{
					throw new PatchConflict(patchkeys[i], "File to be patched '"+patchkeys[i]+"' does not have original sum '"+inf[1]+"' or target sum '"+inf[2]+"' (target: "+target+")");
				} //clean! do the patch
				
			}
			System.out.println("check new files");
			for(int t=0; t<patchcontent.length; t++)
			{
				for(int i=0; i<patchcontent[t].length; i++)
				{
					if( patchcontent[t][i].getName().startsWith(target+"/add/"))
					{
						String outname = patchcontent[t][i].getName();
						outname = outname.substring((target+"/add/").length());
						if(outname.length() == 0)
							continue;
						for(int j=0; j<incontent[t].length; j++)
						{
							if(incontent[t][j].getName().equals(outname))
							{
								throw new PatchConflict(outname, "File to be added '"+outname+"' already exists. should this have been a patch? (target: "+target+")");
							}
						}
					}
				}
			}
			System.out.println("INFO: all files match MD5sums, continuing with patch");
			
			
			//String home = System.getProperty("user.home", ".");
			//File patchtemp = new File(home+"/.patchtemp");
			
			//should check that new are actually new
			System.out.println("add new directories");
			for(int i=0; i<patchcontent[1].length; i++)
			{
				if( patchcontent[1][i].getName().startsWith(target+"/add/"))
				{
					String outname = patchcontent[1][i].getName();
					outname = outname.substring((target+"/add/").length());
					if(outname.length() == 0)
						continue;
					//System.out.println("'"+outname+"'");
					IEntry outputEntry = output.makeEntry(outname);
					output.putNextEntry(outputEntry);
				}
			}
			System.out.println("add old directories");
			for(int i=0; i<incontent[1].length; i++)
			{
				if(deletions.getProperty(incontent[1][i].getName(), "").equals("") || incontent[1][i].getName().toLowerCase().equals("meta-inf/"))
				{
					IEntry outputEntry = output.makeEntry(incontent[1][i].getName());
					output.putNextEntry(outputEntry);
				}
			}
				

			System.out.println("add new files");
			for(int i=0; i<patchcontent[0].length; i++)
			{
				if( patchcontent[0][i].getName().startsWith(target+"/add/"))
				{
					String outname = patchcontent[0][i].getName();
					outname = outname.substring((target+"/add/").length());
					//System.out.println(outname);
					IEntry outputEntry = output.makeEntry(outname);
					output.putNextEntry(outputEntry);
					outputEntry.setTime(patchcontent[0][i].getTime());
					
					
					IEntry in =patchcontent[0][i];
					int inSize = (int)in.getSize();
					byte[] inBytes = new byte[inSize];
					InputStream sourceStream = patchreader.getInputStream(in);
					
					//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
					for (int erg = sourceStream.read(inBytes); erg < inBytes.length; erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
					sourceStream.close();
	
					output.write(inBytes);
				}
				
			}

			System.out.println("add old files and apply patches");
			GDiffPatcher patcher = new GDiffPatcher();
			for(int i=0; i<incontent[0].length; i++)
			{
				if(!deletions.getProperty(incontent[0][i].getName(), "").equals("") && !incontent[0][i].getName().toLowerCase().equals("meta-inf/manifest.mf"))
				{
					//System.out.println(incontent[0][i].getName()+" deleted");
					continue; //deletion
				}
				else if (!patches.getProperty(incontent[0][i].getName(), "").equals(""))
				{
					String[] inf = patches.getProperty(incontent[0][i].getName()).split(" ");
					//already verified that inf will be 3 long at this point
					IEntry patch = patchreader.getEntry(target+"/patch/"+incontent[0][i].getName()+inf[0]);
					InputStream patchinput = patchreader.getInputStream(patch);
					IEntry outputEntry = output.makeEntry(incontent[0][i].getName());
					output.putNextEntry(outputEntry);
					
					IEntry in =incontent[0][i];
					int inSize = (int)in.getSize();
					byte[] inBytes = new byte[inSize];
					InputStream sourceStream = originalreader.getInputStream(in);
					
					//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
					for (int erg = sourceStream.read(inBytes); 
					erg < inBytes.length; 
					erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
					sourceStream.close();
					
					patcher.patch(inBytes, patchinput, output.getOStream());
				}
				else
				{
					IEntry outputEntry = output.makeEntry(incontent[0][i].getName());
					output.putNextEntry(outputEntry);
					
					IEntry in =incontent[0][i];
					int inSize = (int)in.getSize();
					//System.out.println(incontent[0][i].getName()+" "+inSize);
					if(inSize > 0)
					{
						byte[] inBytes = new byte[inSize];
						InputStream sourceStream = originalreader.getInputStream(in);
						
						//System.out.println(""+(in==null)+" "+(inBytes==null));
	
						//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
						for (int erg = sourceStream.read(inBytes); 
						erg < inBytes.length;
						erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
						sourceStream.close();
						
						output.write(inBytes);
					}
				}
			}
			
		//} catch (FileNotFoundException e) {
		//	e.printStackTrace();
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
	}
	
	public static void makepatch(String target, IArchive originalreader, IArchive patchreader, IDirOutputStream output, boolean dodelete) throws IOException
	{
			System.out.println("getting patch names ...");
			IEntry[][] patchcontent = readZip(patchreader);
	
			System.out.println("getting input names ...");
			IEntry[][] incontent = readZip(originalreader);
			
			IEntry[][] addedcontent = new IEntry[2][];
			IEntry[][][] sharedcontent = new IEntry[2][][];
			IEntry[][] delcontent = new IEntry[2][];
			
			
			Properties deletions = new Properties();
			Properties patches = new Properties();
			
			
			System.out.println("added and shared");
			for(int ptype=0; ptype<patchcontent.length; ptype++)
			{
				ArrayList<IEntry> added = new ArrayList<IEntry>();
				ArrayList<IEntry[]> shared = new ArrayList<IEntry[]>();
				for(int i = 0; i<patchcontent[ptype].length; i++)
				{
					boolean isnew = true;
					String curname = patchcontent[ptype][i].getName();
					for(int j =0; j<incontent[ptype].length && isnew; j++)
					{
						if(incontent[ptype][j].getName().equalsIgnoreCase(curname))
						{
							isnew=false;
							shared.add(new IEntry[]{patchcontent[ptype][i], incontent[ptype][j]});
						}
					}
					if(isnew)
					{
						added.add(patchcontent[ptype][i]);
					}
				}
				IEntry[][] A = new IEntry[0][];
				IEntry[] B = new IEntry[0];
				sharedcontent[ptype] = shared.toArray(A);
				addedcontent[ptype] = added.toArray(B);
			}
			System.out.println("added dirs");
			for(int i=0; i<addedcontent[1].length; i++)
			{	
				IEntry outputEntry = output.makeEntry(target+"/add/"+addedcontent[1][i].getName());
				output.putNextEntry(outputEntry);
			}
			System.out.println("added files");
			for(int i=0; i<addedcontent[0].length; i++)
			{	
				IEntry outputEntry = output.makeEntry(target+"/add/"+addedcontent[0][i].getName());
				
				output.putNextEntry(outputEntry);
				
				IEntry in =addedcontent[0][i];
				int inSize = (int)in.getSize();
				byte[] inBytes = new byte[inSize];
				InputStream sourceStream = patchreader.getInputStream(in);
				
				//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
				for (int erg = sourceStream.read(inBytes); erg < inBytes.length; erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
				sourceStream.close();
				
				output.write(inBytes);
			}
			
			System.out.println("dodelete");
			if(dodelete)
			{
				System.out.println("deleted");
				for(int ptype=0; ptype<incontent.length; ptype++)
				{
					ArrayList<IEntry> deleted = new ArrayList<IEntry>();
					for(int i =0; i<incontent[ptype].length; i++)
					{
						boolean isdel = true;
						String curname = incontent[ptype][i].getName();
						for(int j =0; j<patchcontent[ptype].length && isdel; j++)
						{
							if(patchcontent[ptype][j].getName().equalsIgnoreCase(curname))
							{
								isdel=false;
							}
						}
						if(isdel)
						{
							deleted.add(incontent[ptype][i]);
						}
					}
					IEntry[] B = new IEntry[0];
					delcontent[ptype] = deleted.toArray(B);
				}
				
				System.out.println("deleted files");
				//files ...
				for(int i=0; i<delcontent[0].length; i++)
				{
					String name = delcontent[0][i].getName();
					System.out.println(name);
					if(name.equals("META-INF/MANIFEST.MF"))
					{
						continue; //not allowed to delete this for mac compatibility - don't create patch archives that say to delete it, don't obey patch archives that were manually created to say to
					}
					String sum = MD5Checksum.make(originalreader.getInputStream(delcontent[0][i]));
					deletions.put(name, sum);
				}
				System.out.println("deleted dirs");
				//..and then directories. this is because all files in a directory must be deleted before the directory can be deleted.
				for (int i=0; i<delcontent[1].length; i++)
				{
					String name = delcontent[1][i].getName();
					System.out.println(name);
					if(name.equals("META-INF/"))
					{
						continue; //see above about deletion
					}

					deletions.put(name, "directory");
				}
			}
			/*
			System.out.println("Added files:");
			for(int i=0; i<addedcontent[0].length;i++)
			{
				System.out.println(addedcontent[0][i].getName());
			}
			
			System.out.println("Deleted files:");
			for(int i=0; i<delcontent[0].length;i++)
			{
				System.out.println(delcontent[0][i].getName());
			}
			*/
			
			
			System.out.println("patches:");
			for(int i=0; i<sharedcontent[0].length;i++)
			{
				String incheck = MD5Checksum.make(originalreader.getInputStream(sharedcontent[0][i][1]));
				String patchcheck = MD5Checksum.make(patchreader.getInputStream(sharedcontent[0][i][0]));
				boolean differs = !incheck.equals(patchcheck);
				//System.out.println(sharedcontent[0][i][0].getName() + ": " + (differs ? "differs" : "equals"));
				if(differs)
				{
					//TODO: generic IO, so that gzip out and flat folder in works too
					IEntry in =sharedcontent[0][i][1];
					IEntry pat = sharedcontent[0][i][0];
					String pfname = target + "/patch/" + in.getName();
					
					
					String extension = ".gdiff";
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					DiffWriter diffWriter = new GDiffWriter(new DataOutputStream(outputStream));
					Delta d = new Delta();
					
					int inSize = (int)in.getSize();
					byte[] inBytes = new byte[inSize];
					InputStream sourceStream = originalreader.getInputStream(in);
					
					//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
					for (int erg = sourceStream.read(inBytes); erg < inBytes.length; erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
					sourceStream.close();
					
					d.compute(inBytes, patchreader.getInputStream(pat), diffWriter);
					diffWriter.close();
					
					IEntry outputEntry = output.makeEntry(pfname + extension);
					outputEntry.setTime(pat.getTime());
					output.putNextEntry(outputEntry);
					output.write(outputStream.toByteArray());
					patches.put(in.getName(), extension+" "+incheck+" "+patchcheck);
				}
			}
			System.out.println("patch.properties:");
			patches.store(System.out, "");
			
	
			IEntry patchprop = output.makeEntry(target + "/patch/patch.properties");
			output.putNextEntry(patchprop);
			patches.store(output.getOStream(), "");
			
			IEntry delprop = output.makeEntry(target + "/delete.properties");
			output.putNextEntry(delprop);
			deletions.store(output.getOStream(), "");
	}
	
	public static void main(String[] args) {
		
		//System.getProperty("user.home");
		
		//makepatch
		if(args.length < 3 || args.length > 5)
		{
			System.out.println("require exactly five arguments:");
			System.out.println("... Patcher make TARGET ORIGINALZIP ALTEREDZIP PATCHZIP");
			System.out.println("... Patcher apply TARGET ORIGINALZIP PATCHZIP ALTEREDZIP");
			System.out.println("last argument is always the output.");
			System.out.println("probably best to provide 'test' as the target for now.");
			return;
		}
		
		try {
			if(args[0].equalsIgnoreCase("make"))
			{
				String target = args[1];
				File original = new File(args[2]); 
				File patch = new File(args[3]);
				File altered = new File(args[4]);
				IDirOutputStream outwriter = null;
				if((!args[4].endsWith("/")) && !altered.isDirectory())
					outwriter = new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(altered)));
				else
					outwriter = new DirOutputStream(altered);
				
				IArchive inreader = null;
				if(original.isFile())
					inreader = new ZipArchive(original);
				else
					inreader = new DirArchive(original);
				IArchive patchreader = null;
				if(patch.isFile())
					patchreader = new ZipArchive(patch);
				else
					patchreader = new DirArchive(patch);
				
				makepatch(target,inreader, patchreader, outwriter, true);
				
				outwriter.close();
				inreader.close();
				patchreader.close();
			}
			else if(args[0].equalsIgnoreCase("package"))
			{
				File minecraftdir = new File(Util.getAppDir("minecraft")+"/");
				
				File patcha = null;
				if (!args[1].equals("-"))
					patcha = new File(args[1]);
				
				File patchb = null;
				if (args.length == 4 && !args[2].equals("-"))
					patchb = new File(args[2]);
				
				IArchive patchreader = null;
				
				
				File altered = new File(args[args.length-1]);
				IDirOutputStream outwriter = new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(altered)));
				
				IArchive inreader = new DirArchive(minecraftdir);
				
				
				if(patcha != null)
				{
					if(patcha.isFile())
						patchreader = new ZipArchive(patcha);
					else
						patchreader = new DirArchive(patcha);
					makepatch("main",inreader, patchreader, outwriter, false);
					patchreader.close();
				}
				
				
				if(patchb != null)
				{
					if(patchb.isFile())
						patchreader = new ZipArchive(patchb);
					else
						patchreader = new DirArchive(patchb);
					makepatch("mcjar",new ZipArchive(new File(minecraftdir, "bin/minecraft.jar")), patchreader, outwriter, false);
					patchreader.close();
				}
				outwriter.close();
				inreader.close();
			}
			else if(args[0].equalsIgnoreCase("apply"))
			{
				String target = args[1];
				File original = new File(args[2]); 
				File patch = new File(args[3]);
				File altered = new File(args[4]);
				IDirOutputStream outwriter = null;
				if((!args[4].endsWith("/")) && !altered.isDirectory())
					outwriter = new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(altered)));
				else
					outwriter = new DirOutputStream(altered);
				
				IArchive inreader = null;
				if(original.isFile())
					inreader = new ZipArchive(original);
				else
					inreader = new DirArchive(original);
				IArchive patchreader = null;
				if(patch.isFile())
					patchreader = new ZipArchive(patch);
				else
					patchreader = new DirArchive(patch);
				
				
				try {
					applypatch(target,inreader, patchreader, outwriter, true);
				} catch (PatchConflict e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FormatError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outwriter.close();
				inreader.close();
				patchreader.close();
			}
			else
			{

				System.out.println("you silly, there is no command '"+args[0]+"'");
			}
			
			
			
			
			
			//patchreader.close();
			//inreader.close();
			
			
			
			
			/*
			for(int i=0; i<patchcontent[0].length; i++)
			{
				String thisfile = patchcontent[0][i];
				
				if(thisfile.startsWith("add/"))
				{
					System.out.println("add: " + thisfile.substring(3));
					addfiles.put(thisfile, thisfile.substring(3));
				}
				else if(thisfile.startsWith("remove/"))
				{
					System.out.println("del: " + thisfile.substring(6));
					delfiles.put(thisfile, thisfile.substring(6));
				}
				else if(thisfile.startsWith("patches/"))
				{
					System.out.println("patch: " + thisfile.substring(7));
					patchfiles.put(thisfile, thisfile.substring(7));
				}
			}
			
			
			
			Properties mod_info = new Properties();
			mod_info.load(reader.getInputStream(reader.getEntry("mod.properties")));
			String modname = (String) mod_info.get("name");
			String modversion = (String) mod_info.get("modversion");
			String modmcversion = (String) mod_info.get("modversion");
			
			
			
			
				
			System.out.println(modname);
			
			*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		/*
		if(args.length == 4 && args[0] == "make")
		{
			JarDelta patchmaker = new JarDelta();
		
			try {
				patchmaker.computeDelta(new IArchive(args[0]), new IArchive(args[1]), new IDirOutputStream(new FileOutputStream(args[2])));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (args.length == 4 && args[0] == "patch")
		{
			try {
				new JarPatcher().applyDelta(new IArchive(args[0]), new IArchive(args[1]), new IDirOutputStream(new FileOutputStream(args[2])));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		
		
		//ja
		/*
		try {
			
			ClassPool pool = ClassPool.getDefault();
			
			CtClass DestClass = pool.get("Dest");
			CtClass SrcClass = pool.get("Src");
			//boolean removestate;
			CtMethod DestMethod = null;
			String MethodName = null;
			
			CtConstructor[] newconsts = SrcClass.getConstructors();
			
			CtMethod[] newmeths =  SrcClass.getMethods();
			
			CtField[] newfields = SrcClass.getFields();
			
			
			CtConstructor[] oldconsts = DestClass.getConstructors();
			
			CtMethod[] oldmeths =  DestClass.getMethods();
			
			CtField[] oldfields = DestClass.getFields();
			ClassMap newtoold = new ClassMap();
			newtoold.put(SrcClass, DestClass);
			//initializer
			//annotations
			//interfaces
			//nested classes
			//attributes
			
			//SrcClass.
			///
			System.out.println("constructors");
			for(int i=0; i<newconsts.length;i++)
			{
				System.out.println(newconsts[i].getSignature());
				boolean replaced = false;
				for(int j=0; j<oldconsts.length; j++)
				{
					if(!replaced && newconsts[i].getSignature().equals(oldconsts[j].getSignature()))
					{
						oldconsts[j].setBody(newconsts[i], newtoold);
						replaced = true;
					}
				}
				if(!replaced)
				{
					DestClass.addConstructor(new CtConstructor(newconsts[i], DestClass, newtoold));
				}
					
			}

			System.out.println("methods");
			for(int i=0; i<newmeths.length;i++)
			{
				System.out.println(newmeths[i].getSignature());
				boolean replaced = false;
				for(int j=0; j<oldmeths.length; j++)
				{
					if(!replaced && newmeths[i].getSignature().equals(oldmeths[j].getSignature()))
					{
						oldmeths[j].setBody(newmeths[i], newtoold);
						replaced = true;
					}
				}
				if(!replaced) 
				{
					DestClass.addMethod(new CtMethod(newmeths[i], DestClass, newtoold));
				}
					
			}
			for(int i=0; i<newfields.length; i++)
			{
				System.out.println(newfields[i].getName()+": "+newfields[i].getType().getName());
				//boolean replaced = false;
				for(int j=0; j<oldfields.length;j++)
				{
					if(newfields[i].getName().equals(oldfields[j].getName()))
					{
						DestClass.removeField(oldfields[j]);
						//DestClass.addField(new CtField(newfields[i]);
					}
				}
				
				DestClass.addField(new CtField(newfields[i], DestClass));
			}
			
			DestClass.writeFile();
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
