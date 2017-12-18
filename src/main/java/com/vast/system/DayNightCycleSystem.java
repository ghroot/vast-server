package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.DayNightCycle;
import com.vast.component.Event;
import com.vast.component.Player;
import com.vast.data.WorldConfiguration;

public class DayNightCycleSystem extends IteratingSystem {
	private ComponentMapper<DayNightCycle> dayNightCycleMapper;
	private ComponentMapper<Event> eventMapper;

	private WorldConfiguration worldConfiguration;

	private EntitySubscription clockSubscription;
	private boolean changed;

	public DayNightCycleSystem(WorldConfiguration worldConfiguration) {
		super(Aspect.all(Player.class, Active.class));

		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		clockSubscription = world.getAspectSubscriptionManager().get(Aspect.all(DayNightCycle.class));

		DayNightCycle dayNightCycle = dayNightCycleMapper.get(clockSubscription.getEntities().get(0));
		dayNightCycle.isDay = true;
		dayNightCycle.countdown = worldConfiguration.dayDuration * 60.0f;
	}

	@Override
	protected void inserted(int playerEntity) {
		DayNightCycle dayNightCycle = dayNightCycleMapper.get(clockSubscription.getEntities().get(0));

		eventMapper.create(playerEntity).name = dayNightCycle.isDay ? "dayInital" : "nightInitial";
		eventMapper.get(playerEntity).ownerOnly = true;
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void begin() {
		DayNightCycle dayNightCycle = dayNightCycleMapper.get(clockSubscription.getEntities().get(0));

		dayNightCycle.countdown -= world.getDelta();
		if (dayNightCycle.countdown <= 0.0f) {
			dayNightCycle.isDay = !dayNightCycle.isDay;
			dayNightCycle.countdown = dayNightCycle.isDay ? worldConfiguration.dayDuration * 60.0f : worldConfiguration.nightDuration * 60.0f;

			changed = true;
		} else {
			changed = false;
		}
	}

	@Override
	protected void process(int playerEntity) {
		if (changed) {
			DayNightCycle dayNightCycle = dayNightCycleMapper.get(clockSubscription.getEntities().get(0));

			eventMapper.create(playerEntity).name = dayNightCycle.isDay ? "dayChanged" : "nightChanged";
			eventMapper.get(playerEntity).ownerOnly = true;
		}
	}
}
