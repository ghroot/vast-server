package test.system;

import com.artemis.BaseSystem;
import test.IncomingRequest;
import test.MyServerApplication;

import java.util.List;

public class IncomingRequestTransferSystem extends BaseSystem {
	private MyServerApplication serverApplication;
	private List<IncomingRequest> incomingRequests;

	public IncomingRequestTransferSystem(MyServerApplication serverApplication, List<IncomingRequest> incomingRequests) {
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
