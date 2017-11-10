package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalIteratingSystem;
import com.vast.Profiler;
import com.vast.component.AIComponent;
import com.vast.component.PathComponent;
import com.vast.component.TransformComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

@Profile(enabled = true, using = Profiler.class)
public class AISystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<PathComponent> pathComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;

	public AISystem() {
		super(Aspect.one(AIComponent.class).exclude(PathComponent.class), 1.0f);
	}

	@Override
	protected void process(int entity) {
		pathComponentMapper.create(entity).targetPosition = new Point2f(transformComponentMapper.get(entity).position);
		pathComponentMapper.create(entity).targetPosition.add(new Point2f((float) (-2.0f + Math.random() * 4.0f), (float) (-2.0f + Math.random() * 4.0f)));
	}
}
