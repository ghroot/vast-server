package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Harvestable extends Component {
	public float durability = 200.0f;
}
