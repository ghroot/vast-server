package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Collision extends Component {
	public boolean isStatic = false;
	public float radius = 0.2f;
}
