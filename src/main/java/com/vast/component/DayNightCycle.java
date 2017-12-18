package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class DayNightCycle extends Component {
	public float countdown = 0.0f;
	public boolean isDay = true;
}
