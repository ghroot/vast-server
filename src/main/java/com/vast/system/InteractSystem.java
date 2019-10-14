package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.Properties;
import com.vast.interact.InteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector2f;
import java.util.List;

public class InteractSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(InteractSystem.class);

	private final float INTERACTION_SPACING = 0.3f;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Message> messageMapper;
	private ComponentMapper<Sync> syncMapper;

	private List<InteractionHandler> interactionHandlers;

	private Vector2f reusableVector;

	public InteractSystem(List<InteractionHandler> interactionHandlers) {
		super(Aspect.all(Interact.class));
		this.interactionHandlers = interactionHandlers;

		reusableVector = new Vector2f();
	}

	@Override
	protected void initialize() {
		for (InteractionHandler interactionHandler : interactionHandlers) {
			world.inject(interactionHandler);
			interactionHandler.initialize();
		}
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	protected void removed(int entity) {
		Interact interact = interactMapper.get(entity);
		if (interact != null && interact.phase == Interact.Phase.INTERACTING) {
			if (interact.handler != null) {
				interact.handler.stop(entity, interact.entity);
			}
		}
	}

	@Override
	protected void process(int entity) {
		Interact interact = interactMapper.get(entity);

		if (interact.entity == -1) {
			logger.debug("Entity {} canceled interaction because the other entity no longer exists", entity);
			interactMapper.remove(entity);
		} else {
			if (isWithinInteractionDistance(entity, interact.entity)) {
				if (interact.phase == Interact.Phase.INTERACTING) {
					if (interact.handler.canInteract(entity, interact.entity)) {
						Transform transform = transformMapper.get(entity);
						float targetRotation = getAngle(reusableVector);
						transform.rotation = targetRotation;
						syncMapper.create(entity).markPropertyAsDirty(Properties.ROTATION);

						if (interact.handler.process(entity, interact.entity)) {
							logger.debug("Entity {} completed interaction with entity {}", entity, interact.entity);
							interactMapper.remove(entity);
						}
					} else {
						logger.debug("Entity {} can no longer interact with entity {}", entity, interact.entity);
						interactMapper.remove(entity);
					}
				} else {
					pathMapper.remove(entity);

					if (isBeingInteractedWith(interact.entity)) {
						logger.debug("Entity {} can not interact with entity {} because it is already being interacted with", entity, interact.entity);
						interactMapper.remove(entity);
						messageMapper.create(entity).text = "Someone is already using this...";
					} else {
						InteractionHandler handler = findInteractionHandler(entity, interact.entity);
						if (handler != null && handler.attemptStart(entity, interact.entity)) {
							interact.handler = handler;
							logger.debug("Entity {} started interacting with entity {}", entity, interact.entity);
							interact.phase = Interact.Phase.INTERACTING;
						} else {
							logger.debug("Entity {} can not interact with entity {}", entity, interact.entity);
							interactMapper.remove(entity);
						}
					}
				}
			} else {
				Transform otherTransform = transformMapper.get(interact.entity);
				if (interact.phase == Interact.Phase.APPROACHING) {
					pathMapper.create(entity).targetPosition.set(otherTransform.position);
				} else {
					if (interact.phase == Interact.Phase.INTERACTING) {
						interact.handler.stop(entity, interact.entity);
					}
					logger.debug("Entity {} started approaching entity {}", entity, interact.entity);
					pathMapper.create(entity).targetPosition.set(otherTransform.position);
					interact.phase = Interact.Phase.APPROACHING;
				}
			}
		}
	}

	private boolean isWithinInteractionDistance(int entity, int otherEntity) {
		Transform transform = transformMapper.get(entity);
		Collision collision = collisionMapper.get(entity);
		Transform otherTransform = transformMapper.get(otherEntity);
		Collision otherCollision = collisionMapper.get(otherEntity);

		float interactDistance = INTERACTION_SPACING;
		if (collision != null) {
			interactDistance += collision.radius;
		}
		if (otherCollision != null) {
			interactDistance += otherCollision.radius;
		}
		reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
		return reusableVector.length() <= interactDistance;
	}

	private boolean isBeingInteractedWith(int entity) {
		IntBag interactEntities = world.getAspectSubscriptionManager().get(Aspect.all(Interact.class)).getEntities();
		for (int i = 0; i < interactEntities.size(); i++) {
			int interactEntity = interactEntities.get(i);
			Interact interact = interactMapper.get(interactEntity);
			if (interact != null && interact.phase == Interact.Phase.INTERACTING && interact.entity == entity) {
				return true;
			}
		}
		return false;
	}

	private InteractionHandler findInteractionHandler(int entity, int otherEntity) {
		for (InteractionHandler interactionHandler : interactionHandlers) {
			if (interactionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
					interactionHandler.getAspect2().isInterested(world.getEntity(otherEntity)) &&
					interactionHandler.canInteract(entity, otherEntity)) {
				return interactionHandler;
			}
		}
		return null;
	}

	public float getAngle(Vector2f direction) {
		float angle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
		if (angle < 0){
			angle += 360;
		}
		return angle;
	}
}
