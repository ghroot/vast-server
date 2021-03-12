package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Fueled;
import com.vast.component.Scan;
import com.vast.component.Sync;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(FuelSystem.class);

	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Sync> syncMapper;

	public FuelSystem() {
		super(Aspect.all(Fueled.class));
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
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
