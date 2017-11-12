package com.vast.collision;

import com.artemis.Aspect;
import com.artemis.World;

public interface CollisionHandler {
	void initialize();
	Aspect getAspect1();
	Aspect getAspect2();
	void handleCollision(int entity1, int entity2);
}
