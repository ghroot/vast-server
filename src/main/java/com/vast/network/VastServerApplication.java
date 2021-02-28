package com.vast.network;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.ServerApplication;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.vast.data.*;
import com.vast.VastWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VastServerApplication extends ServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(VastServerApplication.class);

	private String snapshotFile;
	private int numberOfPeersToSimulate;
	private long randomSeed;
	private boolean showMonitor;
	private Metrics metrics;

	private List<IncomingRequest> incomingRequests;
	private List<PeerListener> peerListeners;

	private VastWorld world;
	private Thread worldThread;

	public VastServerApplication(String snapshotFile, int numberOfPeersToSimulate, long randomSeed, boolean showMonitor, Metrics metrics) {
		this.snapshotFile = snapshotFile;
		this.numberOfPeersToSimulate = numberOfPeersToSimulate;
		this.randomSeed = randomSeed;
		this.showMonitor = showMonitor;
		this.metrics = metrics;
	}

	@Override
	protected void setup() {
		incomingRequests = new ArrayList<>();
		peerListeners = new ArrayList<>();

		Random random = randomSeed >= 0 ? new Random(randomSeed) : new Random();
		WorldConfiguration worldConfiguration = new WorldConfiguration("world.json");
		Items items = new Items("items.json");
		Recipes recipes = new Recipes("recipes.json", items);

		world = new VastWorld(this, snapshotFile, random, showMonitor, metrics, worldConfiguration,
				items, recipes);
		worldThread = new Thread(world, "World");
		worldThread.setPriority(Thread.MAX_PRIORITY);
		worldThread.start();

		if (numberOfPeersToSimulate > 0) {
			for (int i = 0; i < numberOfPeersToSimulate; i++) {
				String name = "fakePeer" + (i + 1);
				VastPeer fakePeer = new FakePeer(this, name, metrics);
				for (PeerListener peerListener : peerListeners) {
					peerListener.peerAdded(fakePeer);
				}
				logger.info("Added fake peer: {}", name);
			}
		}
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
		for (PeerListener peerListener : peerListeners) {
			peerListener.peerAdded(peer);
		}
		logger.info("Added peer: {} ({})", peer.getName(), peer.getId());
		return peer;
	}

	public void onPeerDisconnected(VastPeer peer, DisconnectReason disconnectReason, String detail) {
		for (PeerListener peerListener : peerListeners) {
			peerListener.peerRemoved(peer);
		}
		logger.info("Removed peer: {} ({}) (reason: {}, detail: {})", peer.getName(), peer.getId(), disconnectReason, detail);
	}

	public void onPeerReceived(VastPeer peer, RequestMessage requestMessage) {
		synchronized (incomingRequests) {
			incomingRequests.add(new IncomingRequest(peer, requestMessage));
		}
		logger.debug("Got request message from peer: {} from {} ({})", requestMessage, peer.getName(), peer.getId());
	}

	public void addPeerListener(PeerListener peerListener) {
		peerListeners.add(peerListener);
	}

	public void removePeerListener(PeerListener peerListener) {
		peerListeners.remove(peerListener);
	}
}
