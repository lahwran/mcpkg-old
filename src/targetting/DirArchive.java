package targetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class DirArchive implements IArchive {

	public File directory;
	
	@Override
	public void close() throws IOException {
		//nothing to do here
	}

	@Override
	public Enumeration<IEntry> entries() {
		return new DirEnumeration(this);
	}

	@Override
	public IEntry getEntry(String location) {
		DirEntry e = new DirEntry(location, this);
		if(!(new File(directory, location)).exists())
			return null;
		return e;
	}

	@Override
	public InputStream getInputStream(IEntry entry) throws IOException,
			FileNotFoundException {
		FileInputStream f1 = null;
		
		f1 = new FileInputStream(new File(directory, ((DirEntry)entry).location));
		return f1;
	}
	
	public DirArchive(File dir)
	{
		if (dir.isDirectory())
		{
			directory = dir;
		}
		else
		{
			throw new IllegalArgumentException("cannot create DirArchive from non-directory file '"+dir+"'");
		}
	}
	
	public DirArchive(String dir)
	{
		File _dir = new File(dir);
		if (_dir.isDirectory())
		{
			directory = _dir;
		}
		else
		{
			throw new IllegalArgumentException("cannot create DirArchive from non-directory file '"+dir+"'");
		}
	}

}
