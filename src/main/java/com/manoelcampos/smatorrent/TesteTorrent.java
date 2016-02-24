package com.manoelcampos.smatorrent;

import java.io.*;

public class TesteTorrent {
   public static void main(String[] args) {
	   try {
		 String path = "/home/manoel/workspace/iTorrent/src/com/manoelcampos/itorrent/";
		 //String file = "iTorrent.torrent";
		 //String file = "ubuntu.torrent";
		 
		 //String file = "matlab.torrent";
		 String file = "ubuntu2.torrent";
                 //String file = "teste.torrent";
		 BufferedInputStream is =
			 new BufferedInputStream(new FileInputStream(path+file));
		 Torrent t;
		 try {
		   t = new Torrent(is);
		 } finally {
		   is.close();
		 }
	     System.out.println(t);
	     t.writeToFile(path+"teste.torrent");
	   }
	   catch(InvalidTorrentException e) {
		   System.out.println(e.getMessage());
	   }
	   catch (IOException e) {
		   System.out.println(e.getMessage());
	   }
   }
}
