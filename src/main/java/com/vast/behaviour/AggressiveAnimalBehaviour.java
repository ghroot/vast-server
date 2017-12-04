package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.interact.InteractionHandler;

import javax.vecmath.Vector2f;
import java.util.List;

public class AggressiveAnimalBehaviour extends AbstractBehaviour {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Interact> interactMapper;

	private final float ATTACK_DISTANCE = 1.5f;
	private final float GIVE_UP_DISTANCE = 4.5f;

	private Vector2f reusableVector;

	public AggressiveAnimalBehaviour(List<InteractionHandler> interactionHandlers) {
		super(interactionHandlers);

		reusableVector = new Vector2f();
	}

	@Override
	public void process(int entity) {
		AI ai = aiMapper.get(entity);

		if (ai.state.equals("none")) {
			if (scanMapper.has(entity)) {
				Transform transform = transformMapper.get(entity);
				for (int nearbyEntity : getNearbyEntities(entity)) {
					if (playerMapper.has(nearbyEntity)) {
						Transform nearbyTransform = transformMapper.get(nearbyEntity);
						reusableVector.set(transform.position.x - nearbyTransform.position.x, transform.position.y - nearbyTransform.position.y);
						if (reusableVector.length() <= ATTACK_DISTANCE) {
							interactMapper.create(entity).entity = nearbyEntity;
							speedMapper.get(entity).modifier = 2.0f;
							ai.state = "attacking";
							break;
						}
					}
				}
				if (ai.state.equals("none")) {
					if (Math.random() <= 0.2f) {
						pathMapper.create(entity).targetPosition.set(
							transformMapper.get(entity).position.x - 2.0f + (float) Math.random() * 4.0f,
							transformMapper.get(entity).position.y - 2.0f + (float) Math.random() * 4.0f
						);
						ai.state = "moving";
					} else {
						ai.countdown = 2.0f + (float) Math.random() * 2.0f;
						ai.state = "idling";
					}
				}
				scanMapper.remove(entity);
			} else {
				scanMapper.create(entity);
			}
		} else if (ai.state.equals("attacking")) {
			boolean stop = false;
			if (interactMapper.has(entity)) {
				Transform transform = transformMapper.get(entity);
				Transform interactTransform = transformMapper.get(interactMapper.get(entity).entity);
				reusableVector.set(transform.position.x - interactTransform.position.x, transform.position.y - interactTransform.position.y);
				if (reusableVector.length() >= GIVE_UP_DISTANCE) {
					stop = true;
				}
			} else {
				stop = true;
			}
			if (stop) {
				pathMapper.remove(entity);
				interactMapper.remove(entity);
				speedMapper.get(entity).modifier = 1.0f;
				ai.countdown = 2.0f + (float) Math.random() * 2.0f;
				ai.state = "idling";
			}
		} else if (ai.state.equals("moving")) {
			if (!pathMapper.has(entity)) {
				ai.countdown = 2.0f + (float) Math.random() * 2.0f;
				ai.state = "idling";
			}
		} else if (ai.state.equals(("idling"))) {
			ai.countdown -= world.getDelta();
			if (ai.countdown <= 0.0f) {
				ai.countdown = 0.0f;
				ai.state = "none";
			}
		}
	}
}
