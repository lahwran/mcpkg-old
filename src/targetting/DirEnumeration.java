package targetting;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Enumeration;

public class DirEnumeration implements Enumeration<IEntry> {
	
	public DirArchive owner;
	ArrayDeque<File> directories = new ArrayDeque<File>();
	
	public int rootslice;
	
	public int curdirpos = 0;
	public ArrayDeque<File> curdir = new ArrayDeque<File>();
	
	public DirEnumeration(DirArchive _owner)
	{
		owner=_owner;
		addFiles(owner.directory.listFiles());
		rootslice = owner.directory.getPath().length();
	}
	
	@Override
	public boolean hasMoreElements() {
		//if curdir has more files, there are definitely more files
		//if directories has more dirs, there are definitely more dirs, and possibly more files
		return curdir.size() > 0 || directories.size() > 0;
	}
	
	private void addFiles(File[] files)
	{
		for(int i=0; i<files.length; i++)
			curdir.add(files[i]);
	}

	@Override
	public DirEntry nextElement() {
		if(!(curdir.size() > 0))
		{
			curdirpos=0;
			File d = null;
			if(directories.size() > 0)
				d = directories.removeFirst();
			else
				throw new IllegalArgumentException("called nextElement with no files left!");
			addFiles(d.listFiles());
			return new DirEntry(d.getPath().substring(rootslice+1),owner);
		}
		else
		{
			curdirpos=0;
			
			File f = curdir.removeFirst();
			while(f.isDirectory())
			{
				directories.add(f);
				if(curdir.size() > 0)
					f = curdir.removeFirst();
				else
					return nextElement();//recurse to go back to top half of if
			}
			return new DirEntry(f.getPath().substring(rootslice+1),owner);
		}
		
	}

}
