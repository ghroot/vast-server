package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
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
	private ComponentMapper<Pickup> pickupMapper;
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Building> buildingMapper;

	private WorldConfiguration worldConfiguration;

	private Archetype playerArchetype;
	private Archetype treeArchetype;
	private Archetype pickupArchetype;
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
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Interactable.class)
				.add(Harvestable.class)
				.build(world);

		pickupArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Pickup.class)
				.build(world);

		aiArchetype = new ArchetypeBuilder()
				.add(AI.class)
				.add(Type.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Scan.class)
				.add(Interactable.class)
				.add(Attack.class)
				.build(world);

		buildingArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Building.class)
				.add(Interactable.class)
				.build(world);

		crateArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Pickup.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < worldConfiguration.numberOfTrees; i++) {
			int treeEntity = world.create(treeArchetype);
			typeMapper.get(treeEntity).type = "tree";
			transformMapper.get(treeEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(treeEntity).isStatic = true;
			collisionMapper.get(treeEntity).radius = 0.1f;
			harvestableMapper.get(treeEntity).itemType = 2;
			harvestableMapper.get(treeEntity).itemCount = 4;
		}

		for (int i = 0; i < worldConfiguration.numberOfPickups; i++) {
			int pickupEntity = world.create(pickupArchetype);
			typeMapper.get(pickupEntity).type = "pickup";
			transformMapper.get(pickupEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(pickupEntity).isStatic = true;
			collisionMapper.get(pickupEntity).radius = 0.1f;
			pickupMapper.create(pickupEntity).type = 3;
		}

		for (int i = 0; i < worldConfiguration.numberOfAIs; i++) {
			int aiEntity = world.create(aiArchetype);
			typeMapper.get(aiEntity).type = "ai";
			transformMapper.get(aiEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(aiEntity).radius = 0.3f;
			healthMapper.get(aiEntity).maxHealth = 2;
			healthMapper.get(aiEntity).health = 2;
		}
	}

	public int createPlayer(String name, boolean ai) {
		int playerEntity = world.create(playerArchetype);
		playerMapper.get(playerEntity).name = name;
		typeMapper.get(playerEntity).type = "player";
		transformMapper.get(playerEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
		collisionMapper.get(playerEntity).radius = 0.3f;
		healthMapper.get(playerEntity).maxHealth = 5;
		healthMapper.get(playerEntity).health = 5;
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
		return buildingEntity;
	}

	public int createCrate(Point2f position) {
		int crateEntity = world.create(crateArchetype);
		typeMapper.get(crateEntity).type = "pickup";
		transformMapper.get(crateEntity).position.set(position);
		collisionMapper.get(crateEntity).isStatic = true;
		collisionMapper.get(crateEntity).radius = 0.1f;
		pickupMapper.create(crateEntity).type = 2;
		return crateEntity;
	}
}
