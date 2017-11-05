package test;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.nhnent.haste.transport.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPeer extends ClientPeer {
	private static final Logger logger = LoggerFactory.getLogger(MyPeer.class);

	private MyServerApplication serverApplication;
	private String name;

	public MyPeer(InitialRequest initialRequest, NetworkPeer networkPeer, MyServerApplication serverApplication, String name) {
		super(initialRequest, networkPeer);
		this.serverApplication = serverApplication;
		this.name = name;
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
}
