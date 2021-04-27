package com.manoelcampos.smatorrent.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import javax.swing.JOptionPane;

import com.manoelcampos.smatorrent.*;

import jade.domain.FIPAException;

import java.util.ArrayList;

/**Class that implements a agent behaviour that periodically
 * find peers that has the file that it needs.
 * This behaviour find peers for each torrent shared
 * by the application.
 * @author Manoel Campos da Silva Filho*/
public class FindPeersBehaviour extends TickerBehaviour {
	/**Number of peers wanted to be obtained from the DF.
	 * While the client don't have this number of peers to
	 * download pieces, this behaviour will search for 
	 * more peers.*/
	public  static final int numWant = 50;
	private static final long serialVersionUID = 2218351838341542642L;
	private int row;
	private XmlTorrentDataModel dm;
	private RequestPiecesBehaviour rpb; 
	private String infoHash;
	private long startTimeSeconds;
	
	/**Class constructor
	 * @param a The agent that will own this behaviour
	 * @param period Time interval to the behaviour be executed
	 * @param dm The Data Model of the Xml file that
	 * contains the torrents loaded in the application.
	 * @param infoHash The SHA1 20 bytes hash of the torrent file info field,
	 * that identify uniquelly a torrent. This identify the torrent
	 * that will be searched for other peers that have the same torrent to share*/
	public FindPeersBehaviour(Agent a, long period, XmlTorrentDataModel dm,
	  String infoHash) {
		super(a, period);
		this.dm = dm;
		this.infoHash = infoHash;
		this.row = dm.findTorrent(infoHash);
		this.startTimeSeconds = System.currentTimeMillis()/1000;
	}
	
	/**Set in the XML of registered files, the
	 * elapsed time of the torrent registered for this
	 * behaviour*/
	public void setElapsedTime() {
		double newTimeSeconds = System.currentTimeMillis()/1000 - startTimeSeconds;
		double elapsedMinutes = newTimeSeconds/60; 
		dm.addElapsedMinutes(elapsedMinutes, row);
		startTimeSeconds = System.currentTimeMillis()/1000;
	}
		
	@Override
	/**The periodic action executed by the behaviour.
	 * This method search for agents (peers) that has
	 * the same torrent file (identified by the
	 * piecesHex field - the pieces field into torrent file,
	 * in hexadecimal format) to share.*/
	public void onTick() {
		if(dm.isCompleted(row)) {
			this.stop();
			return;
		}
		setElapsedTime();
		
		/**List of obtained peers with has the torrent needed.*/
		ArrayList<AID> peers = new ArrayList<AID>();
		
		/*If the download is not complete and the number of peers
		 * obtained is less than the number of peers wanted,
		 * search for more peers.*/
		if(dm.getRow(row).getPeersCount() < numWant) {
			System.out.println(myAgent.getLocalName() + 
				": Finding peers for the torrent       " + infoHash);
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			//search for agents with services that have the name of FILE_SHARING
			sd.setName(TorrentClientAgent.FILE_SHARING);
			/* Search for agents with services that have the type infoHash.
			 * This is the hash of the info field of torrent file, 
			 * in hexadecimal string format, that
			 * indicates a torrent shared. Thus, search for agents
			 * that has the same torrent to share.*/			
			sd.setType(infoHash);
			template.addServices(sd);
			try {	
			   DFAgentDescription[] peersDesc = DFService.search(myAgent, template);
			   for(DFAgentDescription peerDesc: peersDesc)
				   //The request don't be sent to it self.
				   if (peerDesc.getName().compareTo(myAgent.getAID())!=0)
				       peers.add(peerDesc.getName());
			   
			   if(peers.size() > 0) {
				   //step==-1 indicate an error in the class constructor
				   if(rpb==null || rpb.getStep()==-1) {
					 rpb = new RequestPiecesBehaviour(
							 	this,
								myAgent, peers, 
								infoHash, dm.getRow(row).getBitField(),
								dm.getRow(row).getPieceLength(), dm);
				     myAgent.addBehaviour(rpb);
				   }
				   else rpb.addNewPeers(peers);
				   System.out.println(myAgent.getLocalName() + 
							": " + peers.size() + " peers found for the torrent " + infoHash);
			   }
			   else
					System.out.println(myAgent.getLocalName() + 
							": Neither peers found for the torrent " + infoHash);
			} catch(FIPAException e) {
				String msg = StrUtils.breakString(e.getMessage(), 100);
				JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
}
