package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Craft extends Component {
	public int itemType = -1;
	public float countdown = 0.0f;
}
