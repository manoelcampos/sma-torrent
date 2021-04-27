package com.manoelcampos.smatorrent;

/**Class with functions to manipulate Strings
 * @author Manoel Campos da Silva Filho*/
public class StrUtils {
	public static String stringOfChar(final char c, final int count) {
		final StringBuffer res = new StringBuffer();
		for (int i = 0; i < count; i++)
			res.append(c);
		return res.toString();
	}
	
	/**Break a string into lines with maximum length of maxLineLenght.
	 * The function insert a line break after the next blank space
	 * found into the string, at every maxLineLength characters.
	 * 
	 * @param str The string to be split
	 * @param maxLineLength The maximum length of the string
	 * @return The split string  */
	public static String breakString(final String str, final int maxLineLength) {
		final StringBuffer sb = new StringBuffer();
		int pos = 0;
		for(int i = 0; i < str.length(); i++) {
			sb.append(str.charAt(i));
			if(++pos > maxLineLength) {
			   	if(str.charAt(i)==' ') {
			   	   sb.append('\n');
			   	   pos=0;
			   	}
			   	//if anyone blank char be found at the next 20 characters
			   	//after the maxLineLength, break string anyway.
			   	else if(Math.abs(pos-maxLineLength) > 20) {
			   	   sb.append('\n');
			   	   pos=0;
			   	}
			}
		}

		return sb.toString();
	}
	
	/**Generate a string only with digits from 0 to 9,
	 * randomly, with the length of len parameter.
	 * @param len The length of the random string to be generated
	 * @return A string with length len, with digits randomly
	 * generated.*/
	public static String randomStringNumbers(final int len) {
		String s="";
		for(int i=0; i < len; i++)
			s = s + (int)(Math.random()*10);

		return s;
	}
}
