package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalIteratingSystem;
import com.vast.Profiler;
import com.vast.component.AI;
import com.vast.component.Path;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

@Profile(enabled = true, using = Profiler.class)
public class AISystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;

	public AISystem() {
		super(Aspect.one(AI.class).exclude(Path.class), 1.0f);
	}

	@Override
	protected void process(int entity) {
		pathMapper.create(entity).targetPosition = new Point2f(transformMapper.get(entity).position);
		pathMapper.create(entity).targetPosition.add(new Point2f((float) (-2.0f + Math.random() * 4.0f), (float) (-2.0f + Math.random() * 4.0f)));
	}
}
