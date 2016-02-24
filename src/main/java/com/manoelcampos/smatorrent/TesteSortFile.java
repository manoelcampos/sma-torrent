package com.manoelcampos.smatorrent;

import java.text.DecimalFormat;
import java.io.*;
import java.util.*;
import java.util.Arrays;

public class TesteSortFile {
	//valores padrões para tamanho de pedaço de arquivo: 256KB, 512KB, 1MB
	private static final int pieceLength = 512*1024; //em bytes

	private static final String path = "/home/manoel/Videos/teste/";
	private static final String fileName = "frejat-segredos";
	private static final String newFileName2 = fileName+".from pieces";
	private static final String ext = ".mpg";
	//private static final String fileName = "Madagascar2 dvdrip";
	//private static final String ext = ".avi";
	
	private static final String bencodedFileName = fileName+".pieces.bencoded.tmp";

	public static void main(String[] args) {
		createPieceFile();
		readPieceFileAndRecreate();
	}
	/**Le o arquivo b-encoded com as partes do arquivo
	 * original e recria o arquivo para provar que 
	 * o mesmo funciona depois de juntar novamente as partes*/
	public static void readPieceFileAndRecreate() {
		File fileIn = new File(path+fileName+ext);
		long fileLength = fileIn.length(); //em bytes

		try {
			SharedFile.reconstructFile(
					path+bencodedFileName, fileLength, path+newFileName2+ext);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidBEncodedStringException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Integer> randomizePieces(int piecesCount) {
		ArrayList<Integer> piecesUnordered = new ArrayList<Integer>();
		ArrayList<Integer> piecesOrdered = new ArrayList<Integer>();
		int i;
		
		for(i = 0; i < piecesCount; i++)
			piecesOrdered.add(i);
		
		int piece;
		do {
		   i = (int)(Math.random()*piecesOrdered.size());
		   piece = piecesOrdered.get(i);
		   piecesUnordered.add(piece);
		   piecesOrdered.remove(i);
		} while (piecesOrdered.size() > 0);
		
		return piecesUnordered;
	}
	
	/**Le um arquivo e cria um outro
	 * em formato b-encode contendo, para 
	 * cada parte do arquivo, um campo str com
	 * o número da parte e um campo str com o conteúdo da parte.*/
	public static void createPieceFile() {
		try {
			File fileIn = new File(path+fileName+ext);
			float fileLength = fileIn.length(); //em bytes
			
			//long pieces = (long)((fileLength/pieceLength)+0.9);
			Integer piecesCount = Torrent.numPieces(fileLength, pieceLength);
			RandomAccessFile is =
					new RandomAccessFile(fileIn, "r");
			
			File fileOutBStr = new File(path+bencodedFileName);
			DataOutputStream osBStr = 
				new DataOutputStream(new FileOutputStream(fileOutBStr));

			int bytes=0, pieceNumber;
			byte[] piece, aux = new byte[pieceLength];
			System.out.println(
					"Tamanho do arquivo: " + 
					(long)(fileLength/(1024*1024)) + 
					"mb (" + (long)fileLength + " bytes)" +
					" Total de partes: " + piecesCount);
			
			ArrayList<Integer> pieces = randomizePieces(piecesCount);
			long start;
  			for(int i=0; i < piecesCount; i++){
  				pieceNumber = pieces.get(i);
  				start = pieceLength * pieceNumber;
  				is.seek(start);
  				bytes=is.read(aux, 0, pieceLength);
  				if(bytes < pieceLength) 
  					piece = Arrays.copyOf(aux, bytes);
  				else piece = aux;
  				
  				int numDigits = piecesCount.toString().length();
  				String format = StrUtils.stringOfChar('0', numDigits);
  				DecimalFormat df = new DecimalFormat(format);
  				df.setDecimalSeparatorAlwaysShown(false);
  				
  				
  				//b-encoded str field com o número da parte do arquivo
  				osBStr.writeBytes(BEncode.bstring(df.format(pieceNumber)));
				/*b-encoded string field com o conteúdo 
				 da parte do arquivo */
				osBStr.writeBytes(String.valueOf(piece.length));
  				osBStr.writeBytes(":");
				osBStr.write(piece);

  				System.out.println(
  					pieceNumber+" de " + piecesCount + " - Bytes lidos: " + bytes);
  			}
  			is.close();
  			osBStr.close();
			System.out.println(
					"Tamanho do arquivo: " + 
					(long)(fileLength/(1024*1024)) + 
					"mb (" + (long)fileLength + " bytes)" +
					" Total de partes: " + piecesCount);
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
