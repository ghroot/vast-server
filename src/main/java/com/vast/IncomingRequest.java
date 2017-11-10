package com.vast;

import com.nhnent.haste.protocol.messages.RequestMessage;

public class IncomingRequest {
	private VastPeer peer;
	private RequestMessage message;

	public IncomingRequest(VastPeer peer, RequestMessage message) {
		this.peer = peer;
		this.message = message;
	}
	public VastPeer getPeer() {
		return peer;
	}

	public RequestMessage getMessage() {
		return message;
	}
}
