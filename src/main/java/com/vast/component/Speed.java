package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Speed extends Component {
	public float baseSpeed = 0.0f;
	public float modifier = 1.0f;

	public float getModifiedSpeed() {
		return baseSpeed * modifier;
	}
}
