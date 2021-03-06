package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Event;
import com.vast.component.Player;
import com.vast.component.Weather;

import java.util.Random;

public class WeatherSystem extends IteratingSystem {
	private ComponentMapper<Weather> weatherMapper;
	private ComponentMapper<Event> eventMapper;

	@All(Weather.class)
	private EntitySubscription weatherSubscription;

	private Random random;

	private boolean changed;

	public WeatherSystem(Random random) {
		super(Aspect.all(Player.class, Active.class));
		this.random = random;
	}

	@Override
	protected void inserted(int playerEntity) {
		Weather weather = weatherMapper.get(weatherSubscription.getEntities().get(0));

		if (weather.isRaining) {
			eventMapper.create(playerEntity).addEntry("startedRaining").setOwnerPropagation();
		}
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void begin() {
		Weather weather = weatherMapper.get(weatherSubscription.getEntities().get(0));

		weather.countdown -= world.getDelta();
		if (weather.countdown <= 0.0f) {
			boolean wasRaining = weather.isRaining;
			weather.isRaining = random.nextFloat() < 0.25f;
			weather.countdown = 60.0f;

			changed = weather.isRaining != wasRaining;
		} else {
			changed = false;
		}
	}

	@Override
	protected void process(int playerEntity) {
		if (changed) {
			Weather weather = weatherMapper.get(weatherSubscription.getEntities().get(0));

			if (weather.isRaining) {
				eventMapper.create(playerEntity).addEntry("startedRaining").setOwnerPropagation();
			} else {
				eventMapper.create(playerEntity).addEntry("stoppedRaining").setOwnerPropagation();
			}
		}
	}
}
