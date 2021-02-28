package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.Random;

public class PickupSystem extends IteratingSystem {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Lifetime> lifetimeMapper;

	private final float PICKUP_LIFETIME = 3f;

	private Random random;

	private CreationManager creationManager;
	private Vector2f reusableVector;
	private Point2f reusablePosition;


	public PickupSystem(Random random) {
		super(Aspect.all(Player.class, Active.class, Scan.class));
		this.random = random;

		reusableVector = new Vector2f();
		reusablePosition = new Point2f();
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

			double randomAngle = Math.toRadians(random.nextDouble() * 360f);
			reusableVector.set(
				(float) Math.cos(randomAngle) * scan.distance * 0.95f,
				(float) Math.sin(randomAngle) * scan.distance * 0.95f
			);
			reusablePosition.set(
				transform.position.x + reusableVector.x,
				transform.position.y + reusableVector.y
			);

			int pickupEntity;
			if (random.nextFloat() < 0.55f) {
				pickupEntity = creationManager.createPickup("woodPile", reusablePosition);
			} else {
				pickupEntity = creationManager.createPickup("stonePile", reusablePosition);
			}
			lifetimeMapper.create(pickupEntity).timeLeft = PICKUP_LIFETIME;
		}
	}
}
