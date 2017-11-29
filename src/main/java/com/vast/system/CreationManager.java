package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Buildings;
import com.vast.data.Cost;
import com.vast.data.Items;
import com.vast.data.WorldConfiguration;
import fastnoise.FastNoise;
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
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Container> containerMapper;
	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Aura> auraMapper;
	private ComponentMapper<Parent> parentMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private WorldConfiguration worldConfiguration;
	private Items items;
	private Buildings buildings;

	private Archetype playerArchetype;
	private Archetype treeArchetype;
	private Archetype rockArchetype;
	private Archetype aiArchetype;
	private Archetype buildingArchetype;
	private Archetype crateArchetype;

	public CreationManager(WorldConfiguration worldConfiguration, Items items, Buildings buildings) {
		this.worldConfiguration = worldConfiguration;
		this.items = items;
		this.buildings = buildings;
	}

	@Override
	protected void initialize() {
		playerArchetype = new ArchetypeBuilder()
				.add(Player.class)
				.add(Type.class)
				.add(SubType.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Scan.class)
				.add(Known.class)
				.add(Attack.class)
				.add(SyncPropagation.class)
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Static.class)
				.add(Harvestable.class)
				.add(Inventory.class)
				.add(SyncPropagation.class)
				.build(world);

		rockArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Static.class)
				.add(Harvestable.class)
				.add(Inventory.class)
				.add(SyncPropagation.class)
				.build(world);

		aiArchetype = new ArchetypeBuilder()
				.add(AI.class)
				.add(Type.class)
				.add(SubType.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Attack.class)
				.add(SyncPropagation.class)
				.build(world);

		buildingArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(SubType.class)
				.add(Owner.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Static.class)
				.add(Constructable.class)
				.add(SyncPropagation.class)
				.build(world);

		crateArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
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
		FastNoise noise = new FastNoise();
		for (float x = -worldConfiguration.width / 2.0f; x < worldConfiguration.width / 2.0f; x += 3.0f) {
			for (float y = -worldConfiguration.height / 2.0f; y < worldConfiguration.height / 2.0f; y += 3.0f) {
				if (noise.GetSimplex(x, y) > 0.35f) {
					createTree(new Point2f(x - 1.0f + (float) Math.random() * 2.0f, y - 1.0f + (float) Math.random() * 2.0f));
				}
				if (noise.GetWhiteNoise(x, y) > 0.8f) {
					createRock(new Point2f(x, y));
				}
			}
		}

		for (int i = 0; i < worldConfiguration.numberOfAIs; i++) {
			createAI(getRandomPositionInWorld());
		}
	}

	private int createTree(Point2f position) {
		int treeEntity = world.create(treeArchetype);
		typeMapper.get(treeEntity).type = "tree";
		transformMapper.get(treeEntity).position.set(position);
		collisionMapper.get(treeEntity).radius = 0.1f;
		inventoryMapper.get(treeEntity).add(items.getItem("wood").getType(), 3);
		syncPropagationMapper.get(treeEntity).setUnreliable(Properties.DURABILITY);
		return treeEntity;
	}

	private int createRock(Point2f position) {
		int rockEntity = world.create(rockArchetype);
		typeMapper.get(rockEntity).type = "rock";
		transformMapper.get(rockEntity).position.set(position);
		collisionMapper.get(rockEntity).radius = 0.2f;
		inventoryMapper.get(rockEntity).add(items.getItem("stone").getType(), 2);
		syncPropagationMapper.get(rockEntity).setUnreliable(Properties.DURABILITY);
		return rockEntity;
	}

	private int createAI(Point2f position) {
		int aiEntity = world.create(aiArchetype);
		typeMapper.get(aiEntity).type = "ai";
		subTypeMapper.get(aiEntity).subType = aiEntity % 3;
		transformMapper.get(aiEntity).position.set(position);
		aiMapper.get(aiEntity).behaviourName = "basic";
		collisionMapper.get(aiEntity).radius = 0.3f;
		healthMapper.get(aiEntity).maxHealth = 2;
		healthMapper.get(aiEntity).health = 2;
		syncPropagationMapper.get(aiEntity).setUnreliable(Properties.POSITION);
		syncPropagationMapper.get(aiEntity).setUnreliable(Properties.ROTATION);
		return aiEntity;
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
		scanMapper.get(playerEntity).distance = 15.0f;
		collisionMapper.get(playerEntity).radius = 0.3f;
		healthMapper.get(playerEntity).maxHealth = 5;
		healthMapper.get(playerEntity).health = 5;
		syncPropagationMapper.get(playerEntity).setUnreliable(Properties.POSITION);
		syncPropagationMapper.get(playerEntity).setUnreliable(Properties.ROTATION);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.INVENTORY);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.HOME);
		if (fakePlayer) {
			aiMapper.create(playerEntity).behaviourName = "fakeHuman";
		}
		return playerEntity;
	}

	public int createPlayer(String name, int subType, boolean fakePlayer) {
		return createPlayer(name, subType, null, fakePlayer);
	}

	public int createBuilding(Point2f position, int buildingType) {
		int buildingEntity = world.create(buildingArchetype);
		typeMapper.get(buildingEntity).type = "building";
		subTypeMapper.get(buildingEntity).subType = buildingType;
		transformMapper.get(buildingEntity).position.set(position);
		constructableMapper.get(buildingEntity).buildDuration = buildings.getBuilding(buildingType).getBuildDuration();
		if (buildingType == 0) {
			collisionMapper.get(buildingEntity).radius = 0.8f;
		} else if (buildingType == 1) {
			collisionMapper.get(buildingEntity).radius = 0.5f;
			inventoryMapper.create(buildingEntity);
			containerMapper.create(buildingEntity).persistent = true;
		} else if (buildingType == 2) {
			collisionMapper.get(buildingEntity).radius = 0.5f;
			healthMapper.create(buildingEntity).maxHealth = 3;
			healthMapper.create(buildingEntity).health = 3;
		} else if (buildingType == 3) {
			fueledMapper.create(buildingEntity).cost = new Cost(items.getItem("wood").getType(), 1);
			fueledMapper.get(buildingEntity).fueledAuraEffectName = "heal";
		}
		return buildingEntity;
	}

	public int createCrate(Point2f position, Inventory inventory) {
		int crateEntity = world.create(crateArchetype);
		typeMapper.get(crateEntity).type = "crate";
		transformMapper.get(crateEntity).position.set(position);
		collisionMapper.get(crateEntity).radius = 0.1f;
		inventoryMapper.get(crateEntity).add(inventory);
		return crateEntity;
	}

	public int createAura(Point2f position, String effectName, float range, int parentEntity) {
		int auraEntity = world.create();
		transformMapper.create(auraEntity).position.set(position);
		spatialMapper.create(auraEntity);
		auraMapper.create(auraEntity).range = range;
		auraMapper.get(auraEntity).effectName = effectName;
		scanMapper.create(auraEntity);
		if (parentEntity != -1) {
			parentMapper.create(auraEntity).parentEntity = parentEntity;
		}
		return auraEntity;
	}

	private Point2f getRandomPositionInWorld() {
		return new Point2f(-worldConfiguration.width / 2.0f + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2.0f + (float) Math.random() * worldConfiguration.height);
	}
}
