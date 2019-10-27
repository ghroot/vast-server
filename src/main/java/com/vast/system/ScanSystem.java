package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Scan;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ScanSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;

	private QuadTree quadTree;
	private WorldConfiguration worldConfiguration;

	public ScanSystem(QuadTree quadTree, WorldConfiguration worldConfiguration) {
		super(Aspect.all(Transform.class, Scan.class));
		this.quadTree = quadTree;
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int scanEntity) {
		Transform transform = transformMapper.get(scanEntity);
		Scan scan = scanMapper.get(scanEntity);

		scan.nearbyEntities.clear();
		quadTree.getExact(scan.nearbyEntities, transform.position.x - scan.distance,
			transform.position.y - scan.distance, 2 * scan.distance, 2 * scan.distance);
	}
}
