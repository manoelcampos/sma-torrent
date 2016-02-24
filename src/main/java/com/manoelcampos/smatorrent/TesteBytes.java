package com.manoelcampos.smatorrent;

import java.security.*;
import java.io.*;

public class TesteBytes {
	public static void main(String[] args) {
		 //String path = "/home/manoel/workspace/iTorrent/bin/com/manoelcampos/itorrent/";
		 //String file = "bencoded.txt";
		 String frase = "manoel";
		 //valores hexadecimais dos códigos ASCI dos caracteres A, B, C, D, E
		 byte[] d, c, b = new byte[] { 65, 0x42, 0x43, 0x44, 0x45 };
		 String hexStr, str2 = "ZãÃ£÷+n4BÒ5š";
		 //String str = "ABCDE";
		
		 try {
			 MessageDigest md = MessageDigest.getInstance("SHA1");
			 md.update(frase.getBytes());
			 byte[] hashBytes = md.digest();
			 System.out.println((char)65 +" - Byte Hash as Str: " + new String(hashBytes) +
					 " -- As Hexa: " + BEncode.byteArrayToHexStr(hashBytes));
			 
			 hexStr = BEncode.byteArrayToHexStr(b);
			 System.out.println(
				"Byte array: " + b + " Len: " + b.length +
				" Byte Array as Str: " + new String(b)
			    + " Len: "+new String(b).length() + " -- As Hexa: " + 
			    hexStr + " Len: " + hexStr.length());
			 
			 c=BEncode.hexStrToByteArray(hexStr);
			 System.out.println("HexStr: "+ hexStr + 
				" ToByteArrayToStr: " + new String(c));
			 
			 //System.out.println("Str: " + str + " to hex str: " +strToHexStr(str));
			 
			 System.out.println("Str2: " + str2 + 
				" to hex str: " +BEncode.byteArrayToHexStr(str2.getBytes()));
			 c=str2.getBytes();
			 hexStr = BEncode.byteArrayToHexStr(c);
			 d = BEncode.hexStrToByteArray(hexStr);
			 System.out.println("Str2 to hex str: " +hexStr + 
					 " toStr: " + new String(d));
			 
			 System.out.println("Str2: " + str2 + " to hex str: " +
					BEncode.strToHexStr(str2));
		 } catch(NoSuchAlgorithmException e) {
			 System.out.println(e.getMessage());
		 }
		 
		 File f = new File(".");
		 try {
		     System.out.println("dir: "+f.getCanonicalPath());
		 }catch(IOException e) {
			 System.out.println(e.getMessage());
		 }
	}
}
