package com.vast;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.Message;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.nhnent.haste.transport.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VastPeer extends ClientPeer {
	private static final Logger logger = LoggerFactory.getLogger(VastPeer.class);

	private static long nextId = 1;

	private VastServerApplication serverApplication;
	private long id;
	private String name;
	private Metrics metrics;

	public VastPeer(InitialRequest initialRequest, NetworkPeer networkPeer, VastServerApplication serverApplication, String name, Metrics metrics) {
		super(initialRequest, networkPeer);
		this.serverApplication = serverApplication;
		this.name = name;
		this.metrics = metrics;

		id = nextId++;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void onReceive(RequestMessage requestMessage, SendOptions sendOptions) {
		serverApplication.onPeerReceived(this, requestMessage);
	}

	@Override
	protected void onDisconnect(DisconnectReason disconnectReason, String detail) {
		serverApplication.onPeerDisconnected(this, disconnectReason, detail);
	}

	public boolean send(Message message) {
		return send(message, SendOptions.ReliableSend);
	}

	public boolean sendUnreliable(Message message) {
		return send(message, SendOptions.take((byte) 0, false, QoS.UNRELIABLE_SEQUENCED));
	}

	@Override
	protected boolean send(byte[] payload, int payloadLength, byte channel, boolean encrypt, QoS qos) {
		metrics.incrementNumberOfSentMessages();
		return super.send(payload, payloadLength, channel, encrypt, qos);
	}
}
