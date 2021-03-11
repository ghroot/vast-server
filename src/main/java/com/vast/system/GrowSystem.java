package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Growing;
import com.vast.component.State;
import com.vast.component.Sync;
import com.vast.network.Properties;

public class GrowSystem extends IteratingSystem {
	private ComponentMapper<Growing> growingMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<State> stateMapper;

	public GrowSystem() {
		super(Aspect.all(Growing.class, State.class));
	}

	@Override
	protected void inserted(int growEntity) {
		stateMapper.get(growEntity).name = "growing";
		syncMapper.create(growEntity).markPropertyAsDirty(Properties.STATE);
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int growEntity) {
		Growing growing = growingMapper.get(growEntity);

		if (growing.timeLeft <= 0f) {
			growingMapper.remove(growEntity);
			stateMapper.get(growEntity).name = "none";
			syncMapper.create(growEntity).markPropertyAsDirty(Properties.STATE);
		} else {
			growing.timeLeft -= world.getDelta();
			syncMapper.create(growEntity).markPropertyAsDirty(Properties.PROGRESS);
		}
	}
}
