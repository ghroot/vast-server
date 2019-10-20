package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Static;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuadTreeUpdateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(QuadTreeUpdateSystem.class);

	private ComponentMapper<Transform> transformMapper;

	private QuadTree quadTree;
	private WorldConfiguration worldConfiguration;

	public QuadTreeUpdateSystem(QuadTree quadTree, WorldConfiguration worldConfiguration) {
		super(Aspect.all(Transform.class).exclude(Static.class));
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
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		quadTree.update(entity, transform.position.x + worldConfiguration.width / 2f,
			transform.position.y + worldConfiguration.height / 2f, 0, 0);
	}
}
