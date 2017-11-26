package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.Event;
import com.vast.component.Fueled;
import com.vast.component.Player;
import com.vast.component.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FueledInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(FueledInteractionHandler.class);

	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	public FueledInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Fueled.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int fueledEntity) {
		return !fueledMapper.get(fueledEntity).isFueled();
	}

	@Override
	public void start(int playerEntity, int fueledEntity) {
		eventMapper.create(playerEntity).name = "startedFueling";
	}

	@Override
	public boolean process(int playerEntity, int fueledEntity) {
		fueledMapper.get(fueledEntity).timeLeft = 60.0f;
		syncMapper.create(fueledEntity).markPropertyAsDirty(Properties.FUELED);
		return true;
	}

	@Override
	public void stop(int playerEntity, int fueledEntity) {
		eventMapper.create(playerEntity).name = "stoppedFueling";
	}
}
