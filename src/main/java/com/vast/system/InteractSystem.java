package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Profiler;
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

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;

	private Set<InteractionHandler> interactionHandlers;

	private Vector2f reusableVector;

	public InteractSystem(Set<InteractionHandler> interactionHandlers) {
		super(Aspect.one(Interact.class));
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
	protected void inserted(int entity) {
		logger.debug("Started interaction with entity {}", interactMapper.get(entity).entity);
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int entity) {
		Interact interact = interactMapper.get(entity);
		Transform transform = transformMapper.get(entity);

		Transform otherTransform = transformMapper.get(interact.entity);

		reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
		if (reusableVector.length() > 0.5f) {
			if (interact.phase != Interact.Phase.APPROACHING) {
				if (!pathMapper.has(entity)) {
					pathMapper.create(entity).targetPosition = new Point2f(otherTransform.position);
				}
				interact.phase = Interact.Phase.APPROACHING;
			}
		} else {
			if (interact.phase == Interact.Phase.INTERACTING) {
				for (InteractionHandler interactionHandler : interactionHandlers) {
					if (interactionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
							interactionHandler.getAspect2().isInterested(world.getEntity(interact.entity))) {
						if (interactionHandler.process(entity, interact.entity)) {
							logger.debug("Completed interaction with entity {}", interact.entity);
							interactMapper.remove(entity);
						}
						break;
					}
				}
			} else {
				if (pathMapper.has(entity)) {
					pathMapper.remove(entity);
				}
				interact.phase = Interact.Phase.INTERACTING;
			}
		}
	}
}
