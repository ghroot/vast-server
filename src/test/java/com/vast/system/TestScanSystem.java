package com.vast.system;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.vast.component.Scan;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestScanSystem {
	@Test
	public void addsNearbyEntities() {
		QuadTree quadTree = mock(QuadTree.class);
		when(quadTree.getExact(any(), anyFloat(), anyFloat(), anyFloat(), anyFloat())).then(invocation -> {
			IntBag nearbyEntities = invocation.getArgument(0, IntBag.class);
			nearbyEntities.add(123);
			return nearbyEntities;
		});
		WorldConfiguration worldConfiguration = new WorldConfiguration(30, 30);
		ScanSystem scanSystem = new ScanSystem(quadTree, worldConfiguration);

		World world = new World(new WorldConfigurationBuilder().with(
			scanSystem
		).build());

		int entity = world.create();
		world.getMapper(Transform.class).create(entity);
		Scan scan = world.getMapper(Scan.class).create(entity);

		world.process();

		Assert.assertEquals(1, scan.nearbyEntities.size());
		Assert.assertEquals(123, scan.nearbyEntities.get(0));
	}
}
