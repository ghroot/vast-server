package com.vast;

import com.nhnent.haste.bootstrap.GameServerBootstrap;
import com.nhnent.haste.bootstrap.options.UDPOption;
import com.nhnent.haste.transport.MetricListener;
import com.vast.data.Metrics;
import com.vast.network.VastServerApplication;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.concurrent.TimeUnit;

public class Main {
	private static final int PORT = 5056;

	public static void main(String[] args) {
		String snapshotFile = null;
		int numberOfPeersToSimulate = 0;
		long randomSeed = -1;
		boolean showMonitor = false;

		try {
			Options options = new Options();
			options.addOption("log", true, "Logging level");
			options.addOption("snapshot", true, "Snapshot file");
			options.addOption("simulate", true, "Number of peers to simulate");
			options.addOption("seed", true, "Random seed");
			options.addOption("monitor", "Show monitor");
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
			System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
			System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
			System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
			System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss");
			if (cmd.hasOption("log")) {
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", cmd.getOptionValue("log"));
			} else {
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
			}

			snapshotFile = cmd.getOptionValue("snapshot", "snapshot.json");
			numberOfPeersToSimulate = cmd.hasOption("simulate") ? Integer.parseInt(cmd.getOptionValue("simulate", "0")) : 0;
			randomSeed = cmd.hasOption("seed") ? Long.parseLong(cmd.getOptionValue("seed", "-1")) : -1;
			showMonitor = cmd.hasOption("monitor");
		} catch (Exception ignored) {
		}

		final Metrics metrics = showMonitor ? new Metrics() : null;

		GameServerBootstrap bootstrap = new GameServerBootstrap();
		VastServerApplication serverApplication = new VastServerApplication(snapshotFile, numberOfPeersToSimulate, randomSeed, showMonitor, metrics);
		bootstrap.application(serverApplication).option(UDPOption.THREAD_COUNT, 4).bind(PORT);
		if (showMonitor) {
			bootstrap.metricListener(new MetricListener() {
				@Override
				public long periodMilliseconds() {
					return TimeUnit.SECONDS.toMillis(20);
				}

				@Override
				public void onReceive(int peerCount, double meanOfRoundTripTime, double meanOfRoundTripTimeDeviation) {
					metrics.setRoundTripTime(meanOfRoundTripTime);
				}
			});
		}
		bootstrap.start();
	}
}
