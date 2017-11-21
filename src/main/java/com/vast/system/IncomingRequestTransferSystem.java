package com.vast.system;

import com.vast.IncomingRequest;
import com.vast.VastServerApplication;

import java.util.List;

public class IncomingRequestTransferSystem extends AbstractProfiledBaseSystem {
	private VastServerApplication serverApplication;
	private List<IncomingRequest> incomingRequests;

	public IncomingRequestTransferSystem(VastServerApplication serverApplication, List<IncomingRequest> incomingRequests) {
		this.serverApplication = serverApplication;
		this.incomingRequests = incomingRequests;
	}

	@Override
	protected void processSystem() {
		synchronized (serverApplication.getIncomingRequests()) {
			incomingRequests.addAll(serverApplication.getIncomingRequests());
			serverApplication.getIncomingRequests().clear();
		}
	}
}
