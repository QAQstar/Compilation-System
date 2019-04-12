package grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lexical.DFA;
import lexical.DFAFactory;
import lexical.LexFactory;
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
	
	public AnalysisTable(List<Map<Symbol, Item>> table, ProductionList productions, ProjectSetList projectSets) {
		this.table = table;
		this.productions = productions;
		this.projectSets = projectSets;
	}
	
	/**
	 * 对输入的代码进行分析
	 * @param code 需要分析的代码
	 * @param faFilePath fa文件路径
	 * @return 分析树
	 */
	public List<SymbolTree> analysis(String code, String faFilePath) {
		List<SymbolTree> result = new ArrayList<>();
		DFA dfa = null;
		if(faFilePath.indexOf(faFilePath.length()-3) == 'd') { //由DFA转换表文件构建
			dfa = DFAFactory.creator(faFilePath);
		} else { //由NFA转换表文件构建
			dfa = DFAFactory.creatorUseNFA(faFilePath);
		}
		dfa.init(code);
		
//		Token token = null;
//		while((token=dfa.getNext()) != null) {
//			
//		}
		
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
	 * flag表示该项是s(-1)还是r(1)
	 * 若该项是GOTO表中的元素，那么flag为0
	 * status表示跳转到哪个状态
	 */
	
	int flag, status;
	
	/**
	 * 代表了分析表中的一项
	 * @param flag flag表示该项是s(-1)还是r(1);若该项是GOTO表中的元素，那么flag为0
	 * @param status 表示跳转到的项目集标号
	 */
	public Item(int flag, int status) {
		this.flag = flag;
		this.status = status;
	}
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