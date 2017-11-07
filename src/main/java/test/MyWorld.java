package test;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import test.system.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWorld implements Runnable {
	private final float UPDATE_RATE = 30.0f;

	private World world;
	private List<MyPeer> peers;
	private List<IncomingRequest> incomingRequests;
	private Map<String, Integer> entitiesByPeerName;
	private Map<Integer, List<Integer>> nearbyEntitiesByEntity;

	private boolean alive;

	public MyWorld(MyServerApplication serverApplication) {
		peers = new ArrayList<MyPeer>();
		incomingRequests = new ArrayList<IncomingRequest>();
		entitiesByPeerName = new HashMap<String, Integer>();
		nearbyEntitiesByEntity = new HashMap<Integer, List<Integer>>();
		WorldConfiguration config = new WorldConfigurationBuilder().with(
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new NearbyEntitySystem(nearbyEntitiesByEntity),
			new PeerEntitySystem(peers, entitiesByPeerName, nearbyEntitiesByEntity),
			new PathAssignSystem(incomingRequests),
			new AISystem(nearbyEntitiesByEntity),
			new FollowSystem(),
			new PathMoveSystem(),
			new CollisionSystem(),
			new SyncTransformSystem(peers),
			new IncomingRequestClearSystem(incomingRequests),
			new WorldSerializationSystem(entitiesByPeerName),

			new WorldSerializationManager()
		).build();
		world = new World(config);
		alive = true;
	}

	@Override
	public void run() {
		while (alive) {
			try {
				world.setDelta(1 / UPDATE_RATE);
				world.process();
				Thread.sleep((int) (1000 / UPDATE_RATE));
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
