package com.manoelcampos.smatorrent;

import com.manoelcampos.smatorrent.behaviours.FindPeersBehaviour;
import com.manoelcampos.smatorrent.behaviours.ReceiveRequestsBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import javax.swing.*;
import java.io.IOException;

/**
 * Implementa o agente que realiza as operações de seeder ou leecher
 * no cliente de torrent.
 *
 * @author Manoel Campos da Silva Filho
 */
public class TorrentClientAgent extends Agent {
    public static final String FILE_SHARING = "Torrent-File-Sharing";
    private static final long serialVersionUID = -789313031046973140L;
    private DFAgentDescription dfd;

    /**
     * Indicates if the DFAGentDescription is already registered
     * in the Jade Directory Facilitator.
     */
    private boolean dfAgentDescriptionRegistered = false;

    private MainForm gui;
    private String peerId;

    /**
     * Returns the 20 bytes peer identification.
     * This value is randomly set in the setup method.
     *
     * @return
     */
    public String getPeerId() {
        return peerId;
    }

    public MainForm getGui() {
        return gui;
    }

    @Override
    protected void setup() {
        dfd = new DFAgentDescription();
        dfd.setName(getAID());

        gui = new MainForm(this);
        gui.setVisible(true);

        String rand = StrUtils.randomStringNumbers(15);
        //IT is the abreviation for iTorrent
        //The peerId must have 20 characters length
        peerId = "IT" + MainForm.APP_VERSION + rand;

        final XmlTorrentDataModel dm = gui.getDataModel();
        addBehaviour(new ReceiveRequestsBehaviour(this, dm));
        for (int row = 0; row < dm.getRowCount(); row++) {
            if (!dm.isCompleted(row))
                registerFindPeersBehaviour(row);
        }
    }

    public void registerFindPeersBehaviour(final int row) {
        final XmlTorrentDataModel dm = gui.getDataModel();
        addBehaviour(new FindPeersBehaviour(this, 5000, dm, dm.getRow(row).getInfoHash()));
    }

    public void registerTorrent(final String infoHash, final String bitField) throws FIPAException {
        final TorrentConfigEntry t = new TorrentConfigEntry();
        t.setInfoHash(infoHash).setBitField(bitField);
        registerTorrent(t);
    }

    /**
     * Register a torrent file in the Jade Directory Facilitator
     * to indicate that the client agent has the torrent
     * to share with others and/or to get pieces from others
     * agents.
     *
     * @throws FIPAException When the torrent cannot be registered in Jade DF because a FIPA error.
     */
    public void registerTorrent(final TorrentConfigEntry torrent) throws FIPAException {
        final var sd = new ServiceDescription();
        sd.setName(FILE_SHARING);
        sd.setType(torrent.getInfoHash());
        sd.addProperties(new Property("downloaded", torrent.getDownloaded()));
        sd.addProperties(new Property("uploaded", torrent.getUploaded()));
        sd.addProperties(new Property("bitfield", torrent.getBitField()));
        dfd.addServices(sd);
        if (!dfAgentDescriptionRegistered)
            DFService.register(this, dfd);
        else DFService.modify(this, dfd);
        System.out.println(this.getLocalName() +
                ": Service registered for the torrent  " + torrent.getInfoHash());
        dfAgentDescriptionRegistered = true;
    }

    /**
     * Register all torrents included in the XML config file,
     * of client application, in the Jade Directory Facilitator.
     *
     * @param dm The XmlTorrentDataModel that loads the XML config file.
     *           This file contains the torrents added to the client program.
     *           The method get information about torrents from this file to register
     *           the torrents in the Jade Directory Facilitator.
     * @throws FIPAException When the torrent cannot be registered in Jade DF.
     */
    public void registerTorrents(final XmlTorrentDataModel dm) throws FIPAException {
        for (int r = 0; r < dm.getRowCount(); r++) {
            if (dm.getRow(r).getStatus().compareToIgnoreCase(Torrent.STARTED) == 0)
                registerTorrent(dm.getRow(r));
        }
    }

    /**
     * Deregister a torrent from the Jade Directory Facilitator.
     * This method must be called when the user pause/stop
     * a torrent in the graphical interface, indicating
     * that don't want to share the torrent for while,
     * or when the user remove the torrent from torrent list
     * in the graphical interface.
     *
     * @param infoHash The hash of torrent file info field, in hexadecimal format,
     *                 to be deregistered in Jade Directory Facilitator. This hash
     *                 identify one torrent uniquely.
     * @throws FIPAException When the torrent cannot be registered in Jade DF because a FIPA error.
     */
    public void deregisterTorrent(final String infoHash) throws FIPAException {
        final var dfDesc = new DFAgentDescription();
        final var sd = new ServiceDescription();
        dfDesc.setName(getAID());

        sd.setName(FILE_SHARING);
        sd.setType(infoHash);
        dfDesc.removeServices(sd);
        DFService.modify(this, dfDesc);
        System.out.println(getLocalName() + ": Service deregistered");
    }

    @Override
    protected void takeDown() {
        try {
            gui.getDataModel().saveXml();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    gui, e.getLocalizedMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        System.out.println(this.getLocalName() + ": Agent destroyed.");
        try {
            DFService.deregister(this);
            System.out.println(this.getLocalName() + ": Services deregistered.");
        } catch (FIPAException e) {
            System.out.println(
                    this.getLocalName() + ": Error deregistering services.\n" +
                            e.getMessage());
        }
        gui.dispose();
    }
}
