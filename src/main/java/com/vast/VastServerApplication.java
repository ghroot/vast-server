package com.vast;

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

public class VastServerApplication extends ServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(VastServerApplication.class);

	private String snapshotFormat;
	private boolean showMonitor;
	private Metrics metrics;

	private List<VastPeer> peers;
	private List<IncomingRequest> incomingRequests;

	private VastWorld world;
	private Thread worldThread;

	public VastServerApplication(String snapshotFormat, boolean showMonitor, Metrics metrics) {
		this.snapshotFormat = snapshotFormat;
		this.showMonitor = showMonitor;
		this.metrics = metrics;
	}

	@Override
	protected void setup() {
		peers = new ArrayList<VastPeer>();
		incomingRequests = new ArrayList<IncomingRequest>();

		world = new VastWorld(this, snapshotFormat, showMonitor, metrics);
		worldThread = new Thread(world, "World");
		worldThread.start();

		// TODO: Add fake peer for testing
//		synchronized (peers) {
//			for (int i = 0; i < 500; i++) {
//				String name = "fakePeer" + (i + 1);
//				peers.add(new FakePeer(this, name, metrics));
//				logger.info("Added fake peer: {}", name);
//			}
//		}
	}

	public List<VastPeer> getPeers() {
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
		VastPeer peer = new VastPeer(initialRequest, networkPeer, this, name, metrics);
		synchronized (peers) {
			peers.add(peer);
		}
		logger.info("Added peer: {} ({})", peer.getName(), peer.getId());
		return peer;
	}

	public void onPeerDisconnected(VastPeer peer, DisconnectReason disconnectReason, String detail) {
		synchronized (peers) {
			peers.remove(peer);
		}
		logger.info("Removed peer: {} ({}) (reason: {}, detail: {})", peer.getName(), peer.getId(), disconnectReason, detail);
	}

	public void onPeerReceived(VastPeer peer, RequestMessage requestMessage) {
		synchronized (incomingRequests) {
			incomingRequests.add(new IncomingRequest(peer, requestMessage));
		}
		logger.debug("Got request message from peer: {} from {} ({})", requestMessage, peer.getName(), peer.getId());
	}
}
