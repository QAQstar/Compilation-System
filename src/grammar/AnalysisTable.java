package grammar;

import java.util.List;
import java.util.Map;


public class AnalysisTable {
	/**
	 * 该类是一个LALR分析的分析表
	 * table存储的格式为，第i个状态遇到某个symbol后的项是什么
	 * Item = table.get(i).get(symbol)
	 * projects存储的是项目集合
	 * productions存储所有产生式的列表
	 * projectSets存储所有项目集的列表
	 */
	
	List<Map<Symbol, Item>> table;
	ProductionList productions;
	ProjectSetList projectSets;
	
	public AnalysisTable(List<Map<Symbol, Item>> table, ProductionList productions, ProjectSetList projectSets) {
		this.table = table;
		this.productions = productions;
		this.projectSets = projectSets;
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