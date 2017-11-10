package com.vast;

import com.nhnent.haste.bootstrap.GameServerBootstrap;
import com.nhnent.haste.bootstrap.options.UDPOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Main {
	private static final int PORT = 5056;

	public static void main(String[] args) {
		String snapshotFormat;
		boolean showMonitor = false;

		try {
			Options options = new Options();
			options.addOption("log", true, "Logging level");
			options.addOption("snapshotFormat", true, "Snapshot format (json or bin)");
			options.addOption("monitor","Show monitor");
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("log")) {
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", cmd.getOptionValue("log"));
			} else {
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
			}
			snapshotFormat = cmd.getOptionValue("snapshotFormat", "json");
			showMonitor = cmd.hasOption("monitor");
		} catch (Exception ignored) {
			snapshotFormat = "json";
		}

		GameServerBootstrap bootstrap = new GameServerBootstrap();
		bootstrap.application(new VastServerApplication(snapshotFormat, showMonitor))
				.option(UDPOption.THREAD_COUNT, 2)
				.option(UDPOption.SO_RCVBUF, 1024 * 60)
				.option(UDPOption.SO_SNDBUF, 1024 * 60)
				.bind(PORT).start();
	}
}