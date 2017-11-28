package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.vast.component.Disabled;
import com.vast.component.Scan;
import com.vast.interact.InteractionHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviour implements Behaviour {
	protected World world;
	protected ComponentMapper<Scan> scanMapper;
	protected ComponentMapper<Disabled> disabledMapper;

	private List<InteractionHandler> interactionHandlers;

	private List<Integer> reusableNearbyInteractableEntities;

	public AbstractBehaviour(List<InteractionHandler> interactionHandlers) {
		this.interactionHandlers = interactionHandlers;

		reusableNearbyInteractableEntities = new ArrayList<Integer>();
	}

	protected List<Integer> getNearbyInteractableEntities(int entity) {
		Scan scan = scanMapper.get(entity);
		reusableNearbyInteractableEntities.clear();
		for (int nearbyEntity : scan.nearbyEntities) {
			if (nearbyEntity != entity && !disabledMapper.has(nearbyEntity) && hasInteractionHandler(entity, nearbyEntity)) {
				reusableNearbyInteractableEntities.add(nearbyEntity);
			}
		}
		return reusableNearbyInteractableEntities;
	}

	private boolean hasInteractionHandler(int entity, int otherEntity) {
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
