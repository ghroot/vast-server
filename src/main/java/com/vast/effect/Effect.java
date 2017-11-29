package com.vast.effect;

import java.util.Set;

public interface Effect {
	void process(int effectEntity, Set<Integer> nearbyEntities);
}
