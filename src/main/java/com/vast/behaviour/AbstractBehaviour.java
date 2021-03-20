package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.vast.component.AI;
import com.vast.component.Scan;
import com.vast.data.WorldConfiguration;
import com.vast.interact.InteractionHandler;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractBehaviour implements Behaviour {
	protected World world;

	protected ComponentMapper<AI> aiMapper;
	protected ComponentMapper<Scan> scanMapper;

	private InteractionHandler[] interactionHandlers;
	private WorldConfiguration worldConfiguration;
	protected Random random;

	private List<Integer> reusableNearbyEntities;
	private List<Integer> reusableNearbyInteractableEntities;

	public AbstractBehaviour(InteractionHandler[] interactionHandlers, WorldConfiguration worldConfiguration, Random random) {
		this.interactionHandlers = interactionHandlers;
		this.worldConfiguration = worldConfiguration;
		this.random = random;

		reusableNearbyEntities = new ArrayList<>();
		reusableNearbyInteractableEntities = new ArrayList<>();
	}

	protected List<Integer> getNearbyEntities(int entity) {
		Scan scan = scanMapper.get(entity);
		reusableNearbyEntities.clear();
		int[] nearbyEntities = scan.nearbyEntities.getData();
		for (int i = 0, size = scan.nearbyEntities.size(); i < size; ++i) {
			int nearbyEntity = nearbyEntities[i];
			if (nearbyEntity != entity) {
				reusableNearbyEntities.add(nearbyEntity);
			}
		}
		return reusableNearbyEntities;
	}

	protected boolean canInteract(int entity, int otherEntity) {
		for (InteractionHandler interactionHandler : interactionHandlers) {
			if (interactionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
					interactionHandler.getAspect2().isInterested(world.getEntity(otherEntity)) &&
					interactionHandler.canInteract(entity, otherEntity)) {
				return true;
			}
		}
		return false;
	}

	protected Point2f getRandomMovePosition(Point2f position, float distance) {
		for (int i = 0; i < 10; i++) {
			int randomAngle = random.nextInt(360);
			float dx = (float) Math.cos(Math.toRadians(randomAngle)) * distance;
			float dy = (float) Math.sin(Math.toRadians(randomAngle)) * distance;
			if (isPositionInWorld(position.x + dx, position.y + dy)) {
				return new Point2f(position.x + dx, position.y + dy);
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
