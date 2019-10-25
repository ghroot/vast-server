package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.vast.component.AI;
import com.vast.component.Scan;
import com.vast.interact.InteractionHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviour implements Behaviour {
	protected World world;

	protected ComponentMapper<AI> aiMapper;
	protected ComponentMapper<Scan> scanMapper;

	private List<InteractionHandler> interactionHandlers;

	private List<Integer> reusableNearbyEntities;
	private List<Integer> reusableNearbyInteractableEntities;

	public AbstractBehaviour(List<InteractionHandler> interactionHandlers) {
		this.interactionHandlers = interactionHandlers;

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
}
