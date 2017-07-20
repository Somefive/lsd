package edu.cmu.mat.lsd.ws;

import com.google.gson.Gson;

import java.util.Date;

@SuppressWarnings("ALL")
public class Message {
	private static Gson gson = new Gson();
	private String type;
	private Long timestamp;
	public Message(String _type) {
		type = _type;
	}
	public String toJson() {
		timestamp = new Date().getTime();
		return gson.toJson(this);
	}
}
