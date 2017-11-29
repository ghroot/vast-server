package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Health extends Component {
	public int maxHealth;
	public int health;

	public void takeDamage(int damage) {
		health = Math.max(health - damage, 0);
	}

	public void heal(int amount) {
		health = Math.min(health + amount, maxHealth);
	}

	public boolean isFull() {
		return health == maxHealth;
	}

	public boolean isDead() {
		return health == 0;
	}
}
