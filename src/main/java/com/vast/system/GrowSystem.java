package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Growing;
import com.vast.component.Sync;
import com.vast.network.Properties;

public class GrowSystem extends IteratingSystem {
	private ComponentMapper<Growing> growingMapper;
	private ComponentMapper<Sync> syncMapper;

	public GrowSystem() {
		super(Aspect.all(Growing.class));
	}

	@Override
	protected void process(int growEntity) {
		Growing growing = growingMapper.get(growEntity);

		if (growing.finished) {
			growingMapper.remove(growEntity);
		} else {
			growing.timeLeft -= world.getDelta();
			if (growing.timeLeft <= 0.0f) {
				growing.timeLeft = 0f;
				growing.finished = true;
				syncMapper.create(growEntity).markPropertyAsDirty(Properties.GROWING);
			}
		}
	}
}
