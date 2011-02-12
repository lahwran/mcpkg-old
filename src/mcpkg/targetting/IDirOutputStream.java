package mcpkg.targetting;

import java.io.IOException;
import java.io.OutputStream;

public interface IDirOutputStream {
	public IEntry makeEntry(String name);
	public void putNextEntry(IEntry e) throws IOException;
	public void write(byte[] inbytes) throws IOException;
	public void write(int inbyte) throws IOException;
	public void write(byte[] inbytes, int off, int len) throws IOException;
	
	
	public OutputStream getOStream();
	
	public void close() throws IOException;
}
