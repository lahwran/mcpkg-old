package mcpkg.targetting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public interface IArchive {
	public Enumeration<IEntry> entries();
	public IEntry getEntry(String location);
	public InputStream getInputStream(IEntry entry) throws IOException, FileNotFoundException;
	public void close() throws IOException;
}
