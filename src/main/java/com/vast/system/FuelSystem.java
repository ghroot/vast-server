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
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Aura> auraMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Parent> parentMapper;
	private ComponentMapper<Sync> syncMapper;

	public FuelSystem() {
		super(Aspect.all(Fueled.class));
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
					int auraEntity = world.create();
					transformMapper.create(auraEntity).position.set(transformMapper.get(fueledEntity).position);
					spatialMapper.create(auraEntity);
					auraMapper.create(auraEntity).effectName = fueled.fueledAuraEffectName;
					scanMapper.create(auraEntity);
					parentMapper.create(auraEntity).parentEntity = fueledEntity;
					fueled.effectEntity = auraEntity;
				}
			}
		}
	}
}
