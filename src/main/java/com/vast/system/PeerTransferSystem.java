package com.vast.system;

import com.vast.VastPeer;
import com.vast.VastServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PeerTransferSystem extends ProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerTransferSystem.class);

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
				if (!peers.containsKey(peer.getName()) || peers.get(peer.getName()).getId() < peer.getId()) {
					peers.put(peer.getName(), peer);
				}
			}
		}
	}
}
