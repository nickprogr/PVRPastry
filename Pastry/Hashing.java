package Pastry;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class Hashing {
    
    
    
    public static String SHA1_128bit (String text) {

        try{
            byte[] hash = SHA1(text);
            byte[] hash128 = new byte[17];

            hash128[0] = 0; //to avoid negative hexadecimals because of the possibility the first bit to be 1.

            for(int i=0;i<16;i++)
                hash128[i+1] = hash[i];
            
            return convertToHex(hash128);
        }
        catch(NoSuchAlgorithmException ne){
            System.err.println("There is not SHA-1 algorithm.");
        }
        catch(UnsupportedEncodingException ue){
            System.err.println("Encoding is not supported.");
        }

        return null;

    }
    
    
    
    
    private static byte[] SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {

        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");

        byte[] hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        
        hash = md.digest();
        
        return hash;

    }
    
    
    
    
    private static String convertToHex(byte[] hash) {

        StringBuffer sbuf = new StringBuffer();
        
        for (byte i = 0; i < hash.length; i++) {
            
            int halfbyte = (hash[i] >>> 4) & 0x0F;
            int two_halfs = 0;

            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    sbuf.append((char) ('0' + halfbyte));
                else
                    sbuf.append((char) ('a' + (halfbyte - 10)));

                halfbyte = hash[i] & 0x0F;
            } while(two_halfs++ < 1);
        }

        return sbuf.toString().substring(2);  //we want 32 digits (128 bits) but it returns 34 digits.
    }
    
    
}
