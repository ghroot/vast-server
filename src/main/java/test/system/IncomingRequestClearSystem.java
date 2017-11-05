package test.system;

import com.artemis.BaseSystem;
import test.IncomingRequest;

import java.util.List;

public class IncomingRequestClearSystem extends BaseSystem {
	List<IncomingRequest> incomingRequests;

	public IncomingRequestClearSystem(List<IncomingRequest> incomingRequests) {
		this.incomingRequests = incomingRequests;
	}

	@Override
	protected void processSystem() {
		incomingRequests.clear();
	}
}
