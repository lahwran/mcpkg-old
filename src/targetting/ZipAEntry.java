package targetting;

import java.util.zip.ZipEntry;

public class ZipAEntry implements IEntry {

	public ZipEntry e;
	
	public ZipAEntry(String arg0) {
		e = new ZipEntry(arg0);
	}

	public ZipAEntry(ZipAEntry arg0) {
		e = new ZipEntry(arg0.e);
	}
	
	public ZipAEntry(ZipEntry _e)
	{
		e = _e;
	}

	@Override
	public String getName() {
		return e.getName();
	}

	@Override
	public long getSize() {
		return e.getSize();
	}

	@Override
	public long getTime() {
		return e.getTime();
	}

	@Override
	public boolean isDirectory() {
		return e.isDirectory();
	}

	@Override
	public void setTime(long time) {
		e.setTime(time);
	}

}
