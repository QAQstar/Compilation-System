package grammar;

import java.util.List;
import java.util.Map;


public class AnalysisTable {
	/**
	 * 该类是一个LALR分析的分析表
	 * table存储的格式为，第i个状态遇到某个symbol后的项是什么
	 * Item = table.get(i).get(symbol)
	 */
	
	List<Map<Symbol, Item>> table = null;
	
	public AnalysisTable(List<Map<Symbol, Item>> table) {
		this.table = table;
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
	
	public Item(int flag, int status) {
		this.flag = flag;
		this.status = status;
	}
}