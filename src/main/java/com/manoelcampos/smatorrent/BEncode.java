package com.manoelcampos.smatorrent;

import java.util.*;
import java.io.*;

/**Class that represents, load, and parse 
 * a b-encoded data format, used in Torrent files,
 * for example.
 * @author Manoel Campos da Silva Filho*/
public class BEncode {
	/**Returns a string in b-encode format, like length:string<br/>
	 * Example: 6:Manoel
	 * 
	 * @param s The string to be b-encoded
	 * @return The string b-encoded
	 * @see BEncode#binteger(long)*/
	public static String bstring(final String s) {
		return s.length() + ":" + s;
	}
	
	/**Returns a integer/long value in b-encode format, like iINTEGER_VALUEe<br/>
	 * Example: i1234e
	 * @param l The integer/long value to be b-encoded
	 * @return The integer/long value b-encoded
	 * @see BEncode#bstring(String)*/
	public static String binteger(final long l) {
		return "i" + l + "e";
	}	

	/**B-encode the field name and the int field value
	 * like FIELD_NAME_SIZE:FIELD_NAMEiINTEGER_VALUEe.<br/>
	 * Example: 5:piecei320e
	 * 
	 * @param l The integer/long value to be b-encoded
	 * @param fieldName The field name to be b-encoded
	 * @return The field name b-encoded and int field value b-encoded
	 * like StrBEncodedFieldNameIntBEncodedFieldValue
	 * @see BEncode#binteger(long)
	 * @see BEncode#bstring(String)*/
	public static String bDictionaryIntField(final String fieldName, final long l) {
		return bstring(fieldName) + binteger(l);
	}
	
	/**B-encode the field name and the int field value
	 * like like FieldNameSize:FielNameFieldValueSize:FieldValue<br/>
	 * Example: 11:description23:String dictionary field
	 * 
	 * @param s The string to be b-encoded
	 * @param fieldName The field name to be b-encoded
	 * @return The field name b-encoded and string field value b-encoded
	 * like StrBEncodedFieldNameIntBEncodedFieldValue
	 * @see BEncode#bstring(String)*/
	public static String bDictionaryStrField(final String fieldName, final String s) {
		return bstring(fieldName) + bstring(s);
	}
	
	/**Convert a Byte ArrayList to a Byte Array.
	 * @param al The Byte ArrayList to be converted.
	 * @return A byte array converted from ArrayList*/
	protected static byte[] arrayListToByteArray(ArrayList<Byte> al) {
		byte c, bytes[] = new byte[al.size()];
		for(int i = 0; i < al.size(); i++) {
			c = al.get(i);
			bytes[i]=(byte)c;
		}
		return bytes;
	}
	
	/**Extracts the first field of a b-encoded string. After the method
	 * finishs, the InputStream used to extract the field will be 
	 * positioned in the last position of the extracted field.
	 * Thus, you can re-call the method to obtain the next field.
	 * When the InputStream is in the end position, the method
	 * returns null, indicating that you reached the end
	 * of the InputStream and cannot re-call the method,
	 * because don't exists a next field to be returned. 
	 * @param is A InputStream containing the b-encoded content
	 * @return A byte array containing the field extracted.
	 * If the input stream is in the end and no more fields
	 * exists, returns null.
	 * @throws IOException When the file cannot be accessed
	 * @throws InvalidBEncodedStringException 
	 *   When the b-encoded content in the InputStream is malformed*/
	protected static byte[] getNextField(InputStream is) 
	  throws InvalidBEncodedStringException, IOException {		
		int c; 
		if(is == null)
			return null;

		PushbackInputStream pbis;
		if(is instanceof PushbackInputStream)
		    pbis = (PushbackInputStream)is;
		else pbis = new PushbackInputStream(is);
		
		c = pbis.read();
		pbis.unread(c);
		//If the first character is a digit, the field is a string field.
		//String field has the format sizeOfValue:value
		if(Character.isDigit(c))
			return getNextStringField(pbis);
		//If the first character is "i", the field is a integer field.
		//Integer field has the format iINTEGERe
		else if((char)c ==  'i') 
			return getNextIntegerField(pbis);
		//If the first character is "d", the field is a dictionary.
		//Dictionary fields has the format dFIELDSe
		else if((char)c=='d') 
			return getNextDictionaryField(pbis);
		//If the first character is "l", the field is a list.
		//List fields has the format lFIELDSe
		else if((char)c=='l') 
			return getNextListField(pbis);
		//The InputStream reached the end
		else if(c==-1)
			return null;
		else throw new InvalidBEncodedStringException("Unknown header field character '" + (char)c + "'"); 
	}

	private static byte[] getNextListField(PushbackInputStream pbis) throws IOException, InvalidBEncodedStringException {
		int c;
		ArrayList<Byte> al = new ArrayList<Byte>();
		c = pbis.read();
		while((c=pbis.read())!= -1) 
		   al.add((byte)c);
		
		if(al.size()<2)
			throw new InvalidBEncodedStringException("The list field is empty");

		if(al.get(al.size()-1)!= 'e')
			throw new InvalidBEncodedStringException("The list field trailing 'e' is missing");
		//delete the trailing character 'e' from the list
		al.remove(al.size()-1);
		return arrayListToByteArray(al);
	}

	private static byte[] getNextDictionaryField(PushbackInputStream pbis) throws IOException, InvalidBEncodedStringException {
		int c;
		ArrayList<Byte> al = new ArrayList<Byte>();
		c = pbis.read();
		while((c=pbis.read())!= -1)
			al.add((byte)c);
		
		if(al.size()<2)
			throw new InvalidBEncodedStringException("The dictionary field is empty");

		if(al.get(al.size()-1)!= 'e')
			throw new InvalidBEncodedStringException("The dictionary field trailing 'e' is missing");
		//delete the trailing character 'e' from the dictionary
		al.remove(al.size()-1);
		return arrayListToByteArray(al);
	}

	private static byte[] getNextIntegerField(PushbackInputStream pbis) throws IOException, InvalidBEncodedStringException {
		int c;
		ArrayList<Byte> al = new ArrayList<Byte>();
		c = pbis.read();
		while((c=pbis.read()) != 'e' && c!= -1)  
		   al.add((byte)c);
		
		if(al.size()==0)
			throw new InvalidBEncodedStringException("The integer field is empty");			
		if(c==-1)
			throw new InvalidBEncodedStringException("The integer field trailing 'e' is missing");
		
		return arrayListToByteArray(al);
	}

	private static byte[] getNextStringField(PushbackInputStream pbis) throws IOException, InvalidBEncodedStringException {
		int c, i;
		c = pbis.read();
		//Find the first ":". This define the end of the string field length
		i=0;
		StringBuffer sb = new StringBuffer();
		do  
		   sb.append((char)c);
		while((c=pbis.read()) != ':' && c!= -1);
		
		if(c==-1)
			throw new InvalidBEncodedStringException("The string field delimiter ':' is missing");

		int fieldLen = Integer.parseInt(sb.toString());
		byte[] field = new byte[fieldLen];
		i=-1;
		while(i < fieldLen-1 && (c=pbis.read())!= -1) 
		   field[++i]=(byte)c;
		if(c==-1)
			throw new InvalidBEncodedStringException("The string field terminated before the specified size");
		return field;
	}
	
	/**Convert a b-encoded string into a Hashtable of byte arrays.
	 * Dictionaries and List fields aren't parsed. You need the 
	 * re-call this method for each one dictionary/list field
	 * returned by Hashtable to create a Hashtable for
	 * each one dictionary/list field.
	 * @param is A InputStream containing the b-encoded content
	 * @return A Hashtable containing the parsed b-encoded fields as byte arrays 
	 * @throws IOException When the file cannot be accessed
	 * @throws InvalidBEncodedStringException 
	 *   When the b-encoded content in the InputStream is malformed*/
	public static Hashtable<String, byte[]> getFields(InputStream is) 
	  throws InvalidBEncodedStringException, IOException {
		Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
		byte[] value, key;
		int c;
		PushbackInputStream pbis = new PushbackInputStream(is); 
				
		//Checks if the first characther. The "d" or "l" character, 
        //the header of dictionary/list of of b-encoded string,
		//ignoring it
		c=pbis.read();
		if(c=='d' || c=='l') {
			pbis.unread(c);
			//If the initial field is a dictionary ou list,
			//the call of getNextField method only remove the
			//header and trailling of field
			value = getNextField(pbis);
			//Create a new Stream with the new byte array,
			//without header and trailling
			pbis = new PushbackInputStream(new ByteArrayInputStream(value));
		}
		//The InputStream is empty, returns a empty Hashtable.
		else if (c==-1)
			return ht;
		else pbis.unread(c);
		
		do {
		   key = getNextField(pbis);
		   if(key != null) {
			   value = getNextField(pbis);
			   if(value != null)
			      ht.put(new String(key), value);
		   }
		} while (key!=null);
		
		return ht;
	}

	/**Converts a byte array to a hexadecimal string representation.
	 * <br/>References:&nbsp;
	 *   <a href="http://codare.net/2007/02/02/java-gerando-codigos-hash-md5-sha/">codare.net</a>
	 *   &nbsp;&nbsp;<a href="http://codare.net/2007/01/04/java-impressao-de-bytes-como-hexa/">codare.net</a>
	 * @param bytes The byte array to be converted
	 * @return A hexadecimal string representation of the byte array.
	 * */
	public static String byteArrayToHexStr(byte[] bytes) {
		   StringBuilder s = new StringBuilder();
		   int parteAlta, parteBaixa;
		   for (int i = 0; i < bytes.length; i++) {
		       parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
		       parteBaixa = bytes[i] & 0xf;
		       if (parteAlta == 0) 
		    	   s.append('0');
		       s.append(Integer.toHexString(parteAlta | parteBaixa));
		   }
		   return s.toString();
	}

	/**Converts a hexadecimal string representation to byte array.<br/>
	 * References:&nbsp;
	 *   <a href="http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java">stackoverflow.com</a>
	 * @param hexStr The hexadecimal string representation to be converted to byte array
	 * @return The byte array converted from the hexadecimal string representation
	 * */
	public static byte[] hexStrToByteArray(final String hexStr) {
		if(hexStr == "" || hexStr == null)
			return new byte[0];
		
	    if ((hexStr.length() % 2) != 0)
	        throw new IllegalArgumentException("Input string must contain an even number of characters");

	    final byte result[] = new byte[hexStr.length()/2];
	    final char enc[] = hexStr.toCharArray();
	    for (int i = 0; i < enc.length; i += 2) {
	        StringBuilder curr = new StringBuilder(2);
	        curr.append(enc[i]).append(enc[i + 1]);
	        result[i/2] = (byte) Integer.parseInt(curr.toString(), 16);
	    }
	    return result;
	}
	
	/**Converts a string to a hexadecimal representation 
	 * @param str The string to be converted to hexadecimal string
	 * @return A hexadecimal string representation of the string
	 * */
	public static String strToHexStr(final String str) {
		return byteArrayToHexStr(str.getBytes());
	}

	/**Converts a hexadecimal string representation to character string 
	 * @param hexStr The hexadecimal string to be converted to character string
	 * @return A character string representation of the hexadecimal string
	 * */
	public static String hexStrToStr(final String hexStr) {
		byte[] b = hexStrToByteArray(hexStr);
		return new String(b);
	}	
	
	/**Converts a Byte Array to String
	 * @param b The byte array to be converted
	 * @return A String representing the bytes of byte array*/
	public static String byteArrayToStr(byte[] b) {
		if(b != null && b.length > 0)
			return new String(b);
		else return "";
	}
	
	/**Converts a String to Byte Array 
	 * @param str The string to be converted to byte array
	 * @return A byte array converted from the string*/
	/*public static byte[] strToByteArray(String str) {
		byte[] b = new byte[str.length()];
		for(int i = 0; i < str.length(); i++)
			b[i]=(byte)str.charAt(i);
		return b;
	}*/	
}
