package test.system;

import com.artemis.BaseSystem;
import test.MyPeer;
import test.MyServerApplication;

import java.util.List;

public class PeerTransferSystem extends BaseSystem {
	private MyServerApplication serverApplication;
	private List<MyPeer> peers;

	public PeerTransferSystem(MyServerApplication serverApplication, List<MyPeer> peers) {
		this.serverApplication = serverApplication;
		this.peers = peers;
	}

	@Override
	protected void processSystem() {
		synchronized (serverApplication.getPeers()) {
			peers.clear();
			peers.addAll(serverApplication.getPeers());
		}
	}
}
