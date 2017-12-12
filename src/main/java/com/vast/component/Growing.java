package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Growing extends Component {
	public float timeLeft = 0.0f;
}
