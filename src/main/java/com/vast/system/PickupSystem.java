package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;

import javax.vecmath.Point2f;
import java.util.Random;

public class PickupSystem extends IteratingSystem {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Lifetime> lifetimeMapper;

	private final float PICKUP_LIFETIME = 3f;

	private WorldConfiguration worldConfiguration;
	private Random random;

	private CreationManager creationManager;
	private Point2f reusableSpawnPosition;


	public PickupSystem(WorldConfiguration worldConfiguration, Random random) {
		super(Aspect.all(Player.class, Active.class, Scan.class));
		this.worldConfiguration = worldConfiguration;
		this.random = random;

		reusableSpawnPosition = new Point2f();
	}

	@Override
	protected void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	protected void process(int playerEntity) {
		Scan scan = scanMapper.get(playerEntity);

		boolean hasPickupNearby = false;
		int[] nearbyEntities = scan.nearbyEntities.getData();
		for (int i = 0, size = scan.nearbyEntities.size(); i < size; ++i) {
			int nearbyEntity = nearbyEntities[i];
			if (typeMapper.has(nearbyEntity) && typeMapper.get(nearbyEntity).type.equals("pickup")) {
				lifetimeMapper.create(nearbyEntity).timeLeft = PICKUP_LIFETIME;
				hasPickupNearby = true;
			}
		}
		if (!hasPickupNearby) {
			Transform transform = transformMapper.get(playerEntity);

			Point2f randomSpawnPosition = getRandomSpawnPosition(transform.position, scan.distance * 0.95f);
			if (randomSpawnPosition != null) {
				int pickupEntity;
				if (random.nextFloat() < 0.55f) {
					pickupEntity = creationManager.createPickup("woodPile", randomSpawnPosition);
				} else {
					pickupEntity = creationManager.createPickup("stonePile", randomSpawnPosition);
				}
				lifetimeMapper.create(pickupEntity).timeLeft = PICKUP_LIFETIME;
			}
		}
	}

	protected Point2f getRandomSpawnPosition(Point2f position, float distance) {
		for (int i = 0; i < 10; i++) {
			int randomAngle = random.nextInt(360);
			float dx = (float) Math.cos(Math.toRadians(randomAngle)) * distance;
			float dy = (float) Math.sin(Math.toRadians(randomAngle)) * distance;
			if (isPositionInWorld(position.x + dx, position.y + dy)) {
				reusableSpawnPosition.set(position.x + dx, position.y + dy);
				return reusableSpawnPosition;
			}
		};

		return null;
	}

	private boolean isPositionInWorld(float x, float y) {
		if (x < -worldConfiguration.width / 2f  || x > worldConfiguration.width / 2f ||
				y < -worldConfiguration.height / 2f || y > worldConfiguration.height / 2f) {
			return false;
		} else {
			return true;
		}
	}
}
