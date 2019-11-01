package com.vast.network;

public interface PeerListener {
	void peerAdded(VastPeer peer);
	void peerRemoved(VastPeer peer);
}
