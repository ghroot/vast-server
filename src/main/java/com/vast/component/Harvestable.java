package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Harvestable extends Component {
	public int durability = 200;
	public int itemType;
	public int itemCount;
}
