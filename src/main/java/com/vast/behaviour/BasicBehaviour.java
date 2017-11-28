package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.component.Transform;

import javax.vecmath.Point2f;
import java.util.List;

public class BasicBehaviour extends AbstractBehaviour {
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;

	public BasicBehaviour() {
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
				pathMapper.create(entity).targetPosition = new Point2f(transformMapper.get(entity).position);
				pathMapper.create(entity).targetPosition.add(new Point2f((float) (-2.0f + Math.random() * 4.0f), (float) (-2.0f + Math.random() * 4.0f)));
			}
			scanMapper.remove(entity);
		} else {
			scanMapper.create(entity);
		}
	}
}
