package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Interact extends Component {
	@EntityId public int entity = -1;
	public String phase = null;
}
