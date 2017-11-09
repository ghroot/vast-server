package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.systems.IntervalSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Profiler;
import test.component.PeerComponent;
import test.component.TypeComponent;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Profile(enabled = true, using = Profiler.class)
public class WorldSerializationSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(WorldSerializationSystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;

	private String format;

	public WorldSerializationSystem(String format) {
		super(Aspect.all(), 10.0f);
		this.format = format;
	}

	@Override
	protected void initialize() {
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);

		loadWorld();
	}

	@Override
	protected void processSystem() {
		saveWorld(format);
	}

	private void saveWorld(final String format) {
		final String snapshotFileName;
		switch (format) {
			case "json":
				snapshotFileName = "snapshot.json";
				break;
			case "bin":
			case "binary":
				snapshotFileName = "snapshot.bin";
				break;
			default:
				snapshotFileName = "snapshot";
				break;
		}
		logger.debug("Serializing world to snapshot file {}", snapshotFileName);
		long startTime = System.currentTimeMillis();
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		if (format.equals("json")) {
			worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
		} else if (format.equals("bin") || format.equals("binary")) {
			worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		worldSerializationManager.save(baos, new SaveFileFormat(world.getAspectSubscriptionManager().get(Aspect.all()).getEntities()));
		int generateDuration = (int) (System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(snapshotFileName);
			baos.writeTo(fileOutputStream);
			fileOutputStream.close();
			baos.close();
			int saveDuration = (int) (System.currentTimeMillis() - startTime);
			logger.debug("Serialization completed successfully (generate: {} ms, save: {} ms)", generateDuration, saveDuration);
		} catch (Exception exception) {
			logger.error("Error saving world", exception);
		}
	}

	private void loadWorld() {
		try {
			WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
			String snapshotFileName;
			FileInputStream fileInputStream;
			try {
				snapshotFileName = "snapshot.json";
				fileInputStream = new FileInputStream(snapshotFileName);
				worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
			} catch (FileNotFoundException exception) {
				snapshotFileName = "snapshot.bin";
				fileInputStream = new FileInputStream(snapshotFileName);
				worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
			}
			SaveFileFormat saveFileFormat = worldSerializationManager.load(fileInputStream, SaveFileFormat.class);
			fileInputStream.close();
			logger.info("Loading world from snapshot file {}", snapshotFileName);
			for (int i = 0; i < saveFileFormat.entities.size(); i++) {
				int entity = saveFileFormat.entities.get(i);
				if (peerComponentMapper.has(entity)) {
					logger.info("Loaded entity: {} (peer)", entity);
				} else {
					logger.info("Loaded entity: {} ({})", entity, typeComponentMapper.get(entity).type);
				}
			}
		} catch (Exception exception) {
			if (exception instanceof FileNotFoundException) {
				logger.info("No snapshot file found, creating a new world");
				CreationManager creationManager = world.getSystem(CreationManager.class);
				creationManager.createWorld();
			} else {
				logger.error("Error loading world", exception);
			}
		}
	}
}
