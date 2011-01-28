import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class CompareVersion {

	static boolean debug = false;
	public static void main(String[] args) {
		try {
			BufferedReader inputStream =  new BufferedReader(new FileReader("versions.lst"));
			String l = "";
			while((l = inputStream.readLine()) != null)
			{
				System.out.println();
				String[] SplitTest = l.split(" ");
				System.out.println("beforetest: "+l+" will be checked against "+new Integer(SplitTest[2]) );
				int res = compare(SplitTest[0],SplitTest[1]);
				System.out.print("test: "+SplitTest[0]+" "+SplitTest[1]+" "+new Integer(SplitTest[2])+" "+(res == new Integer(SplitTest[2])? "==" : "!=") );
				System.out.println(" "+res);
				if(res != new Integer(SplitTest[2]))
				{
					debug = true;
					res = compare(SplitTest[0],SplitTest[1]);
					System.out.println(res);
					return;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void p(String... s)
	{
		if(!debug) return;
		for(int i=0; i<s.length; i++)
		{
			System.out.print(s[i]+" ");
		}
		System.out.println();
	}
	
	public static int compare(String A, String B) //1 means A > B, -1 means A < B
	{
		if(A.equals(B))
		{
			p("A equals B",A,B);
			return 0;
		}
		
		int epochA = 0;
		int epochB = 0;
		String[] SplitA = segment(A);
		String[] SplitB = segment(B);
		if(SplitA[0] != null)
		{
			epochA = new Integer(SplitA[0]);
		}
		if(SplitB[0] != null)
		{
			epochB = new Integer(SplitB[0]);
		}
		
		if(epochA < epochB)
		{
			p("epochA < epochB",""+epochA,""+epochB);
			return -1; 
		}
		else if (epochA > epochB)
		{
			p("epochA > epochB",""+epochA,""+epochB);
			return 1;
		}
		
		int upstream_compare = compareSubsection(SplitA[1], SplitB[1]);
		if(upstream_compare != 0)
			return upstream_compare;
		
		return compareSubsection(SplitA[2], SplitB[2]);
	}
	
	public static String[] segment(String tosplit)
	{
		String epoch = null;
		String upstream = null;
		String debian = null; //yeah, not actually debian, but it's a debian standard, what are you going to call it?
		
		String remaining = new String(tosplit);
		
		String[] epochsplit = remaining.split(":");
		if(epochsplit.length > 1)
		{
			remaining="";
			for(int i=1; i<epochsplit.length; i++)
			{
				if(i>1)
					remaining +=":";
				remaining += epochsplit[i];
			}
			epoch = epochsplit[0];
		}

		String[] debiansplit = remaining.split("-");
		if(debiansplit.length > 1)
		{
			remaining="";
			for(int i=0; i<debiansplit.length-1; i++)
			{
				if(i>0)
					remaining +="-";
				remaining += debiansplit[i];
			}
			debian = debiansplit[debiansplit.length-1];
		}
		
		upstream = remaining;
		
		if(!upstream.matches("^[a-zA-Z0-9.+_:~-]*$"))
		{
			throw new IllegalArgumentException("Invalid character in upstream version '"+upstream+"'");
		}
		if(debian == null)
			debian = "0"; //easier to just set it to 0 instead of 
		if(!debian.matches("^[a-zA-Z0-9+.~]*$"))
		{
			throw new IllegalArgumentException("Invalid character in debian version '"+debian+"'");
		}
		
		//System.out.println(epoch);
		//System.out.println(upstream);
		//System.out.println(debian);

		p("split:",epoch, upstream, debian);
		return new String[] {epoch, upstream, debian};
	}
	
	public static int compareSubsection(String A, String B)
	{
		int mode = 0; //0 = string, 1 = number
		p("compare subsection:",A, B);
		while(A.length() > 0 || B.length() > 0)
		{
			p("compare subsection mode",""+mode,A, B);
			int AOffset = 0;
			
			String regex = "";
			if(mode == 0)
			{
				regex = "^[^0-9]*$";
			}
			else
			{
				regex = "^[0-9]*$";
			}
			
			while(AOffset < A.length() && A.substring(0, AOffset+1).matches(regex))
			{
				AOffset++;
				
			}
			int BOffset = 0;
			while(BOffset < B.length() && B.substring(0, BOffset+1).matches(regex))
			{
				BOffset++;
				
			}

			String ASub = A.substring(0, AOffset);
			String BSub = B.substring(0, BOffset);
			
			A = A.substring(AOffset, A.length());
			B = B.substring(BOffset, B.length());
			p("compare subsection chomp",A,B,"subs:",ASub,BSub);
			if(mode == 0)
			{
				while(ASub.length() > 0 || BSub.length() > 0)
				{

					p("character compare",ASub,BSub);
					int av = 0;
					int bv = 0;
					if(ASub.length() > 0)
					{
						av = characterSortValue(ASub.substring(0, 1));
						if(ASub.length()-1 > 0)
							ASub = ASub.substring(1, ASub.length());
						else
							ASub = "";
					}
					if(BSub.length() > 0)
					{
						bv = characterSortValue(BSub.substring(0, 1));
						if(BSub.length()-1 > 0)
							BSub = BSub.substring(1, BSub.length());
						else
							BSub = "";
					}
					if (av < bv)
					{
						p("av < bv",""+av,""+bv);
						return -1;
					}
					else if(av > bv)
					{
						p("av > bv",""+av,""+bv);
						return 1;
					}
				}
			}
			else
			{
				p("integer compare");
				int av = 0;
				int bv = 0;
				if(!ASub.equals(""))
				{
					av = new Integer(ASub);
				}
				if(!BSub.equals(""))
				{
					bv = new Integer(BSub);
				}
				if (av < bv)
				{
					p("av < bv",""+av,""+bv);
					return -1;
				}
				else if(av > bv)
				{
					p("av > bv",""+av,""+bv);
					return 1;
				}
			}
			mode = (mode == 0 ? 1 : 0); //invert mode - this seems a little hard to read
			p("mode now",""+mode);
		}
		p("result = they are equal, returning 0");
		return 0;
	}
	public static int characterSortValue(String c)
	{
		if(c == null || c.equals(""))
			return 0;
		
		if(c.equals("~"))
		{
			return -1;
		}
		else if (c.matches("^\\d$"))
		{
			return new Integer(c) + 1; //should never ever ever happen... should we throw?
		}
		else if (c.matches("^[A-Za-z]$"))
		{
			return (int)c.toCharArray()[0];
		}
		else
		{
			return ((int)c.toCharArray()[0]) + 256;
		}
	}

}
