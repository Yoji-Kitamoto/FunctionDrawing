package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	static final double CANVAS_WIDTH  = 400;
	static final double CANVAS_HEIGHT = 400;
	static final double CANVAS_MIDDLE_WIDTH  = CANVAS_WIDTH / 2;
	static final double CANVAS_MIDDLE_HEIGHT = CANVAS_HEIGHT / 2;

	static final double[] ORIGIN   = {CANVAS_MIDDLE_WIDTH, CANVAS_MIDDLE_HEIGHT};
	static final double   DURATION = 1.0;

	// 描画する関数
	double function(double x) {
		return 150 * Math.sin(x / 20);
	}

	void drawFrame(Canvas canvas, GraphicsContext graphicsContext) {
		graphicsContext.strokeLine(0, 0, canvas.getWidth(), 0);
		graphicsContext.strokeLine(0, 0, 0, canvas.getHeight());
		graphicsContext.strokeLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight());
		graphicsContext.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
	}

	void drawAxis(Canvas canvas, GraphicsContext graphicsContext) {
		// x 軸
		graphicsContext.strokeLine(0, CANVAS_MIDDLE_HEIGHT, canvas.getWidth(), CANVAS_MIDDLE_HEIGHT);
		// y 軸
		graphicsContext.strokeLine(CANVAS_MIDDLE_WIDTH, 0, CANVAS_MIDDLE_WIDTH, canvas.getHeight());
	}

	void drawFunction(GraphicsContext graphicsContext, double x) {
		graphicsContext.strokeLine(ORIGIN[0] + (x * DURATION), ORIGIN[1] - function(x * DURATION), ORIGIN[0] + ((x + 1) * DURATION), ORIGIN[1] - function((x + 1) * DURATION));
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			var label = new Label("y = 150sin(x / 20)");
			label.setFont(new Font(20));

			var canvas = new Canvas(400, 400);
			var graphicsContext = canvas.getGraphicsContext2D();
			// 枠を描画
			drawFrame(canvas, graphicsContext);
			// x 軸, y 軸を描画
			drawAxis(canvas, graphicsContext);

			var button = new Button("実行");
			button.setMinSize(40, 40);
			button.setOnAction((actionEvent) -> {
				for(int i = -200; i < 200; i++) {
					drawFunction(graphicsContext, i);
				}

				button.setDisable(true);
			});

			var clearButton = new Button("クリア");
			clearButton.setOnAction((actionEvent) -> {
				graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				// 枠を描画
				drawFrame(canvas, graphicsContext);
				// x 軸, y 軸を描画
				drawAxis(canvas, graphicsContext);

				button.setDisable(false);
			});

			var borderPane = new BorderPane();
			borderPane.setCenter(new HBox(canvas, new VBox(label, button, clearButton)));

			Scene scene = new Scene(borderPane, 600, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("FunctionDrawing");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
