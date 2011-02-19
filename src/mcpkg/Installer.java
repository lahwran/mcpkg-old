package mcpkg;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import mcpkg.errors.installer.ModConflict;
import mcpkg.errors.patcher.FormatError;
import mcpkg.errors.patcher.PatchConflict;
import mcpkg.targetting.DirArchive;
import mcpkg.targetting.DirOutputStream;
import mcpkg.targetting.ZipArchive;
import mcpkg.targetting.ZipDirOutputStream;




public class Installer {

	public static final String[] targets = new String[]{"mcjar", "main"};
	public static boolean run() throws FileNotFoundException, IOException, ZipException, FormatError, ModConflict
	{

		String mcvers = Util.getCachedMinecraftVersion();
		boolean shouldwarn = false;
		StringBuilder warning = new StringBuilder("Warning! these packages are not compatible with minecraft "+mcvers+":\n");
		int count = 0;
		for (int i=0; i<Queue.thequeue.size(); i++)
		{
			Package p = Queue.thequeue.get(i);
			if (!p.MCVersion.equals(mcvers))
			{
				shouldwarn = true;
				warning.append(p.Name);
				warning.append(" ");
				warning.append(p.MCVersion);
				warning.append("/");
				warning.append(p.Version);
				if(count%2 == 0) warning.append("    ");
				else warning.append("\n");
				count++;
			}
		}
		warning.append("\n\ndo you want to continue launch?");
		if (shouldwarn)
		{
			if(!Messaging.confirm(warning.toString()))
			{
				return false;
			}
		}
		
		
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();
		
		File minecraftdir = new File(Util.getAppDir("minecraft")+"/");
		
		File backupdir = new File(appdir,"backups/"+Util.getCachedMinecraftVersion());
		if(!backupdir.exists())
		{
			Messaging.message("Making backup ...");
			Util.copyFiles(minecraftdir, backupdir);
		}
		HashMap<String, ArrayList<String>> filechanges = new HashMap<String, ArrayList<String>>();
		
		File in = backupdir;
		File out = new File(appdir, "tmp1");
		int target = 1;
		//TODO: I think this will break when no packages are selected?
		for(int i=0; i<Queue.thequeue.size(); i++)
		{
			Messaging.message(Queue.thequeue.get(i).Name+"...");
			System.out.println("about to install: "+Queue.thequeue.get(i).Name);
			System.out.println("in/ot");
			System.out.println(in.getPath());
			System.out.println(out.getPath());
			if(out.exists())
				Util.deleteDirMean(out); 
			out.mkdirs();
			
			Package thispackage = Queue.thequeue.get(i);
			thispackage.cache();
			ZipArchive patch = new ZipArchive(new File(cachedir, thispackage.getCachename()));
			String curtarget = "";
			try {
				
				
				//apply to .minecraft
				curtarget = "main"; //for error handling, because we need to know which filechanges element to get
				Patcher.applypatch(curtarget, new DirArchive(in), patch, new DirOutputStream(out), true);
				
				//then to minecraft.jar
				curtarget = "mcjar";
				ZipArchive a = new ZipArchive(new File(in,"bin/minecraft.jar"));
				ZipDirOutputStream b =  new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(new File(out, "bin/minecraft.jar"))));
				Patcher.applypatch(curtarget, a, patch, b, true);
				a.close();
				b.close();
			} catch (PatchConflict e) {
				String ch = "";
				ArrayList<String> chA = filechanges.get(curtarget+":"+e.filename);
				if(chA == null)
				{
					throw new ModConflict("File '"+e.filename+"' conflicts with vanilla");
				}
				for(int j=0; j<chA.size(); j++)
				{
					if(j>0)
						ch += " ";
					ch += chA.get(0);
				}
				throw new ModConflict("File '"+e.filename+"' conflicts with previous changes: "+ch);
			}
			String modname = thispackage.Name;
			for(int targetnumber=0; targetnumber<targets.length;targetnumber++)
			{
				String[] patchfiles = Patcher.listPatch(targets[targetnumber], patch);
				for(int l=0; l<patchfiles.length; l++)
				{
					String key = targets[targetnumber];
					key += ":";
					key += patchfiles[l].substring(1);
					ArrayList<String> patches = filechanges.get(key);
					if(patches == null)
						patches = new ArrayList<String>();
					patches.add(patchfiles[l].substring(0,1)+modname);
					filechanges.put(key, patches);
				}
			}
			if(i+1 < Queue.thequeue.size()) //we don't want to swap them if we're done, because we still have to apply it to .minecraft
			{
				if (target == 1)
				{
					target = 2;
					in = new File(appdir, "tmp1");
					out = new File(appdir, "tmp2");
					
				}
				else
				{
					target = 1;
					in = new File(appdir, "tmp2");
					out = new File(appdir, "tmp1");
				}
			}
		}
		
		if(Queue.thequeue.size() == 0)
		{
			Messaging.message("No mods in queue...");
			out = minecraftdir;
		}
		else
		{
			in = out;
			out = minecraftdir;
		}
		System.out.println("about to finalize");
		System.out.println("in/ot");
		System.out.println(in.getPath());
		System.out.println(out.getPath());
		Util.copyFilesMean(in, out);
		in = new File(appdir, "tmp2");
		out = new File(appdir, "tmp1");
		System.out.println("about to clean up");
		System.out.println("in/ot");
		System.out.println(in.getPath());
		System.out.println(out.getPath());
		if(out.exists())
			Util.deleteDirMean(out); 
		if(in.exists())
			Util.deleteDirMean(in); 
		
		return true;
		
		
	}
}
