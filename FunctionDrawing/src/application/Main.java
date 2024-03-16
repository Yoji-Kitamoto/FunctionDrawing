package application;

import java.util.ArrayList;
import java.util.Stack;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

	// operand の優先順位を返す (数値が小さい方が優先順位が高い)
	int priority(String operand) {
		/*
		 *  1: sin cos tan ... (括弧付き関数)
		 *  2: ^2 ^3 ^n x!
		 *  3: a b/c (分数)
		 *  4: - (負記号)
		 *  5: 単位変換
		 *  6: 乗算省略
		 *  7: nPr nCr
		 *  8: 内積
		 *  9: * /
		 * 10: + -
		 * 11: and
		 * 12: or xor xnor
		 */
		int priority = 0;

		switch(operand) {
			case "sin":
				priority = 1;
				break;

			case "^":
				priority = 2;
				break;

			case "*":
			case "#":
			case "/":
			case "&":
				priority = 9;
				break;

			case "+":
			case "-":
				priority = 10;
				break;
		}

		return priority;
	}

	// TODO: 各種操作に対応する
	// formula の逆ポーランド記法の式を求める
	// 操車場アルゴリズムを使用
	ArrayList<String> reversePolishNotation(String formula) {
		// 以下の操作には未対応
		// π (円周率)
		// log ln exp
		// cos tan (sec) (csc (cosec)) (cot)
		// Asin Acos Atan (Asec) (Acsc (Acosec)) (Acot)
		// sinh cosh tanh (sech) (csch (cosech)) (coth)
		// Asinh Acosh Atanh (Asech) (Acsch (Acosech)) (Acoth)
		// nroot(x) -> x^(1/n) (-> x 1 n / ^)
		// log(a, n) -> log(n) / log(a) (-> n log a log /)

		boolean isStartsWithMinus = formula.startsWith("-");
		if(isStartsWithMinus) {
			formula = formula.substring(1);
		}

		String[] chars = formula.split("");

		var result   = new ArrayList<String>();
		var operands = new Stack<String>();

		var token = new StringBuffer("");
		for(int i = 0; i < chars.length; i++) {
			if(chars[i].equals("")) {
				continue;
			}

			if(chars[i].equals("(")) {
				operands.push(chars[i]);
			}

			// 数値を token に格納
			while((i < chars.length) && (chars[i].matches("\\d|\\."))) {
				token.append(chars[i]);
				i++;
			}

			// result への数値の追加及び token の初期化
			result.add(token.toString());
			token = new StringBuffer("");

			// 結合性, 左結合性の演算子 (+, -, * /) をスタックに格納
			if((i < chars.length) && (chars[i].matches("\\+|-|\\*|\\/|#|&"))) {
				if(!operands.empty() && !operands.peek().equals("(") && !operands.peek().equals("sin") && (priority(chars[i]) >= priority(operands.peek()))) {
					result.add(operands.pop());
				}

				operands.push(chars[i]);
			}

			if((i < (chars.length - 2)) && chars[i].equals("s") && chars[i + 1].equals("i") && chars[i + 2].equals("n")) {
				operands.push("sin");
				i += 2;
				continue;
			}

			// 右結合性の演算子 (^) をスタックに格納
			if((i < chars.length) && (chars[i].equals("^"))) {
				operands.push(chars[i]);
			}

			if((i < chars.length) && chars[i].equals(")")) {
				while(!(operands.peek().equals("("))) {
					result.add(operands.pop());
				}

				operands.pop();
			}
		}

		// 最後にスタックに残った全ての演算子を result へ格納
		while(!operands.empty()) {
			result.add(operands.pop());
		}

		// result 内の空文字列を削除
		for(int i = 0; i < result.size(); i++) {
			while(result.get(i).equals("")) {
				result.remove(i);
			}
		}

		if(isStartsWithMinus) {
			result.set(0, "-" + result.get(0));
		}

		return result;
	}

	// 描画する関数
	double function(String formula, double x) {
		// TODO: x に負の数を代入した場合に対応する
		// formula に x を代入した式 (x が負の数の場合には未対応)
		// 独自の演算子 "#", "&" を定義
		// a # b = -a * b
		// a & b = -a / b
		String formulaSubstituedX = formula.replaceAll("(?<=\\d)(x)", "*" + String.valueOf(x))
											.replaceAll("(?<!\\d)(x)", String.valueOf(x))
											.replaceAll("\\+-", "-")
											.replaceAll("--", "+")
											.replaceAll("\\*-", "#")
											.replaceAll("\\/-", "&")
											.replaceAll("\\(-", "(0-")
											.replaceAll("(?<=\\d)(sin)", "*sin");

		var reversePolishNotationArrayList = reversePolishNotation(formulaSubstituedX);

		var nums = new Stack<Double>();

		for(int i = 0; i < reversePolishNotationArrayList.size(); i++) {
			if(reversePolishNotationArrayList.get(i).matches("-?\\d+(\\.\\d+)?")) {
				nums.push(Double.parseDouble(reversePolishNotationArrayList.get(i)));
			} else if(reversePolishNotationArrayList.get(i).equals("+")) {
				// a + b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(a + b);
			} else if(reversePolishNotationArrayList.get(i).equals("-")) {
				// a - b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(a - b);
			} else if(reversePolishNotationArrayList.get(i).equals("*")) {
				// a * b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(a * b);
			} else if(reversePolishNotationArrayList.get(i).equals("#")) {
				// -a * b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(-a * b);
			} else if(reversePolishNotationArrayList.get(i).equals("/")) {
				// a / b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(a / b);
			} else if(reversePolishNotationArrayList.get(i).equals("&")) {
				// -a / b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(-a / b);
			} else if(reversePolishNotationArrayList.get(i).equals("^")) {
				// a ^ b
				double b = nums.pop();
				double a = nums.pop();

				nums.push(Math.pow(a, b));
			} else if(reversePolishNotationArrayList.get(i).equals("sin")) {
				// sin(a)
				double a = nums.pop();

				nums.push(Math.sin(a));
			}
		}

		// 戻り値 (暫定)
		return nums.pop();
	}

	// canvas の枠を描画
	void drawFrame(Canvas canvas, GraphicsContext graphicsContext) {
		graphicsContext.strokeLine(0, 0, canvas.getWidth(), 0);
		graphicsContext.strokeLine(0, 0, 0, canvas.getHeight());
		graphicsContext.strokeLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight());
		graphicsContext.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
	}

	// x 軸, y 軸を描画
	void drawAxis(Canvas canvas, GraphicsContext graphicsContext) {
		// x 軸
		graphicsContext.strokeLine(0, CANVAS_MIDDLE_HEIGHT, canvas.getWidth(), CANVAS_MIDDLE_HEIGHT);
		// y 軸
		graphicsContext.strokeLine(CANVAS_MIDDLE_WIDTH, 0, CANVAS_MIDDLE_WIDTH, canvas.getHeight());
	}

	// 関数を描画
	void drawFunction(GraphicsContext graphicsContext, String formula) {
		for(int i = -200; i < 200; i++) {
			graphicsContext.strokeLine(ORIGIN[0] + (i * DURATION), ORIGIN[1] - function(formula, i * DURATION), ORIGIN[0] + ((i + 1) * DURATION), ORIGIN[1] - function(formula, (i + 1) * DURATION));
		}
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			var label = new Label("f(x) = ");
			label.setFont(new Font(20));

			var textField = new TextField("0");
			textField.setFont(new Font(16));
			textField.setPrefWidth(130);

			var canvas = new Canvas(400, 400);
			var graphicsContext = canvas.getGraphicsContext2D();

			drawFrame(canvas, graphicsContext);
			drawAxis(canvas, graphicsContext);

			var button = new Button("実行");
			button.setMinSize(40, 40);
			button.setOnAction((actionEvent) -> {
				drawFunction(graphicsContext, textField.getText());

				button.setDisable(true);
			});

			var clearButton = new Button("クリア");
			clearButton.setOnAction((actionEvent) -> {
				graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				// 枠を描画
				drawFrame(canvas, graphicsContext);
				// x 軸, y 軸を描画
				drawAxis(canvas, graphicsContext);

				textField.setText("0");

				button.setDisable(false);
			});

			var borderPane = new BorderPane();
			borderPane.setCenter(new HBox(canvas, new VBox(new HBox(label, textField), button, clearButton)));

			Scene scene = new Scene(borderPane, 700, 400);
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
