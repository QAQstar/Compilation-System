package grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Production {
	/**
	 * 记录一个产生式
	 * leftSymbol表示产生式左部的符号
	 * rightSymbols表示产生式右部的符号
	 */
	
	Symbol leftSymbol;
	List<Symbol> rightSymbols;
	
	public Production(Symbol leftSymbol, List<Symbol> rightSymbols) {
		this.leftSymbol = leftSymbol;
		this.rightSymbols = rightSymbols;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSymbol.toString()+"->");
		for(Symbol s : rightSymbols) {
			sb.append(s.toString()+" ");
		}
		sb.delete(sb.length()-1, sb.length());
		return sb.toString();
	}
}

class Project {
	/**
	 * 该类是一个项目类
	 * 每个项目中都含有一个产生式和一个位置信息
	 * leftSymbol代表产生式左部
	 * production=ArrayList<Symbol>代表产生式右部
	 * outlook表示展望符
	 * pos代表·的位置信息
	 * 如产生式A->·BCD
	 * 那么leftSymbol=A
	 * production=BCD
	 * pos=0
	 */
	
	private Symbol leftSymbol;
	private List<Symbol> production;
	private Set<Symbol> outlook;
	private int pos;
	
	/**
	 * 构造一个新的项目
	 * @param leftSymbol 产生式左部的符号
	 * @param production 产生式右部的符号List；若产生式为空，则传入null
	 * @param outlook 展望符集合
	 * @param pos 符号·所在的位置；如A->B·CD，pos=1
	 */
	public Project(Symbol leftSymbol, List<Symbol> production, Set<Symbol> outlook, int pos) {
		this.leftSymbol = leftSymbol;
//		if(production.get(0).equals("nil")) { //空产生式
//			this.production = new ArrayList<>();
//		} else {
		this.production = production;
//		}
		this.outlook = outlook;
		if(production.get(0).equals(new Symbol(true, "nil"))) //空产生式，那么pos=1
			this.pos = 1;
		else
			this.pos = pos;
	}
	
	/**
	 * @return ·的下一个符号；如A->BC·D，将会返回D；若该项目是规约项目，则返回null
	 */
	public Symbol getPosSymbol() {
		if(pos >= production.size()) return null;
		return production.get(pos);
	}
	
	/**
	 * @param pos 指定位置
	 * @return 指定位置的符号
	 */
	public Symbol getPosSymbol(int pos) {
		if(pos >= production.size()) return null;
		return production.get(pos);
	}
	
	/**
	 * @return ·的下一个符号之后的符号序列，如A->B·CD，将会返回D
	 */
	public List<Symbol> getBehindPos() {
		if(pos+1 >= production.size()) return null;
		return production.subList(pos+1, production.size());
	}
	
	/**
	 * @return 该项目是规约状态返回true；否则返回false
	 */
	public boolean isReduce() {
		return pos == production.size();
	}

	/**
	 * @return 产生式左部的符号
	 */
	public Symbol getLeftSymbol() {
		return leftSymbol;
	}
	
	/**
	 * @return 右部的产生式
	 */
	public List<Symbol> getProduction() {
		return production;
	}
	
	/**
	 * @return 展望符集
	 */
	public Set<Symbol> getOutlook() {
		return outlook;
	}
	
	/**
	 * @return ·的位置
	 */
	public int getPos() {
		return pos;
	}
	
	/**
	 * 得到该项目的产生式
	 * @return 该项目的产生式
	 */
	public Production production() {
		return new Production(leftSymbol, production);
	}
	
	/**
	 * 判断两个状态是否是可合并的
	 * @param p 另一个项目
	 * @return
	 */
	public boolean canMerge(Project p) {
		return p.toString().substring(0, p.toString().indexOf("[", 3)-1).equals(this.toString().substring(0, this.toString().indexOf("[", 3)-1));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof Project) {
			return ((Project)obj).toString().equals(this.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSymbol.toString()+"->");
		if(production.get(0).getName().equals("nil")) {
			sb.append("·");
		} else {
			for(int i=0; i<production.size(); i++) {
				if(i == pos) sb.append("·");
				sb.append(production.get(i).toString());
			}
			if(pos == production.size()) {
				sb.append("·");
			}
		}
		sb.append(" [");
		for(Symbol s : outlook) {
			sb.append(s.toString() + " ");
		}
		sb.replace(sb.length()-1, sb.length(), "]");
		return sb.toString();
	}
}

class ProjectSet {
	/**
	 * 该类维护项目集
	 */
	
	Set<Project> projects = null;
	
	public ProjectSet(Set<Project> projects) {
		this.projects = projects;
	}
	
	public Set<Project> getProjects() {
		return projects;
	}
	
	public Set<Symbol> canGoto() {
		Set<Symbol> result = new HashSet<>();
		for(Project p : projects) {
			Symbol b = p.getPosSymbol();
			if(b != null) result.add(b);
		}
		return result;
	}
	
	public boolean isEmpty() {
		return projects == null || projects.size() == 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Project p : projects) {
			sb.append(p.toString() + "\n");
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o instanceof ProjectSet) {
			return ((ProjectSet)o).toString().equals(this.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}