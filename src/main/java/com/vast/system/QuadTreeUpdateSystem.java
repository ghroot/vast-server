package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Static;
import com.vast.component.Transform;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuadTreeUpdateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(QuadTreeUpdateSystem.class);

	private ComponentMapper<Transform> transformMapper;

	private QuadTree quadTree;

	public QuadTreeUpdateSystem(QuadTree quadTree) {
		super(Aspect.all(Transform.class).exclude(Static.class));
		this.quadTree = quadTree;
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
		quadTree.update(entity, transform.position.x, transform.position.y, 0, 0);
	}
}
