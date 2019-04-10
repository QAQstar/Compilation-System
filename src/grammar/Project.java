package grammar;

import java.util.List;

public class Project {
	/**
	 * 该类是一个项目类
	 * 每个项目中都含有一个产生式和一个位置信息
	 * leftSymbol代表产生式左部
	 * production=ArrayList<Symbol>代表产生式右部
	 * pos代表·的位置信息
	 * 如产生式A->·BCD
	 * 那么leftSymbol=A
	 * production=BCD
	 * pos=0
	 */
	
	private Symbol leftSymbol;
	private List<Symbol> production;
	private int pos;
	
	public Project(Symbol leftSymbol, List<Symbol> production, int pos) {
		this.leftSymbol = leftSymbol;
		this.production = production;
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSymbol.toString())
	}
}
