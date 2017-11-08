package test.system;

import com.artemis.BaseSystem;
import test.MyPeer;
import test.MyServerApplication;

import java.util.Map;

public class PeerTransferSystem extends BaseSystem {
	private MyServerApplication serverApplication;
	private Map<String, MyPeer> peers;

	public PeerTransferSystem(MyServerApplication serverApplication, Map<String, MyPeer> peers) {
		this.serverApplication = serverApplication;
		this.peers = peers;
	}

	@Override
	protected void processSystem() {
		synchronized (serverApplication.getPeers()) {
			peers.clear();
			for (MyPeer peer : serverApplication.getPeers()) {
				peers.put(peer.getName(), peer);
			}
		}
	}
}
