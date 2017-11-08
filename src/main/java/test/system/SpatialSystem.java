package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Profiler;
import test.component.SpatialComponent;
import test.component.TransformComponent;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SpatialSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SpatialSystem.class);

	private final float SECTION_SIZE = 2.0f;

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SpatialComponent> spatialComponentMapper;

	private Map<Point2i, Set<Integer>> spatialHashes;

	public SpatialSystem(Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(TransformComponent.class, SpatialComponent.class));
		this.spatialHashes = spatialHashes;
	}

	@Override
	protected void inserted(int entity) {
		SpatialComponent spatialComponent = spatialComponentMapper.get(entity);

		spatialComponent.memberOfSpatialHash = null;
		spatialComponent.lastUsedPosition = null;
	}

	@Override
	protected void process(int entity) {
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		SpatialComponent spatialComponent = spatialComponentMapper.get(entity);

		if (spatialComponent.lastUsedPosition == null || !spatialComponent.lastUsedPosition.equals(transformComponent.position)) {
			if (spatialComponent.memberOfSpatialHash != null) {
				spatialHashes.get(spatialComponent.memberOfSpatialHash).remove(entity);
				spatialComponent.memberOfSpatialHash = null;
			}

			Point2i hash = new Point2i(
					(int) (Math.round(transformComponent.position.x / SECTION_SIZE) * SECTION_SIZE),
					(int) (Math.round(transformComponent.position.y / SECTION_SIZE) * SECTION_SIZE)
			);

			spatialComponent.memberOfSpatialHash = hash;
			spatialComponent.lastUsedPosition = new Point2f(transformComponent.position);

			Set<Integer> entitiesInHash;
			if (spatialHashes.containsKey(hash)) {
				entitiesInHash = spatialHashes.get(hash);
			} else {
				entitiesInHash = new HashSet<Integer>();
				spatialHashes.put(hash, entitiesInHash);
			}
			entitiesInHash.add(entity);
		}
	}
}
