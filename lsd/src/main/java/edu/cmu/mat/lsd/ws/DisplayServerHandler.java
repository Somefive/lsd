package edu.cmu.mat.lsd.ws;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebSocket(maxIdleTime = -1)
public class DisplayServerHandler {
	
	public static List<Session> Sessions = new ArrayList<>();
	
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
	}
	
	@OnWebSocketError
	public void onError(Throwable t) {
		System.out.println("Error: " + t.getMessage());
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.println("Connect: " + session.getRemoteAddress().getAddress());
		Sessions.add(session);
		try {
			session.getRemote().sendString(new VerboseMessage("Hello World!").toJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnWebSocketMessage
	public void onMessage(String message) {
		System.out.println("Message: " + message);
	}
	
}
