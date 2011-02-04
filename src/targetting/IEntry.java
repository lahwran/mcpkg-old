package targetting;

public interface IEntry {
	public boolean isDirectory();
	public String getName();
	public long getSize();
	public long getTime();
	public void setTime(long time);
}
