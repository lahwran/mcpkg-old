import java.util.Comparator;
import java.util.HashMap;

//once again I'm doing it wrong
public class SearchComparer implements Comparator<Package> {

	HashMap<Package,Integer> matches = null;
	@Override
	public int compare(Package o1, Package o2) {
		// TODO Auto-generated method stub
		return matches.get(o2)-matches.get(o1);
	}

}
