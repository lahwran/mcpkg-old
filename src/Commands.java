import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

//proxy class - interfaces to the package manager (gui, commandline, etc) may only use this class


public class Commands {

	public static String[] getRepos()
	{
		Index.loadrepos();
		return Index.mainrepos;
	}
	
	public static void disableRepo(String id)
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		File appdir = new File(Index.getAppDir("mcpkg")+"/");
		appdir.mkdirs();//won't do anything if it's not needed
		File repolist = new File(appdir,"repos.lst");
		//TODO: should cache a hash of the file, reload only if it changed, that way when this function is called a lot (which it will be) it will not do anything when unneeded.
		FileInputStream f1 = null;
		try {
			f1 = new FileInputStream(repolist);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader f2 = new InputStreamReader(f1);
		BufferedReader f3 = new BufferedReader(f2);
		String lookedup = null;
		try {
			lookedup = Index.mainrepos[new Integer(id)];
		} catch (NumberFormatException e){}
		try {
			String line = null;
			while ((line = f3.readLine()) != null)
			{
				if(line.equals(id) || (lookedup != null && line.equals(lookedup)))
					lines.add("#"+line);
				else
					lines.add(line);
			}
			
			f3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			FileOutputStream fo = new FileOutputStream(repolist);
			
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			
			BufferedWriter writer = new BufferedWriter(osw); /// WHY can't I just do File.write()??? I mean seriously? 
			
			for(int i=0; i<lines.size();i++)
			{
				writer.write(lines.get(i)+"\n");
			}
			
			writer.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Index.loadrepos();
	}
}
