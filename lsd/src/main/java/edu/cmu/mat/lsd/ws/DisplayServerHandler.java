package edu.cmu.mat.lsd.ws;

import com.google.gson.Gson;
import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Page;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the server handler that is used to deal with the communication with web version.
 */
@WebSocket(maxIdleTime = -1)
public class DisplayServerHandler {
	private static Gson gson = new Gson();
	public static List<Session> Sessions = new ArrayList<>();
	
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		HCMPLogger.fine("[DisplayServer] Close: statusCode=" + statusCode + ", reason=" + reason);
	}
	
	@OnWebSocketError
	public void onError(Throwable t) {
		HCMPLogger.severe("[DisplayServer] Error: " + t.getMessage());
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		HCMPLogger.fine("[DisplayServer] Connect: " + session.getRemoteAddress().getAddress());
		Sessions.add(session);
		try {
			session.getRemote().sendString(new VerboseMessage("Hello World!").toJson());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnWebSocketMessage
	public void onMessage(String pack) {
		Message message = gson.fromJson(pack, Message.class);
		switch (message.type) {
			case "verbose":
				VerboseMessage verboseMessage = gson.fromJson(pack, VerboseMessage.class);
				HCMPLogger.info("[DisplayServer] Receive client message: " + verboseMessage.message);
				break;
			case "page":
				try {
					PageMessage pageMessage = gson.fromJson(pack, PageMessage.class);
					Page page = Model.Instance.getCurrentScore().getPages().get(pageMessage.pageNumber);
					DisplayServer.broadcast(new PageMessage(page));
					HCMPLogger.info("[DisplayServer] Client request page: " + pageMessage.pageNumber);
				} catch (IndexOutOfBoundsException e) {
					DisplayServer.broadcast(new VerboseMessage("Invalid PageNumber."));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "beats":
				if (Model.Instance.CurrentPlaybackEvents != null) {
					DisplayServer.broadcast(new BeatsMessage(Model.Instance.CurrentPlaybackEvents));
					HCMPLogger.info("[DisplayServer] Client request beats.");
				}
				break;
		}
	}
	
}
