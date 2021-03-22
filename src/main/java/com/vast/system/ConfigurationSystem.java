package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Configuration;
import com.vast.component.Observer;
import com.vast.component.Sync;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationSystem.class);

	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Configuration> configurationMapper;
	private ComponentMapper<Sync> syncMapper;

	public ConfigurationSystem() {
		super(Aspect.all(Observer.class).exclude(Configuration.class));
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int observerEntity) {
		configurationMapper.create(observerEntity).version = 1;
		syncMapper.create(observerEntity).markPropertyAsDirty(Properties.CONFIGURATION);
	}
}
