package targetting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipArchive implements IArchive {

	public ZipFile f;
	
	public ZipArchive(String arg0) throws IOException {
		f = new ZipFile(arg0);
	}

	public ZipArchive(File arg0) throws ZipException, IOException {
		f = new ZipFile(arg0);
	}

	public ZipArchive(File arg0, int arg1) throws IOException {
		f = new ZipFile(arg0, arg1);
	}

	@Override
	public InputStream getInputStream(IEntry entry) throws IOException, FileNotFoundException {
		return f.getInputStream(((ZipAEntry)entry).e);
	}
	
	public IEntry getEntry(String location)
	{
		return new ZipAEntry(f.getEntry(location));
	}

	@Override
	public void close() throws IOException {
		f.close();
		
	}

	@Override
	public Enumeration<IEntry> entries() {
		// TODO Auto-generated method stub
		return new ZipAEntryEnumeration(f.entries());
	}

}
