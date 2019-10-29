package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Interact;
import com.vast.component.Transform;
import com.vast.component.Used;
import com.vast.interact.AbstractInteractionHandler;
import com.vast.interact.InteractionHandler;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TestInteractSystem {
	private World world;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Used> usedMapper;

	private void setupWorld(InteractionHandler interactionHandler) {
		world = new World(new WorldConfigurationBuilder().with(
			new InteractSystem(interactionHandler != null ?
				new InteractionHandler[] {interactionHandler} : new InteractionHandler[0])
		).build());

		interactMapper = world.getMapper(Interact.class);
		transformMapper = world.getMapper(Transform.class);
		usedMapper = world.getMapper(Used.class);
	}

	private void setupWorld() {
		setupWorld(null);
	}

	@Test
	public void removesInteractIfOtherEntityIsRemoved() {
		setupWorld();

		int interactEntity = world.create();
		interactMapper.create(interactEntity);

		world.process();

		Assert.assertFalse(interactMapper.has(interactEntity));
	}

	@Test
	public void startsApproachingIfFarAway() {
		setupWorld();

		int interactEntity = world.create();
		int otherEntity = world.create();

		transformMapper.create(interactEntity);
		interactMapper.create(interactEntity).entity = otherEntity;

		transformMapper.create(otherEntity).position.set(100f, 100f);

		world.process();

		Assert.assertEquals(Interact.Phase.APPROACHING, interactMapper.get(interactEntity).phase);
	}

	@Test
	public void startsInteractingIfApproachingAndWithinDistance() {
		setupWorld(new TestInteractionHandler());

		int interactEntity = world.create();
		int otherEntity = world.create();

		transformMapper.create(interactEntity);
		interactMapper.create(interactEntity).entity = otherEntity;
		interactMapper.get(interactEntity).phase = Interact.Phase.APPROACHING;

		transformMapper.create(otherEntity);

		world.process();

		Assert.assertEquals(Interact.Phase.INTERACTING, interactMapper.get(interactEntity).phase);
	}

	@Test
	public void processesInteractionIfInteractingAndWithinDistance() {
		InteractionHandler interactionHandler = spy(new TestInteractionHandler());
		setupWorld(interactionHandler);

		int interactEntity = world.create();
		int otherEntity = world.create();

		transformMapper.create(interactEntity);
		interactMapper.create(interactEntity).entity = otherEntity;
		interactMapper.get(interactEntity).handler = interactionHandler;
		interactMapper.get(interactEntity).phase = Interact.Phase.INTERACTING;

		transformMapper.create(otherEntity);

		world.process();

		verify(interactionHandler).process(interactEntity, otherEntity);
	}

	@Test
	public void removesInteractIfOtherEntityIsBeingInteractedWith() {
		InteractionHandler interactionHandler = new TestInteractionHandler();
		setupWorld(interactionHandler);

		int interactEntity = world.create();
		int otherEntity = world.create();
		int otherInteractEntity = world.create();

		transformMapper.create(interactEntity);
		interactMapper.create(interactEntity).entity = otherEntity;

		transformMapper.create(otherEntity);
		usedMapper.create(otherEntity).usedByEntity = otherInteractEntity;

		world.process();

		Assert.assertFalse(interactMapper.has(interactEntity));
	}

	@Test
	public void stopsInteractingIfInteractIsRemoved() {
		InteractionHandler interactionHandler = mock(InteractionHandler.class);
		setupWorld(interactionHandler);

		int interactEntity = world.create();
		int otherEntity = world.create();

		transformMapper.create(interactEntity);
		interactMapper.create(interactEntity).entity = otherEntity;
		interactMapper.get(interactEntity).handler = interactionHandler;
		interactMapper.get(interactEntity).phase = Interact.Phase.INTERACTING;

		transformMapper.create(otherEntity);

		world.process();

		interactMapper.remove(interactEntity);

		verify(interactionHandler).stop(interactEntity, otherEntity);
	}

	class TestInteractionHandler extends AbstractInteractionHandler {
		public TestInteractionHandler() {
			super(Aspect.all(), Aspect.all());
		}

		@Override
		public boolean canInteract(int entity1, int entity2) {
			return true;
		}

		@Override
		public boolean attemptStart(int entity1, int entity2) {
			return true;
		}

		@Override
		public boolean process(int entity1, int entity2) {
			return true;
		}

		@Override
		public void stop(int entity1, int entity2) {
		}
	}
}
