package com.manoelcampos.smatorrent;

import java.util.*;
import java.io.*;
import java.security.*;

/**Class that implements a metainfo torrent file.
 * References: http://wiki.theory.org/BitTorrentSpecification
 * @author Manoel Campos da Silva Filho */
public class Torrent {
	public static final String STARTED="started";
	public static final String STOPPED="stopped";
	/**Required fields in torrent file*/
	private String announce;
	
	//TODO Not implemented
	//private String announceList;
	
	/**Dictionary that describes the file(s) of the torrent*/
	private TorrentInfoField info;
	
    /**Optional fields in torrent file */
	private String comment;
	/**String encoding format used to generate the pieces part of the info 
	 * dictionary in the .torrent */
	private String encoding;
	/**Name and version of the program used to create the .torrent*/
	private String createdBy;
	/**In standard UNIX epoch format*/
	private long creationDate;
	
	/**Generate a String containing in each position, the pieces of the file
	 * the client already has. If the client is a seeder,
	 * all positions is set to 1, else, is set to 0. 
	 * @param numPieces Number os pieces of the shared file
	 * @param seeder If the client is a seeder or not.
	 * @return Return a string of length numPieces where positions
	 * with 1 indicates the pieces that the client already has.*/
	public static String generateBitField(int numPieces, boolean seeder) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < numPieces; i++)
			sb.append(seeder ? "1" : "0");
		return sb.toString();
	}

	/**Get the number of pieces of the shared file 
	 * @param fileLength The length of the shared file 
	 * @param pieceLength The length of each piece in the file
	 * @return The number of pieces of the file*/
	public static int numPieces(float fileLength, float pieceLength) {
		return (int)Math.ceil((fileLength/pieceLength));		
	}

	/**Get the length of the last piece of the shared file 
	 * @param fileLength The length of the shared file 
	 * @param pieceLength The length of each piece in the file
	 * @return The length of the last piece of the file*/
	public static int lastPieceLength(float fileLength, float pieceLength) {
		int parts = (int)(fileLength/pieceLength);
		float frac = (fileLength/pieceLength) - parts;
		return (int)(pieceLength*frac);
	}
	
	/**Get the length of a specified piece of the shared file,
	 * based on the default piece length. All pieces have 
	 * tha same length, except the last piece that can have
	 * a minor length.  
	 * @param fileLength The length of the shared file 
	 * @param pieceLength The length of each piece in the file
	 * @param pieceNumber The number of the desired piece to obtain the length.
	 * @return The length of the specified piece of the file*/
	public static int getSpecificPieceLength(float fileLength, 
	  float pieceLength, int pieceNumber) {
		if(pieceNumber == numPieces(fileLength, pieceLength)-1)
			return lastPieceLength(fileLength, pieceLength);
		else return (int)pieceLength;
	}
	
	
	/**Class constructor with required torrent fields
	 * and some useful optional fields
	 */
	public Torrent(String announce, TorrentInfoField info, String comment, String createdBy) {
		this.announce = announce;
		this.info = info;
		this.comment = comment;
		this.createdBy = createdBy;
		if(info==null)
			info = new TorrentInfoField();
	}
	
	/**Class constructor with all torrent fields: required and optional */
	public Torrent(String announce, TorrentInfoField info, 
	  String comment, String createdBy, String encoding) {
		this(announce, info, comment, createdBy);
		this.encoding = encoding;
	}

	/**Write a torrent file using the class's properties.
	 * @param torrentFileName Name of torrent file to write.
	 * @throws IOException When the file cannot be accessed
	 * @throws FileNotFoundException When the file doesn't exists*/
	public void writeToFile(String torrentFileName) 
	  throws IOException, FileNotFoundException {
		File f = new File(torrentFileName);
		
		Calendar cal = Calendar.getInstance();
		int year, month, day, hour, min, sec;
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		hour = cal.get(Calendar.HOUR);
		min = cal.get(Calendar.MINUTE);
		sec = cal.get(Calendar.SECOND);
		//To convert date to UTC format.
		cal.set(year + 1900, month, day, hour, min, sec);
		long utcDate = cal.getTime().getTime();
		
		f.createNewFile();
		
		DataOutputStream os = 
		 	new DataOutputStream(new FileOutputStream(f));
		try {
			String s;
			os.writeByte('d'); //header of file
			s = BEncode.bDictionaryStrField("announce", announce);
			os.writeBytes(s);
			
			s = BEncode.bDictionaryStrField("comment", comment);
			os.writeBytes(s);
			
			s  = BEncode.bDictionaryStrField("created by", createdBy);
			os.writeBytes(s);
			
			s=BEncode.bDictionaryIntField("creation date", utcDate);
			os.writeBytes(s);
			
			if(encoding != "") {
				s=BEncode.bDictionaryStrField("encoding", encoding);
				os.writeBytes(s);
			}
	
			os.writeBytes("4:infod");
	
			s=BEncode.bDictionaryIntField("length", info.getLength());
			os.writeBytes(s);
	
			if(info.getMd5sum() != "") {
				s=BEncode.bDictionaryStrField("md5sum", info.getMd5sum());
				os.writeBytes(s);
			}
			
			s=BEncode.bDictionaryStrField("name", info.getName());
			os.writeBytes(s);
			
			s=BEncode.bDictionaryIntField("piece length", info.getPieceLength());
			os.writeBytes(s);
			
			s=BEncode.bstring("pieces");
			os.writeBytes(s);
			
			s= String.valueOf(info.getPieces().length);
			os.writeBytes(s + ':');
			os.write(info.getPieces());
			
			os.writeByte('e'); //trailling of info dictionary
			os.writeByte('e'); //trailing of file
		} finally {
			os.close();
		}
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		s.append("announce: "+this.announce);
		s.append("\ncomment: "+this.comment);
		s.append("\ncreated by: "+this.createdBy);
		s.append("\nencoding: "+this.encoding);
		s.append("\n\ninfo\n"+this.info);
		
		return s.toString();
	}
	
	/**Load the class fields from a Hashtable containing the torrent file
	 * field values.
	 * @param ht The Hashtable containing the torrent file field values*/
	public void loadFieldsFromHashtable(Hashtable<String, byte[]> ht) {
		String value;
		
    	this.announce = BEncode.byteArrayToStr(ht.get("announce"));
    	this.comment = BEncode.byteArrayToStr(ht.get("comment"));
    	this.createdBy = BEncode.byteArrayToStr(ht.get("created by"));
    	value = BEncode.byteArrayToStr(ht.get("creation date"));
    	if(value != "")
    		this.creationDate = Long.parseLong(value);
    	this.encoding = BEncode.byteArrayToStr(ht.get("encoding"));
	}

	/**Default class constructor */
	public Torrent() {
		this.info = new TorrentInfoField();
	}

	/**Class constructor that read fields of a b-encoded format
	 * from a torrent file
	 * @param fileName The torrent file name  
	 * @throws IOException When the file cannot be accessed
	 * @throws InvalidTorrentException When the torrent file is malformed*/
	public Torrent(String fileName) throws 
	 IOException, InvalidTorrentException {
		this(new DataInputStream(new FileInputStream(fileName)));
	}
	
	/**Class constructor that read fields of a b-encoded format
	 * from a InputStream
	 * @param is The InputStream containing the b-encoded data to be read 
	 * @throws IOException When the file cannot be accessed
	 * @throws InvalidTorrentException When the torrent file is malformed*/
	public Torrent(InputStream is) 
  	  throws IOException, InvalidTorrentException {
		this();
		
		Hashtable<String, byte[]> ht;
		byte[] value;
	    try {
	    	ht = BEncode.getFields(is);
	    	loadFieldsFromHashtable(ht);
	    	
	    	value = ht.get("info");
	    	if(value != null && value.length > 0) {
	    	  ByteArrayInputStream bais = new ByteArrayInputStream(value); 
	    	  ht = BEncode.getFields(bais);
	    	  info.loadFieldsFromHashtable(ht);
	    	}
        } catch(InvalidBEncodedStringException e) {
        	throw new InvalidTorrentException(e.getMessage());
        }
	}
	
	public String getAnnounce() {return announce;};
	public TorrentInfoField getInfo() {return info;};
	public String getComment() {return comment;};
	public String getEncoding() {return encoding;};
	public String getCreatedBy() {return createdBy;};
	public long creationDate() {return creationDate;}

	/**Break a message in the format <part1><part2><part3>.....<partN>
	 * into a array of String without < and > characters
	 * @param msg The string to be splitted.
	 * @return A array with the parts of the splitted string. */
	public static String[] splitMessage(String msg) {
		//Example of message: <pstrlen><pstr><reserved><info_hash><peer_id>
		msg = msg.replace("<", "");
		return msg.split(">");
	};	
	
	/**Randomically choose a piece number from other peer bit field.
	 * The piece number choosed is one that doens't exist in the
	 * myBitField parameter. The params myBitField and peerBitField
	 * must has the same length.
	 * @param myBitField The bit field containing 1 to indicate
	 * the pieces that the client already has. The positions
	 * with 0 indicates the pieces missing.
	 * @param remotePeerBitField The bit field of a peer that you want
	 * to download a piece
	 * @return The piece number, radomically choosed, considering
	 * that the choosed piece doesn't exists in the myBitField.
	 * If the peer don't have neither piece that the client
	 * yeat don't have, the function returns -1*/
	public static int chooseRandomPiece(String myBitField, String remotePeerBitField) {
		if(myBitField.length() != remotePeerBitField.length())
			return -1;
		
		/*A new bit field with 1 only in the positions
		 * of the pieces that the remote peer has and the client
		 * requering pieces doesn't have.*/
		String availablePieces="";
		int availablePiecesCount = 0;
		for(int i=0; i < remotePeerBitField.length(); i++) {
			if(remotePeerBitField.charAt(i) == '1' && myBitField.charAt(i)=='0') {
				availablePieces = availablePieces + "1";
				availablePiecesCount++;
			}
			else availablePieces = availablePieces + "0";
		}

		/*If the other peer don't have neither piece that 
		 * the client has, returns -1*/
		if(availablePiecesCount==0)
			return -1;
		
		int pos;
		do {
		  //the piece number, randomically choosed
		  pos=(int)(Math.random()*availablePieces.length());
		} while(availablePieces.charAt(pos)=='0');
		return pos;
	}
	
	/**Set a position of the bit field to 1, to indicate that
	 * the client downloaded the piece of the position indicated.
	 * @param bitField The bit field to have a position setted to 1.
	 * @param pos The position (number of the piece) to be setted
	 * in the bit field. This is a zero based index.
	 * @return A new String bit field with the indicated position
	 * setted to 1.
	 * @throws IndexOutOfBoundsException When the position is less than zero or
	 * greather or equal than the bit field length*/
	public static String setBitFieldPosition(String bitField, int pos)
	  throws IndexOutOfBoundsException {
		if(pos < 0 || pos >= bitField.length())
			throw new IndexOutOfBoundsException(
				"The position must be grather or equal than 0 and less than the bit field length.");
		char[] array = bitField.toCharArray();
		array[pos] = '1';
		bitField = String.valueOf(array);
		return bitField;
	}

	/**Returns the byte position in the shared file that a piece
	 * begins.
	 * @param pieceNumber The piece number of the piece
	 * that you can get the begin. This is a zero based index.
	 * @param pieceLength The length of each piece
	 * @return The byte number in the shared file that
	 * the specified piece begins*/
	public static long pieceBegin(
		int pieceNumber, long pieceLength) {
		return pieceLength*pieceNumber;
	}
	
	/**Checks if a peer os a seeder, based on you bit field,
	 * sended in a bitfield message. If a peer is seeder,
	 * all positions in its bit field is 1, indicating
	 * that it has all pieces of the shared file.
	 * The positions with 0, indicates a missing piece. 
	 * 
	 * @param bitField The bit field of the peer.
	 * @return Return true if all positions in the
	 * bit field is 1, otherwise, returns false.*/
	public static boolean isSeeder(String bitField) {
		for(int i=0; i < bitField.length(); i++)
		    if(bitField.charAt(i)=='0')
		    	return false;
		return true;
	}
}

//TODO A implementação está sendo feita para torrent de apenas um arquivo
class TorrentInfoField {
	//Required fields 
	/**Number of bytes in each piece*/
	private int pieceLength;		
	/**Concatenation of all 20-byte SHA1 hash values, one per piece 
	 * (as byte array, where each position contains the ASCII
	 * code for each character in Hexadecimal representation)*/
	private byte[] pieces;
	/**Length of the file in bytes*/
	private long length; 
	
	//Optional fields
	/** If it is set to "1", the client MUST publish its presence 
	 * to get other peers ONLY via the trackers explicitly described 
	 * in the metainfo file. If this field is set to "0" or is not present, 
	 * the client may obtain peer from other means, e.g. PEX peer exchange, dht. 
	 * Here, "private" may be read as "no external peer source". */
	private int privateField;
	/**The filename. This is purely advisory*/
	private String name;
	/**A 32-character hexadecimal string corresponding to the MD5 
	 * sum of the file. This is not used by BitTorrent at all, 
	 * but it is included by some programs for greater compatibility*/
	private String md5sum;

	/**Default class constructor*/
	public TorrentInfoField() {
		super();
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		s.append("name: "+this.name);
		s.append("\nlength: "+this.length);
		s.append("\npiece length: "+this.pieceLength);
		s.append("\nmd5sum: "+this.md5sum);
		if(privateField >= 0)
		   s.append("\nprivate: "+this.privateField);
		s.append("\npieces: "+ BEncode.byteArrayToStr(this.pieces));
		
		return s.toString();
	}
	
	/**Class constructor with required fields
	 * of the dictionary info of a torrent file,
	 * and some usefull optional fields*/
	public TorrentInfoField(String name, int pieceLength, byte[] pieces) {
		setName(name);
		setPieceLength(pieceLength);
		setPieces(pieces); 
		setPrivateField(-1);
	}

	/**Class constructor with all fields
	 * of the dictionary info of a torrent file: required and optional*/
	public TorrentInfoField(String name, int pieceLength, byte[] pieces, 
	  String md5sum, int privateField) {
		this(name, pieceLength, pieces);
		setMd5sum(md5sum);
		setPrivateField(privateField); 
	}

	/**Load the class fields from a Hashtable containing the torrent file
	 * field values.
	 * @param ht The Hashtable containing the torrent file field values*/
	public void loadFieldsFromHashtable(Hashtable<String, byte[]> ht) {
		String value;
    	value = BEncode.byteArrayToStr(ht.get("length"));
    	if(value != "")
    		this.length = Integer.parseInt(value);
        this.md5sum = BEncode.byteArrayToStr(ht.get("md5sum"));
        this.name = BEncode.byteArrayToStr(ht.get("name"));

        value = BEncode.byteArrayToStr(ht.get("piece length"));
        if(value != "")
        	this.pieceLength = Integer.parseInt(value);
        
        this.pieces = ht.get("pieces");
        
        value = BEncode.byteArrayToStr(ht.get("private"));
        if(value != "")
        	this.privateField = Integer.parseInt(value);
	}
	
	public String getName() {return name;}; 
	public int getPieceLength() {return pieceLength;};
	public byte[] getPieces() {return pieces;};
	public long getLength() {return length;};
	public int getPrivateField() {return privateField;};
	public String getMd5sum() {return md5sum;};
	
	/**Returns the SHA1 20 bytes hash of the torrent file info field,
	 * that identify uniquelly a torrent.
	 * @return The info field hash 
	 * @throws NoSuchAlgorithmException When the cryptograph algorithm 
	 * specified doesn't exists.*/
	public String hash() throws NoSuchAlgorithmException {
		 MessageDigest md = MessageDigest.getInstance("SHA1");
		 md.update(getPieces());
		 byte[] hashBytes = md.digest();
		 
		 return BEncode.byteArrayToHexStr(hashBytes);
	}
		
	public void setName(String name) { 
		this.name = name;
		File f = new File(name);
		this.length = f.length();
	}
	public void setPieceLength(int pieceLength) {
		this.pieceLength=pieceLength;
	};
	public void setPieces(byte[] pieces) {
		this.pieces=pieces;
	};
	public void setPrivateField(int privateField) {
		this.privateField=privateField;
	};
	public void setMd5sum(String md5sum) {
		this.md5sum=md5sum;
	};
}

/**Class to raise exceptions when try to parse a invalid
 * torrent file.
 * @author Manoel Campos da Silva Filho*/
class InvalidTorrentException extends Exception {
	private static final long serialVersionUID = 326914220348328507L;
	
	public InvalidTorrentException(String message) {
		super(message);
	}
}