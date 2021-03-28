package com.vast.component;

import com.artemis.PooledComponent;

import java.util.Arrays;

/**
 * Skill can affect:
 * - Harvesting speed
 * - Crafting speed
 * - Building speed
 * - Movement speed
 */
public class Skill extends PooledComponent {
	public String[] names = new String[0];
	public int[] xp = new int[0];

	@Override
	protected void reset() {
        names = new String[0];
		xp = new int[0];
	}

	// TODO: Can this be optimized? Different data structure? Adding a timer like before?
	public void addXp(String name, int amount) {
		boolean didIncrease = false;

		for (int i = 0; i < names.length; i++) {
			if (name.equals(names[i])) {
                xp[i] += amount;
                didIncrease = true;
				break;
			}
		}

		if (!didIncrease) {
            names = Arrays.copyOf(names, names.length + 1);
            names[names.length - 1] = name;
			xp = Arrays.copyOf(xp, xp.length + 1);
            xp[xp.length - 1] = amount;
		}
	}

	public void addXp(String name) {
		addXp(name, 1);
	}

	public int getLevel(String name) {
		for (int i = 0; i < names.length; i++) {
			if (name.equals(names[i])) {
				return (int) Math.floor(xp[i] / 100f);
			}
		}

		return 0;
	}
}
