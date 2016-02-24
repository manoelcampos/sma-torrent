package com.manoelcampos.smatorrent.behaviours;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.manoelcampos.smatorrent.*;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.*;

/**A cyclic behaviour that waits for requests
 * for pieces from others peers.
 * @author Manoel Campos da Silva Filho*/
public class ReceiveRequestsBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 2210622182075000149L;
	private XmlTorrentDataModel dm;

	/**Class constructor
	 * @param a The agent that will own this behaviour
	 * @param dm The Data Model of the Xml file that
	 * contains the torrents loaded in the application.*/
	public ReceiveRequestsBehaviour(TorrentClientAgent a, 
	  XmlTorrentDataModel dm) {
		super(a);
		this.dm = dm;
	}
	
	/**Send a propose ou refuse message to a request
	 * of a piece (a Call For proposal - CFP),
	 * informing to the requester, the pieces
	 * that I have. This is informed by a bit field
	 * message of the torrent protocol.*/
	private void attendCPF(ACLMessage receivedMsg) {
		System.out.println(myAgent.getLocalName() + 
				": CPF (hadshake) received from " +
				receivedMsg.getSender().getLocalName() +
				". Msg = " + receivedMsg.getContent());

		//handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
		String[] handshake = Torrent.splitMessage(receivedMsg.getContent());
		int row = dm.findTorrent(handshake[3]);
		ACLMessage reply = receivedMsg.createReply();
		reply.setReplyWith(handshake[3]);
		if(row > -1) {
			//bitfield msg: <len=0001+X><id=5><bitfield> 
			String bitFieldMsg =  dm.getBitField(row);
			bitFieldMsg = 
				"<len=0001"+bitFieldMsg.length()+"><id=5>"+
				"<" + bitFieldMsg + ">";
			/* Reply with the bitField, indicanting the pieces 
			 * of the file that I have.*/
			reply.setContent(bitFieldMsg);
			reply.setPerformative(ACLMessage.PROPOSE);
		//If the agent doesn't have this torrent to share, send a error msg
		}else {
			reply.setContent("I don't have this torrent to share: "+handshake[3]);
			reply.setPerformative(ACLMessage.REFUSE);
		}
		
		myAgent.send(reply);
		switch(reply.getPerformative()) {
			case ACLMessage.PROPOSE:
				System.out.println(myAgent.getLocalName() + 
					": Propose/bitfield msg sended '" +
					reply.getContent() + "' to " + 
					receivedMsg.getSender().getLocalName());
			break;
			case ACLMessage.REFUSE:
				System.out.println(myAgent.getLocalName() + 
					": Refuse msg sended '" +
					reply.getContent() + "' to " + 
					receivedMsg.getSender().getLocalName());
			break;
		}
	}

	/**Send the requested piece for the requester peer.
	 * @param receivedMsg The message received from a peer, with
	 * accepting the proposal, indicating the requested piece, and that
	 * will be used to send the reply.*/
	private void sendPiece(ACLMessage receivedMsg) {
		//TODO: Como o cliente que recebe a solicitação saberá
		//de qual torrent é a parte solicitada, pois na msg
		//request não há nenhuma informação sobre o torrent
		
		//request msg: <len=0013><id=6><index><begin><length>
		String requestMsg = receivedMsg.getContent();
		String splited[] = Torrent.splitMessage(requestMsg);
		String begin=splited[3];
		int pieceLength= Integer.parseInt(splited[4]);
		int pieceNumber = Integer.parseInt(splited[2]);
		
		ACLMessage reply = receivedMsg.createReply();
		String infoHash = receivedMsg.getReplyWith();
		reply.setReplyWith(infoHash);
		/* All messages received are marked with infoHash in the
		 * replyTo ACL Message field to the 
		 * peer identify, in all messages sent, the torrent
		 * required*/
		
		int row = dm.findTorrent(infoHash);
		
		if(row > -1) {
			try {
				String path = new File(".").getCanonicalPath();
				path += File.separator + "bin" + File.separator;
				String fileName = path+dm.getDescription(row);
				File file = new File(fileName);
				if(!file.exists()) {
					System.out.println(myAgent.getLocalName()+
						": File not found '" + fileName + "'");
					return;
				}
				SharedFile sf = new SharedFile(fileName);
				//X is the length of the block
				//piece msg: <len=0009+X><id=7><index><begin><block>
				byte[] piece = sf.getPiece(pieceNumber, pieceLength);
				byte[] terminator = {'>'};
				String aux = 
					"<len=0009+"+pieceLength+"><id=7>" +
					"<"+pieceNumber+"><"+begin+"><";
				byte[] pieceMsg = 
					new byte[aux.length() + piece.length + 1];
				
				//Transfer the content as a byte array
				System.arraycopy(
					aux.getBytes(), 0, pieceMsg, 0, aux.length());
				System.arraycopy(
					piece, 0, pieceMsg, aux.length(), piece.length);
				System.arraycopy(
					terminator, 0, pieceMsg, pieceMsg.length-1, 1);
					 
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContentObject(pieceMsg);
				sf.close();
			} catch(IOException e) {
				JOptionPane.showMessageDialog(
					null, e.getLocalizedMessage(), "Error", 
					JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else {
			String msg = "I don't have this torrent to share: " + infoHash;
			reply.setPerformative(ACLMessage.REFUSE);
			reply.setContent(msg);
		}

		myAgent.send(reply);
		switch(reply.getPerformative()) {
			case ACLMessage.INFORM:
				System.out.println(
					myAgent.getLocalName() + ": Piece " + pieceNumber + 
					" sended to " + receivedMsg.getSender().getLocalName());
			break;
			case ACLMessage.REFUSE:
				System.out.println(myAgent.getLocalName() + 
					": Refuse msg sended '" +
					reply.getContent() + "' to " + 
					receivedMsg.getSender().getLocalName());
			break;
		}
	}
	
	@Override
	/**Executes the action of the behaviour, waiting for Call For Proposal 
	 * (handshake msgs) or Acccept Proposal (a request of a piece).*/
	public void action() {
		System.out.println(myAgent.getLocalName() + ": Waiting request msgs"); 
		MessageTemplate template =
		   MessageTemplate.or(
		      MessageTemplate.MatchPerformative(ACLMessage.CFP),
		      MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)
		     );
		
		ACLMessage msg = myAgent.receive(template);
		if(msg != null) {
			switch(msg.getPerformative()) {
				case ACLMessage.CFP: attendCPF(msg); break;
				case ACLMessage.ACCEPT_PROPOSAL: sendPiece(msg); break;
			}
		}
		else block();
	}

}
