package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.VastPeer;
import com.vast.VastServerApplication;

import java.util.Map;

public class PeerTransferSystem extends BaseSystem {
	private VastServerApplication serverApplication;
	private Map<String, VastPeer> peers;

	public PeerTransferSystem(VastServerApplication serverApplication, Map<String, VastPeer> peers) {
		this.serverApplication = serverApplication;
		this.peers = peers;
	}

	@Override
	protected void processSystem() {
		synchronized (serverApplication.getPeers()) {
			peers.clear();
			for (VastPeer peer : serverApplication.getPeers()) {
				peers.put(peer.getName(), peer);
			}
		}
	}
}
