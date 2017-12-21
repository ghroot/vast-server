package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Weather extends Component {
	public transient float countdown = 0.0f;
	public boolean isRaining = false;
}
