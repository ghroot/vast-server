package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Collision;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.component.Transform;
import com.vast.interact.InteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.List;

public class InteractSystem  extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(InteractSystem.class);

	private final float INTERACTION_SPACING = 0.3f;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Collision> collisionMapper;

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
		if (interact.phase == Interact.Phase.INTERACTING) {
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
			if (interact.handler != null && !interact.handler.canInteract(entity, interact.entity)) {
				logger.debug("Entity {} can no longer interact with entity {}", entity, interact.entity);
				interact.handler = null;
			}
			if (interact.handler == null) {
				InteractionHandler handler = findInteractionHandler(entity, interact.entity);
				if (handler != null) {
					interact.handler = handler;
					logger.debug("Entity {} assigned interaction handler {}", entity, interact.handler.getClass());
				} else {
					logger.debug("Entity {} can not interact with entity {}", entity, interact.entity);
				}
			}
			if (interact.handler != null) {
				processInteraction(entity);
			} else {
				interactMapper.remove(entity);
			}
		}
	}

	private void processInteraction(int entity) {
		Interact interact = interactMapper.get(entity);
		Transform transform = transformMapper.get(entity);
		Transform otherTransform = transformMapper.get(interact.entity);

		float interactDistance = collisionMapper.get(entity).radius + INTERACTION_SPACING + collisionMapper.get(interact.entity).radius;
		reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
		if (reusableVector.length() > interactDistance) {
			if (interact.phase == Interact.Phase.APPROACHING) {
				pathMapper.create(entity).targetPosition = new Point2f(otherTransform.position);
			} else {
				if (interact.phase == Interact.Phase.INTERACTING) {
					interact.handler.stop(entity, interact.entity);
				}
				logger.debug("Entity {} started approaching entity {}", entity, interact.entity);
				pathMapper.create(entity).targetPosition = new Point2f(otherTransform.position);
				interact.phase = Interact.Phase.APPROACHING;
			}
		} else {
			if (interact.phase == Interact.Phase.INTERACTING) {
				if (interact.handler.process(entity, interact.entity)) {
					logger.debug("Entity {} completed interaction with entity {}", entity, interact.entity);
					interactMapper.remove(entity);
				}
			} else {
				pathMapper.remove(entity);

				interact.handler.start(entity, interact.entity);
				logger.debug("Entity {} started interacting with entity {}", entity, interact.entity);
				interact.phase = Interact.Phase.INTERACTING;
			}
		}
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
}
