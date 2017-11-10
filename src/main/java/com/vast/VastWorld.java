package com.vast;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.managers.WorldSerializationManager;
import com.vast.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2i;
import java.util.*;

public class VastWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(VastWorld.class);

	private final int UPDATE_RATE = 30;
	private final float FRAME_TIME_MILLIS = (float) 1000 / UPDATE_RATE;
	private final float FRAME_TIME_SECONDS = FRAME_TIME_MILLIS / 1000;

	private World world;
	private Metrics metrics = new Metrics();
	private boolean alive;

	public VastWorld(VastServerApplication serverApplication, String snapshotFormat, boolean showMonitor) {
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		List<IncomingRequest> incomingRequests = new ArrayList<IncomingRequest>();
		Map<Point2i, Set<Integer>> spatialHashes = new HashMap<Point2i, Set<Integer>>();
		WorldDimensions worldDimensions = new WorldDimensions(5000, 5000, 10);

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldDimensions),
			new WorldSerializationManager(),
			new MetricsManager(metrics),

			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequests),
			new PeerEntitySystem(peers, worldDimensions, spatialHashes),
			new PathAssignSystem(incomingRequests),
			new AISystem(),
			new PathMoveSystem(),
			new SpatialSystem(worldDimensions, spatialHashes),
			new CollisionSystem(spatialHashes),
			new SyncTransformSystem(peers, worldDimensions, spatialHashes),
			new IncomingRequestClearSystem(incomingRequests),
			new WorldSerializationSystem(snapshotFormat)
		);
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.LOW, new TerminalSystem(metrics, worldDimensions, spatialHashes));
		}
		world = new World(worldConfigurationBuilder.build());

		alive = true;
	}

	@Override
	public void run() {
		while (alive) {
			try {
				long frameStartTime = System.currentTimeMillis();
				world.setDelta(FRAME_TIME_SECONDS);
				world.process();
				long processEndTime = System.currentTimeMillis();
				int processDuration = (int) (processEndTime - frameStartTime);
				int timeToSleep = (int) (FRAME_TIME_MILLIS - processDuration);
				if (timeToSleep > 0) {
					Thread.sleep(timeToSleep);
				}
				long frameEndTime = System.currentTimeMillis();
				metrics.setTimePerFrameMs((int) (frameEndTime - frameStartTime));
			} catch (InterruptedException exception) {
				logger.error("Error in world loop", exception);
			}
		}
	}

	public void destroy() {
		world.dispose();
		alive = false;
	}
}
