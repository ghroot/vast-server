package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.data.Properties;
import com.vast.component.Parent;
import com.vast.component.Sync;
import com.vast.component.Transform;

public class ParentSystem extends IteratingSystem {
	private ComponentMapper<Parent> parentMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Sync> syncMapper;

	public ParentSystem() {
		super(Aspect.all(Parent.class));
	}

	@Override
	protected void process(int entity) {
		Parent parent = parentMapper.get(entity);

		if (parent.parentEntity == -1) {
			world.delete(entity);
		} else {
			Transform transform = transformMapper.get(entity);
			Transform parentTransform = transformMapper.get(parent.parentEntity);
			if (!transform.position.equals(parentTransform.position)) {
				transform.position.set(parentTransform.position);
				syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
			}
		}
	}
}
