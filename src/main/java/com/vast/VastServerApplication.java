package com.vast;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.framework.ServerApplication;
import com.nhnent.haste.protocol.messages.*;
import com.nhnent.haste.security.BigInteger;
import com.nhnent.haste.security.SHA256;
import com.nhnent.haste.transport.ApplicationPeer;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.nhnent.haste.transport.QoS;
import com.nhnent.haste.transport.state.ConnectionState;
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
		synchronized (peers) {
			for (int i = 0; i < 1500; i++) {
				String name = "fakePeer" + (i + 1);
				peers.add(new FakePeer(this, name));
				logger.info("Added fake peer: {}", name);
			}
		}
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
		VastPeer peer = new VastPeer(initialRequest, networkPeer, this, name);
		synchronized (peers) {
			peers.add(peer);
		}
		logger.info("Added peer: {}", name);
		return peer;
	}

	public void onPeerDisconnected(VastPeer peer, DisconnectReason disconnectReason, String detail) {
		synchronized (peers) {
			peers.remove(peer);
		}
		logger.info("Removed peer: {} (reason: {}, detail: {})", peer.getName(), disconnectReason, detail);
	}

	public void onPeerReceived(VastPeer peer, RequestMessage requestMessage) {
		synchronized (incomingRequests) {
			incomingRequests.add(new IncomingRequest(peer, requestMessage));
		}
		logger.debug("Got request message from peer: {} from {}", requestMessage, peer.getName());
	}
}

class FakePeer extends VastPeer {
	public FakePeer(VastServerApplication serverApplication, String name) {
		super(new InitialRequest(), new NetworkPeer() {
			@Override
			public long getMeanOfRoundTripTime() {
				return 0;
			}

			@Override
			public long getMeanOfRoundTripTimeDeviation() {
				return 0;
			}

			@Override
			public long getLowestRoundTripTime() {
				return 0;
			}

			@Override
			public long getHighestRoundTripTimeDeviation() {
				return 0;
			}

			@Override
			public byte[] getSecretKey() {
				return SHA256.hash(new BigInteger("123"));
			}

			@Override
			public ConnectionState getConnectionState() {
				return null;
			}

			@Override
			public boolean enqueueOutgoingCommand(byte[] bytes, int i, byte b, boolean b1, QoS qoS) {
				return false;
			}

			@Override
			public void setApplicationPeer(ApplicationPeer applicationPeer) {

			}

			@Override
			public void disconnect(DisconnectReason disconnectReason, String s) {

			}
		}, serverApplication, name);
	}

	@Override
	protected boolean send(ResponseMessage response, SendOptions options) {
		return true;
	}

	@Override
	protected boolean send(ResponseMessage response, byte channel, boolean encrypt, QoS qos) {
		return true;
	}

	@Override
	public boolean send(Message message, SendOptions options) {
		return true;
	}

	@Override
	protected boolean send(Message message, byte channel, boolean encrypt, QoS qos) {
		return true;
	}

	@Override
	protected boolean send(InitialResponse initialResponse, byte channel, boolean encrypt, QoS qos) {
		return true;
	}

	@Override
	protected boolean send(byte[] payload, int payloadLength, byte channel, boolean encrypt, QoS qos) {
		return true;
	}

	@Override
	protected void onReceive(RequestMessage requestMessage, SendOptions sendOptions) {
	}

	@Override
	protected void onDisconnect(DisconnectReason disconnectReason, String s) {
	}
}