package mcpkg.errors.dependency;

import mcpkg.Package;

public class UnsolvableConflict extends Exception {
	
	public Package a;
	public Package b;
	
	
	public UnsolvableConflict(Package node, Package package1, String message)
	{
		super(message);
		a=node;
		b=package1;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = -1669307403177347107L;
	
}
