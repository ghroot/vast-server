package com.vast.system;

import com.artemis.Aspect;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.systems.IntervalSystem;
import com.artemis.utils.IntBag;
import com.vast.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

public class WorldSerializationSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(WorldSerializationSystem.class);

	private String format;
	private Metrics metrics;

	public WorldSerializationSystem(String format, Metrics metrics) {
		super(Aspect.all(), TimeUnit.MINUTES.toSeconds(5));
		this.format = format;
		this.metrics = metrics;
	}

	@Override
	protected void initialize() {
		loadWorld();
	}

	@Override
	protected void processSystem() {
		saveWorld(format);
	}

	// TODO: Can this be done in a thread?
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
		long startTime = System.currentTimeMillis();
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		if (format.equals("json")) {
			worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
		} else if (format.equals("bin") || format.equals("binary")) {
			worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IntBag entitiesToSerialize = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
		logger.debug("Serializing world ({} entities) to snapshot file {}", entitiesToSerialize.size(), snapshotFileName);
		worldSerializationManager.save(byteArrayOutputStream, new SaveFileFormat(entitiesToSerialize));
		int generateDuration = (int) (System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(snapshotFileName);
			byteArrayOutputStream.writeTo(fileOutputStream);
			fileOutputStream.close();
			byteArrayOutputStream.close();
			int saveDuration = (int) (System.currentTimeMillis() - startTime);
			logger.debug("Serialization completed successfully, size: {} bytes (generate: {} ms, save: {} ms)", byteArrayOutputStream.size(), generateDuration, saveDuration);
		} catch (Exception exception) {
			logger.error("Error saving world", exception);
		}
		metrics.setLastSerializeTime(System.currentTimeMillis());
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
			logger.info("Loading world from snapshot file {}, size: {} bytes", snapshotFileName, fileInputStream.getChannel().size());
			fileInputStream.close();
			logger.debug("Loaded {} entities", saveFileFormat.entities.size());
			metrics.setLastSerializeTime(System.currentTimeMillis());
		} catch (Exception exception) {
			if (exception instanceof FileNotFoundException) {
				logger.info("No snapshot file found, creating a new world");
				CreationManager creationManager = world.getSystem(CreationManager.class);
				creationManager.createWorld();
				metrics.setLastSerializeTime(System.currentTimeMillis());
			} else {
				logger.error("Error loading world", exception);
			}
		}
	}
}
