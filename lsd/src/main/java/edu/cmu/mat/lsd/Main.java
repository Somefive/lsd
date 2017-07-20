package edu.cmu.mat.lsd;

import edu.cmu.mat.lsd.frames.Window;
import edu.cmu.mat.lsd.ws.DisplayServer;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Score;

public class Main {
	public static void main(String[] args) throws Exception {
		Controller controller = new Controller();
		Model model = new Model(controller);
		DisplayServer.createServer(9050);
		new Window(model, controller);
	}
}
