package com.manoelcampos.smatorrent.behaviours;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import com.manoelcampos.smatorrent.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**For each torrent shared for the client, must there be
 * a behaviour of this to request from the peers
 * to send a piece. This behaviour is added from the
 * FindPeersBehaviour, for each torrent shared by the application.
 * @author Manoel Campos da Silva Filho
 * @see FindPeersBehaviour*/
public class RequestPiecesBehaviour extends SimpleBehaviour {
	/**List of obtained peers with has the torrent needed.*/
	private ArrayList<AID> peers = new ArrayList<AID>();
	private static final long serialVersionUID = -5926492582398379701L;
	private String infoHash;
	private int step = 0;
	private int piecesRequestCount=0;
	private String myBitField;
	private long pieceLength;
	private String tmpFileName;
	private String originalFileName;
	private String path;
	private XmlTorrentDataModel dm;
	private int row;
	private int requestResponsesCount=0;
	private SharedFile sf;
	private FindPeersBehaviour fpb;

	/**Class constructor
	 * @param fpb The behaviour that instanciate this behaviour
	 * @param a The agent that will own this behaviour
	 * @param peers A ArrayList of the AID of the peers that has 
	 * the same torrent that the behaviour needs.
	 * @param infoHash The SHA1 20 bytes hash of the torrent file info field,
	 * that identify uniquelly a torrent. This identify the torrent
	 * that will be searched for other peers to get pieces from.
	 * @param myBitField The bit field of the torrent file, identified
	 * by the infoHash, that indicates the pieces the client alredy has.
	 * @param pieceLength The length of each piece of the torrent file,
	 * that will be used to determine the begin of a piece in the shared file.
	 * @param dm The Data Model of the Xml file that
	 * contains the torrents loaded in the application.*/
	public RequestPiecesBehaviour(
			FindPeersBehaviour fpb, 
			Agent a, ArrayList<AID> peers,
			String infoHash, String myBitField, long pieceLength,
			XmlTorrentDataModel dm) {
		super(a);
		this.peers = peers;
		this.infoHash = infoHash;
		this.myBitField = myBitField;
		this.pieceLength = pieceLength;
		this.dm = dm;
		this.row = dm.findTorrent(infoHash);
		this.fpb = fpb;

		try {
		    path = new File(".").getCanonicalPath();
		    path += File.separator + "bin" + File.separator;
		    originalFileName = path+dm.getDescription(row);
		    tmpFileName = originalFileName + ".tmp";
		    File file = new File(tmpFileName);

		    sf = new SharedFile(file);
		} catch(IOException e) {
			step=-1; //to finalize the behaviour on the done method
			JOptionPane.showMessageDialog(null, 
				e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**Add new peers to the internal behaviour peers list.
	 * The method don't add duplicated peers.
	 * The peers existing in the internal peers list
	 * and which doesn't exist in the received peers list
	 * (new peers list) will be removed from the internal peers list,
	 * because if they don't exists 
	 * @param newPeers The ArrayList of new peers to add to internal list.
	 * */
	public void addNewPeers(ArrayList<AID> newPeers) {
		for(AID peer: newPeers) {
			if(peers.indexOf(peer)==-1)
				peers.add(peer);
		}
	}
	
	/**Get the current step of the behaviour.
	 * If the step is equals -1, indicates that
	 * an error ocurred in the class constructor*/
	public int getStep() { return step; } 
	
	/**Returns the count of peers in the peer list*/
	public int getPeersCount() { return peers.size(); }

	/**Receive a refuse message indicating that the remote
	 * peer doesn't have the requested torrent to share.
	 * @param receivedMsg The message received from a peer.*/
	private void receiveRefuse(ACLMessage receivedMsg) {
		requestResponsesCount++;
		System.out.println(myAgent.getLocalName() + 
				": Refuse received from " + 
				receivedMsg.getSender().getLocalName() + ": " +
				receivedMsg.getContent());
	}
	
	/**Request a piece to a peer.
	 * @param receivedMsg The message received from a peer, with
	 * a proposal, indicating the pieces that it has, and that
	 * will be used to send a reply.
	 * @param pieceNumber The number of the piece to be requested.*/
	private void requestPiece(ACLMessage receivedMsg, int pieceNumber) {
		ACLMessage reply = receivedMsg.createReply();
		
		//request msg: <len=0013><id=6><index><begin><length>
		long pieceBegin = Torrent.pieceBegin(pieceNumber, pieceLength);
		/*Based on the default piece length, get the length of
		 * a specific piece.*/
		int aPieceLength =
			Torrent.getSpecificPieceLength(
				dm.getFileSize(row), pieceLength, pieceNumber);

		String requestMsg = 
			"<len=0013><id=6><"+pieceNumber+"><"+pieceBegin+"><"+aPieceLength+">";
		reply.setContent(requestMsg);
		reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
		reply.setReplyWith(infoHash);
		myAgent.send(reply);
		piecesRequestCount++;
		System.out.println(myAgent.getLocalName() + 
				": Accept proposal/request msg sended '" +
				requestMsg + "' to " + receivedMsg.getSender().getLocalName());
	}

	/**Wait for replies like PROPOSE (informing the
	 * pieces that the peer has) or INFORM (with
	 * a piece sended by a peer)*/
	private void waitReplies() {
		MessageTemplate template =
			MessageTemplate.or(
			  MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
			  MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		template = 
			MessageTemplate.or(
					template, 
					MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
		
		/*All messages sent are marked with infoHash in the
		 * replyTo ACL Message field to the remote
		 * peer identify, in all messages sent, the torrent
		 * required*/
		template = 
			MessageTemplate.or(
					template, 
					MessageTemplate.MatchInReplyTo(infoHash));
		ACLMessage msg = myAgent.receive(template);
		//ACLMessage msg = myAgent.receive();
		if(msg != null) {
			switch(msg.getPerformative()) {
				case ACLMessage.PROPOSE: receiveProposal(msg); break;
				case ACLMessage.INFORM:  receivePiece(msg); break;
				case ACLMessage.REFUSE:  receiveRefuse(msg); break;
			}
		}
		else {
			if(requestResponsesCount>=piecesRequestCount) {
				step=0;
				piecesRequestCount=0;
				requestResponsesCount=0;
				System.out.println(myAgent.getLocalName() + 
					": Preparing to send new CFP/handshake msg to get more pieces");
			}
			else block();
		}
	}
	
	/**Extract the first field of the received byte array with
	 * a piece message and returns the field
	 * @param pieceMsg A byte array with the piece message received
	 * from a remote peer.
	 * @param isTheLastField If true, indicates that
	 * in the byte array remains only the last field,
	 * simplify the method used to extract the last field.
	 * @return The first field extracted from the byte array
	 * @see RequestPiecesBehaviour#deleteFirstFieldReceivedPieceMsg(byte[], boolean)*/
	private byte[] getNextFieldReceivedPieceMsg(
	 byte[] pieceMsg, boolean isTheLastField) {
		if(isTheLastField) {
			byte[] field = new byte[pieceMsg.length-2];
			System.arraycopy(pieceMsg, 1, field, 0, field.length);
			return field;
		}
		else {
			byte[] field = new byte[pieceMsg.length];
			int i;
			//start in position 1 (ignoring the char '<')
			for(i = 1; i < pieceMsg.length; i++) {
				//stop in the char '>'
				if(pieceMsg[i]=='>')
					break;
				field[i-1]=pieceMsg[i];
			}
			
			//create a variable with the size of the first field
			//found in the pieceMsg, between < and >, but
			//don't add this characters to the field
			byte[] newField = new byte[i-1];
			//copy to the field variable only the characteres before the '>' 
			System.arraycopy(field, 0, newField, 0, newField.length);
			
			return newField;
		}
	}
	
	/**Delete the first field of the received byte array with
	 * a piece message and returns the byte array without the field
	 * @param pieceMsg A byte array with the piece message received
	 * from a remote peer.
	 * @param isTheLastField If true, indicates that
	 * in the byte array remains only the last field,
	 * simplify the method used to extract the last field.
	 * @return The byte array without the extracted field
	 * @see RequestPiecesBehaviour#getNextFieldReceivedPieceMsg(byte[], boolean)*/
	private byte[] deleteFirstFieldReceivedPieceMsg(
	 byte[] pieceMsg, boolean isTheLastField) {
		if(isTheLastField) {
			byte[] newPieceMsg = new byte[pieceMsg.length-2];
			System.arraycopy(pieceMsg, 1, newPieceMsg, 0, newPieceMsg.length);
			return newPieceMsg;
		}
		else {
			int i=0;
			while(pieceMsg[i]!='>')
				i++;
			//create a new variable do store the pieceMsg without
			//the first field
			byte[] newPieceMsg = new byte[pieceMsg.length - ++i];
			System.arraycopy(pieceMsg, i, newPieceMsg, 0, newPieceMsg.length);
			return newPieceMsg;
		}
	}	
	
	/**Receive a piece sended by a peer
	 * @param receivedMsg The message received from a peer, with
	 * a piece.*/
	private void receivePiece(ACLMessage receivedMsg) {
		int pieceNumber;
		try {
			//X is the length of the block
			//piece msg: <len=0009+X><id=7><index><begin><block>
			byte[] pieceMsg = (byte[])receivedMsg.getContentObject();
			
			pieceMsg=deleteFirstFieldReceivedPieceMsg(pieceMsg, false); //len field
			pieceMsg=deleteFirstFieldReceivedPieceMsg(pieceMsg, false); //id field
			
			pieceNumber = 
				Integer.parseInt(
					new String(getNextFieldReceivedPieceMsg(pieceMsg, false)));
			pieceMsg=deleteFirstFieldReceivedPieceMsg(pieceMsg, false);
			
			getNextFieldReceivedPieceMsg(pieceMsg, false); //begin field
			pieceMsg=deleteFirstFieldReceivedPieceMsg(pieceMsg, false);
			
			byte[] piece = getNextFieldReceivedPieceMsg(pieceMsg, true);

			String auxBitField = dm.getBitField(row);
			auxBitField = Torrent.setBitFieldPosition(auxBitField, pieceNumber);		

			dm.setValueAt(auxBitField, row, XmlTorrentDataModel.bitFieldCol);
		    dm.addDownloaded(piece.length, row);
		    dm.saveXml();
		    TorrentClientAgent a = (TorrentClientAgent)myAgent;
		    a.getGui().setMissingPiecesCountStatus();
		    long fileLength = dm.getFileSize(row);
		    int piecesCount = Torrent.numPieces(fileLength, pieceLength);
		    sf.writePiece(pieceNumber, piecesCount, piece);
		} catch(IOException e) {
			JOptionPane.showMessageDialog(
				    null, e.getLocalizedMessage(), 
				    "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (UnreadableException e) {
			JOptionPane.showMessageDialog(
				    null, e.getLocalizedMessage(), 
				    "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println(myAgent.getLocalName() + 
			": Piece " + pieceNumber + " received from " + 
			receivedMsg.getSender().getLocalName());
		requestResponsesCount++;
	}
	
	/**Receive a proposal message.*/
	private void receiveProposal(ACLMessage receivedMsg) {
		//bitfield msg: <len=0001+X><id=5><bitfield>
		String bitFieldMsg = receivedMsg.getContent();
		System.out.println(myAgent.getLocalName() + 
			": Proposal/bitfield msg received '" +
			bitFieldMsg + "' from " + receivedMsg.getSender().getLocalName());
		String peerBitField = Torrent.splitMessage(bitFieldMsg)[2];
		int pieceNumber = 
			Torrent.chooseRandomPiece(myBitField, peerBitField);
		if(pieceNumber != -1) {
			try {
				myBitField =
					Torrent.setBitFieldPosition(myBitField, pieceNumber);
				requestPiece(receivedMsg, pieceNumber); 
			} catch(IndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
				  null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			System.out.println(myAgent.getLocalName() + 
			": The peer " + receivedMsg.getSender().getLocalName() +
			" doesn't have any pieces that I don't have");
		}
	}
	
	@Override
	/**Implements the action of the behaviuor.*/
	public void action() {
		fpb.setElapsedTime();
		if(step==0) 
			sendCFP_Handshake();
		else waitReplies();
	}
	
	public boolean done() {
		//If step == -1, an error ocurred in the constructor
		boolean isDone = (step==-1) || dm.isCompleted(row);
		if(isDone) {
			System.out.println("Pieces received: " + requestResponsesCount);
			try {
			  sf.close();
			  long fileLength = dm.getFileSize(row);
			  SharedFile.reconstructFile(tmpFileName, fileLength, originalFileName);
			} catch(IOException e) {
				JOptionPane.showMessageDialog(null, 
					e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (InvalidBEncodedStringException e) {
				JOptionPane.showMessageDialog(null, 
					e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return isDone;
	}

	private void sendCFP_Handshake() {
		if(peers.size()==0)
			return;
		
		TorrentClientAgent a = (TorrentClientAgent)myAgent; 
		//handshake msg: <pstrlen><pstr><reserved><info_hash><peer_id>
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		String pstr = "BitTorrent protocol";
		String reserved = "00000000";
		StringBuffer  handshakeMsg = new StringBuffer(); 
		handshakeMsg.append("<"+pstr.length()+">");
		handshakeMsg.append("<"+pstr+">");
		handshakeMsg.append("<"+reserved+">");
		handshakeMsg.append("<"+infoHash+">");
		handshakeMsg.append("<"+a.getPeerId()+">");
		msg.setContent(handshakeMsg.toString());
		/**To the remote peer identify the torrent 
		 * in this message (despite exists a info_hash field
		 * in the CPF/handshake message) and in the future
		 * response messages*/
		msg.setReplyWith(infoHash);
		for(AID peer: peers) 
		   msg.addReceiver(peer);
		myAgent.send(msg);
		step++;
		System.out.println(myAgent.getLocalName() + 
				": CPF/handshake msg sended '" + handshakeMsg + "'");
	}

}
