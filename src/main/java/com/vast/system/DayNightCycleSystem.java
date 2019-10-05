package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Player;
import com.vast.data.Time;
import com.vast.data.WorldConfiguration;

public class DayNightCycleSystem extends IteratingSystem {
	private ComponentMapper<Event> eventMapper;

	private WorldConfiguration worldConfiguration;
	private Time time;

	private boolean changed;

	public DayNightCycleSystem(WorldConfiguration worldConfiguration, Time time) {
		super(Aspect.all(Player.class, Active.class));

		this.worldConfiguration = worldConfiguration;
		this.time = time;
	}

	@Override
	protected void initialize() {
		changed = false;
	}

	@Override
	protected void inserted(int playerEntity) {
		eventMapper.create(playerEntity).name = isDay(time.currentTime) ? "dayInital" : "nightInitial";
		eventMapper.get(playerEntity).ownerOnly = true;
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void begin() {
		boolean wasDay = isDay(time.previousTime);
		boolean isDay = isDay(time.currentTime);
		changed = isDay != wasDay;
	}

	@Override
	protected void process(int playerEntity) {
		if (changed) {
			eventMapper.create(playerEntity).name = isDay(time.currentTime) ? "dayChanged" : "nightChanged";
			eventMapper.get(playerEntity).ownerOnly = true;
		}
	}

	private boolean isDay(float time) {
		float timeIntoDay = time % ((worldConfiguration.dayDuration + worldConfiguration.nightDuration) * 60.0f);
		return timeIntoDay < worldConfiguration.dayDuration * 60.0f;
	}
}
