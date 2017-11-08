package test;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.ServerApplication;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyServerApplication extends ServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(MyServerApplication.class);

	private boolean showMonitor;

	private List<MyPeer> peers;
	private List<IncomingRequest> incomingRequests;

	private MyWorld world;
	private Thread worldThread;

	public MyServerApplication(boolean showMonitor) {
		this.showMonitor = showMonitor;
	}

	@Override
	protected void setup() {
		peers = new ArrayList<MyPeer>();
		incomingRequests = new ArrayList<IncomingRequest>();

		world = new MyWorld(this, showMonitor);
		worldThread = new Thread(world, "World");
		worldThread.start();

		// TODO: Add fake peer for testing
//		synchronized (peers) {
//			for (int i = 0; i < 1500; i++) {
//				String name = "fakePeer" + (i + 1);
//				peers.add(new FakePeer(this, name));
//				logger.info("Added fake peer: {}", name);
//			}
//		}
	}

	public List<MyPeer> getPeers() {
		return peers;
	}

	public List<IncomingRequest> getIncomingRequests() {
		return incomingRequests;
	}

	@Override
	protected void tearDown() {
		world.destroy();
	}

	@Override
	protected ClientPeer createPeer(InitialRequest initialRequest, NetworkPeer networkPeer) {
		String name = new String(initialRequest.getCustomData(), StandardCharsets.UTF_8);
		MyPeer peer = new MyPeer(initialRequest, networkPeer, this, name);
		synchronized (peers) {
			peers.add(peer);
		}
		logger.info("Added peer: {}", name);
		return peer;
	}

	public void onPeerDisconnected(MyPeer peer, DisconnectReason disconnectReason, String detail) {
		synchronized (peers) {
			peers.remove(peer);
		}
		logger.info("Removed peer: {} (reason: {}, detail: {})", peer.getName(), disconnectReason, detail);
	}

	public void onPeerReceived(MyPeer peer, RequestMessage requestMessage) {
		synchronized (incomingRequests) {
			incomingRequests.add(new IncomingRequest(peer, requestMessage));
		}
		logger.info("Got request message from peer: {} from {}", requestMessage, peer.getName());
	}
}
