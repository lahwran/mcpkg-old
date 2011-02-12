package mcpkg.targetting;

import java.io.File;

public class DirEntry implements IEntry {

	public String location;
	public File root;
	
	public DirEntry(String _location) {
		location = _location;
	}
	public DirEntry(String _location, DirArchive _owner) {
		location=_location;
		root = _owner.directory;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		try {
			File f = new File(root, location);
			return location + (f.isDirectory() ? "/" : "");
		} catch(NullPointerException e) {
			throw new IllegalArgumentException("Can't call getName on a DirEntry with no owner!");
		}
	}

	@Override
	public long getSize() {
		try {
			return new File(root, location).length();
		} catch(NullPointerException e) {
			throw new IllegalArgumentException("Can't call getSize on a DirEntry with no owner!");
		}
	}

	@Override
	public long getTime() {
		// TODO Auto-generated method stub
		try {
			return new File(root, location).lastModified();
		} catch(NullPointerException e) {
			throw new IllegalArgumentException("Can't call getTime on a DirEntry with no owner!");
		}
	}

	@Override
	public boolean isDirectory() {
		// TODO Auto-generated method stub
		try {
			return new File(root, location).isDirectory();
		} catch(NullPointerException e) {
			throw new IllegalArgumentException("Can't call isDirectory on a DirEntry with no owner!");
		}
	}

	@Override
	public void setTime(long time) {
		try {
			new File(root, location).setLastModified(time);
		} catch(NullPointerException e) {
			throw new IllegalArgumentException("Can't call setTime on a DirEntry with no owner!");
		}
	}

	
	
	
}
