package com.manoelcampos.smatorrent;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
    //TABLE META DATA
    public static final String ROOT_ELEMENT_TAG = "torrent";

    public static final String[] colNames = {
            "description", "fileName", "fileSize",
            "status", "percentCompleted", "uploaded", "downloaded", "infoHash",
            "pieceLength", "bitField", "complete", "incomplete", "elapsedMinutes"
    };

    public static final String[] colCaptions = {
            "Description", "File Name", "File Size",
            "Status", "% Completed", "Uploaded", "Downloaded", "Info Hash",
            "Piece Length", "Bit Field", "Seeders", "Leechers", "Elapsed(min)"
    };

    public static final Class[] colClasses = {
            String.class, String.class, String.class,
            String.class, String.class, String.class,
            String.class, String.class, String.class,
            String.class, String.class, String.class,
            String.class
    };

    public static final int
            descriptionCol = 0, fileNameCol = 1, fileSizeCol = 2, statusCol = 3,
            percentcompletedCol = 4, uploadedCol = 5, downloadedCol = 6, infoHashCol = 7,
            pieceLengthCol = 8, bitFieldCol = 9, completeCol = 10, incompleteCol = 11,
            elapsedMinutesCol = 12;

    private static final long serialVersionUID = 437192815338852310L;
    private final String xmlFileName;

    //DATA
    //DOM object to hold XML document contents
    protected Document doc;

    //used to hold a list of TableModelListeners
    protected java.util.List<Object> tableModelListeners = new ArrayList<>();

    /**
     * Constructor - create a DOM
     *
     * @param xmlFileName
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public XmlTorrentDataModel(String xmlFileName)
            throws ParserConfigurationException, SAXException, IOException {
        this.xmlFileName = xmlFileName;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        doc = docBuilder.parse(new File(xmlFileName));
    }

    public void removeRow(int row) {
        int rowCount = getRowCount();
        if (rowCount > 0 && row >= 0 && row < rowCount) {
            Element parent = doc.getDocumentElement();
            Node node = doc.getElementsByTagName(ROOT_ELEMENT_TAG).item(row);
            parent.removeChild(node);

            //Informa a JTable que houve dados deletados passando a
            //linha removida
            fireTableRowsDeleted(row, row);
        }
    }

    public void addRow(String description, String fileName,
                       Long fileSize, String status, Integer completed, Long uploaded,
                       Long downloaded, String infoHash,
                       Integer pieceLength, String bitField)
    {
        Element parent = doc.getDocumentElement();
        Element el = doc.createElement(ROOT_ELEMENT_TAG);
        el.setAttribute(colNames[descriptionCol], description);
        el.setAttribute(colNames[fileNameCol], fileName);
        el.setAttribute(colNames[fileSizeCol], fileSize.toString());
        el.setAttribute(colNames[statusCol], status);
        el.setAttribute(colNames[percentcompletedCol], completed.toString());
        el.setAttribute(colNames[uploadedCol], uploaded.toString());
        el.setAttribute(colNames[downloadedCol], downloaded.toString());
        el.setAttribute(colNames[infoHashCol], infoHash);
        el.setAttribute(colNames[pieceLengthCol], pieceLength.toString());
        el.setAttribute(colNames[bitFieldCol], bitField);
        el.setAttribute(colNames[completeCol], "0");
        el.setAttribute(colNames[incompleteCol], "0");
        el.setAttribute(colNames[elapsedMinutesCol], "0.0");
        parent.appendChild(el);

        // Informa a jtable de que houve linhas incluidas no modelo
        // Como adicionamos no final, pegamos o tamanho total do modelo
        // menos 1 para obter a linha incluida.
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
        return colClasses.length;
    }

    /**
     * Return the number of torrents in an XML document
     *
     * @return the number or rows in the model
     */
    @Override
    public int getRowCount() {
        NodeList nl = doc.getElementsByTagName(ROOT_ELEMENT_TAG);
        return nl.getLength();
    }

    /**
     * Return an XML data given its location
     *
     * @param     r   the row whose value is to be looked up
     * @param     c    the column whose value is to be looked up
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int r, int c) {
        //must get row first
        //Element row = doc.getDocumentElement();
        //row.getAttribute(name)

        NodeList nl = doc.getElementsByTagName(ROOT_ELEMENT_TAG);
        NamedNodeMap atribs = nl.item(r).getAttributes();

        Node node = atribs.getNamedItem(colNames[c]);
        if (node != null)
            return node.getNodeValue();
        return "";
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
        for (int r = 0; r < getRowCount(); r++) {
            if (infoHash.compareToIgnoreCase(getInfoHash(r)) == 0)
                return r;
        }

        return -1;
    }

    /**
     * Indicates if a torrent is completed or not.
     *
     * @param r A specific row in the XML file that stores the loaded torrents.
     * @return Returns true if the torrent is complete, otherwise, returns false
     */
    public boolean isCompleted(int r) {
        return getDownloaded(r) >= getFileSize(r);
    }

    public int missingPiecesCount(int r) {
        String bitField = getBitField(r);
        int count = 0;
        for (int i = 0; i < bitField.length(); i++)
            if (bitField.charAt(i) == '0')
                count++;

        return count;
    }

    public int getDownloaded(int r) {
        Object val = getValueAt(r, downloadedCol);
        if (val != null)
            return Integer.parseInt(val.toString());
        return 0;
    }

    public int getUploaded(int r) {
        Object val = getValueAt(r, uploadedCol);
        if (val != null)
            return Integer.parseInt(val.toString());
        return 0;
    }

    /**
     * Get the number of seeders
     *
     * @param r The row number of the entry in the XML
     * @return The number of seeders
     */
    public int getComplete(int r) {
        Object val = getValueAt(r, completeCol);
        if (val != null) {
            try {
                return Integer.parseInt(val.toString());
            } catch (NumberFormatException e) {
                throw new NumberFormatException(e.getMessage() + " at row " + r);
            }
        }

        return 0;
    }

    /**
     * Get the number of leechers
     *
     * @param r The row number of the entry in the XML
     * @return The number of leechers
     */
    public int getIncomplete(int r) {
        Object val = getValueAt(r, incompleteCol);
        if (val != null)
            return Integer.parseInt(val.toString());

        return 0;
    }

    /**
     * Get the count of peers (seeders + leechers)
     *
     * @param r The row number of the entry in the XML
     * @return The number of peers
     */
    public int getPeersCount(int r) {
        return getComplete(r) + getIncomplete(r);
    }

    public int getFileSize(int r) {
        Object val = getValueAt(r, fileSizeCol);
        if (val != null)
            return Integer.parseInt(val.toString());

        return 0;
    }

    public double getElapsedMinutos(int r) {
        Object val = getValueAt(r, elapsedMinutesCol);
        if (val != null)
            return Double.parseDouble(val.toString());

        return 0.0;
    }

    public int getPieceLength(int r) {
        Object val = getValueAt(r, pieceLengthCol);
        if (val != null)
            return Integer.parseInt(val.toString());

        return 0;
    }

    public String getDescription(int r) {
        Object val = getValueAt(r, descriptionCol);
        if (val != null)
            return val.toString();

        return "";
    }

    public String getInfoHash(int r) {
        Object val = getValueAt(r, infoHashCol);
        if (val != null)
            return val.toString();

        return "";
    }

    public String getStatus(int r) {
        Object val = getValueAt(r, statusCol);
        if (val != null)
            return val.toString();

        return "";
    }

    public String getBitField(int r) {
        Object val = getValueAt(r, bitFieldCol);
        if (val != null)
            return val.toString();

        return "";
    }

    /**
     * Return the name of column for the table.
     *
     * @return the name of the column
     * @param     c   the index of column
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
        return colClasses[c];
    }

    /**
     * Return false - table is not editable
     *
     * @param     r    the row whose value is to be looked up
     * @param     c    the column whose value is to be looked up
     * @return true if the cell is editable.
     */
    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    /**
     * This method is not implemented, because the table is not editable.
     *
     * @param     value         the new value
     * @param     r     the row whose value is to be changed
     * @param     c     the column whose value is to be changed
     */
    @Override
    public void setValueAt(Object value, int r, int c) {
        NodeList nl = doc.getElementsByTagName(ROOT_ELEMENT_TAG);
        NamedNodeMap atribs = nl.item(r).getAttributes();

        Node node = atribs.getNamedItem(colNames[c]);
        node.setNodeValue(value.toString());
        atribs.setNamedItem(node);
        fireTableCellUpdated(r, c);
    }

    /**
     * Add the downloadedPieceLength to the value of the field downloaded.
     *
     * @param downloadedPieceLength The value to add to the
     *                              downloaded field
     * @param r
     */
    public void addDownloaded(long downloadedPieceLength, int r) {
        NodeList nl = doc.getElementsByTagName(ROOT_ELEMENT_TAG);
        NamedNodeMap atribs = nl.item(r).getAttributes();

        Node node = atribs.getNamedItem(colNames[downloadedCol]);

        Long value = getDownloaded(r) + downloadedPieceLength;
        node.setNodeValue(value.toString());
        atribs.setNamedItem(node);
        fireTableCellUpdated(r, downloadedCol);
    }


    /**
     * Add the newElapsedMinutes to the value of the field elapsedMinutes.
     *
     * @param newElapsedMinutes The value to add to the
     *                          elapsedMinutes field
     * @param r
     */
    public void addElapsedMinutes(Double newElapsedMinutes, int r) {
        NodeList nl = doc.getElementsByTagName(ROOT_ELEMENT_TAG);
        NamedNodeMap atribs = nl.item(r).getAttributes();

        Node node = atribs.getNamedItem(colNames[elapsedMinutesCol]);

        Double value = getElapsedMinutos(r) + newElapsedMinutes;
        node.setNodeValue(value.toString());
        atribs.setNamedItem(node);
        fireTableCellUpdated(r, elapsedMinutesCol);
    }

    /**
     * Add a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param    l the TableModelListener
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
     * @param    l the TableModelListener
     */
    @Override
    public void removeTableModelListener(TableModelListener l) {
        //Remove a listener only if the listener is already registered
        if (tableModelListeners.contains(l))
            tableModelListeners.remove(l);
    }

    public void saveXml() throws IOException {
        File file = new File(xmlFileName);
        XMLSerializer serializer =
                new XMLSerializer(new FileOutputStream(file),
                        new OutputFormat(doc, "iso-8859-1", true));

        serializer.serialize(doc);
    }

}
