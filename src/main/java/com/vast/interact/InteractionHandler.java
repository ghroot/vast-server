package com.vast.interact;

import com.artemis.Aspect;

public interface InteractionHandler {
	void initialize();
	Aspect getAspect1();
	Aspect getAspect2();
	void start(int entity1, int entity2);
	boolean process(int entity1, int entity2);
	void stop(int entity1, int entity2);
}
