package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.Properties;
import com.vast.component.Fueled;
import com.vast.component.Sync;

public class FuelSystem extends IteratingSystem {
	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Sync> syncMapper;

	public FuelSystem() {
		super(Aspect.all(Fueled.class));
	}

	@Override
	protected void process(int fueledEntity) {
		Fueled fueled = fueledMapper.get(fueledEntity);

		if (fueled.timeLeft > 0.0f) {
			fueled.timeLeft -= world.getDelta();
			if (fueled.timeLeft <= 0.0f) {
				syncMapper.create(fueledEntity).markPropertyAsDirty(Properties.FUELED);
			}
		}
	}
}
