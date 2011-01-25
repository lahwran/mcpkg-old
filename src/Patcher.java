import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.nothome.delta.Delta;
import com.nothome.delta.DiffWriter;
import com.nothome.delta.GDiffPatcher;
import com.nothome.delta.GDiffWriter;

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


public class Patcher {

	public static String getMcDir()
	{
		String s1 = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("linux") || os.contains("unix"))
        {
            return new StringBuilder().append(s1).append("/.minecraft/").toString();
        }
        else if(os.contains("windows"))
        {
            String s2 = System.getenv("APPDATA");
            if(s2 != null)
            {
            	return new StringBuilder().append(s2).append("/.minecraft/").toString();
            } else
            {
            	return new StringBuilder().append(s1).append("/.minecraft/").toString();
            }
        }
        else if (os.contains("mac"))
        {
            return s1+"Library/Application Support/minecraft/";
        }
        
        else
        {
            return s1+"/minecraft/";
        }
	}
	
	public static ZipEntry[][] readZip(ZipFile f) throws IOException
	{
		
		ArrayList<ZipEntry> files = new ArrayList<ZipEntry>();
		ArrayList<ZipEntry> directories = new ArrayList<ZipEntry>();

		
		
		
		for (Enumeration enumer = f.entries(); enumer.hasMoreElements(); )
		{
			ZipEntry entry = (ZipEntry)enumer.nextElement();
			if(entry.isDirectory())
			{
				directories.add(entry);
			}
			else
			{
				files.add(entry);
			}
		}
		ZipEntry[] A = new ZipEntry[0];
		return new ZipEntry[][]{files.toArray(A),directories.toArray(A)};
	}
	
	public static boolean applypatch(String target, ZipFile originalreader, ZipFile patchreader, ZipOutputStream output, boolean dodelete)
	{
		try
		{
			System.out.println("getting patch names ...");
			ZipEntry[][] patchcontent = readZip(patchreader);
	
			System.out.println("getting input names ...");
			ZipEntry[][] incontent = readZip(originalreader);
			
			Properties deletions = new Properties();
			Properties patches = new Properties();
			
			InputStream is = null;
			
			deletions.load(patchreader.getInputStream((patchreader.getEntry(target+"/delete.properties"))));
			patches.load(patchreader.getInputStream((patchreader.getEntry(target+"/patch/patch.properties"))));
			
			System.out.println("checking all md5sums");
			System.out.println("delkeys");
			String[] delkeys = deletions.keySet().toArray(new String[0]);
			for(int i=0; i<delkeys.length; i++)
			{
				String sum = deletions.getProperty(delkeys[i]);
				if (sum.equals("directory")) continue; //should verify that it will be empty? maybe...
				try{
					is = originalreader.getInputStream(originalreader.getEntry(delkeys[i]));
				} catch (FileNotFoundException e)
				{
					System.out.println("NOTE: file to be deleted '"+delkeys[i]+"' appears to already have been removed");
					continue;
				}
				if(!MD5Checksum.check(is,sum))
				{
					System.out.println("ERROR: MD5 for file to be deleted '"+delkeys[i]+"' does not match sum provided in patch");
					return false;
				}
			}
			System.out.println("patchkeys");
			String[] patchkeys = patches.keySet().toArray(new String[0]);
			for(int i=0; i<patchkeys.length; i++)
			{
				String[] inf = patches.getProperty(patchkeys[i]).split(" ");
				if(inf.length != 3)
				{
					System.out.println("ERROR: field count for patch for file '"+patchkeys[i]+"' is not three");
					return false;
				}
				try{
					is = originalreader.getInputStream(originalreader.getEntry(patchkeys[i]));
				} catch (FileNotFoundException e)
				{
					System.out.println("ERROR: file to be patched '"+patchkeys[i]+"' appears to be missing");
					return false;
				}
				String sum = MD5Checksum.make(is);
				if (sum.equals(inf[2])) //already matches target md5 - remove from list to be patches
				{
					patches.remove(patchkeys[i]);
				}
				else if(!sum.equals(inf[1])) //unclean - matches neither target nor source md5, abort
				{
					System.out.println("ERROR: file to be patched '"+patchkeys[i]+"' does not match target or source MD5sums");
					return false;
				} //clean! do the patch
				
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
					outname = outname.substring((target+"/add/").length()-1);
					System.out.println(outname);
					ZipEntry outputEntry = new ZipEntry(outname);
					output.putNextEntry(outputEntry);
				}
			}
			System.out.println("add old directories");
			for(int i=0; i<incontent[1].length; i++)
			{
				if(deletions.getProperty(incontent[1][i].getName(), "").equals("") || incontent[1][i].getName().toLowerCase().equals("meta-inf/"))
				{
					ZipEntry outputEntry = new ZipEntry(incontent[1][i].getName());
					output.putNextEntry(outputEntry);
				}
			}
				

			System.out.println("add new files");
			for(int i=0; i<patchcontent[0].length; i++)
			{
				if( patchcontent[0][i].getName().startsWith(target+"/add/"))
				{
					String outname = patchcontent[0][i].getName();
					outname = outname.substring((target+"/add/").length()-1);
					System.out.println(outname);
					ZipEntry outputEntry = new ZipEntry(outname);
					output.putNextEntry(outputEntry);
					outputEntry.setTime(patchcontent[0][i].getTime());
					
					
					ZipEntry in =patchcontent[0][i];
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
					continue; //deletion
				}
				else if (!patches.getProperty(incontent[0][i].getName(), "").equals(""))
				{
					String[] inf = patches.getProperty(incontent[0][i].getName()).split(" ");
					//already verified that inf will be 3 long at this point
					ZipEntry patch = patchreader.getEntry(target+"/patch/"+incontent[0][i].getName()+inf[0]);
					InputStream patchinput = patchreader.getInputStream(patch);
					ZipEntry outputEntry = new ZipEntry(incontent[0][i].getName());
					output.putNextEntry(outputEntry);
					
					ZipEntry in =incontent[0][i];
					int inSize = (int)in.getSize();
					byte[] inBytes = new byte[inSize];
					InputStream sourceStream = originalreader.getInputStream(in);
					
					//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
					for (int erg = sourceStream.read(inBytes); 
					erg < inBytes.length; 
					erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
					sourceStream.close();
					
					patcher.patch(inBytes, patchinput, output);
				}
				else
				{
					ZipEntry outputEntry = new ZipEntry(incontent[0][i].getName());
					output.putNextEntry(outputEntry);
					
					ZipEntry in =incontent[0][i];
					int inSize = (int)in.getSize();
					System.out.println(incontent[0][i].getName()+" "+inSize);
					if(inSize > 0)
					{
						byte[] inBytes = new byte[inSize];
						InputStream sourceStream = originalreader.getInputStream(in);
						
						System.out.println(""+(in==null)+" "+(inBytes==null));
	
						//this is icky, jd-gui generated it, I just hope it's some optimization and that the original wasn't icky like this
						for (int erg = sourceStream.read(inBytes); 
						erg < inBytes.length;
						erg += sourceStream.read(inBytes, erg, inBytes.length - erg));
						sourceStream.close();
						
						output.write(inBytes);
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static void makepatch(String target, ZipFile originalreader, ZipFile patchreader, ZipOutputStream output, boolean dodelete)
	{
		try
		{
			System.out.println("getting patch names ...");
			ZipEntry[][] patchcontent = readZip(patchreader);
	
			System.out.println("getting input names ...");
			ZipEntry[][] incontent = readZip(originalreader);
			
			ZipEntry[][] addedcontent = new ZipEntry[2][];
			ZipEntry[][][] sharedcontent = new ZipEntry[2][][];
			ZipEntry[][] delcontent = new ZipEntry[2][];
			
			
			Properties deletions = new Properties();
			Properties patches = new Properties();
			
			
			System.out.println("added and shared");
			for(int ptype=0; ptype<patchcontent.length; ptype++)
			{
				ArrayList<ZipEntry> added = new ArrayList<ZipEntry>();
				ArrayList<ZipEntry[]> shared = new ArrayList<ZipEntry[]>();
				for(int i = 0; i<patchcontent[ptype].length; i++)
				{
					boolean isnew = true;
					String curname = patchcontent[ptype][i].getName();
					for(int j =0; j<incontent[ptype].length && isnew; j++)
					{
						if(incontent[ptype][j].getName().equalsIgnoreCase(curname))
						{
							isnew=false;
							shared.add(new ZipEntry[]{patchcontent[ptype][i], incontent[ptype][j]});
						}
					}
					if(isnew)
					{
						added.add(patchcontent[ptype][i]);
					}
				}
				ZipEntry[][] A = new ZipEntry[0][];
				ZipEntry[] B = new ZipEntry[0];
				sharedcontent[ptype] = shared.toArray(A);
				addedcontent[ptype] = added.toArray(B);
			}
			System.out.println("added dirs");
			for(int i=0; i<addedcontent[1].length; i++)
			{	
				ZipEntry outputEntry = new ZipEntry(target+"/add/"+addedcontent[1][i].getName());
				output.putNextEntry(outputEntry);
			}
			System.out.println("added files");
			for(int i=0; i<addedcontent[0].length; i++)
			{	
				ZipEntry outputEntry = new ZipEntry(target+"/add/"+addedcontent[0][i].getName());
				
				output.putNextEntry(outputEntry);
				
				ZipEntry in =addedcontent[0][i];
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
					ArrayList<ZipEntry> deleted = new ArrayList<ZipEntry>();
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
					ZipEntry[] B = new ZipEntry[0];
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
					ZipEntry in =sharedcontent[0][i][1];
					ZipEntry pat = sharedcontent[0][i][0];
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
					
					ZipEntry outputEntry = new ZipEntry(pfname + extension);
					outputEntry.setTime(pat.getTime());
					output.putNextEntry(outputEntry);
					output.write(outputStream.toByteArray());
					patches.put(in.getName(), extension+" "+incheck+" "+patchcheck);
				}
			}
			System.out.println("patch.properties:");
			patches.store(System.out, "");
			
	
			ZipEntry patchprop = new ZipEntry(target + "/patch/patch.properties");
			output.putNextEntry(patchprop);
			patches.store(output, "");
			
			ZipEntry delprop = new ZipEntry(target + "/delete.properties");
			output.putNextEntry(delprop);
			deletions.store(output, "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		//System.getProperty("user.home");
		
		//makepatch
		if(args.length != 5)
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
				String originalzip = args[2];
				String alteredzip = args[3];
				String patchzip = args[4];
				ZipOutputStream outwriter = new ZipOutputStream(new FileOutputStream(patchzip));
				ZipFile inreader = new ZipFile(originalzip);
				ZipFile patchreader = new ZipFile(alteredzip);
				
				makepatch(target,inreader, patchreader, outwriter, true);
				
				outwriter.close();
				inreader.close();
				patchreader.close();
			}
			else if(args[0].equalsIgnoreCase("apply"))
			{
				String target = args[1];
				String originalzip = args[2]; 
				String patchzip = args[3];
				String alteredzip = args[4];
				ZipOutputStream outwriter = new ZipOutputStream(new FileOutputStream(alteredzip));
				ZipFile inreader = new ZipFile(originalzip);
				ZipFile patchreader = new ZipFile(patchzip);
				
				
				if(applypatch(target,inreader, patchreader, outwriter, true))
				{
					System.out.println("appears to have succeeded.");
				}
				else
				{
					System.out.println("drat.");
					return;
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
				patchmaker.computeDelta(new ZipFile(args[0]), new ZipFile(args[1]), new ZipOutputStream(new FileOutputStream(args[2])));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (args.length == 4 && args[0] == "patch")
		{
			try {
				new JarPatcher().applyDelta(new ZipFile(args[0]), new ZipFile(args[1]), new ZipOutputStream(new FileOutputStream(args[2])));
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
