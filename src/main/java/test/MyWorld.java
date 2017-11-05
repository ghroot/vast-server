package test;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.system.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MyWorld.class);

	private World world;
	private List<MyPeer> peers;
	private List<IncomingRequest> incomingRequests;
	private Map<String, Integer> entitiesByName;

	private boolean alive;

	public MyWorld(MyServerApplication serverApplication) {
		peers = new ArrayList<MyPeer>();
		incomingRequests = new ArrayList<IncomingRequest>();
		entitiesByName = new HashMap<String, Integer>();
		WorldConfiguration config = new WorldConfigurationBuilder().with(
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new PeerEntitySystem(peers, entitiesByName),
			new PathAssignSystem(incomingRequests),
			new AISystem(),
			new PathMoveSystem(),
			new SyncTransformSystem(serverApplication.getPeers()),
			new IncomingRequestClearSystem(incomingRequests),
			new WorldSerializationSystem(entitiesByName),

			new WorldSerializationManager()
		).build();
		world = new World(config);
		alive = true;
	}

	@Override
	public void run() {
		while (alive) {
			try {
				world.setDelta(167);
				world.process();
				Thread.sleep(167);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void destroy() {
		world.dispose();
		alive = false;
	}
}
