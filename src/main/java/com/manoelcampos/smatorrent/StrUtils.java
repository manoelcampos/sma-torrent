package com.manoelcampos.smatorrent;

/**Class with functions to manipulate Strings
 * @author Manoel Campos da Silva Filho*/
public class StrUtils {
	public static String stringOfChar(char c, int count) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < count; i++)
			res.append(c);
		return res.toString();
	}
	
	/**Break a string into lines with maximum length of maxLineLenght.
	 * The function insert a line break after the next blank space
	 * found into the string, at every maxLineLength characters.
	 * 
	 * @param str The string to be breaked
	 * @param maxLineLenght The maximum length of the string
	 * @return The breaked string  */
	public static String breakString(String str, int maxLineLenght) {
		StringBuffer sb = new StringBuffer();
		int pos = 0;
		for(int i = 0; i < str.length(); i++) {
			sb.append(str.charAt(i));
			if(++pos > maxLineLenght) {
			   	if(str.charAt(i)==' ') {
			   	   sb.append('\n');
			   	   pos=0;
			   	}
			   	//if anyone blank char be found at the next 20 characters
			   	//after the maxLineLenght, break string anyway.
			   	else if(Math.abs(pos-maxLineLenght) > 20) {
			   	   sb.append('\n');
			   	   pos=0;
			   	}
			}
		}
		return sb.toString();
	}
	
	/**Generate a string only with digits from 0 to 9,
	 * randomically, with the length of len parameter.
	 * @param len The length of the random string to be generated
	 * @return A string with length len, with digits randomically
	 * generated.*/
	public static String randomStringNumbers(int len) {
		String s="";
		for(int i=0; i < len; i++)
			s = s + (int)(Math.random()*10);
		return s;
	}
}
