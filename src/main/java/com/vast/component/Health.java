package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Health extends Component {
	public int maxHealth;
	public int health;
}
