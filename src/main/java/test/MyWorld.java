package test;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.system.*;

import javax.vecmath.Point2i;
import java.util.*;

public class MyWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MyWorld.class);

	private final float UPDATE_RATE = 30.0f;

	private World world;
	private List<MyPeer> peers;
	private List<IncomingRequest> incomingRequests;
	private Map<String, Integer> entitiesByPeerName;
	private Map<Point2i, Set<Integer>> spatialHashes;

	private Metrics metrics = new Metrics();

	private boolean alive;

	public MyWorld(MyServerApplication serverApplication) {
		peers = new ArrayList<MyPeer>();
		incomingRequests = new ArrayList<IncomingRequest>();
		entitiesByPeerName = new HashMap<String, Integer>();
		spatialHashes = new HashMap<Point2i, Set<Integer>>();

		WorldConfiguration config = new WorldConfigurationBuilder().with(
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new PeerEntitySystem(peers, entitiesByPeerName, spatialHashes),
			new PathAssignSystem(incomingRequests),
			new AISystem(),
			new PathMoveSystem(),
			new SpatialSystem(spatialHashes),
			new CollisionSystem(spatialHashes),
			new SyncTransformSystem(peers),
			new TerminalSystem(metrics),
			new IncomingRequestClearSystem(incomingRequests),
			new WorldSerializationSystem(entitiesByPeerName),

			new WorldSerializationManager(),
			new MetricsManager(metrics)
		).build();

		world = new World(config);

		alive = true;
	}

	@Override
	public void run() {
		while (alive) {
			try {
				long startTime = System.currentTimeMillis();
				world.setDelta(1 / UPDATE_RATE);
				world.process();
				long endTime = System.currentTimeMillis();
				int timeToSleep = (int) (1000 / UPDATE_RATE) - (int) (endTime - startTime);
				if (timeToSleep > 0) {
					Thread.sleep(timeToSleep);
				}
				endTime = System.currentTimeMillis();
				metrics.timePerFrameMs = (int) (endTime - startTime);
				metrics.fps = (int) (1000 / (endTime - startTime));
			} catch (InterruptedException exception) {
				logger.error("", exception);
			}
		}
	}

	public void destroy() {
		world.dispose();
		alive = false;
	}
}
