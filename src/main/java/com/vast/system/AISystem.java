package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.behaviour.Behaviour;
import com.vast.component.AI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AISystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<AI> aiMapper;

	private Map<String, Behaviour> behaviours;

	public AISystem(Map<String, Behaviour> behaviours) {
		super(Aspect.all(AI.class));
		this.behaviours = behaviours;
	}

	@Override
	protected void initialize() {
		for (Behaviour behaviour : behaviours.values()) {
			world.inject(behaviour);
		}
	}

	@Override
	protected void inserted(int aiEntity) {
		AI ai = aiMapper.get(aiEntity);

		if (behaviours.containsKey(ai.behaviourName)) {
			ai.behaviour = behaviours.get(ai.behaviourName);
		} else {
			logger.warn("Unable to find behaviour '{}' for entity {}", ai.behaviourName, aiEntity);
		}
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int aiEntity) {
		AI ai = aiMapper.get(aiEntity);

		if (ai.behaviour != null) {
			ai.behaviour.process(aiEntity);
		}
	}
}
