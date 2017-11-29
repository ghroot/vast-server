package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(FuelSystem.class);

	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;

	private ComponentMapper<Sync> syncMapper;

	private CreationManager creationManager;

	public FuelSystem() {
		super(Aspect.all(Fueled.class));
	}

	@Override
	protected void initialize() {
		creationManager = world.getSystem(CreationManager.class);
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
				if (fueled.effectEntity != -1) {
					world.delete(fueled.effectEntity);
					fueled.effectEntity = -1;
				}
				syncMapper.create(fueledEntity).markPropertyAsDirty(Properties.FUELED);
			} else {
				if (fueled.effectEntity == -1) {
					fueled.effectEntity = creationManager.createAura(transformMapper.get(fueledEntity).position, fueled.fueledAuraEffectName, 1.5f, fueledEntity);
				}
			}
		}
	}
}
