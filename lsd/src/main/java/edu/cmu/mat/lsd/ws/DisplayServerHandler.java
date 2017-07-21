package edu.cmu.mat.lsd.ws;

import com.google.gson.Gson;
import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.scores.Page;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebSocket(maxIdleTime = -1)
public class DisplayServerHandler {
	private static Gson gson = new Gson();
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
	public void onMessage(String pack) {
		Message message = gson.fromJson(pack, Message.class);
		if (message.type.equals("verbose")) {
			VerboseMessage verboseMessage = gson.fromJson(pack, VerboseMessage.class);
			System.out.println("Receive client message: " + verboseMessage.message);
		} else if (message.type.equals("page")) {
			try {
				PageMessage pageMessage = gson.fromJson(pack, PageMessage.class);
				Page page = Model.Instance.getCurrentScore().getPages().get(pageMessage.pageNumber);
				DisplayServer.broadcast(new PageMessage(page));
				System.out.println("Client request page: " + pageMessage.pageNumber);
			} catch (IndexOutOfBoundsException e) {
				DisplayServer.broadcast(new VerboseMessage("Invalid PageNumber."));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
