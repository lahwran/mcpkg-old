package mcpkg.targetting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;

public class DirOutputStream extends OutputStream implements IDirOutputStream {

	FileOutputStream ostream;
	File root;
	
	public DirOutputStream(File rootdir)
	{
		rootdir.mkdirs();
		if(rootdir.isDirectory())
		{
			root = rootdir;
		}
		else
			throw new IllegalArgumentException("rootdir of DirOutputStream must be a directory! try the zip implementation?");
		
	}
	
	@Override
	public void close() throws IOException {
		ostream.close();
	}

	@Override
	public java.io.OutputStream getOStream() {
		return ostream;
	}

	@Override
	public IEntry makeEntry(String name) {
		return new DirEntry(name);
	}

	@Override
	public void putNextEntry(IEntry Entry) throws IOException {
		if(ostream != null)
			ostream.close();
		File f = new File(root, ((DirEntry)Entry).location);
		if(((DirEntry)Entry).location.endsWith("/"))
		{
			f.mkdirs();
			ostream = null;
		}
		else
		{
			if (!f.exists())
			{
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			ostream = new FileOutputStream(f);
		}
	}

	@Override
	public void write(byte[] inbytes) throws IOException {
		ostream.write(inbytes);
	}

	@Override
	public void write(int inbyte) throws IOException {
		ostream.write(inbyte);
	}

	@Override
	public void write(byte[] inbytes, int off, int len) throws IOException {
		ostream.write(inbytes, off, len);

	}

}
