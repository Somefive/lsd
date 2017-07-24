package edu.cmu.mat.lsd.ws;

@SuppressWarnings("ALL")
public class VerboseMessage extends Message {
	public String message;
	public VerboseMessage(String _message) {
		super("verbose");
		message = _message;
	}
}
