package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import grammar.AnalysisTable;
import grammar.AnalysisTableFactory;
import grammar.GrammarTree;
import grammar.Symbol;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lexical.DFA;
import lexical.DFAFactory;
import lexical.Token;
import semantic.Code;
import semantic.Semantic;
import semantic.SymbolTable;
import semantic.SymbolTableRow;

public class GUI extends Application{
//	private DFA dfa = null;
//	private AnalysisTable at = null;
	private DFA dfa = DFAFactory.creatorUseNFA("NFA.nfa");
	private AnalysisTable at = AnalysisTableFactory.creator("grammar.txt", dfa);
	
	@Override
	public void start(Stage primaryStage) {
		/* ************************ */
		//界面布局
		MenuItem itemLoadRule = new MenuItem("导入规则");
		MenuItem itemWriteRule = new MenuItem("导出规则");
		MenuItem itemLoadCode = new MenuItem("导入代码");
		MenuItem itemlexRule = new MenuItem("词法规则");
		MenuItem itemLexRun = new MenuItem("运行");
		Menu menuLex = new Menu("词法分析", null, itemLoadRule, itemWriteRule, itemLoadCode, itemlexRule, itemLexRun);
		MenuItem itemLoadGrammar = new MenuItem("导入文法");
		MenuItem itemAnalysisTable = new MenuItem("LR分析表");
		MenuItem itemGrammarRun = new MenuItem("运行");
		Menu menuGrammar = new Menu("语法制导翻译", null, itemLoadGrammar, itemAnalysisTable, itemGrammarRun);
		MenuBar menubar = new MenuBar(menuLex, menuGrammar);
		
		CodeArea codeArea = new CodeArea();
		codeArea.setStyle("-fx-font-family:consolas;" +
						  "-fx-font-size:16;");
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		VirtualizedScrollPane<CodeArea> codePane = new VirtualizedScrollPane<>(codeArea);
		
		BorderPane mainPane = new BorderPane();
		mainPane.setTop(menubar);
		mainPane.setCenter(codePane);
		
		Scene scene = new Scene(mainPane, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("file:/E:/Code/JAVA/Compilation-System/icon/icon.png"));
		primaryStage.setTitle("1160300708-周宇星-编译系统实验");
		primaryStage.show();
		
		/* ************************ */
		/* 事件监听 */
		//按下导入规则按钮
		itemLoadRule.setOnAction(event->{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("请选择FA转换表文件");
			//只接收DFA转换文件和NFA转换文件
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FA文件", "*.dfa;*.nfa"));
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				if(file.getName().charAt(file.getName().length()-3) == 'd') { //由DFA转换表文件构建
					dfa = DFAFactory.creator(file.getPath());
				} else { //由NFA转换表文件构建
					dfa = DFAFactory.creatorUseNFA(file.getPath());
				}
				//以下弹出提示，提示用户成功构建有穷自动机
				DialogPane tip = new DialogPane();
				Label tipLabel = new Label("成功构建DFA！");
				tipLabel.setStyle("-fx-font-size:18;"
								+ "-fx-font-weight:bold;"
								+ "-fx-text-fill:#1E90FF;");
				tip.setContent(tipLabel);
				Stage tipStage = new Stage();
				tipStage.setScene(new Scene(tip));
				tipStage.initOwner(primaryStage);
				tipStage.initModality(Modality.WINDOW_MODAL);
				tipStage.setResizable(false);
				tipStage.show();
			}
		});
		
		//按下导出规则按钮
		itemWriteRule.setOnAction(event->{
			if(dfa == null) { //还未导入词法规则文件
				DialogPane warning = new DialogPane();
				warning.setContentText("未导入FA转换表文件...");
				warning.setStyle("-fx-font-size:18;"
							   + "-fx-font-weight:bold;");
				Stage tipStage = new Stage();
				tipStage.setScene(new Scene(warning));
				tipStage.setResizable(false);
				tipStage.initOwner(primaryStage);
				tipStage.initModality(Modality.WINDOW_MODAL);
				tipStage.show();
			} else {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("保存DFA转换表文件");
				fileChooser.setInitialFileName("rule");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FA文件", "*.dfa"));
				File file = fileChooser.showSaveDialog(primaryStage);
				if (file != null) {
					dfa.write2File(file);
					//以下弹出提示，提示用户成功导出DFA规则文件
					DialogPane tip = new DialogPane();
					Label tipLabel = new Label("成功导出DFA转换表！");
					tipLabel.setStyle("-fx-font-size:18;"
									+ "-fx-font-weight:bold;"
									+ "-fx-text-fill:#1E90FF;");
					tip.setContent(tipLabel);
					Stage tipStage = new Stage();
					tipStage.setScene(new Scene(tip));
					tipStage.initOwner(primaryStage);
					tipStage.initModality(Modality.WINDOW_MODAL);
					tipStage.setResizable(false);
					tipStage.show();
				}
			}
		});
		
		//按下导入代码按钮
		itemLoadCode.setOnAction(event->{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("打开代码文件");
			//只接收DFA转换文件和NFA转换文件
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("代码文件", "*.txt"));
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				try(FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr)) {
					StringBuilder sb = new StringBuilder();
					String line = null;
					while((line=br.readLine()) != null)
						sb.append(line+"\n");
					codeArea.replaceText(0, codeArea.getLength(), sb.toString());
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		//按下词法规则按钮
		itemlexRule.setOnAction(event->{
			Stage s = lexRule();
			s.initOwner(primaryStage);
			s.initModality(Modality.WINDOW_MODAL);
			s.show();
		});
		
		//按下运行按钮
		itemLexRun.setOnAction(event->{
			Stage s = lexAnalysis(codeArea.getText());
			s.initOwner(primaryStage);
			s.initModality(Modality.WINDOW_MODAL);
			s.show();
		});
		
		//按下导入文法按钮
		itemLoadGrammar.setOnAction(event->{
			if(dfa == null) { //还未导入词法规则文件
				DialogPane warning = new DialogPane();
				warning.setContentText("未导入FA转换表文件...");
				warning.setStyle("-fx-font-size:18;"
							   + "-fx-font-weight:bold;");
				Stage tipStage = new Stage();
				tipStage.setScene(new Scene(warning));
				tipStage.setResizable(false);
				tipStage.initOwner(primaryStage);
				tipStage.initModality(Modality.WINDOW_MODAL);
				tipStage.show();
				return;
			}
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("请选择文法文件");

			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文法文件", "*.txt"));
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				at = AnalysisTableFactory.creator(file.getPath(), dfa);
				//以下弹出提示，提示用户成功构建有穷自动机
				DialogPane tip = new DialogPane();
				Label tipLabel = new Label("成功解析文法！");
				tipLabel.setStyle("-fx-font-size:18;"
								+ "-fx-font-weight:bold;"
								+ "-fx-text-fill:#1E90FF;");
				tip.setContent(tipLabel);
				Stage tipStage = new Stage();
				tipStage.setScene(new Scene(tip));
				tipStage.initOwner(primaryStage);
				tipStage.initModality(Modality.WINDOW_MODAL);
				tipStage.setResizable(false);
				tipStage.show();
			}
		});
		
		//按下分析表按钮
		itemAnalysisTable.setOnAction(event->{
			Stage s = analysisTable();
			s.initOwner(primaryStage);
			s.initModality(Modality.WINDOW_MODAL);
			s.show();
		});
		
		//按下运行按钮
		itemGrammarRun.setOnAction(event->{
			Stage s = grammarAnalysis(codeArea.getText());
			s.initOwner(primaryStage);
			s.initModality(Modality.WINDOW_MODAL);
			s.show();
		});
	}
	
	/**
	 * 按下词法分析菜单中的词法规则按钮后的界面
	 * 将之前输入的转移表显示出来
	 * @return
	 */
	private Stage lexRule() {
		Stage stage = new Stage();
		
		if(dfa == null) { //还未导入词法规则文件
			DialogPane warning = new DialogPane();
			warning.setContentText("未导入FA转换表文件...");
			warning.setStyle("-fx-font-size:18;"
						   + "-fx-font-weight:bold;");
			Scene scene = new Scene(warning);
			stage.setScene(scene);
			stage.setResizable(false);
			return stage;
		}
		
		
		ObservableList<String[]> list = FXCollections.observableArrayList();
		String[] table = null;
		for(int i=0; i<dfa.getStatusNum(); i++) {
			table = dfa.getTable(i);
			list.add(table);
		}
		
		TableView<String[]> tableView = new TableView<>(list);
		
		TableColumn<String[], String> tc_status = new TableColumn<>("状态");
		tc_status.setCellValueFactory(param->{ //加载状态名列
			SimpleStringProperty type = new SimpleStringProperty(param.getValue()[0]);
			return type;
		});
		tableView.getColumns().add(tc_status);
		
		TableColumn<String[], Object> tc_gotoTable = new TableColumn<>("转移表");
		
		for(int i=0; i<94; i++) {
			TableColumn<String[], String> tc_char = new TableColumn<>(String.valueOf((char)(i+32)));
			final int j = i+1;
			tc_char.setCellValueFactory(param->{ //加载状态名列
				SimpleStringProperty type = new SimpleStringProperty(param.getValue()[j]);
				return type;
			});
			tc_char.setStyle("-fx-alignment:center;");
			tc_gotoTable.getColumns().add(tc_char);
		}
		tableView.getColumns().add(tc_gotoTable);
		
		BorderPane mainPane = new BorderPane();
		mainPane.setCenter(tableView);
		
		stage.setScene(new Scene(mainPane));
		
		return stage;
	}
	
	/**
	 * 按下词法分析菜单中的运行按钮后的界面
	 * 对输入的代码进行分析，并给出token序列和转移过程
	 * @param code 要进行词法分析的代码
	 * @return Stage的界面，展示出分析结果
	 */
	private Stage lexAnalysis(String code) {
		Stage stage = new Stage();
		
		if(dfa == null) { //还未导入词法规则文件
			DialogPane warning = new DialogPane();
			warning.setContentText("未导入FA转换表文件...");
			warning.setStyle("-fx-font-size:18;"
						   + "-fx-font-weight:bold;");
			Scene scene = new Scene(warning);
			stage.setScene(scene);
			stage.setResizable(false);
			return stage;
		}
		
		dfa.init(code);
		
		Token token = null;
		ObservableList<Token> list = FXCollections.observableArrayList(); //一行的数据类型
		while((token=dfa.getNext()) != null) {
			list.add(token);
		}
		
		TableView<Token> tableView = new TableView<>(list);
		
		TableColumn<Token, String> tc_morpheme = new TableColumn<>("单词"); //从Token里提取出String
		tc_morpheme.setStyle("-fx-alignment:center;");
		tc_morpheme.setCellValueFactory(new PropertyValueFactory<>("morpheme")); //利用反射机制

		
		TableColumn<Token, String> tc_type = new TableColumn<>("种别码");
		tc_type.setStyle("-fx-alignment:center;");
		tc_type.setCellValueFactory(new PropertyValueFactory<>("type")); //利用反射机制
//		tc_type.setCellValueFactory(param->{ //加载种别码列
//			SimpleStringProperty type = new SimpleStringProperty(param.getValue().getType());
//			return type;
//		});
		
		TableColumn<Token, String> tc_value = new TableColumn<>("属性值");
		tc_value.setStyle("-fx-alignment:center;");
		tc_value.setCellValueFactory(new PropertyValueFactory<>("value")); //利用反射机制

		TableColumn<Token, String> tc_path = new TableColumn<>("转移路径");
		tc_path.setCellValueFactory(new PropertyValueFactory<>("path")); //利用反射机制

		tableView.getColumns().add(tc_morpheme);
		tableView.getColumns().add(tc_type);
		tableView.getColumns().add(tc_value);
		tableView.getColumns().add(tc_path);
		
		BorderPane mainPane = new BorderPane();
		mainPane.setCenter(tableView);
		
		stage.setScene(new Scene(mainPane));
		
		return stage;
	}
	
	/**
	 * 用来展示分析表的界面
	 * @param grammar 文法文件路径
	 * @return
	 */
	public Stage analysisTable() {
		Stage stage = new Stage();
		
		if(dfa == null || at == null) { //还未导入词法规则文件或文法文件
			DialogPane warning = new DialogPane();
			warning.setContentText("未导入FA转换表或文法...");
			warning.setStyle("-fx-font-size:18;"
						   + "-fx-font-weight:bold;");
			Scene scene = new Scene(warning);
			stage.setScene(scene);
			stage.setResizable(false);
			return stage;
		}
		
//		List<Symbol> symbols = new ArrayList<>();
//		symbols.addAll(at.getAllSymbols());
//		symbols.sort((s1, s2)->s1.getName().compareTo(s2.getName())); //稍微进行一下排序
		
		Set<Symbol> symbols = at.getAllSymbols();
		Map<String, List<Map<Symbol, String>>> table = at.getAnalysisTable();
		List<Map<Symbol, String>> ACTION = table.get("ACTION");
		List<Map<Symbol, String>> GOTO = table.get("GOTO");
		
		class TableRow {
			/**
			 * 代表了分析表中的一行
			 */
			
			Map<Symbol, String> ACTION;
			Map<Symbol, String> GOTO;
			SimpleIntegerProperty order;
			public TableRow(Map<Symbol, String> ACTION, Map<Symbol, String> GOTO, int order) {
				this.ACTION = ACTION;
				this.GOTO = GOTO;
				this.order = new SimpleIntegerProperty(order);
			}
			
			/**
			 * @return 找到该行对应于符号s的项
			 */
			public String getItem(Symbol s) {
				return s.isFinal() ? ACTION.get(s) : GOTO.get(s);
			}
			
			public SimpleIntegerProperty getOrder() {
				return this.order;
			}
		}
		
		ObservableList<TableRow> list = FXCollections.observableArrayList();
		for(int i=0; i<ACTION.size(); i++) {
			list.add(new TableRow(ACTION.get(i), GOTO.get(i), i+1));
		}
		
		TableView<TableRow> tableView = new TableView<>(list);
		TableColumn<TableRow, Number> tc_order = new TableColumn<>();
		TableColumn<TableRow, Object> tc_ACTION = new TableColumn<>("ACTION表");
		TableColumn<TableRow, Object> tc_GOTO = new TableColumn<>("GOTO表");
		tc_ACTION.setStyle("-fx-alignment:center;");
		tc_GOTO.setStyle("-fx-alignment:center;");
		tableView.getColumns().add(tc_order);
		tableView.getColumns().add(tc_ACTION);
		tableView.getColumns().add(tc_GOTO);
		tableView.getColumns().add(new TableColumn<>());

		tc_order.setCellValueFactory(param->{
			return param.getValue().getOrder();
		}); //利用反射机制
		
		for(Symbol s : symbols) {
			TableColumn<TableRow, String> tc_symbol = new TableColumn<>(s.getName());
			tc_symbol.setCellValueFactory(param->{
				SimpleStringProperty item = new SimpleStringProperty(param.getValue().getItem(s));
				return item;
			});
			tc_symbol.setStyle("-fx-alignment:center;");
			if(s.isFinal()) { //终结符
				tc_ACTION.getColumns().add(tc_symbol);
			} else { //非终结符
				tc_GOTO.getColumns().add(tc_symbol);
			}
		}
		
		TextArea ta = new TextArea(at.getProductions());
		ta.setEditable(false);
		ta.setPrefWidth(200);
		
		BorderPane mainPane = new BorderPane();
		mainPane.setCenter(tableView);
		mainPane.setLeft(ta);
		
		stage.setScene(new Scene(mainPane));
		
		return stage;
	}
	
	public Stage grammarAnalysis(String code) {
		Stage stage = new Stage();
		if(dfa == null || at == null) { //还未导入词法规则文件或文法文件
			DialogPane warning = new DialogPane();
			warning.setContentText("未导入FA转换表或文法...");
			warning.setStyle("-fx-font-size:18;"
						   + "-fx-font-weight:bold;");
			Scene scene = new Scene(warning);
			stage.setScene(scene);
			stage.setResizable(false);
			return stage;
		}
		
		GrammarTree gt = at.analysis(code);
		if(gt == null) {
			String errorInfo = at.getErrorInfo();
			TextArea ta_error = new TextArea(errorInfo);
			ta_error.setEditable(false);
			ta_error.setWrapText(true);
			ta_error.setStyle("-fx-font-size:14;"
							+ "-fx-text-fill:#ff0000;");
			stage.setScene(new Scene(ta_error, 300, 100));
			stage.setTitle("语法错误");
			return stage;
		}
		
		TreeView<String> treeView = new TreeView<>();
		TreeItem<String> root = new TreeItem<>();
		treeView.setRoot(root);
		
		class TreeItemAndGrammar {
			TreeItem<String> root;
			GrammarTree gt;
			
			public TreeItemAndGrammar(TreeItem<String> root, GrammarTree gt) {
				this.root = root;
				this.gt = gt;
			}
		}
		
		Stack<TreeItemAndGrammar> stack = new Stack<>();
		stack.push(new TreeItemAndGrammar(root, gt));
		
		while(!stack.isEmpty()) {
			GrammarTree top = stack.peek().gt;
			TreeItem<String> ti = stack.peek().root;
			if(top.isVisited) { //子树已经压入栈中
				stack.pop();
				top.isVisited = false; //重置
			} else { //子树还没压入栈
				if(top.children == null) { //终结符或者是空产生式
					if(top.symbol.isFinal()) { //终结符
						ti.setValue(top.symbol.getName()+"("+top.lineNumber+"):"+top.token.forGrammar());
						top.isVisited = false;
					} else { //空产生式
						TreeItem<String> child = new TreeItem<>("nil");
						ti.getChildren().add(child);
						ti.setValue(top.symbol.getName()+"("+top.lineNumber+")");
						top.isVisited = false;
					}
					stack.pop();
				} else { //非空产生式
					ti.setValue(top.symbol.getName()+"("+top.lineNumber+")");
					top.isVisited = true;
					for(int i=top.children.size()-1; i>=0; i--) { //展开子树
						GrammarTree st = top.children.get(i);
						TreeItem<String> tiTemp = new TreeItem<String>();
						TreeItemAndGrammar temp = new TreeItemAndGrammar(tiTemp, st);
						ti.getChildren().add(tiTemp);
						stack.push(temp);
					}
				}
			}
		}
		
		Button bTree = new Button("生成语法分析树");
		Button bSemantic = new Button("语义分析");
		HBox box = new HBox();
		box.getChildren().addAll(bTree, bSemantic);
		box.setStyle("-fx-alignment:center;"
				   + "-fx-spacing:5;");
		
		BorderPane mainPane = new BorderPane();
		mainPane.setCenter(treeView);
		mainPane.setBottom(box);
		
		bTree.setOnAction(event->{
			String graphvizCode = gt.getGraphvizCode();
			File dot = new File("src/GrammarTree.dot");
			File graph = new File("src/GrammarTree.png");
			Image image = null;
			try(FileWriter fw = new FileWriter(dot);
				BufferedWriter bw = new BufferedWriter(fw)) {
				bw.write(graphvizCode);
				bw.flush();
				
				Process p = null;
				String cmd = "cmd /c dot \""+dot.getAbsolutePath()+"\" -T png -o \""+graph.getAbsolutePath()+"\"";
				p = Runtime.getRuntime().exec(cmd, null, dot.getParentFile().getAbsoluteFile());
				p.waitFor();
				
				while(!graph.exists()) {
					Thread.sleep(1000);
				}
				image = new Image(graph.toURI().toURL().toExternalForm());
			} catch(IOException e1) {
				e1.printStackTrace();
			} catch(InterruptedException e2) {
				e2.printStackTrace();
			}
			
			Stage s = new Stage();
			ScrollPane sp = new ScrollPane();
			s.initModality(Modality.WINDOW_MODAL);
			s.initOwner(stage);
			s.setScene(new Scene(sp));
			ImageView imageView = new ImageView(image);
			imageView.setPreserveRatio(true);
			if(image.getHeight() > 1080) {
//				imageView.setFitHeight(1080);
				s.setMaximized(true);
			}
			sp.setContent(imageView);
			s.show();
			s.setOnCloseRequest(event2->{
				dot.delete();
				graph.delete();
			});
		});
		
		bSemantic.setOnAction(event->{
			Stage s = new Stage();
			ChoiceBox<String> cb = new ChoiceBox<>();
			Map<String, SymbolTable> tables = Semantic.getSymbolTables();
			VBox vb = new VBox();
			vb.setStyle("-fx-spacing:10;"
					  + "-fx-alignment:center;"
					  + "-fx-insets:10;");
			cb.getItems().addAll(tables.keySet());
			cb.getSelectionModel().select("main");
			Label lTip = new Label("请选择符号表");
			Button bSure = new Button("确定");
			bSure.setPrefSize(70, 30);
			vb.getChildren().addAll(lTip, cb, bSure);
			s.setScene(new Scene(vb, 150, 100));
			s.initModality(Modality.WINDOW_MODAL);
			s.initOwner(stage);
			s.show();
			bSure.setOnAction(event2->{
				s.close();
				symbolTable(tables.get(cb.getSelectionModel().getSelectedItem())).show();
			});
		});
		
		stage.setScene(new Scene(mainPane));
		stage.setTitle("语法树");
		
		return stage;
	}
	
	private Stage symbolTable(SymbolTable st) {
		Stage stage = new Stage();
		stage.setTitle(st.getName());
		StringBuilder tableInfo = new StringBuilder();
		if(st.getParent() != null) {
			tableInfo.append("外围符号表："+st.getParent().getName()+"\t");
		}
		tableInfo.append("总长度："+st.getSpaceSum());
		
		ObservableList<SymbolTableRow> list = FXCollections.observableArrayList();
		for(SymbolTableRow row : st.getRows()) {
			list.add(row);
		}
		
		TableView<SymbolTableRow> tableView = new TableView<>(list);
		TableColumn<SymbolTableRow, String> tc_varName = new TableColumn<>("变量名");
		TableColumn<SymbolTableRow, String> tc_type = new TableColumn<>("类型");
		TableColumn<SymbolTableRow, String> tc_value = new TableColumn<>("值");
		TableColumn<SymbolTableRow, Number> tc_space = new TableColumn<>("占用空间");
		TableColumn<SymbolTableRow, Number> tc_addr = new TableColumn<>("地址");
		tc_varName.setStyle("-fx-alignment:center;");
		tc_type.setStyle("-fx-alignment:center;");
		tc_value.setStyle("-fx-alignment:center;");
		tc_space.setStyle("-fx-alignment:center;");
		tc_addr.setStyle("-fx-alignment:center;");
		
		tc_varName.setCellValueFactory(new PropertyValueFactory<>("varName")); //利用反射机制
		tc_type.setCellValueFactory(new PropertyValueFactory<>("type")); //利用反射机制
		tc_value.setCellValueFactory(param->{
			SimpleStringProperty value = new SimpleStringProperty(String.valueOf(param.getValue().getValue()));
			return value;
		});
		tc_space.setCellValueFactory(param->{
			SimpleIntegerProperty space = new SimpleIntegerProperty(param.getValue().getSpace());
			return space;
		});
		tc_addr.setCellValueFactory(param->{
			SimpleIntegerProperty addr = new SimpleIntegerProperty(param.getValue().getAddr());
			return addr;
		});
		
		tableView.getColumns().add(tc_varName);
		tableView.getColumns().add(tc_type);
		tableView.getColumns().add(tc_value);
		tableView.getColumns().add(tc_space);
		tableView.getColumns().add(tc_addr);
		tableView.setTableMenuButtonVisible(true);
		
		TextArea taError = new TextArea();
		TextArea taInfo = new TextArea();
		taError.setEditable(false);
		taInfo.setEditable(false);
		taError.setStyle("-fx-text-fill:#ff0000;");
		taInfo.setStyle("-fx-text-fill:#a9a9a9;");
		taError.setPrefHeight(100);
		taInfo.setPrefHeight(100);
		taError.setWrapText(true);
		taInfo.setWrapText(true);
		taError.setText(st.getError().toString());
		taInfo.setText(st.getInfo().toString());
		HBox hb = new HBox();
		hb.setStyle("-fx-alignment:center;");
		hb.getChildren().addAll(taError, taInfo);
		
		TextArea taCode = new TextArea();
		taCode.setEditable(false);
		taCode.setPrefWidth(230);
		List<Code> codes = st.getCodes();
		for(int i=1; i<=codes.size(); i++) {
			taCode.appendText(i+": "+codes.get(i-1)+"\n");
		}
		
		BorderPane bp = new BorderPane();
		bp.setTop(new Label(tableInfo.toString()));
		bp.setCenter(tableView);
		bp.setBottom(hb);
		bp.setLeft(taCode);
		stage.setScene(new Scene(bp));
		return stage;
	}
	
	public static void main(String[] args) {
		launch();
	}
}
