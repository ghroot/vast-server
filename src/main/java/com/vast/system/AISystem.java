package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

public class AISystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Interactable> interactableMapper;

	private List<Integer> reusableNearbyInteractableEntities;

	public AISystem() {
		super(Aspect.all(AI.class).exclude(Path.class, Interact.class), 2.0f);

		reusableNearbyInteractableEntities = new ArrayList<Integer>();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int aiEntity) {
		if (scanMapper.has(aiEntity)) {
			Scan scan = scanMapper.get(aiEntity);
			reusableNearbyInteractableEntities.clear();
			for (int nearbyEntity : scan.nearbyEntities) {
				if (nearbyEntity != aiEntity && interactableMapper.has(nearbyEntity)) {
					reusableNearbyInteractableEntities.add(nearbyEntity);
				}
			}
			if (reusableNearbyInteractableEntities.size() > 0) {
				int randomIndex = (int) (Math.random() * reusableNearbyInteractableEntities.size());
				int randomNearbyInteractableEntity = reusableNearbyInteractableEntities.get(randomIndex);
				interactMapper.create(aiEntity).entity = randomNearbyInteractableEntity;
			} else {
				pathMapper.create(aiEntity).targetPosition = new Point2f(transformMapper.get(aiEntity).position);
				pathMapper.create(aiEntity).targetPosition.add(new Point2f((float) (-2.0f + Math.random() * 4.0f), (float) (-2.0f + Math.random() * 4.0f)));
			}
			scanMapper.remove(aiEntity);
		} else {
			scanMapper.create(aiEntity);
		}
	}
}
