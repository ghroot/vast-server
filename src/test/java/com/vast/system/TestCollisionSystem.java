package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Collision;
import com.vast.component.Scan;
import com.vast.component.Static;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.vecmath.Point2f;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCollisionSystem {
	private final float EPSILON = Math.ulp(1f);
	private final Point2f ZERO = new Point2f(0f, 0f);

	private World world;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Static> staticMapper;

	@Before
	public void setUp() {
		WorldConfiguration worldConfiguration = new WorldConfiguration();
		QuadTree quadTree = mock(QuadTree.class);
		Random random = mock(Random.class);
		when(random.nextFloat()).thenReturn(0f);
		CollisionSystem collisionSystem = new CollisionSystem(worldConfiguration, quadTree, random, null);

		world = new World(new WorldConfigurationBuilder().with(
			collisionSystem
		).build());

		transformMapper = world.getMapper(Transform.class);
		collisionMapper = world.getMapper(Collision.class);
		scanMapper = world.getMapper(Scan.class);
		staticMapper = world.getMapper(Static.class);
	}

	@Test
	public void collidesWithEntity() {
		int entity = world.create();
		int otherEntity = world.create();

		Transform transform = transformMapper.create(entity);
		collisionMapper.create(entity);
		Scan scan = scanMapper.create(entity);
		scan.nearbyEntities.add(otherEntity);

		Transform otherTransform = transformMapper.create(otherEntity);
		collisionMapper.create(otherEntity);
		Scan otherScan = scanMapper.create(otherEntity);
		otherScan.nearbyEntities.add(entity);

		world.process();

		Assert.assertFalse(transform.position.epsilonEquals(ZERO, EPSILON));
		Assert.assertFalse(otherTransform.position.epsilonEquals(ZERO, EPSILON));
		Assert.assertFalse(transform.position.epsilonEquals(otherTransform.position, EPSILON));
	}

	@Test
	public void collidesWithStaticEntity() {
		int entity = world.create();
		int staticEntity = world.create();

		Transform transform = transformMapper.create(entity);
		collisionMapper.create(entity);
		Scan scan = scanMapper.create(entity);
		scan.nearbyEntities.add(staticEntity);

		Transform staticTransform = transformMapper.create(staticEntity);
		collisionMapper.create(staticEntity);
		staticMapper.create(staticEntity);

		world.process();

		Assert.assertFalse(transform.position.epsilonEquals(ZERO, EPSILON));
		Assert.assertTrue(staticTransform.position.epsilonEquals(ZERO, EPSILON));
	}

	@Test
	public void doesNotCollidesWithNonCollisionEntity() {
		int entity = world.create();
		int nonCollisionEntity = world.create();

		Transform transform = transformMapper.create(entity);
		collisionMapper.create(entity);
		Scan scan = scanMapper.create(entity);
		scan.nearbyEntities.add(nonCollisionEntity);

		Transform nonCollisionTransform = transformMapper.create(nonCollisionEntity);
		Scan nonCollisionScan = scanMapper.create(nonCollisionEntity);
		nonCollisionScan.nearbyEntities.add(entity);

		world.process();

		Assert.assertTrue(transform.position.epsilonEquals(ZERO, EPSILON));
		Assert.assertTrue(nonCollisionTransform.position.epsilonEquals(ZERO, EPSILON));
	}

	@Test
	public void doesNotCollideWithOutOfRangeNearbyEntity() {
		int entity = world.create();
		int otherEntity = world.create();

		Transform transform = transformMapper.create(entity);
		Point2f oldPosition = new Point2f(transform.position);
		collisionMapper.create(entity).radius = 1f;
		Scan scan = scanMapper.create(entity);
		scan.nearbyEntities.add(otherEntity);

		Transform otherTransform = transformMapper.create(otherEntity);
		otherTransform.position.set(3f, 0f);
		Point2f otherOldPosition = new Point2f(otherTransform.position);
		collisionMapper.create(otherEntity).radius = 1f;

		world.process();

		Assert.assertTrue(transform.position.epsilonEquals(oldPosition, EPSILON));
		Assert.assertTrue(otherTransform.position.epsilonEquals(otherOldPosition, EPSILON));
	}

	@Test
	public void doesNotCollideWithFarEntity() {
		int entity = world.create();
		int otherEntity = world.create();

		Transform transform = transformMapper.create(entity);
		Point2f oldPosition = new Point2f(transform.position);
		collisionMapper.create(entity);
		scanMapper.create(entity);

		Transform otherTransform = transformMapper.create(otherEntity);
		Point2f otherOldPosition = new Point2f(otherTransform.position);
		collisionMapper.create(otherEntity);

		world.process();

		Assert.assertTrue(transform.position.epsilonEquals(oldPosition, EPSILON));
		Assert.assertTrue(otherTransform.position.epsilonEquals(otherOldPosition, EPSILON));
	}
}
