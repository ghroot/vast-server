package test;

import com.nhnent.haste.protocol.messages.RequestMessage;

public class IncomingRequest {
	private MyPeer peer;
	private RequestMessage message;

	public IncomingRequest(MyPeer peer, RequestMessage message) {
		this.peer = peer;
		this.message = message;
	}
	public MyPeer getPeer() {
		return peer;
	}

	public RequestMessage getMessage() {
		return message;
	}
}
