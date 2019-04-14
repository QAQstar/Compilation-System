package grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lexical.DFA;
import lexical.DFAFactory;
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
	private Map<Symbol, Token> symbol2Token;
	private DFA dfa;
	
	/**
	 * 构造一个新的LR分析表
	 * @param table 分析表实体
	 * @param productions 产生式集
	 * @param projectSets 项目集列表
	 * @param symbol2Token 终结符对应Token的映射
	 * @param dfa DFA实例
	 */
	public AnalysisTable(List<Map<Symbol, Item>> table, ProductionList productions, ProjectSetList projectSets, Map<Symbol, Token> symbol2Token, DFA dfa) {
		this.table = table;
		this.productions = productions;
		this.projectSets = projectSets;
		this.symbol2Token = symbol2Token;
		this.dfa = dfa;
	}
	
	/**
	 * 对输入的代码进行分析
	 * @param code 需要分析的代码
	 * @return 分析树
	 */
	public List<SymbolTree> analysis(String code) {
		List<SymbolTree> result = new ArrayList<>();
		dfa.init(code);
		
		Stack<Symbol> symbolStack = new Stack<>(); //符号栈
		Stack<Integer> statusStack = new Stack<>(); //状态栈
		symbolStack.push(new Symbol("$", true));
		statusStack.push(0);
		
		Token token = null;
		while((token=dfa.getNext()) != null) {
			
		}
		
		return result;
	}
	
	public String productions2String() {
		return productions.toString();
	}
	
	public String projectSets2String() {
		return projectSets.toString();
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
	 * 作为一个LR的栈，栈中需要存储符号和状态
	 * 虽然符号不是必须存的，但是为了方便还是存了
	 */
}

class SymbolTree {
	/**
	 * 作为栈中的符号
	 * 每个符号有以下属性
	 * name 它的名字
	 * lineNumber 对应的行号
	 * child 它的孩子符号
	 */
	
	String name;
	int lineNumber;
	List<SymbolTree> child;
}