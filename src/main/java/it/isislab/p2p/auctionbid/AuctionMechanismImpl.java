package main.java.it.isislab.p2p.auctionbid;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class AuctionMechanismImpl implements AuctionMechanism{
	final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT=4000;
	
	final private ArrayList<String> s_topics=new ArrayList<String>();

	public AuctionMechanismImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception
	{
		 peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		_dht = new PeerBuilderDHT(peer).start();	
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}
		
		peer.objectDataReply(new ObjectDataReply() {
			
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}
	
	public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty()) 
				_dht.put(Number160.createHash(_auction_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String checkAuction(String _auction_name){

	}

	public String placeAbid(String _auction_name, double _bid_amount){

	}

	public boolean leaveNetwork() {
		
		for(String topic: new ArrayList<String>(s_topics)) unsubscribeFromTopic(topic);
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}
}
