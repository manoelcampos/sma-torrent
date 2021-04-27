package com.manoelcampos.smatorrent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Represents the structure of a torrent entry inside the XML config file.
 */
public class TorrentConfigEntry implements Serializable {
    private String description;
    private String fileName;
    private long fileSize;
    private String status;
    private double percentCompleted;
    private double uploaded;
    private double downloaded;
    private String infoHash;
    private int pieceLength;
    private String bitField;
    private int complete;
    private int incomplete;
    private double elapsedMinutes;
    static final Field[] fields = getFields();

    static { Arrays.stream(fields).forEach(field -> field.setAccessible(true)); }

    private static Field[] getFields() {
        final Field[] fields = TorrentConfigEntry.class.getDeclaredFields();
        return Arrays.stream(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).toArray(Field[]::new);
    }

    public String getDescription() {
        return description;
    }

    public TorrentConfigEntry setDescription(final String description) {
        this.description = description;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public TorrentConfigEntry setFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getFileSize() {
        return fileSize;
    }

    public TorrentConfigEntry setFileSize(final long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TorrentConfigEntry setStatus(final String status) {
        this.status = status;
        return this;
    }

    public double getPercentCompleted() {
        return percentCompleted;
    }

    public TorrentConfigEntry setPercentCompleted(double percentCompleted) {
        this.percentCompleted = percentCompleted;
        return this;
    }

    public double getUploaded() {
        return uploaded;
    }

    public TorrentConfigEntry setUploaded(final double uploaded) {
        this.uploaded = uploaded;
        return this;
    }

    public double getDownloaded() {
        return downloaded;
    }

    public TorrentConfigEntry setDownloaded(double downloaded) {
        this.downloaded = downloaded;
        return this;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public TorrentConfigEntry setInfoHash(final String infoHash) {
        this.infoHash = infoHash;
        return this;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public TorrentConfigEntry setPieceLength(final int pieceLength) {
        this.pieceLength = pieceLength;
        return this;
    }

    public String getBitField() {
        return bitField;
    }

    public TorrentConfigEntry setBitField(final String bitField) {
        this.bitField = bitField;
        return this;
    }

    public double getComplete() {
        return complete;
    }

    public TorrentConfigEntry setComplete(int complete) {
        this.complete = complete;
        return this;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public TorrentConfigEntry setIncomplete(final int incomplete) {
        this.incomplete = incomplete;
        return this;
    }

    public double getElapsedMinutes() {
        return elapsedMinutes;
    }

    public TorrentConfigEntry setElapsedMinutes(final double elapsedMinutes) {
        this.elapsedMinutes = elapsedMinutes;
        return this;
    }

    /**
     * Get the count of peers (seeders + leechers)
     *
     * @return The number of peers
     */
    public int getPeersCount() {
        return complete + incomplete;
    }

    public boolean sameHash(final String infoHash) {
        return infoHash.compareToIgnoreCase(this.infoHash) == 0;
    }

    public boolean sameStatus(final String status) {
        return status.compareToIgnoreCase(this.status) == 0;
    }

    public void setFieldValue(final Object value, final int index){
        try {
            fields[index].set(this, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
