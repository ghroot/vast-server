package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Interact extends Component {
	@EntityId
	public int entity;
}
