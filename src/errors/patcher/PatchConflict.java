package errors.patcher;

public class PatchConflict extends Exception {

	public String filename;
	
	public PatchConflict(String f, String string) {
		super(string);
		filename = f;
	}

	private static final long serialVersionUID = -3021220901102197378L;

}
