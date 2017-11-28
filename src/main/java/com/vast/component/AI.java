package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.vast.behaviour.Behaviour;

@PooledWeaver
public class AI extends Component {
	public String behaviourName = null;
	public transient Behaviour behaviour;
}
