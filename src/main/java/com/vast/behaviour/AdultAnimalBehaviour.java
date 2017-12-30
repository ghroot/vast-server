package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.interact.InteractionHandler;

import javax.vecmath.Vector2f;
import java.util.List;

public class AdultAnimalBehaviour extends AbstractBehaviour {
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private final float SCARED_DISTANCE = 2.5f;
	private final float FLEE_DISTANCE = 4.0f;

	private Vector2f reusableVector;

	public AdultAnimalBehaviour(List<InteractionHandler> interactionHandlers) {
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
					if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
						Transform nearbyTransform = transformMapper.get(nearbyEntity);
						reusableVector.set(transform.position.x - nearbyTransform.position.x, transform.position.y - nearbyTransform.position.y);
						if (reusableVector.length() <= SCARED_DISTANCE) {
							reusableVector.normalize();
							reusableVector.scale(FLEE_DISTANCE);
							pathMapper.create(entity).targetPosition.set(
								transform.position.x + reusableVector.x,
								transform.position.y + reusableVector.y
							);
							speedMapper.get(entity).modifier = 2.0f;
							ai.state = "fleeing";
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
		} else if (ai.state.equals("fleeing")) {
			if (!pathMapper.has(entity)) {
				speedMapper.get(entity).modifier = 1.0f;
				ai.state = "none";
			}
		} else if (ai.state.equals("moving")) {
			if (!pathMapper.has(entity)) {
				ai.countdown = 2.0f + (float) Math.random() * 2.0f;
				ai.state = "idling";
			}
		} else if (ai.state.equals("idling")) {
			ai.countdown -= world.getDelta();
			if (ai.countdown <= 0.0f) {
				ai.countdown = 0.0f;
				ai.state = "none";
			}
		}
	}
}
