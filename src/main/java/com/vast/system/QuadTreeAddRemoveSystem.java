package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.vast.component.Layer;
import com.vast.component.Quad;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QuadTreeAddRemoveSystem extends BaseEntitySystem {
	private static final Logger logger = LoggerFactory.getLogger(QuadTreeAddRemoveSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Quad> quadMapper;
	private ComponentMapper<Layer> layerMapper;

	private Map<String, QuadTree> quadTrees;
	private WorldConfiguration worldConfiguration;

	public QuadTreeAddRemoveSystem(Map<String, QuadTree> quadTrees, WorldConfiguration worldConfiguration) {
		super(Aspect.all(Transform.class));
		this.quadTrees = quadTrees;
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void inserted(int entity) {
		Transform transform = transformMapper.get(entity);
		Layer layer = layerMapper.get(entity);

		Quad quad = quadMapper.create(entity);
		quad.tree = quadTrees.get(layer != null ? layer.name : "default");
		quad.tree.insert(entity,
				transform.position.x + worldConfiguration.width / 2f,
				transform.position.y + worldConfiguration.height / 2f, 0, 0);
	}

	@Override
	protected void removed(int entity) {
		quadMapper.get(entity).tree.remove(entity);
		quadMapper.remove(entity);
	}

	@Override
	protected void processSystem() {
	}
}
