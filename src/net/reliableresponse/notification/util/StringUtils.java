/*
 * Created on Nov 10, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.util;

public class StringUtils {

	public static boolean isEmpty(String test) {
		if ((test == null) || (test.length() == 0)) {
			return true;
		}
		return false;
	}
	
	   public static String capitalize(String inputWord) {
	        //.. Process - Separate word into parts, change case, put together.
	        String firstLetter = inputWord.substring(0,1);  // Get first letter
	        String remainder   = inputWord.substring(1);    // Get remainder of word.
	        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();

	        //.. Output the result.
	        return capitalized;
	    }
	
	public static String htmlEscape(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;");
		string = string.replaceAll(">", "&gt;");
		string = string.replaceAll("\"", "&quot;");
		string = string.replaceAll("\n", "<br>");
		
		return string;
	}
	
	public static String replaceString (String template, String pattern, String replacement) {
		if (isEmpty(pattern)) return template;
		
		final StringBuffer result = new StringBuffer();

		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
	     int startIdx = 0;
	     int idxOld = 0;
	     while ((idxOld = template.indexOf(pattern, startIdx)) >= 0) {
	       // grab a part of aInput which does not include aOldPattern
	       result.append(template.substring(startIdx, idxOld) );
	       // add aNewPattern to take place of aOldPattern
	       result.append( replacement);

	       // reset the startIdx to just after the current match, to see
	       // if there are any further matches
	       startIdx = idxOld + pattern.length();
	     }
	     // the final chunk will go to the end of aInput
	     result.append( template.substring(startIdx) );
	     return result.toString();
	}
	
	public static String escapeForXML(String str) {
		StringBuffer newstr = new StringBuffer();
		int len = str.length();
		for (int i=0; i<len; i++) {
			char ch = str.charAt(i);
			switch (ch) {
				case '&': newstr.append("&amp;"); break;
				case '<': newstr.append("&lt;"); break;
				case '>': newstr.append("&gt;"); break;
				case '\'': newstr.append("&apos;"); break;
				case '"': newstr.append("&quot;"); break;
				default: newstr.append(ch);
			}
		}
		return newstr.toString();
	}

	public static String toHexString ( byte[] b )
	   {
	   StringBuffer sb = new StringBuffer( b.length * 2 );
	   for ( int i=0; i<b.length; i++ )
	      {
	      // look up high nibble char
	      sb.append( hexChar [( b[i] & 0xf0 ) >>> 4] );

	      // look up low nibble char
	      sb.append( hexChar [b[i] & 0x0f] );
	      }
	   return sb.toString();
	   }

//	 table to convert a nibble to a hex char.
	static char[] hexChar = {
	   '0' , '1' , '2' , '3' ,
	   '4' , '5' , '6' , '7' ,
	   '8' , '9' , 'a' , 'b' ,
	   'c' , 'd' , 'e' , 'f'};
	
	
	/**
	 * Implementation of the Levenshtein distance algorithm.  This is for
	 * alert aggregation.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
    
    public static int getInteger(String string, int defaultValue) {
    	if (StringUtils.isEmpty(string)) return defaultValue;
    	
    	try {
    		return Integer.parseInt(string);
    	} catch (NumberFormatException nfExc) {
    		return defaultValue;
    	}
    }
	
}
