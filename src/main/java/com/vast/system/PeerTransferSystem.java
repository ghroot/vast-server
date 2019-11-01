package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.network.PeerListener;
import com.vast.network.VastPeer;
import com.vast.network.VastServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PeerTransferSystem extends BaseSystem implements PeerListener {
	private static final Logger logger = LoggerFactory.getLogger(PeerTransferSystem.class);

	private VastServerApplication serverApplication;
	private Map<String, VastPeer> peers;

	private List<VastPeer> peersToAdd;
	private List<VastPeer> peersToRemove;

	public PeerTransferSystem(VastServerApplication serverApplication, Map<String, VastPeer> peers) {
		this.serverApplication = serverApplication;
		this.peers = peers;

		peersToAdd = new ArrayList<>();
		peersToRemove = new ArrayList<>();
	}

	@Override
	protected void initialize() {
		serverApplication.addPeerListener(this);
	}

	@Override
	protected void dispose() {
		serverApplication.removePeerListener(this);
	}

	@Override
	public void peerAdded(VastPeer peer) {
		synchronized (peersToAdd) {
			peersToAdd.add(peer);
		}
	}

	@Override
	public void peerRemoved(VastPeer peer) {
		synchronized (peersToRemove) {
			peersToRemove.add(peer);
		}
	}

	@Override
	protected void processSystem() {
		synchronized (peersToRemove) {
			for (VastPeer peerToRemove : peersToRemove) {
				peers.remove(peerToRemove.getName());
			}
			peersToRemove.clear();
		}

		synchronized (peersToAdd) {
			for (VastPeer peerToAdd : peersToAdd) {
				peers.put(peerToAdd.getName(), peerToAdd);
			}
			peersToAdd.clear();
		}
	}
}
