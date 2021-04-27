package com.manoelcampos.smatorrent;

//import java.io.*;

import jade.domain.FIPAException;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * Implements the graphical interface of the iTorrent system,
 * the Intelligent Torrent.
 *
 * @author Manoel Campos da Silva Filho
 */
public class MainForm extends JFrame {
    public static final String PLAY = "Play";
    public static final String PAUSE = "Pause";
    public static final String DATA_FILE = "SMATorrent.xml";
    public static final String APP_NAME = "SMA-Torrent";
    //Only 3 digits
    public static final String APP_VERSION = "0.1";
    private static final long serialVersionUID = -1888237523626310847L;
    private final JPanel pnlMenuToolBar;
    private final JTable tb;
    private final JPanel pnlBottom;
    private final JProgressBar progressBar;
    private final JLabel lbStatus;
    /**
     * The XML file containing the torrent files attached to the program
     */
    private String xmlFileName;
    /**
     * The JADE Agent that the graphical interface controls.
     */
    private TorrentClientAgent myAgent;
    private JButton btnRemove;
    private JButton btnAdd;
    private JButton btnAddToSeed;
    private JButton btnStartPause;
    private JMenuBar menuBar;
    private JMenu mnuFile, mnuHelp;
    private JMenuItem mnuAbout;
    private JToolBar toolBar;
    private XmlTorrentDataModel dataModel;

    /**
     * Class constructor
     *
     * @param a A Jade agent that will control the graphical interface.
     */
    public MainForm(TorrentClientAgent a) {
        super();
		final String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        this.myAgent = a;
        try {
            xmlFileName = new File(".").getCanonicalPath() + File.separator;
            xmlFileName += File.separator + DATA_FILE;
            System.out.println("xmlFileName: " + xmlFileName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            myAgent.doDelete();
            System.exit(-1);
        }


        String title = "SMA Torrent Client";
        if (a != null)
            title = this.myAgent.getLocalName() + ": " + title;
        setTitle(title);
        Container cp = getContentPane();
        this.setSize(600, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new MainFormAdapter());

        cp.setLayout(new BorderLayout());
        pnlMenuToolBar = new JPanel(new GridLayout(2, 1));
        createMenu(pnlMenuToolBar);
        createToolBar(pnlMenuToolBar);
        cp.add(pnlMenuToolBar, BorderLayout.NORTH);

        tb = new JTable();
        tb.setSize(this.getWidth() - 30, this.getHeight());
        tb.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cp.add(tb, BorderLayout.CENTER);
        cp.add(new JScrollPane(tb));
        createTableModel();
        pnlBottom = new JPanel(new GridLayout(1, 2));
        pnlBottom.setSize(this.getWidth(), 20);
        progressBar = new JProgressBar();
        progressBar.setSize(200, 20);
        pnlBottom.add(progressBar);
        lbStatus = new JLabel("iTorrent");
        lbStatus.setBorder(new EtchedBorder());
        pnlBottom.add(lbStatus);
        cp.add(pnlBottom, BorderLayout.SOUTH);

        registerTorrents();
    }

    public static void main(final String args[]) {
        String msg = "Use the shell scripts in the project's root folder to create clients/agents and start them!";
        System.out.println(msg);
        JOptionPane.showMessageDialog(null,
                msg, "Warning", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
    }

    /**
     * Set the text of the status label
     *
     * @param status
     */
    public void setStatus(final String status) {
        lbStatus.setText(status);
    }

    public void setMissingPiecesCountStatus() {
        int row = tb.getSelectedRow();
        if (row >= 0)
            setStatus("Missing pieces count: " +
                    dataModel.missingPiecesCount(row));
    }

    private void tableSelectionChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return; // if you don't want to handle intermediate selections

		final ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
		final int selectedIndex = rowSM.getMinSelectionIndex();
        if (selectedIndex >= 0) {
            if (dataModel.getRow(selectedIndex).sameStatus(Torrent.STARTED))
                btnStartPause.setText(PAUSE);
            else btnStartPause.setText(PLAY);
            setMissingPiecesCountStatus();
        }

		final boolean enable = selectedIndex >= 0;
        btnRemove.setEnabled(enable);
        btnStartPause.setEnabled(enable);
    }

    public XmlTorrentDataModel getDataModel() {
        return dataModel;
    }

    /**
     * Create a Swing JTable loading data from a XML file
     *
     * @see MainForm#xmlFileName
     */
    private void createTableModel() {
        try {
            dataModel = new XmlTorrentDataModel(xmlFileName);
            tb.setModel(dataModel);

			final ListSelectionModel rowSM = tb.getSelectionModel();
            rowSM.addListSelectionListener(this::tableSelectionChanged);
        } catch (ParserConfigurationException | IOException e) {
            JOptionPane.showMessageDialog(
                    this, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the tool bar of the main window
     */
    private void createToolBar(final JPanel panel) {
        btnStartPause = new JButton(PLAY);
        btnStartPause.setEnabled(false);
        btnStartPause.addActionListener(e -> playPauseTorrent());

        btnAdd = new JButton("Add Torrent File");
        btnAdd.addActionListener(e -> addTorrent(false));

        btnAddToSeed = new JButton("Add Torrent File to Seed");
        btnAddToSeed.addActionListener(e -> addTorrent(true));
        btnRemove = new JButton("Remove Torrent");
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(e -> removeTorrent());

        toolBar = new JToolBar();
        toolBar.add(btnAdd);
        toolBar.add(btnAddToSeed);
        toolBar.add(btnStartPause);
        toolBar.add(btnRemove);
        panel.add(toolBar);
    }

    private void removeTorrent() {
		final int row = tb.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a torrent first", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String infoHash = dataModel.getRow(row).getInfoHash();
            dataModel.removeRow(row);
            dataModel.saveXml();

            myAgent.deregisterTorrent(infoHash);
            System.out.println(myAgent.getLocalName() + ": Torrent removed");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (FIPAException e) {
            String msg = StrUtils.breakString(e.getLocalizedMessage(), 100);
            JOptionPane.showMessageDialog(
                    this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void playPauseTorrent() {
		final int row = tb.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a torrent first", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            if (btnStartPause.getText().compareToIgnoreCase(PAUSE) == 0) {
                dataModel.setValueAt(Torrent.STOPPED, row, 3);
                myAgent.deregisterTorrent(dataModel.getRow(row).getInfoHash());
                btnStartPause.setText(PLAY);
            } else {
                dataModel.setValueAt(Torrent.STARTED, row, 3);
                myAgent.registerTorrent(dataModel.getRow(row));
                btnStartPause.setText(PAUSE);
            }
        } catch (FIPAException e) {
            String msg = StrUtils.breakString(e.getLocalizedMessage(), 100);
            JOptionPane.showMessageDialog(
                    this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the menu bar of the main window
     */
    private void createMenu(final JPanel panel) {
        mnuFile = new JMenu("File");
        mnuHelp = new JMenu("Help");
        menuBar = new JMenuBar();
        mnuAbout = new JMenuItem("About...");
        mnuAbout.addActionListener(arg0 -> {
			String msg = """
					SMA Torrent - The multi-agent Torrent Client
					Manoel Campos da Silva Filho
					Powered by JADE""";
			JOptionPane.showMessageDialog(mnuAbout, msg);
		});

        mnuHelp.add(mnuAbout);
        menuBar.add(mnuFile);
        menuBar.add(mnuHelp);
        panel.add(menuBar);
    }

    /**
     * Register the torrents loaded from XML config file
     * into Jade Directory Facilitator.
     */
    private void registerTorrents() {
        try {
            myAgent.registerTorrents(dataModel);
        } catch (FIPAException e) {
            String msg = StrUtils.breakString(e.getLocalizedMessage(), 100);
            System.out.println(e.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Add a new torrent to the torrent list.
     *
     * @param addToSeed If true, add a new torrent that the client
     *                  already has all parts, thus, it will seed this file.
     */
    private void addTorrent(final boolean addToSeed) {
		final var fc = new JFileChooser(".");
		final var filter = new FileNameExtensionFilter("Torrent Files (*.torrent)", "torrent");

        //fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			final String torrentFileName = fc.getSelectedFile().getAbsolutePath();
            try {
				final var is = new DataInputStream(new FileInputStream(torrentFileName));
				final var t = new Torrent(is);
				final String infoHash = t.getInfo().hash();

				final int numPieces =
                        Torrent.numPieces(
                                t.getInfo().getLength(),
                                t.getInfo().getPieceLength());
				final String bitField = Torrent.generateBitField(numPieces, addToSeed);
				final long downloaded = addToSeed ? t.getInfo().getLength() : 0;
                myAgent.registerTorrent(infoHash, bitField);
                dataModel.addRow(
                        t.getInfo(), torrentFileName, Torrent.STARTED, downloaded,
                        infoHash, bitField);
                myAgent.registerFindPeersBehaviour(dataModel.getRowCount() - 1);
                dataModel.saveXml();
            } catch (NoSuchAlgorithmException | InvalidTorrentException | IOException e) {
                JOptionPane.showMessageDialog(
                        this, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (FIPAException e) {
                String msg = StrUtils.breakString(e.getLocalizedMessage(), 100);
                JOptionPane.showMessageDialog(
                        this, msg, "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println(e.getMessage());
            }
		}
    }

    /**
     * Sub-class that implements the events of program main window.
     */
    private class MainFormAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent arg0) {
            System.out.println(APP_NAME + ": Torrent Client Gui closed.");
            if (myAgent != null)
                myAgent.doDelete();
        }
    }
}
