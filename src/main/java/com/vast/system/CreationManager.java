package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.*;
import com.vast.network.Properties;
import fastnoise.FastNoise;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

public class CreationManager extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Spatial> spatialMapper;
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
	private Items items;
	private Buildings buildings;
	private Animals animals;

	private Archetype worldArchetype;
	private Archetype playerArchetype;
	private Archetype treeArchetype;
	private Archetype rockArchetype;
	private Archetype animalArchetype;
	private Archetype buildingArchetype;
	private Archetype pickupArchetype;

	private int nextAnimalGroupId = 0;

	public CreationManager(WorldConfiguration worldConfiguration, Items items, Buildings buildings, Animals animals) {
		this.worldConfiguration = worldConfiguration;
		this.items = items;
		this.buildings = buildings;
		this.animals = animals;
	}

	@Override
	protected void initialize() {
		worldArchetype = new ArchetypeBuilder()
			.add(Time.class)
			.add(Weather.class)
			.build(world);

		playerArchetype = new ArchetypeBuilder()
			.add(Player.class)
			.add(Type.class)
			.add(SubType.class)
			.add(State.class)
			.add(Inventory.class)
			.add(Transform.class)
			.add(Speed.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Collision.class)
			.add(Skill.class)
			.add(Teach.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		treeArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(State.class)
			.add(Transform.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Collision.class)
			.add(Static.class)
			.add(Harvestable.class)
			.add(Inventory.class)
			.add(Teach.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		rockArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(State.class)
			.add(Transform.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Collision.class)
			.add(Static.class)
			.add(Harvestable.class)
			.add(Inventory.class)
			.add(Teach.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		animalArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(Group.class)
			.add(Transform.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Teach.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		buildingArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(Owner.class)
			.add(Transform.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Static.class)
			.add(SyncPropagation.class)
			.add(SyncHistory.class)
			.build(world);

		pickupArchetype = new ArchetypeBuilder()
			.add(Type.class)
			.add(SubType.class)
			.add(Transform.class)
			.add(Spatial.class)
			.add(Known.class)
			.add(Collision.class)
			.add(Static.class)
			.add(Inventory.class)
			.add(Container.class)
			.add(SyncPropagation.class)
			.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		world.create(worldArchetype);

		FastNoise noise1 = new FastNoise((int) (Math.random() * 10000000));
		FastNoise noise2 = new FastNoise((int) (Math.random() * 10000000));
		for (float x = -worldConfiguration.width / 2.0f; x < worldConfiguration.width / 2.0f; x += 3.0f) {
			for (float y = -worldConfiguration.height / 2.0f; y < worldConfiguration.height / 2.0f; y += 3.0f) {
				if (noise1.GetSimplex(x, y) > 0.35f) {
					createTree(new Point2f(x - 1.0f + (float) Math.random() * 2.0f, y - 1.0f + (float) Math.random() * 2.0f), false);
				}
				if (noise1.GetWhiteNoise(x, y) > 0.9f) {
					createRock(new Point2f(x, y));
				}
				if (noise2.GetWhiteNoise(x, y) > 0.99f) {
					createAnimalGroup(new Point2f(x, y), Math.random() < 0.5f ? 0 : 2);
				}
			}
		}
	}

	public int createPlayer(String name, int subType, Point2f position, boolean fakePlayer) {
		int playerEntity = world.create(playerArchetype);
		playerMapper.get(playerEntity).name = name;
		typeMapper.get(playerEntity).type = "player";
		subTypeMapper.get(playerEntity).subType = subType;
		if (position != null) {
			transformMapper.get(playerEntity).position.set(position);
		} else {
			transformMapper.get(playerEntity).position.set(getRandomPositionInWorld());
		}
		speedMapper.get(playerEntity).baseSpeed = 4.0f;
		collisionMapper.get(playerEntity).radius = 0.5f;
		inventoryMapper.get(playerEntity).capacity = 50;
		teachMapper.get(playerEntity).addWord("me");
		teachMapper.get(playerEntity).addWord("you");
		syncPropagationMapper.get(playerEntity).setUnreliable(Properties.POSITION);
		syncPropagationMapper.get(playerEntity).setUnreliable(Properties.ROTATION);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.INVENTORY);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.HOME);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.CONFIGURATION);
		if (fakePlayer) {
			aiMapper.create(playerEntity).behaviourName = "human";
		}
		return playerEntity;
	}

	public int createPlayer(String name, int subType, boolean fakePlayer) {
		return createPlayer(name, subType, null, fakePlayer);
	}

	// TODO: Move to json somehow?
	public int createTree(Point2f position, boolean growing) {
		int treeEntity = world.create(treeArchetype);
		typeMapper.get(treeEntity).type = "tree";
		subTypeMapper.get(treeEntity).subType = (int) (Math.random() * 6);
		transformMapper.get(treeEntity).position.set(position);
		transformMapper.get(treeEntity).rotation = (float) Math.random() * 360;
		collisionMapper.get(treeEntity).radius = 0.4f;
		harvestableMapper.get(treeEntity).requiredItemId = items.getItem("axe").getId();
		harvestableMapper.get(treeEntity).harvestEventName = "chopping";
		harvestableMapper.get(treeEntity).durability = 300.0f;
		inventoryMapper.get(treeEntity).add(items.getItem("wood").getId(), 3);
		inventoryMapper.get(treeEntity).add(items.getItem("seed").getId(), 1);
		teachMapper.get(treeEntity).addWord("tree");
		if (growing) {
			growingMapper.create(treeEntity).timeLeft = 60.0f;
		}
		syncPropagationMapper.get(treeEntity).setOwnerPropagation(Properties.INVENTORY);
		return treeEntity;
	}

	// TODO: Move to json somehow?
	public int createRock(Point2f position) {
		int rockEntity = world.create(rockArchetype);
		typeMapper.get(rockEntity).type = "rock";
		subTypeMapper.get(rockEntity).subType = (int) (Math.random() * 3);
		transformMapper.get(rockEntity).position.set(position);
		transformMapper.get(rockEntity).rotation = (float) Math.random() * 360;
		collisionMapper.get(rockEntity).radius = 0.4f;
		harvestableMapper.get(rockEntity).requiredItemId = items.getItem("pickaxe").getId();
		harvestableMapper.get(rockEntity).harvestEventName = "picking";
		harvestableMapper.get(rockEntity).durability = 300.0f;
		inventoryMapper.get(rockEntity).add(items.getItem("stone").getId(), 2);
		teachMapper.get(rockEntity).addWord("rock");
		syncPropagationMapper.get(rockEntity).setOwnerPropagation(Properties.INVENTORY);
		return rockEntity;
	}

	private void createAnimalGroup(Point2f position, int animalId) {
		int adultAnimalId = animalId;
		int youngAnimalId = animalId + 1;
		int groupId = nextAnimalGroupId++;
		createAnimal(new Point2f(position.x - 1.0f + 2.0f * (float) Math.random(), position.y - 1.0f + 2.0f * (float) Math.random()), adultAnimalId, groupId);
		createAnimal(new Point2f(position.x - 1.0f + 2.0f * (float) Math.random(), position.y - 1.0f + 2.0f * (float) Math.random()), youngAnimalId, groupId);
		createAnimal(new Point2f(position.x - 1.0f + 2.0f * (float) Math.random(), position.y - 1.0f + 2.0f * (float) Math.random()), youngAnimalId, groupId);
	}

	public int createAnimal(Point2f position, int animalId, int groupId) {
		Animal animal = animals.getAnimal(animalId);
		int animalEntity = world.create(animalArchetype);
		typeMapper.get(animalEntity).type = "animal";
		subTypeMapper.get(animalEntity).subType = animalId;
		transformMapper.get(animalEntity).position.set(position);
		groupMapper.get(animalEntity).id = groupId;
		teachMapper.get(animalEntity).addWord("animal");

		if (animal.hasAspect("collision")) {
			JSONObject collisionAspect = animal.getAspect("collision");
			Collision collision = collisionMapper.create(animalEntity);
			collision.radius = collisionAspect.getFloat("radius");
		}

		if (animal.hasAspect("inventory")) {
			JSONObject inventoryAspect = animal.getAspect("inventory");
			Inventory inventory = inventoryMapper.create(animalEntity);
			for (String itemName : inventoryAspect.getJSONObject("items").keySet()) {
				int amount = inventoryAspect.getJSONObject("items").getInt(itemName);
				inventory.add(items.getItem(itemName).getId(), amount);
			}
		}

		if (animal.hasAspect("speed")) {
			JSONObject speedAspect = animal.getAspect("speed");
			Speed speed = speedMapper.create(animalEntity);
			speed.baseSpeed = speedAspect.getFloat("baseSpeed");
		}

		if (animal.hasAspect("ai")) {
			JSONObject aiAspect = animal.getAspect("ai");
			AI ai = aiMapper.create(animalEntity);
			ai.behaviourName = aiAspect.getString("behaviour");
		}

		syncPropagationMapper.get(animalEntity).setUnreliable(Properties.POSITION);
		syncPropagationMapper.get(animalEntity).setUnreliable(Properties.ROTATION);
		syncPropagationMapper.get(animalEntity).setOwnerPropagation(Properties.INVENTORY);
		return animalEntity;
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
		int pickupEntity = world.create(pickupArchetype);
		typeMapper.get(pickupEntity).type = "pickup";
		subTypeMapper.get(pickupEntity).subType = subType;
		transformMapper.get(pickupEntity).position.set(position);
		transformMapper.get(pickupEntity).rotation = (float) Math.random() * 360.0f;
		inventoryMapper.get(pickupEntity).add(items);
		syncPropagationMapper.get(pickupEntity).setOwnerPropagation(Properties.INVENTORY);
		return pickupEntity;
	}

	public int createPickup(Point2f position, int subType, Inventory inventory) {
		return createPickup(position, subType, inventory.items);
	}

	private Point2f getRandomPositionInWorld() {
		return new Point2f(-worldConfiguration.width / 2.0f + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2.0f + (float) Math.random() * worldConfiguration.height);
	}
}
