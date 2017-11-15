package com.vast;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import com.vast.collision.CollisionHandler;
import com.vast.collision.PlayerWithPickupCollisionHandler;
import com.vast.interact.HarvestableInteractionHandler;
import com.vast.interact.InteractionHandler;
import com.vast.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VastWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(VastWorld.class);

	private final int FRAME_RATE = 30;
	private final int FRAME_DURATION_MILLIS = 1000 / FRAME_RATE;

	private World world;
	private boolean alive;
	private long lastFrameStartTime;

	public VastWorld(VastServerApplication serverApplication, String snapshotFormat, boolean showMonitor, Metrics metrics) {
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		List<IncomingRequest> incomingRequests = new ArrayList<IncomingRequest>();
		Map<Integer, Set<Integer>> spatialHashes = new HashMap<Integer, Set<Integer>>();
		WorldDimensions worldDimensions = new WorldDimensions(5000, 5000, 4);

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldDimensions),
			new WorldSerializationManager(),
			new MetricsManager(metrics),

			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new PeerEntitySystem(peers, worldDimensions),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new ScanSystem(worldDimensions, spatialHashes),
			new CreateSystem(peers),
			new CullingSystem(peers),
			new MoveOrderSystem(incomingRequests),
			new InteractOrderSystem(incomingRequests),
			new BuildOrderSystem(incomingRequests),
			new AISystem(),
			new PathMoveSystem(),
			new InteractSystem(new HashSet<InteractionHandler>(Arrays.asList(
				new HarvestableInteractionHandler()
			))),
			new SpatialSystem(worldDimensions, spatialHashes),
			new CollisionSystem(new HashSet<CollisionHandler>(Arrays.asList(
				new PlayerWithPickupCollisionHandler()
			)), worldDimensions, spatialHashes, metrics),
			new DeleteSystem(peers),
			new SyncTransformSystem(peers),
			new WorldSerializationSystem(snapshotFormat, metrics)
		);
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.LOW, new TerminalSystem(peers, metrics, worldDimensions, spatialHashes));
		}
		world = new World(worldConfigurationBuilder.build());

		alive = true;
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
