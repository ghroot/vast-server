package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Harvestable extends Component {
	public int requiredItemId = -1;
	public String harvestEventName = null;
	public float durability = 0.0f;
}
