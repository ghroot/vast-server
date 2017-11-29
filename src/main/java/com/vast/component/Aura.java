package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.vast.effect.Effect;

@PooledWeaver
public class Aura extends Component {
	public String effectName = null;
	public transient Effect effect = null;
}
