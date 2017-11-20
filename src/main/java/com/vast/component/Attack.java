package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Attack extends Component {
	public float cooldown = 1.5f;
	public transient float lastAttackTime = 0.0f;
}
