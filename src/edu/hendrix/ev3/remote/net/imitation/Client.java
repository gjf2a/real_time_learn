package edu.hendrix.ev3.remote.net.imitation;

import edu.hendrix.ev3.gui.DelayedQuittingGUI;

public class Client extends DelayedQuittingGUI {
	public static void main(String[] args) {
		setup(250, "ClientGUI.fxml");
		launch(args);
	}
}