package com.manoelcampos.smatorrent;

import java.io.IOException;

import javax.swing.JOptionPane;

import com.manoelcampos.smatorrent.behaviours.*;

import jade.core.Agent;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

/**Implementa o agente que realiza as operações de seeder ou leecher
 * no cliente de torrent.
 * @author Manoel Campos da Silva Filho
 */
public class TorrentClientAgent extends Agent {
	public static final String FILE_SHARING = "Torrent-File-Sharing";
	private DFAgentDescription dfd;
	/**Indicates if the DFAGentDescription is already registered
	 * in the Jade Directory Facilitator.*/
	private boolean dfAgentDescriptionRegistered = false; 
	
	private static final long serialVersionUID = -789313031046973140L;
	private MainForm gui;
	private String peerId;
	
	/**
         * Returns the 20 bytes peer identification.
	 * This value is randomically setted in the setup
	 * method.
         * @return  */
	public String getPeerId() { return peerId; }
	public MainForm getGui() { return gui; }
	
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
		
		XmlTorrentDataModel dm = gui.getDataModel();
		addBehaviour(new ReceiveRequestsBehaviour(this, dm));
		for(int row=0; row < dm.getRowCount(); row++) {
			if(!dm.isCompleted(row))
		       registerFindPeersBehaviour(row);
		}
	}
	
	public void registerFindPeersBehaviour(int row) {
		XmlTorrentDataModel dm = gui.getDataModel();
		addBehaviour(new FindPeersBehaviour(this, 5000, dm, dm.getInfoHash(row)));
	}
	
	/**Register a torrent file in the Jade Directory Facilitator 
	 * to indicate that the client agent has the torrent
	 * to share with others and/or to get pieces from others
	 * agents.
	 * @param infoHash The hash of torrent file info field, in hexadecimal format,
	 *  to be registered in Jade Directory Facilitator. This hash
	 *  identify one torrent uniquely.
	 * @param bitField A string where positions with 1 indicates 
	 * the pieces that the client already has.
	 * @param downloaded The amount of bytes already downloaded for the torrent from
	 * others client agents.
	 * @param uploaded The amount of bytes already uploaded for the torrent
	 * to others client agents.
	 * @throws FIPAException When the torrent cannot be registered in Jade DF because a FIPA error.*/
	public void registerTorrent(String infoHash, String bitField, int downloaded, int uploaded) 
	  throws FIPAException {
		ServiceDescription sd = new ServiceDescription();
		sd.setName(FILE_SHARING);
		sd.setType(infoHash);
		sd.addProperties(new Property("downloaded", downloaded));
		sd.addProperties(new Property("uploaded", uploaded));
		sd.addProperties(new Property("bitfield", bitField));
		dfd.addServices(sd);
		if(!dfAgentDescriptionRegistered)
           DFService.register(this, dfd);
		else DFService.modify(this, dfd); 
        System.out.println(this.getLocalName()+ 
        	": Service registered for the torrent  " + infoHash);
        dfAgentDescriptionRegistered = true;
	}
	
	/**Register all torrents included in the XML config file,
	 * of client application, in the Jade Directory Facilitator.
	 * @param dm The XmlTorrentDataModel that loads the XML config file.
	 * This file contains the torrents added to the client program.
	 * The method get information about torrents from this file to register
	 * the torrents in the Jade Directory Facilitator.
	 * 
	 * @throws FIPAException When the torrent cannot be registered in Jade DF.*/
	public void registerTorrents(XmlTorrentDataModel dm)
	  throws FIPAException {
		for(int r = 0; r < dm.getRowCount(); r++) {
			if(dm.getStatus(r).compareToIgnoreCase(Torrent.STARTED)==0)
			    registerTorrent(
			    	dm.getInfoHash(r), dm.getBitField(r), 
			    	dm.getDownloaded(r), dm.getUploaded(r));
		}
	}
	
	/**Deregister a torrent from the Jade Directory Facilitator.
	 * This method must be called when the user pause/stop
	 * a torrent in the graphical interface, indicating
	 * that don't want to share the torrent for while,
	 * or when the user remove the torrent from torrent list
	 * in the graphical interface.
	 * @param infoHash The hash of torrent file info field, in hexadecimal format,
	 *  to be deregistered in Jade Directory Facilitator. This hash
	 *  identify one torrent uniquely.
	 * @throws FIPAException When the torrent cannot be registered in Jade DF because a FIPA error.*/
	public void deregisterTorrent(String infoHash) throws FIPAException {
		DFAgentDescription dfDesc = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		dfDesc.setName(getAID());

		sd.setName(FILE_SHARING);
		sd.setType(infoHash);
		dfDesc.removeServices(sd);
		DFService.modify(this, dfDesc); 
		System.out.println(getLocalName()+ ": Service deregistered");
	}
	
        @Override
	protected void takeDown() {
		try {
		  gui.getDataModel().saveXml();
		} catch(IOException e) {
			JOptionPane.showMessageDialog(
			    gui, e.getLocalizedMessage(), 
				"Error", JOptionPane.ERROR_MESSAGE);
		}

		System.out.println(this.getLocalName() + ": Agent destroyed.");
		try {
		   DFService.deregister(this);
		   System.out.println(this.getLocalName()+ ": Services deregistered.");
		}
		catch(FIPAException e) {
		   System.out.println(
				this.getLocalName()+": Error deregistering services.\n" +
				e.getMessage());
		}
		gui.dispose();
	}
}
