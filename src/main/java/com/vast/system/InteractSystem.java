package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Profiler;
import com.vast.component.Collision;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.component.Transform;
import com.vast.interact.InteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class InteractSystem  extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(InteractSystem.class);

	private final float INTERACTION_SPACING = 0.3f;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Collision> collisionMapper;

	private Set<InteractionHandler> interactionHandlers;

	private Vector2f reusableVector;

	public InteractSystem(Set<InteractionHandler> interactionHandlers) {
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
	public void removed(int entity) {
		// TODO: Does not work since component is null
//		if (interactMapper.has(entity)) {
//			Interact interact = interactMapper.get(entity);
//			if ("interacting".equals(interact.phase)) {
//				InteractionHandler interactionHandler = getInteractionHandler(entity, interact.entity);
//				if (interactionHandler != null) {
//					interactionHandler.stop(entity, interact.entity);
//				}
//			}
//		}
	}

	@Override
	protected void process(int entity) {
		Interact interact = interactMapper.get(entity);

		if (interact.entity == -1) {
			logger.debug("Entity {} canceled interaction because the interactable entity has been deleted", entity);
			interactMapper.remove(entity);
		} else {
			Transform transform = transformMapper.get(entity);
			Transform otherTransform = transformMapper.get(interact.entity);

			float interactDistance = collisionMapper.get(entity).radius + INTERACTION_SPACING + collisionMapper.get(interact.entity).radius;
			reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
			if (reusableVector.length() > interactDistance) {
				if ("approaching".equals(interact.phase)) {
					pathMapper.create(entity).targetPosition = new Point2f(otherTransform.position);
				} else {
					if ("interacting".equals(interact.phase)) {
						InteractionHandler interactionHandler = getInteractionHandler(entity, interact.entity);
						if (interactionHandler != null) {
							interactionHandler.stop(entity, interact.entity);
						}
					}
					logger.debug("Entity {} started approaching entity {}", entity, interact.entity);
					pathMapper.create(entity).targetPosition = new Point2f(otherTransform.position);
					interact.phase = "approaching";
				}
			} else {
				if ("interacting".equals(interact.phase)) {
					InteractionHandler interactionHandler = getInteractionHandler(entity, interact.entity);
					if (interactionHandler != null) {
						if (interactionHandler.process(entity, interact.entity)) {
							logger.debug("Entity {} completed interaction with entity {}", entity, interact.entity);
							interactionHandler.stop(entity, interact.entity);
							interactMapper.remove(entity);
						}
					}
				} else {
					pathMapper.remove(entity);
					InteractionHandler interactionHandler = getInteractionHandler(entity, interact.entity);
					if (interactionHandler != null) {
						interactionHandler.start(entity, interact.entity);
					}
					logger.debug("Entity {} started interacting with entity {}", entity, interact.entity);
					interact.phase = "interacting";
				}
			}
		}
	}

	private InteractionHandler getInteractionHandler(int entity, int otherEntity) {
		for (InteractionHandler interactionHandler : interactionHandlers) {
			if (interactionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
					interactionHandler.getAspect2().isInterested(world.getEntity(otherEntity))) {
				return interactionHandler;
			}
		}
		return null;
	}
}
