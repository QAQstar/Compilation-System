package grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lexical.DFA;
import lexical.Token;
import semantic.Semantic;


public class AnalysisTable {
	/**
	 * 该类是一个LR分析的分析表
	 * table存储的格式为，第i个状态遇到某个symbol后的项是什么
	 * Item = table.get(i).get(symbol)
	 * projects存储的是项目集合
	 * productions存储所有产生式的列表
	 * projectSets存储所有项目集的列表
	 */
	
	private List<Map<Symbol, Item>> table;
	private ProductionList productions;
	private ProjectSetList projectSets;
	private Map<String, Token> tokenType2Symbol;
	private Map<Token, Symbol> token2Symbol;
	private Set<Symbol> errorHandling;
	private DFA dfa;
	private StringBuilder errorInfo = new StringBuilder();
	
	/**
	 * 构造一个新的LR分析表
	 * @param table 分析表实体
	 * @param productions 产生式集
	 * @param projectSets 项目集列表
	 * @param symbol2Token 终结符对应Token的映射
	 * @param dfa DFA实例
	 */
	public AnalysisTable(List<Map<Symbol, Item>> table, ProductionList productions, ProjectSetList projectSets, Map<Token, Symbol> token2Symbol, Set<Symbol> errorHandling, DFA dfa) {
		this.table = table;
		this.productions = productions;
		this.projectSets = projectSets;
		this.tokenType2Symbol = new HashMap<>();
		for(Token t : token2Symbol.keySet())
			tokenType2Symbol.put(t.getType(), t);
		this.token2Symbol = token2Symbol;
		this.errorHandling = errorHandling;
		this.dfa = dfa;
	}
	
	/**
	 * 对输入的代码进行分析
	 * @param code 需要分析的代码
	 * @return 符号树
	 */
	public GrammarTree analysis(String code) {
		errorInfo.delete(0, errorInfo.length());
		dfa.init(code);
		Semantic.init();
		
		LRStack stack = new LRStack(productions);
		
		Token token = dfa.getNext();
		Symbol finalSymbol = new Symbol("$", true);
		boolean isReduce = false; //前一个动作是否是规约 用来加速的
		Symbol curSymbol = null;
		while(true) {
			if(token == null) { //得到了所有的token
				curSymbol = finalSymbol; //那么最后一个符号为$
			} else if (!isReduce){ //如果之前的状态是规约状态，那么输入没变，不用重新获取一次
				if(token.getType().equals("COMMENT") || token.getType().equals("ERROR")) { //注释和错误Token不进行读取
					token = dfa.getNext();
					continue;
				}
				curSymbol = getSymbolFromToken(token); //当前符号
			}
			
			int topStatus = stack.peekStatus(); //栈顶状态
			
			Item item = table.get(topStatus).get(curSymbol);
			if(item == null) { //出现语法错误
				errorInfo.append(stack.error(table, errorHandling)+"\n");
				return null;
			} else if(item.statusIndex == -1) { //代表可以接收了，语法分析完成
				GrammarTree start = new GrammarTree(productions.productions.get(0).leftSymbol, null);
				while(stack.size() > 1) {
					start.addChild(stack.popGrammarTree());
				}
				return start;
			} else if(item.type == ItemType.SHIFT) { //移入
				stack.shift(item.statusIndex, curSymbol, token);
				token = dfa.getNext();
				isReduce = false;
			} else if(item.type == ItemType.REDUCE) { //规约
				stack.reduce(table, item.statusIndex);
				isReduce = true;
//				System.out.println(productions.productions.get(item.statusIndex));
				Semantic.setProperty(stack.peekGrammarTree());
			}
		}
	}
	
	/**
	 * 得到LR分析表中
	 * @return LR分析表, map.get("ACTION")是ACTION表, map.get("GOTO")是GOTO表; 
	 * 表的结构是List<Map<Symbol, String>>
	 */
	public Map<String, List<Map<Symbol, String>>> getAnalysisTable() {
		Map<String, List<Map<Symbol, String>>> result = new HashMap<>();
		List<Map<Symbol, String>> ACTION = new ArrayList<>();
		List<Map<Symbol, String>> GOTO = new ArrayList<>();
		result.put("ACTION", ACTION);
		result.put("GOTO", GOTO);
		for(int i=0; i<table.size(); i++) {
			ACTION.add(new HashMap<>());
			GOTO.add(new HashMap<>());
			for(Symbol s : productions.getAllSymbol()) {
				Item item = table.get(i).get(s);
				if(item != null) { //有动作
					if(item.type == null) { //接收了
						ACTION.get(i).put(s, item.toString());
						continue;
					}
					switch(item.type) {
					case SHIFT:
						ACTION.get(i).put(s, item.toString());
						break;
					case REDUCE:
						ACTION.get(i).put(s, item.toString());
						break;
					case GOTO:
						GOTO.get(i).put(s, item.toString());
						break;
					}
				}
			}
		}
		
		return result;
	}
	
	public String getProductions() {
		return this.productions.toString();
	}
	
	public String getErrorInfo() {
		return this.errorInfo.toString();
	}
	
	/**
	 * 得到所有的非终结符
	 * @return 所有非终结符集合
	 */
	public Set<Symbol> getNoFinalSymbols() {
		return productions.getNoFinalSymbol();
	}
	
	/**
	 * 得到所有的文法符号，包括终结符
	 * @return 文法符号集合
	 */
	public Set<Symbol> getAllSymbols() {
		return productions.getAllSymbol();
	}
	
	/**
	 * 查找词法分析中得到的Token所对应的语法分析的终结符
	 * @param token 词法分析得到的Token
	 * @return 语法分析中与该Token对应的终结符
	 */
	private Symbol getSymbolFromToken(Token token) {
		Token matchToken = tokenType2Symbol.get(token.getType());
		if(matchToken == null) return null;
		if(matchToken.getValue() == null) return token2Symbol.get(matchToken);
		if(matchToken.getValue().equals(token.getValue())) return token2Symbol.get(matchToken);
		return null;
	}

	/**
	 * 得到分析表中的某一项
	 * @param statusIndex 分析表的状态序号
	 * @param symbol 该状态接收的符号
	 * @return 分析表中对应的项
	 */
	public Item getItem(int statusIndex, Symbol symbol) {
		return table.get(statusIndex).get(symbol);
	}
	
	public String productions2String() {
		return productions.toString();
	}
	
	public String projectSets2String() {
		return projectSets.toString();
	}
	
	public static void main(String[] args) {
		AnalysisTable test = AnalysisTableFactory.creator("grammar.txt", "NFA.nfa");
		String code = "proc test (int a; int b;) {"
					+ "a = 1;"
					+ "b = 2;"
					+ "int c;"
					+ "c = 3;"
					+ "}"
					+ "int d;"
					+ "d = 1;"
					+ "call test(1, d,);";
		GrammarTree root = test.analysis(code);
		System.out.println("语法分析: \n" + root.getResultString());
		System.out.println("\n语义分析: \n" + Semantic.getCode());
	}
}

class Item {
	/**
	 * 代表了分析表中的一项
	 * 若该项是ACTION表中的元素，那么type表示该项是SHIFT(移入)还是REDUCE(规约)
	 * 若该项是GOTO表中的元素，那么type为null
	 * statusIndex表示跳转到哪个状态的编号，接收状态为-1
	 */
	
	ItemType type;
	int statusIndex;
	
	/**
	 * 创建一个分析表中的一项
	 * @param type type表示该项是SHIFT(移入)还是REDUCE(规约)还是跳转(GOTO)
	 * @param statusIndex 表示执行动作后跳转到的项目集标号(状态号)，-1表示接收
	 */
	public Item(ItemType type, int statusIndex) {
		this.type = type;
		this.statusIndex = statusIndex;
	}
	
	@Override
	public String toString() {
		if(statusIndex == -1) return "acc";
		String str = null;
		switch(type) {
		case SHIFT:
			str = "s"+statusIndex;
			break;
		case REDUCE:
			str = "r"+statusIndex;
			break;
		case GOTO:
			str = String.valueOf(statusIndex);
		}
		
		return str;
	}
}

enum ItemType {
	SHIFT, REDUCE, GOTO
}

class LRStack {
	/**
	 * 作为一个LR语法分析的栈，栈中需要存储符号和状态
	 * 虽然符号不是必须存的，但是为了方便还是存了
	 */

	private List<Production> productions;
	private Stack<GrammarTree> symbolStack = new Stack<>(); //符号栈
	private Stack<Integer> statusStack = new Stack<>(); //状态栈
	
	public LRStack(ProductionList productions) {
		this.productions = productions.productions;
		symbolStack.push(new GrammarTree(new Symbol("$", true), null));
		statusStack.push(0);
	}
	
	public String error(final List<Map<Symbol, Item>> table, Set<Symbol> errorHandling) {
		StringBuffer sb = new StringBuffer();
		int lineNumber = -1;
		while(!statusStack.isEmpty()) {
			GrammarTree gt = symbolStack.pop();
			int statusIndex = statusStack.pop();
			if(lineNumber == -1 && gt.token != null) {
				lineNumber = gt.token.getLineNumber();
			}
			for(Symbol s : errorHandling) {
				if(table.get(statusIndex).containsKey(s)) { //如果包含跳到A的跳转，那么就认为是A的式子出错
					if(s.getName().equals("D")) {
						sb.append("Error at Line ["+(lineNumber==-1?1:lineNumber)+"]: 声明语句出错！");
						statusStack.push(table.get(statusIndex).get(s).statusIndex);
						symbolStack.push(new GrammarTree(s, null));
						return sb.toString();
					} else if(s.getName().equals("S")) {
						sb.append("Error at Line ["+(lineNumber==-1?1:lineNumber)+"]: 可执行语句出错！");
						statusStack.push(table.get(statusIndex).get(s).statusIndex);
						symbolStack.push(new GrammarTree(s, null));
						return sb.toString();
					}
					
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 对栈进行移入操作
	 * @param status 移入的状态码
	 * @param symbol 移入的符号
	 * @param token 非终结状态则为null，终结状态则为对应的Token
	 */
	public void shift(int status, Symbol symbol, Token token) {
		statusStack.push(status);
		GrammarTree st = new GrammarTree(symbol, token);
		symbolStack.push(st);
	}
	
	/**
	 * 采用产生式productionIndex进行规约
	 * @param table LR分析表
	 * @param productionIndex 规约的产生式的编号
	 * @return 若成功规约则返回true；若栈顶的符号不满足产生式则返回false
	 */
	public boolean reduce(final List<Map<Symbol, Item>> table, int productionIndex) {
		Stack<GrammarTree> test = new Stack<>();
		Production p = productions.get(productionIndex);
		GrammarTree leftSymbol = new GrammarTree(p.leftSymbol, null);
		if(!p.isNil) { //p是非空产生式时才需要弹栈
			for(int i=p.rightSymbols.size()-1; i>=0; i--) {
				statusStack.pop();
				GrammarTree st = symbolStack.pop();
				test.push(st);
				if(!st.getSymbol().equals(p.rightSymbols.get(i))) {
					while(!test.isEmpty()) { //进行恢复
						symbolStack.push(test.pop());
					}
					return false;
				}
				leftSymbol.addChild(st);
			}
		}
		
		symbolStack.push(leftSymbol);
		leftSymbol.setProductionIndex(productionIndex);
		statusStack.push(table.get(statusStack.peek()).get(leftSymbol.getSymbol()).statusIndex);
		return true;
	}
	
	public int size() {
		return symbolStack.size();
	}
	
	public GrammarTree peekGrammarTree() {
		return symbolStack.peek();
	}
	
	public GrammarTree popGrammarTree() {
		return symbolStack.pop();
	}
	
	public Symbol popSymbol() {
		return symbolStack.pop().getSymbol();
	}
	
	public int popStatus() {
		return statusStack.pop();
	}
	
	public Symbol peekSymbol() {
		return symbolStack.peek().getSymbol();
	}
	
	public int peekStatus() {
		return statusStack.peek();
	}
}