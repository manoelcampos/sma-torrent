package com.manoelcampos.smatorrent;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

//TODO A implementação está sendo feita para torrent de apenas um arquivo
public class TorrentInfoField {
    //Required fields
    /**
     * Number of bytes in each piece
     */
    private int pieceLength;

    /**
     * Concatenation of all 20-byte SHA1 hash values, one per piece
     * (as byte array, where each position contains the ASCII
     * code for each character in Hexadecimal representation)
     */
    private byte[] pieces;

    /**
     * Length of the file in bytes
     */
    private long length;

    //Optional fields
    /**
     * If it is set to "1", the client MUST publish its presence
     * to get other peers ONLY via the trackers explicitly described
     * in the metainfo file. If this field is set to "0" or is not present,
     * the client may obtain peer from other means, e.g. PEX peer exchange, dht.
     * Here, "private" may be read as "no external peer source".
     */
    private int privateField;

    /**
     * The filename. This is purely advisory
     */
    private String name;

    /**
     * A 32-character hexadecimal string corresponding to the MD5
     * sum of the file. This is not used by BitTorrent at all,
     * but it is included by some programs for greater compatibility
     */
    private String md5sum;

    /**
     * Default class constructor
     */
    public TorrentInfoField() {
        super();
    }

    /**
     * Class constructor with required fields
     * of the dictionary info of a torrent file,
     * and some useful optional fields
     */
    public TorrentInfoField(String name, int pieceLength, byte[] pieces) {
        setName(name);
        setPieceLength(pieceLength);
        setPieces(pieces);
        setPrivateField(-1);
    }

    /**
     * Class constructor with all fields
     * of the dictionary info of a torrent file: required and optional
     */
    public TorrentInfoField(String name, int pieceLength, byte[] pieces, String md5sum, int privateField) {
        this(name, pieceLength, pieces);
        setMd5sum(md5sum);
        setPrivateField(privateField);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("name: " + this.name);
        s.append("\nlength: " + this.length);
        s.append("\npiece length: " + this.pieceLength);
        s.append("\nmd5sum: " + this.md5sum);
        if (privateField >= 0)
            s.append("\nprivate: " + this.privateField);
        s.append("\npieces: " + BEncode.byteArrayToStr(this.pieces));

        return s.toString();
    }

    /**
     * Load the class fields from a Hashtable containing the torrent file
     * field values.
     *
     * @param ht The Hashtable containing the torrent file field values
     */
    public void loadFieldsFromHashtable(Hashtable<String, byte[]> ht) {
        String value;
        value = BEncode.byteArrayToStr(ht.get("length"));
        if (value != "")
            this.length = Integer.parseInt(value);
        this.md5sum = BEncode.byteArrayToStr(ht.get("md5sum"));
        this.name = BEncode.byteArrayToStr(ht.get("name"));

        value = BEncode.byteArrayToStr(ht.get("piece length"));
        if (value != "")
            this.pieceLength = Integer.parseInt(value);

        this.pieces = ht.get("pieces");

        value = BEncode.byteArrayToStr(ht.get("private"));
        if (value != "")
            this.privateField = Integer.parseInt(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        File f = new File(name);
        this.length = f.length();
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public void setPieceLength(int pieceLength) {
        this.pieceLength = pieceLength;
    }

    public byte[] getPieces() {
        return pieces;
    }

    public void setPieces(byte[] pieces) {
        this.pieces = pieces;
    }

    public long getLength() {
        return length;
    }

    public int getPrivateField() {
        return privateField;
    }

    public void setPrivateField(int privateField) {
        this.privateField = privateField;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    /**
     * Returns the SHA1 20 bytes hash of the torrent file info field,
     * that identify uniquelly a torrent.
     *
     * @return The info field hash
     * @throws NoSuchAlgorithmException When the cryptograph algorithm
     *                                  specified doesn't exists.
     */
    public String hash() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(getPieces());
        byte[] hashBytes = md.digest();

        return BEncode.byteArrayToHexStr(hashBytes);
    }
}
