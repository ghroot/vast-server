package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.component.Transform;
import com.vast.interact.InteractionHandler;

import java.util.List;

public class BasicBehaviour extends AbstractBehaviour {
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;

	public BasicBehaviour(List<InteractionHandler> interactionHandlers) {
		super(interactionHandlers);
	}

	@Override
	public void process(int entity) {
		if (interactMapper.has(entity) || pathMapper.has(entity)) {
			return;
		}

		if (scanMapper.has(entity)) {
			List<Integer> nearbyInteractableEntities = getNearbyInteractableEntities(entity);
			if (nearbyInteractableEntities.size() > 0) {
				int randomIndex = (int) (Math.random() * nearbyInteractableEntities.size());
				int randomNearbyInteractableEntity = nearbyInteractableEntities.get(randomIndex);
				interactMapper.create(entity).entity = randomNearbyInteractableEntity;
			} else {
				pathMapper.create(entity).targetPosition.set(
					transformMapper.get(entity).position.x - 2.0f + (float) Math.random() * 4.0f,
					transformMapper.get(entity).position.y - 2.0f + (float) Math.random() * 4.0f
				);
			}
			scanMapper.remove(entity);
		} else {
			scanMapper.create(entity);
		}
	}
}
