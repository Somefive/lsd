package edu.cmu.mat.lsd.ws;

import edu.cmu.mat.lsd.logger.HCMPLogger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * This is the server that is used to communicate with web version.
 */
public class DisplayServer extends Server {
	
	private static DisplayServer server;
	
	private DisplayServer(int port) {
		super(port);
		setHandler(new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory webSocketServletFactory) {
				webSocketServletFactory.register(DisplayServerHandler.class);
			}
		});
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static DisplayServer createServer(int port) {
		server = new DisplayServer(port);
		return server;
	}
	
	public static void broadcast(Message message) {
		HCMPLogger.info("[DisplayServer] sending message " + message.type);
		DisplayServerHandler.Sessions.removeIf(session -> !session.isOpen());
		DisplayServerHandler.Sessions.forEach(session -> session.getRemote().sendStringByFuture(message.toJson()));
	}
	
}
