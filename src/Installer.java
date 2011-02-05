import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import targetting.DirArchive;
import targetting.DirOutputStream;
import targetting.ZipArchive;
import targetting.ZipDirOutputStream;


public class Installer {
	public static void run()
	{
		File appdir = new File(Util.getAppDir("mcpkg")+"/");
		File cachedir = new File(Util.getAppDir("mcpkg")+"/cache/");
		cachedir.mkdirs();
		appdir.mkdirs();
		
		File minecraftdir = new File(Util.getAppDir("minecraft")+"/");
		
		File backupdir = new File(appdir,"backups/"+Util.getMinecraftVersion());
		if(!backupdir.exists())
		{
			try {
				Patcher.copyFiles(minecraftdir, backupdir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File in = backupdir;
		File out = new File(appdir, "tmp1");
		int target = 1;
		
		for(int i=0; i<Queue.thequeue.size(); i++)
		{
			ZipArchive patch = null;
			try {
				patch = new ZipArchive(new File(cachedir, Queue.thequeue.get(i).getCachename()));
			} catch (ZipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Patcher.applypatch("main", new DirArchive(in), patch, new DirOutputStream(out), true);
			try {
				Patcher.applypatch("minecraftjar", new ZipArchive(new File(in,"bin/minecraft.jar")), patch, new ZipDirOutputStream(new ZipOutputStream(new FileOutputStream(new File(out, "bin/minecraft.jar")))), true);
			} catch (ZipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(i == Queue.thequeue.size()-2) //next one is -1
			{
				in = out;
				out = minecraftdir;
				Patcher.deleteDir(out); //clear it 
			}
			else if (target == 1)
			{
				target = 2;
				in = new File(appdir, "tmp1");
				out = new File(appdir, "tmp2");
				if(out.exists())
					Patcher.deleteDir(out); 
				
			}
			else
			{
				target = 1;
				in = new File(appdir, "tmp2");
				out = new File(appdir, "tmp1");
				if(out.exists())
					Patcher.deleteDir(out); 
			}
		}
		in = new File(appdir, "tmp2");
		out = new File(appdir, "tmp1");
		if(out.exists())
			Patcher.deleteDir(out); 
		if(in.exists())
			Patcher.deleteDir(in); 
		
	}
}
