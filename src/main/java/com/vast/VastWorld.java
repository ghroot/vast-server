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
import net.mostlyoriginal.api.utils.QuadTree;
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

	public VastWorld(VastServerApplication serverApplication, String snapshotFormat, long randomSeed, boolean showMonitor, Metrics metrics) {
		this.metrics = metrics;

		Random random;
		if (randomSeed >= 0) {
			random = new Random(randomSeed);
		} else {
			random = new Random();
		}
		WorldConfiguration worldConfiguration = new WorldConfiguration();
		Items items = new Items();
		Buildings buildings = new Buildings(items);
		Animals animals = new Animals(items);
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		Map<String, List<IncomingRequest>> incomingRequestsByPeer = new HashMap<String, List<IncomingRequest>>();
		Map<String, Integer> entitiesByPeer = new HashMap<String, Integer>();
		Map<Integer, IntBag> spatialHashes = new HashMap<Integer, IntBag>();
		QuadTree quadTree = new QuadTree(0, 0, worldConfiguration.width, worldConfiguration.height);
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
			new FollowOrderHandler(),
			new ChatOrderHandler()
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
			new ConfigurationPropertyHandler(items, buildings),
			new SkillPropertyHandler()
		));
		Map<String, Behaviour> behaviours = new HashMap<String, Behaviour>();
		behaviours.put("human", new HumanBehaviour(interactionHandlers, random, incomingRequestsByPeer, items, buildings));
		behaviours.put("adultAnimal", new AdultAnimalBehaviour(interactionHandlers, random));
		behaviours.put("youngAnimal", new YoungAnimalBehaviour(interactionHandlers, random));

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldConfiguration, random, items, buildings, animals),
			new TimeManager(),

			new WorldSerializationSystem(snapshotFormat, metrics),
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequestsByPeer),
			new PeerEntitySystem(peers, entitiesByPeer),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new ConfigurationSystem(),
			new QuadTreeAddRemoveSystem(quadTree, worldConfiguration),
			new QuadTreeUpdateSystem(quadTree, worldConfiguration),
			new ScanSystem(quadTree, worldConfiguration),
			new CreateSystem(propertyHandlers),
			new CullingSystem(propertyHandlers),
			new OrderSystem(orderHandlers, incomingRequestsByPeer),
			new InteractSystem(interactionHandlers),
			new AISystem(behaviours, random),
			new SpeedSystem(),
			new PathMoveSystem(),
			new CollisionSystem(worldConfiguration, quadTree, random, metrics),
			new FollowSystem(),
			new FuelSystem(),
			new CraftSystem(items),
			new GrowSystem(),
			new LifetimeSystem(),
			new LearnSystem(),
			new PickupSystem(random),
			new DayNightCycleSystem(worldConfiguration),
			new WeatherSystem(random),
			new ParentSystem(),
			new DeleteSystem(),
			new EventSystem(),
			new SyncSystem(propertyHandlers, metrics)
		).with(
			new WorldSerializationManager(),
			new EntityLinkManager()
		);
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
			if (metrics != null) {
				metrics.setTimePerFrameMs(timeSinceLastFrame);
			}
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
