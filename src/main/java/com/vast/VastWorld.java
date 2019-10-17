package com.vast;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.link.EntityLinkManager;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import com.vast.behaviour.AdultAnimalBehaviour;
import com.vast.behaviour.Behaviour;
import com.vast.behaviour.HumanBehaviour;
import com.vast.behaviour.YoungAnimalBehaviour;
import com.vast.data.*;
import com.vast.interact.*;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.network.VastServerApplication;
import com.vast.order.*;
import com.vast.property.*;
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
	private Metrics metrics;

	public VastWorld(VastServerApplication serverApplication, String snapshotFormat, boolean showMonitor, Metrics metrics) {
		this.metrics = metrics;

		WorldConfiguration worldConfiguration = new WorldConfiguration();
		Items items = new Items();
		Buildings buildings = new Buildings(items);
		Animals animals = new Animals(items);
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		Map<String, List<IncomingRequest>> incomingRequestsByPeer = new HashMap<String, List<IncomingRequest>>();
		Map<String, Integer> entitiesByPeer = new HashMap<String, Integer>();
		Map<Integer, IntBag> spatialHashes = new HashMap<Integer, IntBag>();
		List<InteractionHandler> interactionHandlers = new ArrayList<InteractionHandler>(Arrays.asList(
			new GrowingInteractionHandler(),
			new HarvestableInteractionHandler(),
			new ConstructableInteractionHandler(),
			new PlantableInteractionHandler(items),
			new ContainerInteractionHandler(items),
			new FueledInteractionHandler()
		));
		Set<OrderHandler> orderHandlers = new HashSet<OrderHandler>(Arrays.asList(
			new MoveOrderHandler(),
			new InteractOrderHandler(interactionHandlers),
			new BuildOrderHandler(buildings),
			new EmoteOrderHandler(),
			new SetHomeOrderHandler(),
			new CraftOrderHandler(items),
			new PlantOrderHandler(items),
			new FollowOrderHandler()
		));
		Set<PropertyHandler> propertyHandlers = new HashSet<PropertyHandler>(Arrays.asList(
			new PositionPropertyHandler(),
			new RotationPropertyHandler(),
			new ActivePropertyHandler(),
			new ProgressPropertyHandler(),
			new InventoryPropertyHandler(),
			new FueledPropertyHandler(),
			new HomePropertyHandler(),
			new GrowingPropertyHandler(),
			new StatePropertyHandler(),
			new ConfigurationPropertyHandler(items, buildings)
		));
		Map<String, Behaviour> behaviours = new HashMap<String, Behaviour>();
		behaviours.put("human", new HumanBehaviour(interactionHandlers, incomingRequestsByPeer, items, buildings));
		behaviours.put("adultAnimal", new AdultAnimalBehaviour(interactionHandlers));
		behaviours.put("youngAnimal", new YoungAnimalBehaviour(interactionHandlers));

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldConfiguration, items, buildings, animals),
			new TimeManager(),

			new WorldSerializationSystem(snapshotFormat, metrics),
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequestsByPeer),
			new PeerEntitySystem(peers, entitiesByPeer),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new ConfigurationSystem(),
			new SpatialAddRemoveSystem(worldConfiguration, spatialHashes),
			new SpatialUpdateSystem(worldConfiguration, spatialHashes),
			new ScanSystem(worldConfiguration, spatialHashes),
			new CreateSystem(propertyHandlers),
			new CullingSystem(propertyHandlers),
			new OrderSystem(orderHandlers, incomingRequestsByPeer),
			new InteractSystem(interactionHandlers),
			new AISystem(behaviours),
			new SpeedSystem(),
			new PathMoveSystem(),
			new CollisionSystem(worldConfiguration, spatialHashes, metrics),
			new FollowSystem(),
			new FuelSystem(),
			new CraftSystem(items),
			new GrowSystem(),
			new LifetimeSystem(),
			new LearnSystem(),
			new PickupSystem(),
			new DayNightCycleSystem(worldConfiguration),
			new WeatherSystem(),
			new ParentSystem(),
			new DeleteSystem(),
			new EventSystem(),
			new MessageSystem(),
			new SyncSystem(propertyHandlers, metrics)
		).with(
			new WorldSerializationManager(),
			new EntityLinkManager()
		).alwaysDelayComponentRemoval(true);
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.HIGHEST, new TerminalSystem(peers, metrics, worldConfiguration, spatialHashes));
			worldConfigurationBuilder.register(new ProfiledInvocationStrategy(metrics));
		}
		world = new World(worldConfigurationBuilder.build());

		for (PropertyHandler propertyHandler : propertyHandlers) {
			world.inject(propertyHandler);
		}

		alive = true;
	}

	@Override
	public void run() {
		lastFrameStartTime = System.currentTimeMillis();
		while (alive) {
			long frameStartTime = System.currentTimeMillis();
			int timeSinceLastFrame = (int) (frameStartTime - lastFrameStartTime);
			metrics.setTimePerFrameMs(timeSinceLastFrame);
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
