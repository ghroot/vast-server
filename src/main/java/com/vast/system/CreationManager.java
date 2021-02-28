package com.vast.system;

import com.artemis.*;
import com.artemis.annotations.PrefabData;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.managers.WorldSerializationManager;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;
import com.vast.data.*;
import com.vast.network.Properties;
import com.vast.prefab.VastPrefab;
import fastnoise.FastNoise;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.Random;

public class CreationManager extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Container> containerMapper;
	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Parent> parentMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Growing> growingMapper;
	private ComponentMapper<Plantable> plantableMapper;
	private ComponentMapper<Group> groupMapper;
	private ComponentMapper<Teach> teachMapper;

	private WorldConfiguration worldConfiguration;
	private Random random;
	private Items items;
	private Buildings buildings;

	private Archetype worldArchetype;
	private PlayerTemplate playerTemplate;
	private TreeTemplate treeTemplate;
	private RockTemplate rockTemplate;
	private AnimalTemplate animalTemplate;
	private Archetype buildingArchetype;
	private PickupTemplate pickupTemplate;

	private int nextAnimalGroupId = 0;

	public CreationManager(WorldConfiguration worldConfiguration, Random random, Items items, Buildings buildings) {
		this.worldConfiguration = worldConfiguration;
		this.random = random;
		this.items = items;
		this.buildings = buildings;
	}

	@Override
	protected void initialize() {
		// TODO: This will be overwritten by WorldSerializationSystem
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));

		worldArchetype = new ArchetypeBuilder()
			.add(Time.class)
			.add(Weather.class)
			.build(world);

		playerTemplate = new PlayerTemplate(world);
		treeTemplate = new TreeTemplate(world);
		rockTemplate = new RockTemplate(world);
		animalTemplate = new AnimalTemplate(world);

		buildingArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(Owner.class)
			.add(Transform.class)
			.add(Known.class)
			.add(Static.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		pickupTemplate = new PickupTemplate(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		world.create(worldArchetype);

		FastNoise noise1 = new FastNoise((int) (random.nextDouble() * 10000000));
		FastNoise noise2 = new FastNoise((int) (random.nextDouble() * 10000000));
		for (float x = -worldConfiguration.width / 2f; x < worldConfiguration.width / 2f; x += 3f) {
			for (float y = -worldConfiguration.height / 2f; y < worldConfiguration.height / 2f; y += 3f) {
				if (noise1.GetSimplex(x, y) > 0.35f) {
					createTree(new Point2f(x - 1f + random.nextFloat() * 2f, y - 1f + random.nextFloat() * 2f), false);
				}
				if (noise1.GetWhiteNoise(x, y) > 0.9f) {
					createRock(new Point2f(x, y));
				}
				if (noise2.GetWhiteNoise(x, y) > 0.99f) {
					createAnimalGroup(new Point2f(x, y), random.nextFloat() < 0.5f ? "rabbit" : "deer");
				}
			}
		}
	}

	public int createPlayer(String name, int subType, boolean fakePlayer) {
		return createPlayer(name, subType, null, fakePlayer);
	}

	public int createPlayer(String name, int subType, Point2f position, boolean fakePlayer) {
		return playerTemplate.create(name, subType, position, fakePlayer);
	}

	@PrefabData("com/vast/prefab/player.json")
	public class PlayerTemplate extends VastPrefab {
		public PlayerTemplate(World world) {
			super(world);
		}

		public int create(String name, int subType, Point2f position, boolean fakePlayer) {
			int playerEntity = create().get("player").getId();
			playerMapper.get(playerEntity).name = name;
			subTypeMapper.get(playerEntity).subType = subType;
			if (position != null) {
				transformMapper.get(playerEntity).position.set(position);
			} else {
				transformMapper.get(playerEntity).position.set(getRandomPositionInWorld());
			}
			if (fakePlayer) {
				aiMapper.create(playerEntity).behaviourName = "human";
			}
			return playerEntity;
		}
	}

	public int createTree(Point2f position, boolean growing) {
		return treeTemplate.create(position, growing);
	}

	@PrefabData("com/vast/prefab/tree.json")
	public class TreeTemplate extends VastPrefab {
		public TreeTemplate(World world) {
			super(world);
		}

		public int create(Point2f position, boolean growing) {
			int treeEntity = create().get("tree").getId();
			subTypeMapper.get(treeEntity).subType = (int) (random.nextFloat() * 6);
			transformMapper.get(treeEntity).position.set(position);
			transformMapper.get(treeEntity).rotation = random.nextFloat() * 360;
			if (growing) {
				growingMapper.create(treeEntity).timeLeft = 60f;
			}
			return treeEntity;
		}
	}

	public int createRock(Point2f position) {
		return rockTemplate.create(position);
	}

	@PrefabData("com/vast/prefab/rock.json")
	public class RockTemplate extends VastPrefab {
		public RockTemplate(World world) {
			super(world);
		}

		public int create(Point2f position) {
			int rockEntity = create().get("rock").getId();
			subTypeMapper.get(rockEntity).subType = (int) (random.nextFloat() * 3);
			transformMapper.get(rockEntity).position.set(position);
			transformMapper.get(rockEntity).rotation = random.nextFloat() * 360;
			return rockEntity;
		}
	}

	private void createAnimalGroup(Point2f position, String animalNamePrefix) {
		int groupId = nextAnimalGroupId++;
		createAnimal(new Point2f(position.x - 1f + 2f * random.nextFloat(), position.y - 1f + 2f * random.nextFloat()), animalNamePrefix + "Adult", groupId);
		createAnimal(new Point2f(position.x - 1f + 2f * random.nextFloat(), position.y - 1f + 2f * random.nextFloat()), animalNamePrefix + "Young", groupId);
		createAnimal(new Point2f(position.x - 1f + 2f * random.nextFloat(), position.y - 1f + 2f * random.nextFloat()), animalNamePrefix + "Young", groupId);
	}

	public int createAnimal(Point2f position, String animalName, int groupId) {
		return animalTemplate.create(position, animalName, groupId);
	}

	@PrefabData("com/vast/prefab/animal.json")
	public class AnimalTemplate extends VastPrefab {
		public AnimalTemplate(World world) {
			super(world);
		}

		public int create(Point2f position, String animalName, int groupId) {
			int animalEntity = create().get(animalName).getId();
			transformMapper.get(animalEntity).position.set(position);
			groupMapper.get(animalEntity).id = groupId;
			return animalEntity;
		}
	}

	public int createBuilding(Point2f position, int buildingId) {
		Building building = buildings.getBuilding(buildingId);
		int buildingEntity = world.create(buildingArchetype);
		typeMapper.get(buildingEntity).type = "building";
		subTypeMapper.get(buildingEntity).subType = buildingId;
		transformMapper.get(buildingEntity).position.set(position);

		JSONObject constructableAspect = building.getAspect("constructable");
		Constructable constructable = constructableMapper.create(buildingEntity);
		constructable.buildDuration = (float) constructableAspect.getInt("duration");

		if (building.hasAspect("collision")) {
			JSONObject collisionAspect = building.getAspect("collision");
			Collision collision = collisionMapper.create(buildingEntity);
			collision.radius = collisionAspect.getFloat("radius");
		}

		if (building.hasAspect("inventory")) {
			JSONObject inventoryAspect = building.getAspect("inventory");
			Inventory inventory = inventoryMapper.create(buildingEntity);
			inventory.capacity = inventoryAspect.getInt("capacity");
		}

		if (building.hasAspect("container")) {
			JSONObject containerAspect = building.getAspect("container");
			Container container = containerMapper.create(buildingEntity);
			container.persistent = containerAspect.getBoolean("persistent");
		}

		if (building.hasAspect("fueled")) {
			JSONObject fueledAspect = building.getAspect("fueled");
			Fueled fueled = fueledMapper.create(buildingEntity);
			for (String itemName : fueledAspect.getJSONObject("cost").keySet()) {
				int amount = fueledAspect.getJSONObject("cost").getInt(itemName);
				fueled.cost = new Cost(items.getItem(itemName).getId(), amount);
				break;
			}
		}

		if (building.hasAspect("plantable")) {
			plantableMapper.create(buildingEntity);
		}

		syncPropagationMapper.get(buildingEntity).setOwnerPropagation(Properties.INVENTORY);

		return buildingEntity;
	}

	public int createPickup(Point2f position, int subType, short[] items) {
		return pickupTemplate.create(position, subType, items);
	}

	@PrefabData("com/vast/prefab/pickup.json")
	public class PickupTemplate extends VastPrefab {
		public PickupTemplate(World world) {
			super(world);
		}

		public int create(Point2f position, int subType, short[] items) {
			int pickupEntity = create().get("pickup").getId();
			subTypeMapper.get(pickupEntity).subType = subType;
			transformMapper.get(pickupEntity).position.set(position);
			transformMapper.get(pickupEntity).rotation = random.nextFloat() * 360f;
			inventoryMapper.get(pickupEntity).add(items);
			return pickupEntity;
		}
	}

	public int createPickup(Point2f position, int subType, Inventory inventory) {
		return createPickup(position, subType, inventory.items);
	}

	private Point2f getRandomPositionInWorld() {
		return new Point2f(-worldConfiguration.width / 2f + random.nextFloat() * worldConfiguration.width, -worldConfiguration.height / 2f + random.nextFloat() * worldConfiguration.height);
	}
}
