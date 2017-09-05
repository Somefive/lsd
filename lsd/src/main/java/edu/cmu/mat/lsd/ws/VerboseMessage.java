package edu.cmu.mat.lsd.ws;

/**
 * This is a kind of message that just includes some useless message.
 */
@SuppressWarnings("ALL")
public class VerboseMessage extends Message {
	public String message;
	public VerboseMessage(String _message) {
		super("verbose");
		message = _message;
	}
}
