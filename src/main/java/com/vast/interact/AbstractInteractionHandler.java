package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.World;

public abstract class AbstractInteractionHandler implements InteractionHandler {
	protected World world;

	private Aspect.Builder aspectBuilder1;
	private Aspect.Builder aspectBuilder2;

	private Aspect aspect1;
	private Aspect aspect2;

	public AbstractInteractionHandler(Aspect.Builder aspectBuilder1, Aspect.Builder aspectBuilder2) {
		this.aspectBuilder1 = aspectBuilder1;
		this.aspectBuilder2 = aspectBuilder2;
	}

	public void initialize() {
		aspect1 = aspectBuilder1.build(world);
		aspect2 = aspectBuilder2.build(world);
	}

	@Override
	public Aspect getAspect1() {
		return aspect1;
	}

	@Override
	public Aspect getAspect2() {
		return aspect2;
	}

	@Override
	public abstract boolean process(int entity1, int entity2);
}
