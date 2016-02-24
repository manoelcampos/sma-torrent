package com.manoelcampos.smatorrent;

import java.util.*;
import java.io.*;

public class TesteBEncode {
   public static void main(String[] args) {
	   Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
	   
	   String s = "manoel";
	   try {
	     byte b[] = s.getBytes("UTF-8");
	     System.out.println("b: "+ new String(b));
	   }
	   catch(UnsupportedEncodingException e) {
		 System.out.println(e.getMessage());
	   }
	   
	   try {
		 String path = "/home/manoel/workspace/iTorrent/bin/com/manoelcampos/itorrent/";
		 String file = "iTorrent.torrent";
		 BufferedInputStream is =
			 new BufferedInputStream(new FileInputStream(path+file));
		 try {
		     ht = BEncode.getFields(is);
		     
		     Enumeration<String> keys = ht.keys();
		     String key;
		     while(keys.hasMoreElements()) {
		    	 key=keys.nextElement();
		    	 System.out.println(key + ": "+ new String(ht.get(key)));
		     }
		 } finally {
			 is.close();
		 }
	     
	   }
	   catch(InvalidBEncodedStringException e) {
		   System.out.println(e.getMessage());
	   }
	   catch (IOException e) {
		   System.out.println(e.getMessage());
	   }
   }
}
