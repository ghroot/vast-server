package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Delete;
import com.vast.component.Lifetime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifetimeSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(LifetimeSystem.class);

	private ComponentMapper<Lifetime> lifeTimeMapper;
	private ComponentMapper<Delete> deleteMapper;

	public LifetimeSystem() {
		super(Aspect.all(Lifetime.class));
	}

	@Override
	protected void process(int lifeTimeEntity) {
		Lifetime lifetime = lifeTimeMapper.get(lifeTimeEntity);

		lifetime.timeLeft -= world.getDelta();
		if (lifetime.timeLeft <= 0.0f) {
			deleteMapper.create(lifeTimeEntity).reason = "expired";
		}
	}
}
