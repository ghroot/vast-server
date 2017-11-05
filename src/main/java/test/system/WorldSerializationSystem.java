package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.systems.IntervalSystem;
import com.artemis.utils.IntBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.AIComponent;
import test.component.PeerComponent;
import test.component.SyncTransformComponent;
import test.component.TransformComponent;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

public class WorldSerializationSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(WorldSerializationSystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<AIComponent> aiComponentMapper;

	private Map<String, Integer> entitiesByName;

	public WorldSerializationSystem(Map<String, Integer> entitiesByName) {
		super(Aspect.all(), 1000);
		this.entitiesByName = entitiesByName;
	}

	@Override
	protected void initialize() {
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));

		try {
			FileInputStream fileInputStream = new FileInputStream("snapshot.json");
			SaveFileFormat saveFileFormat = worldSerializationManager.load(fileInputStream, SaveFileFormat.class);
			for (int i = 0; i < saveFileFormat.entities.size(); i++) {
				int entity = saveFileFormat.entities.get(i);
				logger.info("Loaded entity: {}", entity);
				String name = peerComponentMapper.get(entity).name;
				entitiesByName.put(name, entity);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	@Override
	protected void processSystem() {
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IntBag entities = new IntBag();
		for (int entity : entitiesByName.values()) {
			entities.add(entity);
		}
		worldSerializationManager.save(baos, new SaveFileFormat(entities));
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("snapshot.json");
			baos.writeTo(fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
}
