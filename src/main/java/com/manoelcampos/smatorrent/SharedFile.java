package com.manoelcampos.smatorrent;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**Class that manipulate a shared file in a bittorrent
 * network, managing and getting specific pieces
 * of a file, and reconstructing the received file
 * in a b-encoded temporary file, with unordered pieces
 * of a received file, to the original order
 * and format of file.
 * @author Manoel Campos da Silva Filho*/
public class SharedFile implements AutoCloseable{
	private File file;
	private RandomAccessFile raf;
	
	/**Class constructor used when you want to get a piece
	 * of a shared file to send to other client
	 * or when you want to write a received
	 * piece to a b-encoded temporary file.
	 * @param fileName The name of the file to manipulate
	 * the temporary b-encoded file to write pieces
	 * or the shared file to get pieces to send
	 * to another peer.
	 * @see SharedFile#writePiece(Integer, Integer, byte[])
	 * @see SharedFile#getPiece(int, int)*/
	public SharedFile(final String fileName) throws IOException {
		this(new File(fileName));
	}
	
	/**Class constructor used when you want to get a piece
	 * of a shared file to send to other client
	 * or when you want to write a received
	 * piece to a b-encoded temporary file.
	 * @param file A File object to manipulate
	 * the temporary b-encoded file to write pieces
	 * or the shared file to get pieces to send
	 * to another peer.
	 * @see SharedFile#writePiece(Integer, Integer, byte[])
	 * @see SharedFile#getPiece(int, int)*/
	public SharedFile(final File file) throws IOException {
		super();
		this.file = file;
		raf = new RandomAccessFile(file, "rw");
	}	
	
	/**Get the desired piece of the file
	 * @param pieceNumber The zero-based index of the desired piece
	 * @param pieceLength The length of the piece to be returned
	 * @return Returns a byte array with the content of the requested
	 * piece of file.*/
	public byte[] getPiece(final int pieceNumber, final int pieceLength) throws IOException {
		final long start = pieceLength * pieceNumber;
		raf.seek(start);

		final byte[] b = new byte[pieceLength];
		
		int off=0, readed=0;
		while((off += readed) < pieceLength && readed > -1) 
		   readed=raf.read(b, off, pieceLength);

		return b;
	}
	
	/**Write a file piece to a temporary file, to be remonteded
	 * in the correct order in other time.
	 * @param pieceNumber The zero-based index of the piece
	 * @param piecesCount The count of the file pieces
	 * @param piece The piece to be writed in the temporary file */
	public void writePiece(final Integer pieceNumber, final Integer piecesCount, final byte[] piece) throws IOException {
		raf.seek(file.length());

		final int numDigits = piecesCount.toString().length();
		final String format = StrUtils.stringOfChar('0', numDigits);
		final DecimalFormat df = new DecimalFormat(format);
		df.setDecimalSeparatorAlwaysShown(false);
		
		raf.writeBytes(BEncode.bstring(df.format(pieceNumber)));
		
		/*b-encoded the string field with the content of the file piece*/
		raf.writeBytes(String.valueOf(piece.length));
		raf.writeBytes(":");
		raf.write(piece);
	}
	
	/**Reconstruct the temporary file, putting the pieces
	 * inside it in the corrrect order and renaming the file
	 * to the original file name.<br/>
	 * References: <a href="http://www.javafaq.nu/java-example-code-130.html">http://www.javafaq.nu/java-example-code-130.html</a>
	 * @param inputTmpFileName The name of the file with the pieces.
	 * @param originalFileLength The length of the original file,
	 * before be divided in pieces.
	 * @param newFileName The name of the reconstructed file*/
	public static void reconstructFile(
			final String inputTmpFileName, final long originalFileLength, final String newFileName)
	  		throws IOException, InvalidBEncodedStringException
	{
		final File inputTmpFile = new File(inputTmpFileName);
		final DataInputStream is =
	    	new DataInputStream(
	    		new BufferedInputStream(
	    			new FileInputStream(inputTmpFile)));

		final File outputFile = new File(newFileName);
		final RandomAccessFile of = new RandomAccessFile(outputFile, "rw");
	    of.setLength(originalFileLength);
		
		byte[] bencodedField, piece;
		int pieceNumber;
		long start;
		try{
			do {
				bencodedField = BEncode.getNextField(is);
				if(bencodedField != null) {
					pieceNumber = Integer.parseInt(BEncode.byteArrayToStr(bencodedField));
					System.out.println(
						"Piece " + pieceNumber + " read to reconstruct the file");
				    bencodedField = BEncode.getNextField(is);
				    if(bencodedField != null) {
					    piece = bencodedField;
					    start = piece.length * pieceNumber;
						of.seek(start);
						of.write(piece);
				    }
				}
			} while(bencodedField != null);
			System.out.println(
					"File reconstruction finished");
		} finally {
			is.close();
			inputTmpFile.delete();
			of.close();
		}
	}
	
	/**Closes the opened file in the class constructor*/
	@Override
	public void close() throws IOException {
		if(raf != null)
		   raf.close();
	}
}
