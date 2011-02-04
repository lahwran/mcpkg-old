package targetting;

import java.util.Enumeration;
import java.util.zip.ZipEntry;

public class ZipAEntryEnumeration implements Enumeration<IEntry> {
	public Enumeration e;
	
	public ZipAEntryEnumeration(Enumeration _e)
	{
		e=_e;
	}
	
	@Override
	public boolean hasMoreElements() {
		return e.hasMoreElements();
	}

	@Override
	public IEntry nextElement() {
		return new ZipAEntry((ZipEntry)e.nextElement());
	}

}
