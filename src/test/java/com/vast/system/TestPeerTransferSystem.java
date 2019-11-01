package com.vast.system;

import com.vast.network.VastPeer;
import com.vast.network.VastServerApplication;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class TestPeerTransferSystem {
	@Test
	public void addsPeer() {
		VastServerApplication serverApplication = Mockito.mock(VastServerApplication.class);
		Map<String, VastPeer> peers = new HashMap<>();
		PeerTransferSystem peerTransferSystem = new PeerTransferSystem(serverApplication, peers);

		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getName()).thenReturn("TestName");
		peerTransferSystem.peerAdded(peer);

		peerTransferSystem.processSystem();

		Assert.assertEquals(1, peers.size());
		Assert.assertEquals(peer, peers.get("TestName"));
	}

	@Test
	public void removesPeer() {
		VastServerApplication serverApplication = Mockito.mock(VastServerApplication.class);
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getName()).thenReturn("TestName");
		peers.put("TestName", peer);
		PeerTransferSystem peerTransferSystem = new PeerTransferSystem(serverApplication, peers);

		peerTransferSystem.peerRemoved(peer);

		peerTransferSystem.processSystem();

		Assert.assertEquals(0, peers.size());
	}
}
