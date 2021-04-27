package com.manoelcampos.smatorrent;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXB;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that implements a TableModel used with
 * JTable swing component in the program main window.<p/>
 * <p>
 * http://developerlife.com/tutorials/?p=25<br/>
 * http://www.devmedia.com.br/articles/viewcomp.asp?comp=3245<br/>
 * http://www.guj.com.br/article.show.logic?id=140<br/>
 * http://www.guj.com.br/article.show.logic?id=147<br/>
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html<br/>
 *
 * @author Manoel Campos da Silva Filho
 */
public class XmlTorrentDataModel extends AbstractTableModel {
    public static final String[] colCaptions = {
            "Description", "File Name", "File Size",
            "Status", "% Completed", "Uploaded", "Downloaded", "Info Hash",
            "Piece Length", "Bit Field", "Seeders", "Leechers", "Elapsed(min)"
    };

    private final String xmlFileName;
    private final TorrentsConfig torrents;

    //used to hold a list of TableModelListeners
    protected List<Object> tableModelListeners = new ArrayList<>();

    /**
     * Creates the table model loading the data from the torrents XML config file.
     *
     * @param xmlFileName
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     */
    public XmlTorrentDataModel(String xmlFileName) throws ParserConfigurationException, IOException {
        this.xmlFileName = xmlFileName;
        this.torrents = JAXB.unmarshal(new File(xmlFileName), TorrentsConfig.class);
    }

    public void removeRow(int row) {
        int rowCount = getRowCount();
        if (rowCount > 0 && row >= 0 && row < rowCount) {
            torrents.remove(row);
            //Informa a JTable que houve dados deletados passando a linha removida
            fireTableRowsDeleted(row, row);
        }
    }

    public void addRow(TorrentInfoField info, String fileName,
                       String status, Long downloaded, String infoHash,
                       String bitField)
    {
        final TorrentConfigEntry entry = new TorrentConfigEntry();
        entry.setDescription(info.getName())
             .setFileName(fileName)
             .setFileSize(info.getLength())
             .setStatus(status)
             .setDownloaded(downloaded)
             .setInfoHash(infoHash)
             .setPieceLength(info.getPieceLength())
             .setBitField(bitField);

        // Informa a jtable de que houve linhas incluidas no modelo.
        // Como adicionamos no final, pegamos o tamanho total do modelo
        // menos 1 para obter a linha incluida.
        torrents.add(entry);
        int lines = getRowCount() - 1;
        fireTableRowsInserted(lines, lines);
        System.out.println("Row added to JTable Model at line " + lines);
    }

    //
    // TableModel implementation
    //

    /**
     * Return the number of columns for the model.
     *
     * @return the number of columns in the model
     */
    @Override
    public int getColumnCount() {
        return TorrentConfigEntry.fields.length;
    }

    /**
     * Return the number of torrents in an XML document
     *
     * @return the number or rows in the model
     */
    @Override
    public int getRowCount() {
        return torrents.size();
    }

    /**
     * Return an XML data given its location
     *
     * @param r the row whose value is to be looked up
     * @param c the column whose value is to be looked up
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int r, int c) {
        final TorrentConfigEntry torrent = torrents.get(r);
        return switch (c){
            case 0 -> torrent.getDescription();
            case 1 -> torrent.getFileName();
            case 2 -> torrent.getFileSize();
            case 3 -> torrent.getStatus();
            case 4 -> torrent.getPercentCompleted();
            case 5 -> torrent.getUploaded();
            case 6 -> torrent.getDownloaded();
            case 7 -> torrent.getInfoHash();
            case 8 -> torrent.getPieceLength();
            case 9 -> torrent.getBitField();
            case 10 -> torrent.getComplete();
            case 11 -> torrent.getIncomplete();
            case 12 -> torrent.getElapsedMinutes();
            default -> "";
        };
    }

    /**
     * Find the row number of a specific torrent file
     * into the torrents registered in the XML file.
     *
     * @param infoHash The SHA1 hash of the Info field
     *                 of the torrent file.
     * @return Returns the row number of the torrent
     * corresponding to the infoHash, or -1 if not found.
     */
    public int findTorrent(String infoHash) {
        final List<TorrentConfigEntry> torrents = this.torrents.getTorrents();
        for (int i = 0; i < torrents.size(); i++) {
            if(torrents.get(i).sameHash(infoHash))
                return i;
        }

        return -1;
    }

    /**
     * Indicates if a torrent is completed or not.
     *
     * @param r A specific row in the XML file that stores the loaded torrents.
     * @return Returns true if the torrent is complete, otherwise, returns false
     */
    public boolean isCompleted(final int r) {
        final TorrentConfigEntry t = torrents.get(r);
        return t.getDownloaded() >= t.getFileSize();
    }

    public int missingPiecesCount(final int r) {
        final TorrentConfigEntry t = torrents.get(r);
        int count = 0;
        for (int i = 0; i < t.getBitField().length(); i++)
            if (t.getBitField().charAt(i) == '0')
                count++;

        return count;
    }


    /**
     * Return the name of column for the table.
     *
     * @param c the index of column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int c) {
        return colCaptions[c];
    }

    /**
     * Return column class
     *
     * @param c the index of column
     * @return the common ancestor class of the object values in the model.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class getColumnClass(int c) {
        return TorrentConfigEntry.fields[c].getDeclaringClass();
    }

    /**
     * Return false - table is not editable
     *
     * @param r the row whose value is to be looked up
     * @param c the column whose value is to be looked up
     * @return true if the cell is editable.
     */
    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    /**
     * This method is not implemented, because the table is not editable.
     *
     * @param value the new value
     * @param r     the row whose value is to be changed
     * @param c     the column whose value is to be changed
     */
    @Override
    public void setValueAt(final Object value, final int r, final int c) {
        torrents.setFieldValue(value, r, c);
    }

    /**
     * Add the downloadedPieceLength to the value of the field downloaded.
     *
     * @param downloadedPieceLength The value to add to the downloaded field
     * @param r
     */
    public void addDownloaded(final long downloadedPieceLength, final int r) {
        final TorrentConfigEntry t = torrents.get(r);
        t.setDownloaded(downloadedPieceLength);
        fireTableRowsUpdated(r, r);
    }


    /**
     * Add the newElapsedMinutes to the value of the field elapsedMinutes.
     *
     * @param newElapsedMinutes The value to add to the elapsedMinutes field
     * @param r
     */
    public void addElapsedMinutes(Double newElapsedMinutes, int r) {
        final TorrentConfigEntry t = torrents.get(r);
        t.setElapsedMinutes(newElapsedMinutes);
        fireTableRowsUpdated(r, r);
    }

    /**
     * Add a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the TableModelListener
     */
    @Override
    public void addTableModelListener(TableModelListener l) {
        //Add a listener only if the listener is not already registered
        if (!tableModelListeners.contains(l))
            tableModelListeners.add(l);
    }

    /**
     * Remove a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the TableModelListener
     */
    @Override
    public void removeTableModelListener(TableModelListener l) {
        //Remove a listener only if the listener is already registered
        if (tableModelListeners.contains(l))
            tableModelListeners.remove(l);
    }

    public void saveXml() throws IOException {
        JAXB.marshal(torrents, new File(xmlFileName));
    }

    public TorrentConfigEntry getRow(final int row){
        return torrents.get(row);
    }
}
