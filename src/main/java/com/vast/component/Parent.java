package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Parent extends Component {
	@EntityId public int parentEntity = -1;
}
