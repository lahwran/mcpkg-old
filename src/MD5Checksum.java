import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

   public static byte[] createChecksum(InputStream fis) throws IOException
   {

     byte[] buffer = new byte[1024];
     MessageDigest complete = null;
	try {
		complete = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
		// won't happen ... well if it does screw it it's a loser java install
		e.printStackTrace();
	}
     int numRead; 
     do {
      numRead = fis.read(buffer);
      if (numRead > 0) {
        complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);
     fis.close();
     return complete.digest();
   }

   // see this How-to for a faster way to convert
   // a byte array to a HEX string
   public static String make(InputStream fis) throws IOException {
     byte[] b = createChecksum(fis);
     String result = "";
     for (int i=0; i < b.length; i++) {
       result +=
          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
      }
     return result;
   }
   
   public static boolean check(InputStream fis, String sum) throws IOException
   {
	    
	    return make(fis).equals(sum);
   }
   
   public static boolean check(InputStream a, InputStream b) throws IOException
   {
	   return make(a).equals(make(b));
   }
}