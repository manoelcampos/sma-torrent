package com.manoelcampos.smatorrent;

import java.text.DecimalFormat;
import java.io.*;
import java.util.Arrays;
import java.security.*;

public class TesteReadWriteFile {
	//valores padrões para tamanho de pedaço de arquivo: 256KB, 512KB, 1MB
	private static final int pieceLength = 512*1024; //em bytes

	private static final String path = "/home/manoel/Videos/teste/";
	private static final String fileName = "frejat-segredos";
	private static final String ext = ".mpg";
	//private static final String fileName = "Madagascar2 dvdrip";
	//private static final String ext = ".avi";
	
	private static final String bencodedFileName = fileName+".pieces.bencoded.tmp";
	private static final String newFileName = fileName+".temp copy";
	private static final String newFileName2 = fileName+".from pieces";

	public static void main(String[] args) {
		recreateFileAndPieceFile(true);
		//readPieceFileAndRecreate();
	}
	/**Le o arquivo b-encoded com as partes do arquivo
	 * original e recria o arquivo para provar que 
	 * o mesmo funciona depois de juntar novamente as partes*/
	public static void readPieceFileAndRecreate() {
		try {
			DataInputStream is =
				new DataInputStream(
						new FileInputStream(path+bencodedFileName));
			
			File file = new File(path+newFileName2+ext);
			file.createNewFile();
			DataOutputStream os =
				new DataOutputStream(
					new FileOutputStream(file));
			try {
				byte[] aux;
				int pieceNumber;
				try {
					do {
					   aux=BEncode.getNextField(is);
					   if(aux != null) {
						   pieceNumber = Integer.parseInt(new String(aux));
						   System.out.println("Parte lida: " + pieceNumber);
						   aux=BEncode.getNextField(is);
						   if(aux != null)
							   os.write(aux);
					   }
					} while(aux != null);
				}catch(InvalidBEncodedStringException e) {
					System.out.println(e.getMessage());
				}
			}finally {
				is.close();
				os.close();
			}
		} catch(IOException e) {
			System.out.println(e.getLocalizedMessage());
		} 
	}
	
	/**Le um arquivo, cria uma cópia dele usando byte array
	 * para provar que a cópia funciona, e cria um outro
	 * arquivo em formato b-encode contendo, para 
	 * cada parte do arquivo, um campo inteiro contendo
	 * o número da parte e um campo string
	 * com o conteúdo da parte.
	 * 
	 * @param createHashForEachPiece Se igual a true, cria
	 * o arquivo b-encoded com as partes do arquivo original
	 * em format de hash SHA1 com uma quebra de linha ao
	 * final de cada parte. Isto criará um arquivo
	 * que não poderá ter seu conteúdo recuperado,
	 * e tem o objetivo apenas de criar uma arquivo
	 * que seja inteligível para nós, podendo ser aberto
	 * em um editor de texto como o Gnome Vim e ser analisado.*/
	public static void recreateFileAndPieceFile(boolean createHashForEachPiece) {
		try {
			File fileIn = new File(path+fileName+ext);
			File fileOut = new File(path+newFileName+ext);
			fileOut.createNewFile();
			float fileLength = fileIn.length(); //em bytes
			
			//long pieces = (long)((fileLength/pieceLength)+0.9);
			Integer pieces = Torrent.numPieces(fileLength, pieceLength);
			DataInputStream is =
				new DataInputStream(
					new BufferedInputStream(new FileInputStream(fileIn)));
			DataOutputStream os =
				new DataOutputStream(new FileOutputStream(fileOut));
			
			File fileOutBStr = new File(path+bencodedFileName);
			DataOutputStream osBStr = 
				new DataOutputStream(new FileOutputStream(fileOutBStr));

			int bytes=0, pieceNumber=-1;
			byte[] hash, piece, aux = new byte[pieceLength];
			System.out.println(
					"Tamanho do arquivo: " + 
					(long)(fileLength/(1024*1024)) + 
					"mb (" + (long)fileLength + " bytes)" +
					" Total de partes: " + pieces);
			//System.exit(0);
			
			MessageDigest md = MessageDigest.getInstance("SHA1");						
  			while((bytes=is.read(aux, 0, pieceLength)) > 0) {
  				pieceNumber++;
  				if(bytes < pieceLength) 
  					piece = Arrays.copyOf(aux, bytes);
  				else piece = aux;

  				
  				int numDigits = pieces.toString().length();
  				String format = StrUtils.stringOfChar('0', numDigits);
  				DecimalFormat df = new DecimalFormat(format);
  				df.setDecimalSeparatorAlwaysShown(false);
  				
  				String s;
  				if(createHashForEachPiece) {
  					md.update(piece);
	  				hash = md.digest();

	  				s = BEncode.bstring(df.format(pieceNumber));
	  				//b-encoded str field com o número da parte do arquivo
  	  				osBStr.writeBytes(s);
  					//b-encoded string field com o conteúdo da parte do arquivo 
	  				osBStr.writeBytes(String.valueOf(hash.length));
	  				osBStr.writeBytes(":");
  					osBStr.writeBytes(BEncode.byteArrayToHexStr(hash));
  				    osBStr.writeChar('\n');
  				} else {
  	  				//b-encoded str field com o número da parte do arquivo
  	  				osBStr.writeBytes(BEncode.bstring(df.format(pieceNumber)));

  					/*b-encoded string field com o conteúdo 
  					 da parte do arquivo */
  					osBStr.writeBytes(String.valueOf(piece.length));
	  				osBStr.writeBytes(":");
  					osBStr.write(piece);
  				}
  				os.write(piece);

  				System.out.println(
  					pieceNumber+" de " + pieces + " - Bytes lidos: " + bytes);
  			}
  			is.close();
  			os.close();
  			osBStr.close();
			System.out.println(
					"Tamanho do arquivo: " + 
					(long)(fileLength/(1024*1024)) + 
					"mb (" + (long)fileLength + " bytes)" +
					" Total de partes: " + pieces);
		} catch(NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
