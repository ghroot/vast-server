package com.vast;

import com.artemis.*;
import com.artemis.link.EntityLinkManager;
import com.artemis.managers.WorldSerializationManager;
import com.vast.behaviour.AdultAnimalBehaviour;
import com.vast.behaviour.Behaviour;
import com.vast.behaviour.HumanBehaviour;
import com.vast.behaviour.YoungAnimalBehaviour;
import com.vast.data.Items;
import com.vast.data.Metrics;
import com.vast.data.Recipes;
import com.vast.data.WorldConfiguration;
import com.vast.interact.*;
import com.vast.network.IncomingRequest;
import com.vast.network.VastPeer;
import com.vast.network.VastServerApplication;
import com.vast.order.handler.*;
import com.vast.order.handler.avatar.*;
import com.vast.order.handler.observer.AttachObserverOrderHandler;
import com.vast.order.handler.observer.BuildOrderHandler;
import com.vast.order.handler.observer.MoveObserverOrderHandler;
import com.vast.property.*;
import com.vast.property.progress.ConstructableProgressPropertyHandler;
import com.vast.property.progress.CraftProgressPropertyHandler;
import com.vast.property.progress.GrowingProgressPropertyHandler;
import com.vast.property.progress.ProducerProgressPropertyHandler;
import com.vast.system.*;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class VastWorld implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(VastWorld.class);

	private final int FRAME_RATE = 30;
	private final int FRAME_DURATION_MILLIS = 1000 / FRAME_RATE;

	private Metrics metrics;
	private WorldConfiguration worldConfiguration;
	private Map<String, VastPeer> peers;
	private Map<String, QuadTree> quadTrees;
	private Items items;

	private World world;
	private boolean isAlive;
	private long lastFrameStartTime;
	private float timeModifier = 1f;

	public VastWorld(VastServerApplication serverApplication, String snapshotFile, Random random,
					 boolean showMonitor, Metrics metrics, WorldConfiguration worldConfiguration,
					 Items items, Recipes recipes) {
		this.metrics = metrics;
		this.worldConfiguration = worldConfiguration;
		this.items = items;

		peers = new HashMap<>();
		Map<String, List<IncomingRequest>> incomingRequestsByPeer = new HashMap<>();
		quadTrees = new HashMap<>();
		quadTrees.put("default", new QuadTree(0, 0, worldConfiguration.width, worldConfiguration.height));
		quadTrees.put("observers", new QuadTree(0, 0, worldConfiguration.width, worldConfiguration.height));
		InteractionHandler[] interactionHandlers = {
			new GrowingInteractionHandler(),
			new HarvestableInteractionHandler(items),
			new ConstructableInteractionHandler(),
			new ProducerInteractionHandler(recipes),
			new ContainerInteractionHandler(items),
			new FueledInteractionHandler()
		};
		OrderHandler[] orderHandlers = {
			new MoveObserverOrderHandler(),
			new AttachObserverOrderHandler(),
			new BuildOrderHandler(recipes),
			new MoveOrderHandler(),
			new InteractOrderHandler(interactionHandlers),
			new EmoteOrderHandler(),
			new ChatOrderHandler(),
			new CraftOrderHandler(recipes, items),
			new FollowOrderHandler(),
			new PlantOrderHandler(items),
			new SetHomeOrderHandler()
		};
		PropertyHandler[] propertyHandlers = {
			new PositionPropertyHandler(0.3f),
			new RotationPropertyHandler(10f),
			new ActivePropertyHandler(),
			new ConstructableProgressPropertyHandler(10),
			new CraftProgressPropertyHandler(10),
			new GrowingProgressPropertyHandler(20),
			new ProducerProgressPropertyHandler(recipes, 10),
			new InventoryPropertyHandler(),
			new FueledPropertyHandler(),
			new HomePropertyHandler(),
			new StatePropertyHandler(),
			new ConfigurationPropertyHandler(items, recipes),
			new SkillPropertyHandler(5),
			new ValidPropertyHandler()
		};
		Map<String, Behaviour> behaviours = new HashMap<>();
		behaviours.put("human", new HumanBehaviour(interactionHandlers, worldConfiguration, random, items, recipes));
		behaviours.put("adultAnimal", new AdultAnimalBehaviour(interactionHandlers, worldConfiguration, random));
		behaviours.put("youngAnimal", new YoungAnimalBehaviour(interactionHandlers, worldConfiguration, random));

		WorldConfigurationBuilder worldConfigurationBuilder = new WorldConfigurationBuilder().with(
			new CreationManager(worldConfiguration, random),
			new TimeManager(),

			new ScanClearingSystem(),
			new WorldSerializationSystem(snapshotFile, metrics, TimeUnit.MINUTES.toSeconds(5)),
			new PeerTransferSystem(serverApplication, peers),
			new IncomingRequestTransferSystem(serverApplication, incomingRequestsByPeer),
			new PeerEntitySystem(peers),
			new DeactivateSystem(peers),
			new ActivateSystem(peers),
			new ConfigurationSystem(),
			new QuadTreeAddRemoveSystem(quadTrees, worldConfiguration),
			new QuadTreeUpdateSystem(worldConfiguration),
			new QuadScanSystem(quadTrees.get("default"), worldConfiguration),
			new ObserverScanSystem(),
			new CreateSystem(propertyHandlers),
			new CullingSystem(propertyHandlers),
			new IncomingObserverOrderSystem(incomingRequestsByPeer),
			new OrderSystem(orderHandlers),
			new InteractSystem(interactionHandlers),
			new ValidSystem(0.75f),
			new AISystem(behaviours, random),
			new EncumbranceSystem(75),
			new PathMoveSystem(3f),
			new CollisionSystem(worldConfiguration, quadTrees.get("default"), random, metrics),
			new FollowSystem(),
			new FuelSystem(),
			new CraftSystem(items),
			new ProducerSystem(items, recipes),
			new GrowSystem(),
			new LifetimeSystem(),
			new PickupSystem(worldConfiguration, random),
			new DayNightCycleSystem(worldConfiguration),
			new WeatherSystem(random),
			new ParentSystem(),
			new DeleteSystem(),
			new EventSystem(metrics),
			new SyncSystem(peers, propertyHandlers, metrics),
			new SyncHistoryRemoveSystem()
		).with(
			new WorldSerializationManager(),
			new EntityLinkManager()
		);
		if (showMonitor) {
			worldConfigurationBuilder.with(WorldConfigurationBuilder.Priority.LOWEST, new MonitorSystem(this, 1f / 30));
			worldConfigurationBuilder.register(new ProfiledInvocationStrategy(metrics));
		}
		world = new World(worldConfigurationBuilder.build());

		for (PropertyHandler propertyHandler : propertyHandlers) {
			world.inject(propertyHandler);
		}

		isAlive = true;
	}

	@Override
	public void run() {
		lastFrameStartTime = System.currentTimeMillis();
		while (isAlive) {
			long frameStartTime = System.currentTimeMillis();
			int timeSinceLastFrame = (int) (frameStartTime - lastFrameStartTime);
			if (metrics != null) {
				metrics.setTimePerFrameMs(timeSinceLastFrame);
			}
			float delta = (timeSinceLastFrame / 1000f) * timeModifier;
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
		isAlive = false;
	}

	public void setTimeModifier(float timeModifier) {
		this.timeModifier = timeModifier;
	}

	public float getTimeModifier() {
		return timeModifier;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public WorldConfiguration getWorldConfiguration() {
		return worldConfiguration;
	}

	public Items getItems() {
		return items;
	}

	public Collection<QuadTree> getQuadTrees() {
		return quadTrees.values();
	}

	public QuadTree getQuadTree(String name) {
		return quadTrees.get(name);
	}

	public World getWorld() {
		return world;
	}
}
