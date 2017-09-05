package edu.cmu.mat.lsd.logger;

import java.util.Date;
import java.util.logging.*;

/**
 * This is a logger which can be used as `HCMPLogger::fine(someMessage)`. The thread and time will be printed as well.
 * Different level of message can be printed in different color.
 */
@SuppressWarnings("ALL")
public class HCMPLogger {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	private static String getColor(Level level) {
		if (level == Level.SEVERE) return ANSI_RED;
		else if (level == Level.WARNING) return ANSI_YELLOW;
		else if (level == Level.INFO) return ANSI_BLUE;
		else if (level == Level.FINE) return ANSI_GREEN;
		else return ANSI_WHITE;
	}
	
	private static Formatter formatter = new Formatter() {
		@Override
		public String format(LogRecord record) {
			return String.format("%s%s[%tT] %s[Thread %d] %s[%s] %s%s\n",
					ANSI_RESET,
					ANSI_CYAN, new Date(),
					ANSI_PURPLE, record.getThreadID(),
					getColor(record.getLevel()), record.getLevel().getName(),
					ANSI_RESET, record.getMessage());
		}
	};
	
	private static class HCMPHandler extends ConsoleHandler {
		HCMPHandler() {
			super();
			setOutputStream(System.out);
		}
	}
	
	private static Handler handler;
	private static Logger logger;
	
	public static void setup() {
		logger = Logger.getLogger("HCMP");
		logger.setLevel(Level.FINEST); //If you would like to change the level threshold, you can modify here.
		logger.setUseParentHandlers(false);
		handler = new HCMPHandler();
		handler.setFormatter(formatter);
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
	}
	
	public static void fine(String message) {
		logger.fine(message);
	}
	
	public static void info(String message) {
		logger.info(message);
	}
	
	public static void warning(String message) {
		logger.warning(message);
	}
	
	public static void severe(String message) {
		logger.severe(message);
	}
}
