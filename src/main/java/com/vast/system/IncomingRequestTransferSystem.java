package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.IncomingRequest;
import com.vast.VastServerApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncomingRequestTransferSystem extends BaseSystem {
	private VastServerApplication serverApplication;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

	public IncomingRequestTransferSystem(VastServerApplication serverApplication, Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
		this.serverApplication = serverApplication;
		this.incomingRequestsByPeer = incomingRequestsByPeer;
	}

	@Override
	protected void processSystem() {
		synchronized (serverApplication.getIncomingRequests()) {
			for (IncomingRequest incomingRequest : serverApplication.getIncomingRequests()) {
				List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(incomingRequest.getPeer().getName());
				if (incomingRequests == null) {
					incomingRequests = new ArrayList<IncomingRequest>();
					incomingRequestsByPeer.put(incomingRequest.getPeer().getName(), incomingRequests);
				}
				incomingRequests.add(incomingRequest);
			}
			serverApplication.getIncomingRequests().clear();
		}
	}
}
