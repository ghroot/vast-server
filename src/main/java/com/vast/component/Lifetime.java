package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Lifetime extends Component {
	public float timeLeft = Float.MAX_VALUE;
}
