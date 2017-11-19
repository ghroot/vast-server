package com.vast;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import com.vast.collision.CollisionHandler;
import com.vast.collision.PlayerWithPickupCollisionHandler;
import com.vast.interact.BuildingInteractionHandler;
import com.vast.interact.HarvestableInteractionHandler;
import com.vast.interact.InteractionHandler;
import com.vast.order.BuildOrderHandler;
import com.vast.order.InteractOrderHandler;
import com.vast.order.MoveOrderHandler;
import com.vast.order.OrderHandler;
import com.vast.property.*;
import com.vast.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Properties;

public class VastWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(VastWorld.class);

	private final int FRAME_RATE = 30;
	private final int FRAME_DURATION_MILLIS = 1000 / FRAME_RATE;

	private World world;
	private boolean alive;
	private long lastFrameStartTime;

	public VastWorld(VastServerApplication serverApplication, String snapshotFormat, boolean showMonitor, Metrics metrics) {
		WorldConfiguration worldConfiguration = new WorldConfiguration(loadWorldProperties());
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		List<IncomingRequest> incomingRequests = new ArrayList<IncomingRequest>();
		Map<String, Integer> entitiesByPeer = new HashMap<String, Integer>();
		Map<Integer, Set<Integer>> spatialHashes = new HashMap<Integer, Set<Integer>>();
		Set<PropertyHandler> propertyHandlers = new HashSet<PropertyHandler>(Arrays.asList(
				new PositionPropertyHandler(),
				new ActivePropertyHandler(),
				new DurabilityPropertyHandler(),
				new ProgressPropertyHandler()
		));

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldConfiguration),
			new WorldSerializationManager(),
			new MetricsManager(metrics),

			new WorldSerializationSystem(snapshotFormat, metrics),
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new PeerEntitySystem(peers, entitiesByPeer, worldConfiguration),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new ScanSystem(worldConfiguration, spatialHashes),
			new CreateSystem(peers, propertyHandlers),
			new CullingSystem(peers, propertyHandlers),
			new OrderSystem(new HashSet<OrderHandler>(Arrays.asList(
				new MoveOrderHandler(),
				new InteractOrderHandler(),
				new BuildOrderHandler()
			)), incomingRequests, entitiesByPeer),
			new AISystem(),
			new PathMoveSystem(),
			new InteractSystem(new HashSet<InteractionHandler>(Arrays.asList(
				new HarvestableInteractionHandler(),
				new BuildingInteractionHandler()
			))),
			new SpatialSystem(worldConfiguration, spatialHashes),
			new CollisionSystem(new HashSet<CollisionHandler>(Arrays.asList(
				new PlayerWithPickupCollisionHandler()
			)), metrics),
			new DeleteSystem(peers),
			new SyncSystem(propertyHandlers, peers)
		);
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.HIGHEST, new TerminalSystem(peers, metrics, worldConfiguration, spatialHashes));
		}
		world = new World(worldConfigurationBuilder.build());

		alive = true;
	}

	private Properties loadWorldProperties() {
		Properties worldProperties = null;
		try {
			worldProperties = new Properties();
			worldProperties.load(getClass().getResourceAsStream("world.properties"));
		} catch (Exception exception) {
			logger.error("Unable to load world properties file", exception);
		}
		return worldProperties;
	}

	@Override
	public void run() {
		lastFrameStartTime = System.currentTimeMillis();
		while (alive) {
			long frameStartTime = System.currentTimeMillis();
			int timeSinceLastFrame = (int) (frameStartTime - lastFrameStartTime);
			float delta = (float) timeSinceLastFrame / 1000;
			world.setDelta(delta);
			long processStartTime = System.currentTimeMillis();
			world.process();
			long processEndTime = System.currentTimeMillis();
			int processDuration = (int) (processEndTime - processStartTime);
			int sleepDuration = FRAME_DURATION_MILLIS - processDuration;
			if (sleepDuration > 0) {
				try {
					Thread.sleep(sleepDuration);
				} catch (InterruptedException exception) {
					logger.error("Interrupted while sleeping after processing world", exception);
				}
			}
			lastFrameStartTime = frameStartTime;
		}
	}

	public void destroy() {
		world.dispose();
		alive = false;
	}
}
