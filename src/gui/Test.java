package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Test extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		ScrollPane sp = new ScrollPane();
		GridPane gp = new GridPane();
		sp.setContent(gp);
		for(int i=0; i<=50; i++) {
			gp.add(new Button(), 0, i);
		}
		primaryStage.setScene(new Scene(sp));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
