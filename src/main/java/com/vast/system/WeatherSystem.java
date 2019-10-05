package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Player;
import com.vast.data.Weather;

public class WeatherSystem extends IteratingSystem {
	public ComponentMapper<Event> eventMapper;

	private Weather weather;
	private boolean changed;

	public WeatherSystem(Weather weather) {
		super(Aspect.all(Player.class, Active.class));
		this.weather = weather;
	}

	@Override
	protected void initialize() {
		weather.isRaining = false;
		weather.countdown = 60.0f;
	}

	@Override
	protected void inserted(int playerEntity) {
		if (weather.isRaining) {
			eventMapper.create(playerEntity).name = "startedRaining";
			eventMapper.get(playerEntity).ownerOnly = true;
		}
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void begin() {
		weather.countdown -= world.getDelta();
		if (weather.countdown <= 0.0f) {
			boolean wasRaining = weather.isRaining;
			weather.isRaining = Math.random() < 0.25f;
			weather.countdown = 60.0f;

			changed = weather.isRaining != wasRaining;
		} else {
			changed = false;
		}
	}

	@Override
	protected void process(int playerEntity) {
		if (changed) {
			if (weather.isRaining) {
				eventMapper.create(playerEntity).name = "startedRaining";
			} else {
				eventMapper.create(playerEntity).name = "stoppedRaining";
			}
			eventMapper.get(playerEntity).ownerOnly = true;
		}
	}
}
