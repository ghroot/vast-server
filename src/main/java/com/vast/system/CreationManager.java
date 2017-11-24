package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import com.vast.data.Buildings;
import com.vast.data.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

public class CreationManager extends AbstractProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Constructable> constructableMapper;
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
		super.initialize();

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
				.add(Interactable.class)
				.add(Attack.class)
				.add(SyncPropagation.class)
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Interactable.class)
				.add(Harvestable.class)
				.add(Inventory.class)
				.add(SyncPropagation.class)
				.build(world);

		rockArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
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
				.add(Interactable.class)
				.add(Attack.class)
				.add(SyncPropagation.class)
				.build(world);

		buildingArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(SubType.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Constructable.class)
				.add(SyncPropagation.class)
				.build(world);

		crateArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Inventory.class)
				.add(Interactable.class)
				.add(Container.class)
				.add(SyncPropagation.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < worldConfiguration.numberOfTrees; i++) {
			createTree(getRandomPositionInWorld());
		}
		for (int i = 0; i < worldConfiguration.numberOfRocks; i++) {
			createRock(getRandomPositionInWorld());
		}
		for (int i = 0; i < worldConfiguration.numberOfAIs; i++) {
			createAI(getRandomPositionInWorld());
		}
	}

	private Point2f getRandomPositionInWorld() {
		return new Point2f(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
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
		return rockEntity;
	}

	private int createAI(Point2f position) {
		int aiEntity = world.create(aiArchetype);
		typeMapper.get(aiEntity).type = "ai";
		subTypeMapper.get(aiEntity).subType = aiEntity % 4;
		transformMapper.get(aiEntity).position.set(position);
		collisionMapper.get(aiEntity).isStatic = false;
		collisionMapper.get(aiEntity).radius = 0.3f;
		healthMapper.get(aiEntity).maxHealth = 2;
		healthMapper.get(aiEntity).health = 2;
		activeMapper.create(aiEntity);
		syncPropagationMapper.get(aiEntity).setUnreliable(Properties.POSITION);
		return aiEntity;
	}

	public int createPlayer(String name, int subType, boolean ai) {
		int playerEntity = world.create(playerArchetype);
		playerMapper.get(playerEntity).name = name;
		typeMapper.get(playerEntity).type = "player";
		subTypeMapper.get(playerEntity).subType = subType;
		transformMapper.get(playerEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
		collisionMapper.get(playerEntity).isStatic = false;
		collisionMapper.get(playerEntity).radius = 0.3f;
		healthMapper.get(playerEntity).maxHealth = 5;
		healthMapper.get(playerEntity).health = 5;
		syncPropagationMapper.get(playerEntity).setUnreliable(Properties.POSITION);
		syncPropagationMapper.get(playerEntity).setOwnerPropagation(Properties.INVENTORY);
		if (ai) {
			aiMapper.create(playerEntity);
		}
		return playerEntity;
	}

	public int createBuilding(Point2f position, int buildingType) {
		int buildingEntity = world.create(buildingArchetype);
		typeMapper.get(buildingEntity).type = "building";
		subTypeMapper.get(buildingEntity).subType = buildingType;
		transformMapper.get(buildingEntity).position.set(position);
		collisionMapper.get(buildingEntity).radius = 0.5f;
		constructableMapper.get(buildingEntity).buildDuration = buildings.getBuilding(buildingType).getBuildDuration();
		return buildingEntity;
	}

	public int createCrate(Point2f position, short[] items) {
		int crateEntity = world.create(crateArchetype);
		typeMapper.get(crateEntity).type = "crate";
		transformMapper.get(crateEntity).position.set(position);
		collisionMapper.get(crateEntity).radius = 0.1f;
		inventoryMapper.get(crateEntity).add(items);
		return crateEntity;
	}
}
