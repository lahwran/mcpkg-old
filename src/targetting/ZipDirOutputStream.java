package targetting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirOutputStream extends OutputStream implements
		IDirOutputStream {

	ZipOutputStream s;
	
	public ZipDirOutputStream(ZipOutputStream _s)
	{
		s=_s;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		s.close();
	}

	@Override
	public OutputStream getOStream() {
		return s;
	}

	@Override
	public IEntry makeEntry(String name) {
		// TODO Auto-generated method stub
		return new ZipAEntry(name);
	}

	@Override
	public void putNextEntry(IEntry entry) throws IOException {
		// TODO Auto-generated method stub
		s.putNextEntry(((ZipAEntry)entry).e);
	}

	@Override
	public void write(byte[] inbytes) throws IOException {
		s.write(inbytes);

	}

	@Override
	public void write(int inbyte) throws IOException {
		// TODO Auto-generated method stub
		s.write(inbyte);
	}
	
	@Override
	public void write(byte[] inbytes, int off, int len) throws IOException {
		s.write(inbytes, off, len);

	}

}
