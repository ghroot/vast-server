package test;

import com.nhnent.haste.bootstrap.GameServerBootstrap;
import com.nhnent.haste.bootstrap.options.UDPOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final int PORT = 5056;

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

		GameServerBootstrap bootstrap = new GameServerBootstrap();

		bootstrap.application(new MyServerApplication())
				.option(UDPOption.THREAD_COUNT, 2)
				.option(UDPOption.SO_RCVBUF, 1024 * 60)
				.option(UDPOption.SO_SNDBUF, 1024 * 60)
				.bind(PORT).start();
	}
}
