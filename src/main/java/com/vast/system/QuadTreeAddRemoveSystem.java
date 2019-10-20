package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuadTreeAddRemoveSystem extends BaseEntitySystem {
	private static final Logger logger = LoggerFactory.getLogger(QuadTreeAddRemoveSystem.class);

	private ComponentMapper<Transform> transformMapper;

	private QuadTree quadTree;
	private WorldConfiguration worldConfiguration;

	public QuadTreeAddRemoveSystem(QuadTree quadTree, WorldConfiguration worldConfiguration) {
		super(Aspect.all(Transform.class));
		this.quadTree = quadTree;
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void inserted(int entity) {
		Transform transform = transformMapper.get(entity);
		quadTree.insert(entity, transform.position.x + worldConfiguration.width / 2f,
			transform.position.y + worldConfiguration.height / 2f, 0, 0);
	}

	@Override
	protected void removed(int entity) {
		quadTree.remove(entity);
	}

	@Override
	protected void processSystem() {
	}
}
