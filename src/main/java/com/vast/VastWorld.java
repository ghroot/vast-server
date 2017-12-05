package com.vast;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.link.EntityLinkManager;
import com.artemis.managers.WorldSerializationManager;
import com.vast.behaviour.*;
import com.vast.collision.CollisionHandler;
import com.vast.data.Buildings;
import com.vast.data.Items;
import com.vast.data.WorldConfiguration;
import com.vast.effect.Effect;
import com.vast.effect.HealEffect;
import com.vast.interact.*;
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
		Map<String, VastPeer> peers = new HashMap<String, VastPeer>();
		Map<String, List<IncomingRequest>> incomingRequestsByPeer = new HashMap<String, List<IncomingRequest>>();
		Map<String, Integer> entitiesByPeer = new HashMap<String, Integer>();
		Map<Integer, Set<Integer>> spatialHashes = new HashMap<Integer, Set<Integer>>();
		Set<OrderHandler> orderHandlers = new HashSet<OrderHandler>(Arrays.asList(
			new MoveOrderHandler(),
			new InteractOrderHandler(),
			new BuildOrderHandler(buildings),
			new EmoteOrderHandler(),
			new SetHomeOrderHandler(),
			new CraftOrderHandler(items)
		));
		List<InteractionHandler> interactionHandlers = new ArrayList<InteractionHandler>(Arrays.asList(
			new HarvestableInteractionHandler(),
			new ConstructableInteractionHandler(),
			new AttackInteractionHandler(),
			new ContainerInteractionHandler(),
			new FueledInteractionHandler()
		));
		Set<PropertyHandler> propertyHandlers = new HashSet<PropertyHandler>(Arrays.asList(
			new PositionPropertyHandler(),
			new RotationPropertyHandler(),
			new ActivePropertyHandler(),
			new DurabilityPropertyHandler(),
			new ProgressPropertyHandler(),
			new HealthPropertyHandler(),
			new MaxHealthPropertyHandler(),
			new InventoryPropertyHandler(),
			new FueledPropertyHandler(),
			new HomePropertyHandler()
		));
		Map<String, Behaviour> behaviours = new HashMap<String, Behaviour>();
		behaviours.put("basic", new BasicBehaviour(interactionHandlers));
		behaviours.put("fakeHuman", new FakeHumanBehaviour(interactionHandlers, peers, incomingRequestsByPeer));
		behaviours.put("fleeingAnimal", new FleeingAnimalBehaviour(interactionHandlers));
		behaviours.put("aggressiveAnimal", new AggressiveAnimalBehaviour(interactionHandlers));
		Map<String, Effect> effects = new HashMap<String, Effect>();
		effects.put("heal", new HealEffect());

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldConfiguration, items, buildings),
			new TimeManager(),

			new WorldSerializationSystem(snapshotFormat, metrics),
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequestsByPeer),
			new PeerEntitySystem(peers, entitiesByPeer),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new SpatialShiftSystem(worldConfiguration, spatialHashes),
			new SpatialUpdateSystem(worldConfiguration, spatialHashes),
			new ScanSystem(worldConfiguration, spatialHashes),
			new CullingSystem(peers, propertyHandlers),
			new OrderSystem(orderHandlers, incomingRequestsByPeer),
			new AISystem(behaviours),
			new SpeedSystem(),
			new PathMoveSystem(),
			new InteractSystem(interactionHandlers),
			new CollisionSystem(new HashSet<CollisionHandler>(), metrics),
			new FuelSystem(),
			new CraftSystem(items),
			new AuraSystem(effects),
			new LifetimeSystem(),
			new PickupSystem(),
			new DeathSystem(),
			new ParentSystem(),
			new DeleteSystem(peers),
			new CreateSystem(peers, propertyHandlers),
			new EventSystem(peers),
			new MessageSystem(peers),
			new SyncSystem(propertyHandlers, peers, metrics)
		).with(
			new WorldSerializationManager(),
			new EntityLinkManager()
		).register(new ProfiledInvocationStrategy(metrics));
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.HIGHEST, new TerminalSystem(peers, metrics, worldConfiguration, spatialHashes));
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
