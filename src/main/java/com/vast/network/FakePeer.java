package com.vast.network;

import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.*;
import com.nhnent.haste.security.BigInteger;
import com.nhnent.haste.security.SHA256;
import com.nhnent.haste.transport.ApplicationPeer;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.nhnent.haste.transport.QoS;
import com.nhnent.haste.transport.state.ConnectionState;
import com.vast.Metrics;

public class FakePeer extends VastPeer {
	public FakePeer(VastServerApplication serverApplication, String name, Metrics metrics) {
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
		}, serverApplication, name, metrics);
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
