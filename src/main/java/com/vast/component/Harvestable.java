package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Harvestable extends Component {
	public String requiredItemTag = null;
	public String stateName = null;
	public float durability = 0.0f;
}
