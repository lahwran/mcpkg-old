package mcpkg;
import java.util.ArrayList;

import mcpkg.errors.dependency.UnsolvableConflict;



public class Dependency {
	
	public static void resolve(Package node, ArrayList<Package> originalstate, ArrayList<Package> resolved, ArrayList<Package> conflicts) throws UnsolvableConflict
	{
		resolve(node, originalstate, resolved, new ArrayList<Package>(), conflicts, true);
	}
	
	//this has all kinds of problems with unforseen states
	//for instance, what if package X conflicts with to-be-marked package Y>=1.5; package Z depends on X, package A is depends on Z and Y but doesn't care about the version. this will cause it to think that it is an unsolvable state. 
	public static void resolve(Package node, ArrayList<Package> originalstate, ArrayList<Package> resolved, ArrayList<Package> unresolved, ArrayList<Package> conflicts, boolean overwriteprovides) throws UnsolvableConflict
	{
		unresolved.add(node);
		
		
		if(node.Conflicts != null)
			doconflicts(node, node.Conflicts, originalstate, resolved, unresolved, conflicts);
		if(overwriteprovides)
		{
			for(int i=0; i<originalstate.size(); i++)
				if(originalstate.get(i).Provides != null)
					for(int providenum = 0; providenum < originalstate.get(i).Provides.length; providenum++)
						if(originalstate.get(i).Provides[providenum].test(node))
						{
							remove(originalstate.get(i), originalstate, conflicts);
							ArrayList<Package> empty = new ArrayList<Package>();
							remove(originalstate.get(i), resolved, empty);
							if(empty.size() != 0)
								throw new UnsolvableConflict(empty.get(empty.size()-1), originalstate.get(i), "Package "+empty.get(empty.size()-1)+" removal-conflicts with package "+originalstate.get(i)+" which was selected in dependency recursion.");
						}
		}
		else
		{
			for(int i=0; i<originalstate.size(); i++)
				if(originalstate.get(i).Provides != null)
					for(int providenum = 0; providenum < originalstate.get(i).Provides.length; providenum++)
						if(originalstate.get(i).Provides[providenum].test(node))
						{
							unresolved.remove(node);
							return;
						}
		}
			
			
			
		if(node.Depends!=null)
			for(int i=0; i<node.Depends.length; i++)
			{
				Package p = node.Depends[i].get();
				if(!resolved.contains(p))
				{
					if(unresolved.contains(p))
						continue;
					resolve(p, originalstate, resolved, unresolved, conflicts, false);
				}
			}
		if(node.Recommends!=null)
			for(int i=0; i<node.Recommends.length; i++)
			{
				Package p = node.Recommends[i].get();
				if(!resolved.contains(p))
				{
					if(unresolved.contains(p))
						continue;
					resolve(p, originalstate, resolved, unresolved, conflicts, false);
				}
			}
		resolved.add(node);
		unresolved.remove(node);
	}
	
	public static void remove(Package node, ArrayList<Package> originalstate, ArrayList<Package> toremove)
	{
		remove(node, originalstate, new ArrayList<Package>(), toremove);
	}
	
	public static void remove(Package node, ArrayList<Package> originalstate, ArrayList<Package> recursing, ArrayList<Package> toremove )
	{
		recursing.add(node);
		for(int i=0; i<originalstate.size(); i++)
		{
			Package thispkg = originalstate.get(i);
			if(thispkg.Depends != null)
			{
				for(int j=0; j<thispkg.Depends.length; j++)
				{
					if(thispkg.Depends[j].test(node) && !toremove.contains(thispkg) && !recursing.contains(thispkg))
					{
						remove(thispkg, originalstate, recursing, toremove);
					}
				}
			}
		}
		toremove.add(node);
		recursing.remove(node);
		
		
	}
	
	public static void doconflicts(Package node, PackageCompare[] Conflicts, ArrayList<Package> originalstate, ArrayList<Package> resolved, ArrayList<Package> unresolved, ArrayList<Package> conflicted) throws UnsolvableConflict
	{
		for(int conflictnum=0; conflictnum<Conflicts.length; conflictnum++)
		{
			for(int i=0; i<originalstate.size(); i++)
			{
				if(conflicted.contains(originalstate.get(i)))
				{
					continue;
				}
				if(Conflicts[conflictnum].test(originalstate.get(i)))
				{
					remove(originalstate.get(i),originalstate,conflicted);
					ArrayList<Package> empty = new ArrayList<Package>();
					remove(originalstate.get(i), resolved, empty);
					if(empty.size() != 0)
						throw new UnsolvableConflict(empty.get(empty.size()-1), originalstate.get(i), "Package "+empty.get(empty.size()-1)+" removal-conflicts with package "+originalstate.get(i)+" which was selected in dependency recursion.");
				}
			}
			for(int i=0; i<resolved.size(); i++)
			{
				if(Conflicts[conflictnum].test(resolved.get(i)))
				{
					throw new UnsolvableConflict(node, resolved.get(i), "Package "+node.Name+" conflicts with package "+resolved.get(i).Name+" which was selected in dependency recursion.");
				}
			}
		}
	}
	
}
