import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class CompareVersion {

	public static void main(String[] args) {
		try {
			BufferedReader inputStream =  new BufferedReader(new FileReader("versions.lst"));
			String l = "";
			while((l = inputStream.readLine()) != null)
			{
				System.out.println();
				String[] SplitTest = l.split(" ");
				int res = compare(SplitTest[0],SplitTest[1]);
				System.out.print("test: "+l+(res == new Integer(SplitTest[2])? "==" : "!=") );
				System.out.println(" "+res);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int compare(String A, String B) //1 means A > B, -1 means A < B
	{
		if(A.equals(B))
			return 0;
		
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
			return -1; 
		}
		else if (epochA > epochB)
		{
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
		
		if(!upstream.matches("^[a-zA-Z0-9.+-:~]*$"))
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
		
		return new String[] {epoch, upstream, debian};
	}
	
	public static int compareSubsection(String A, String B)
	{
		int mode = 0; //0 = string, 1 = number
		while(A.length() > 0 || A.length() > 0)
		{
			int AOffset = 0;
			while(AOffset+1 < A.length() && A.substring(0, AOffset+1).matches("^["+(mode == 0 ? "^" : "")+"0-9]*$"))
			{
				AOffset++;
				
			}
			int BOffset = 0;
			while(BOffset+1 < B.length() && B.substring(0, BOffset+1).matches("^["+(mode == 0 ? "^" : "")+"0-9]*$"))
			{
				BOffset++;
				
			}

			String ASub = A.substring(0, AOffset);
			String BSub = B.substring(0, BOffset);
			
			A = A.substring(AOffset, A.length());
			B = B.substring(BOffset, B.length());
			if(mode == 0)
			{
				while(ASub.length() > 0 || BSub.length() > 0)
				{
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
						return -1;
					}
					else if(av > bv)
					{
						return 1;
					}
				}
			}
			else
			{
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
					return -1;
				}
				else if(av > bv)
				{
					return 1;
				}
			}
			mode = (mode == 0 ? 1 : 0); //invert mode - this seems a little hard to read
		}
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
