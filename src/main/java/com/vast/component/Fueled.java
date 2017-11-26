package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Fueled extends Component {
	public float timeLeft = 0.0f;

	public boolean isFueled() {
		return timeLeft > 0.0f;
	}
}
