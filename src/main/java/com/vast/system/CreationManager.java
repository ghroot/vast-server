package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.vast.ItemTypes;
import com.vast.Properties;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

public class CreationManager extends AbstractProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Building> buildingMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	private WorldConfiguration worldConfiguration;

	private Archetype playerArchetype;
	private Archetype treeArchetype;
	private Archetype rockArchetype;
	private Archetype aiArchetype;
	private Archetype buildingArchetype;
	private Archetype crateArchetype;

	public CreationManager(WorldConfiguration worldConfiguration) {
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		super.initialize();

		playerArchetype = new ArchetypeBuilder()
				.add(Player.class)
				.add(Type.class)
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
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Building.class)
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
			createTree(new Point2f(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height));
		}
		for (int i = 0; i < worldConfiguration.numberOfRocks; i++) {
			createRock(new Point2f(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height));
		}
		for (int i = 0; i < worldConfiguration.numberOfAIs; i++) {
			createAI(new Point2f(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height));
		}
	}

	private int createTree(Point2f position) {
		int treeEntity = world.create(treeArchetype);
		typeMapper.get(treeEntity).type = "tree";
		transformMapper.get(treeEntity).position.set(position);
		collisionMapper.get(treeEntity).isStatic = true;
		collisionMapper.get(treeEntity).radius = 0.1f;
		inventoryMapper.get(treeEntity).add(ItemTypes.WOOD, 3);
		syncPropagationMapper.get(treeEntity).setReliable(Properties.DURABILITY, false);
		return treeEntity;
	}

	private int createRock(Point2f position) {
		int rockEntity = world.create(rockArchetype);
		typeMapper.get(rockEntity).type = "rock";
		transformMapper.get(rockEntity).position.set(position);
		collisionMapper.get(rockEntity).isStatic = true;
		collisionMapper.get(rockEntity).radius = 0.2f;
		return rockEntity;
	}

	private int createAI(Point2f position) {
		int aiEntity = world.create(aiArchetype);
		typeMapper.get(aiEntity).type = "ai";
		transformMapper.get(aiEntity).position.set(position);
		collisionMapper.get(aiEntity).radius = 0.3f;
		healthMapper.get(aiEntity).maxHealth = 2;
		healthMapper.get(aiEntity).health = 2;
		syncPropagationMapper.get(aiEntity).setReliable(Properties.POSITION, false);
		return aiEntity;
	}

	public int createPlayer(String name, boolean ai) {
		int playerEntity = world.create(playerArchetype);
		playerMapper.get(playerEntity).name = name;
		typeMapper.get(playerEntity).type = "player";
		transformMapper.get(playerEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
		collisionMapper.get(playerEntity).radius = 0.3f;
		healthMapper.get(playerEntity).maxHealth = 5;
		healthMapper.get(playerEntity).health = 5;
		syncPropagationMapper.get(playerEntity).setReliable(Properties.POSITION, false);
		syncPropagationMapper.get(playerEntity).setPropagation(Properties.INVENTORY, SyncPropagation.Propagation.OWNER);
		if (ai) {
			aiMapper.create(playerEntity);
		}
		return playerEntity;
	}

	public int createBuilding(String type, Point2f position) {
		int buildingEntity = world.create(buildingArchetype);
		typeMapper.get(buildingEntity).type = "building";
		transformMapper.get(buildingEntity).position.set(position);
		collisionMapper.get(buildingEntity).isStatic = true;
		collisionMapper.get(buildingEntity).radius = 0.5f;
		buildingMapper.get(buildingEntity).type = type;
		syncPropagationMapper.get(buildingEntity).setReliable(Properties.DURABILITY, false);
		return buildingEntity;
	}

	public int createCrate(Point2f position, short[] items) {
		int crateEntity = world.create(crateArchetype);
		typeMapper.get(crateEntity).type = "crate";
		transformMapper.get(crateEntity).position.set(position);
		collisionMapper.get(crateEntity).isStatic = true;
		collisionMapper.get(crateEntity).radius = 0.1f;
		inventoryMapper.get(crateEntity).add(items);
		return crateEntity;
	}
}
