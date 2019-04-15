package grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lexical.DFA;
import lexical.DFAFactory;
import lexical.Token;


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
	private DFA dfa;
	
	/**
	 * 构造一个新的LR分析表
	 * @param table 分析表实体
	 * @param productions 产生式集
	 * @param projectSets 项目集列表
	 * @param symbol2Token 终结符对应Token的映射
	 * @param dfa DFA实例
	 */
	public AnalysisTable(List<Map<Symbol, Item>> table, ProductionList productions, ProjectSetList projectSets, Map<Token, Symbol> token2Symbol, DFA dfa) {
		this.table = table;
		this.productions = productions;
		this.projectSets = projectSets;
		this.tokenType2Symbol = new HashMap<>();
		for(Token t : token2Symbol.keySet())
			tokenType2Symbol.put(t.getType(), t);
		this.token2Symbol = token2Symbol;
		this.dfa = dfa;
	}
	
	/**
	 * 对输入的代码进行分析
	 * @param code 需要分析的代码
	 * @return 符号树
	 */
	public SymbolTree analysis(String code) {
		dfa.init(code);
		
		LRStack stack = new LRStack(productions);
		
		Token token = dfa.getNext();
		while(true) {
			Symbol curSymbol = null;
			if(token == null) { //得到了所有的token
				curSymbol = new Symbol("$", true); //那么最后一个符号为$
			} else {
				if(token.getType().equals("COMMENT") || token.getType().equals("ERROR")) { //注释和错误Token不进行读取
					continue;
				}
				curSymbol = getSymbolFromToken(token); //当前符号
			}
			
			int topStatus = stack.peekStatus(); //栈顶状态
			
			Item item = table.get(topStatus).get(curSymbol);
			if(item.statusIndex == -1) { //代表可以接收了，语法分析完成
				SymbolTree start = new SymbolTree(productions.productions.get(0).leftSymbol, null);
				while(stack.size() > 1) {
					start.addChild(stack.popSymbolTree());
				}
				return start;
			} else if(item.type == ItemType.SHIFT) { //移入
				stack.shift(item.statusIndex, curSymbol, token);
				token = dfa.getNext();
			} else if(item.type == ItemType.REDUCE) { //规约
				stack.reduce(table, item.statusIndex);
			} else { //错误
				
			}
		}
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
		SymbolTree root = test.analysis("int a = 10");
		System.out.println(root);
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
			str = "ACTION(s"+statusIndex+")";
			break;
		case REDUCE:
			str = "ACTION(r"+statusIndex+")";
			break;
		case GOTO:
			str = "GOTO("+statusIndex+")";
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
	private Stack<SymbolTree> symbolStack = new Stack<>(); //符号栈
	private Stack<Integer> statusStack = new Stack<>(); //状态栈
	
	public LRStack(ProductionList productions) {
		this.productions = productions.productions;
		symbolStack.push(new SymbolTree(new Symbol("$", true), null));
		statusStack.push(0);
	}
	
	/**
	 * 对栈进行移入操作
	 * @param status 移入的状态码
	 * @param symbol 移入的符号
	 * @param token 非终结状态则为null，终结状态则为对应的Token
	 */
	public void shift(int status, Symbol symbol, Token token) {
		statusStack.push(status);
		SymbolTree st = new SymbolTree(symbol, token);
		symbolStack.push(st);
	}
	
	/**
	 * 采用产生式productionIndex进行规约
	 * @param table LR分析表
	 * @param productionIndex 规约的产生式的编号
	 * @return 若成功规约则返回true；若栈顶的符号不满足产生式则返回false
	 */
	public boolean reduce(final List<Map<Symbol, Item>> table, int productionIndex) {
		Stack<SymbolTree> test = new Stack<>();
		Production p = productions.get(productionIndex);
		SymbolTree leftSymbol = new SymbolTree(p.leftSymbol, null);
		if(!p.isNil) { //p是非空产生式时才需要弹栈
			for(int i=p.rightSymbols.size()-1; i>=0; i--) {
				statusStack.pop();
				SymbolTree st = symbolStack.pop();
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
		statusStack.push(table.get(statusStack.peek()).get(leftSymbol.getSymbol()).statusIndex);
		return true;
	}
	
	public int size() {
		return symbolStack.size();
	}
	
	public SymbolTree peekSymbolTree() {
		return symbolStack.peek();
	}
	
	public SymbolTree popSymbolTree() {
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

class SymbolTree {
	/**
	 * 作为符号栈中的符号
	 * 每个符号有以下属性
	 * symbol 它对应的符号
	 * token 它对应的token，若是非终结符则为null
	 * lineNumber 对应的行号
	 * child 它的孩子符号
	 * isVisited 用来递归遍历的时候作为是否访问过的标记
	 */
	
	private Symbol symbol;
	private Token token;
	private int lineNumber;
	private LinkedList<SymbolTree> children;
	private boolean isVisited = false;
	
	public SymbolTree(Symbol symbol, Token token) {
		this.symbol = symbol;
		this.token = token;
		if(token == null) lineNumber = 0;
		else this.lineNumber = token.getLineNumber();
		this.children = null;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	public int lineNumber() {
		return lineNumber;
	}
	
	public void addChild(SymbolTree child) {
		if(children == null) {
			children = new LinkedList<>();
			this.lineNumber = child.lineNumber; //父节点符号的行号为第一个子节点的行号
		}
		children.addFirst(child);
	}
	
	public String getResultString() {
		StringBuffer sb = new StringBuffer();
		Stack<SymbolTree> stack = new Stack<>();
		stack.push(this);
		StringBuffer tab = new StringBuffer();
		
		while(!stack.isEmpty()) {
			SymbolTree top = stack.peek();
			if(top.isVisited) { //子树已经遍历结束
				stack.pop();
				top.isVisited = false; //重置
				tab.delete(tab.length()-2, tab.length());
			} else {
				for(SymbolTree st : top.children) {
					stack.push(st);
					st.isVisited = true;
					if(st.symbol.isFinal()) {
						sb.append(tab.toString()+st.symbol.getName()+"("+lineNumber+")"+st.token.forGrammar());
					} else {
						sb.append(tab.toString()+st.symbol.getName());
					}
				}
				tab.append("  ");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return symbol.toString();
	}
}