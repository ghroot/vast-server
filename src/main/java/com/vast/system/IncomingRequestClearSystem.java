package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.IncomingRequest;

import java.util.List;

public class IncomingRequestClearSystem extends BaseSystem {
	private List<IncomingRequest> incomingRequests;

	public IncomingRequestClearSystem(List<IncomingRequest> incomingRequests) {
		this.incomingRequests = incomingRequests;
	}

	@Override
	protected void processSystem() {
		incomingRequests.clear();
	}
}
