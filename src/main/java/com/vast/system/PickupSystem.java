package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class PickupSystem extends IteratingSystem {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Lifetime> lifeTimeMapper;

	private final float PICKUP_LIFETIME = 3.0f;

	private CreationManager creationManager;
	private Vector2f reusableVector;
	private Point2f reusablePosition;

	public PickupSystem() {
		super(Aspect.all(Player.class, Active.class, Scan.class));

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
		for (int nearbyEntity : scan.nearbyEntities) {
			if (typeMapper.get(nearbyEntity).type.equals("pickup")) {
				lifeTimeMapper.create(nearbyEntity).timeLeft = PICKUP_LIFETIME;
				hasPickupNearby = true;
			}
		}
		if (!hasPickupNearby) {
			Transform transform = transformMapper.get(playerEntity);

			double randomAngle = Math.toRadians(Math.random() * 360.0f);
			reusableVector.set(
				(float) Math.cos(randomAngle) * scan.distance,
				(float) Math.sin(randomAngle) * scan.distance
			);
			reusablePosition.set(
				transform.position.x + reusableVector.x,
				transform.position.y + reusableVector.y
			);

			int pickupEntity;
			if (Math.random() < 0.7) {
				pickupEntity = creationManager.createPickup(reusablePosition, 1, new short[]{1});
			} else {
				pickupEntity = creationManager.createPickup(reusablePosition, 2, new short[]{0, 1});
			}
			lifeTimeMapper.create(pickupEntity).timeLeft = PICKUP_LIFETIME;
		}
	}
}
