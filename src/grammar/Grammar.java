//package grammar;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//class Production {
//	/**
//	 * 记录一个产生式
//	 * leftSymbol表示产生式左部的符号
//	 * rightSymbols表示产生式右部的符号
//	 */
//	
//	Symbol leftSymbol;
//	List<Symbol> rightSymbols;
//	
//	public Production(Symbol leftSymbol, List<Symbol> rightSymbols) {
//		this.leftSymbol = leftSymbol;
//		this.rightSymbols = rightSymbols;
//	}
//	
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(leftSymbol.toString()+"->");
//		for(Symbol s : rightSymbols) {
//			sb.append(s.toString()+" ");
//		}
//		sb.delete(sb.length()-1, sb.length());
//		return sb.toString();
//	}
//}
//
//class Project {
//	/**
//	 * 该类是一个项目类
//	 * 每个项目中都含有一个产生式和一个位置信息
//	 * leftSymbol代表产生式左部
//	 * production=ArrayList<Symbol>代表产生式右部
//	 * outlook表示展望符
//	 * pos代表·的位置信息
//	 * 如产生式A->·BCD
//	 * 那么leftSymbol=A
//	 * production=BCD
//	 * pos=0
//	 */
//	
//	private Symbol leftSymbol;
//	private List<Symbol> production;
//	private Set<Symbol> outlook;
//	private int pos;
//	
//	/**
//	 * 构造一个新的项目
//	 * @param leftSymbol 产生式左部的符号
//	 * @param production 产生式右部的符号List；若产生式为空，则传入null
//	 * @param outlook 展望符集合
//	 * @param pos 符号·所在的位置；如A->B·CD，pos=1
//	 */
//	public Project(Symbol leftSymbol, List<Symbol> production, Set<Symbol> outlook, int pos) {
//		this.leftSymbol = leftSymbol;
////		if(production.get(0).equals("nil")) { //空产生式
////			this.production = new ArrayList<>();
////		} else {
//		this.production = production;
////		}
//		this.outlook = outlook;
//		if(production.get(0).equals(new Symbol(true, "nil"))) //空产生式，那么pos=1
//			this.pos = 1;
//		else
//			this.pos = pos;
//	}
//	
//	/**
//	 * @return ·的下一个符号；如A->BC·D，将会返回D；若该项目是规约项目，则返回null
//	 */
//	public Symbol getPosSymbol() {
//		if(pos >= production.size()) return null;
//		return production.get(pos);
//	}
//	
//	/**
//	 * @param pos 指定位置
//	 * @return 指定位置的符号
//	 */
//	public Symbol getPosSymbol(int pos) {
//		if(pos >= production.size()) return null;
//		return production.get(pos);
//	}
//	
//	/**
//	 * @return ·的下一个符号之后的符号序列，如A->B·CD，将会返回D
//	 */
//	public List<Symbol> getBehindPos() {
//		if(pos+1 >= production.size()) return null;
//		return production.subList(pos+1, production.size());
//	}
//	
//	/**
//	 * @return 该项目是规约状态返回true；否则返回false
//	 */
//	public boolean isReduce() {
//		return pos == production.size();
//	}
//
//	/**
//	 * @return 产生式左部的符号
//	 */
//	public Symbol getLeftSymbol() {
//		return leftSymbol;
//	}
//	
//	/**
//	 * @return 右部的产生式
//	 */
//	public List<Symbol> getProduction() {
//		return production;
//	}
//	
//	/**
//	 * @return 展望符集
//	 */
//	public Set<Symbol> getOutlook() {
//		return outlook;
//	}
//	
//	/**
//	 * @return ·的位置
//	 */
//	public int getPos() {
//		return pos;
//	}
//	
//	/**
//	 * 得到该项目的产生式
//	 * @return 该项目的产生式
//	 */
//	public Production production() {
//		return new Production(leftSymbol, production);
//	}
//	
//	/**
//	 * 判断两个状态是否是可合并的
//	 * @param p 另一个项目
//	 * @return
//	 */
//	public boolean canMerge(Project p) {
//		return p.toString().substring(0, p.toString().indexOf("[", 3)-1).equals(this.toString().substring(0, this.toString().indexOf("[", 3)-1));
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if(obj == this) return true;
//		if(obj instanceof Project) {
//			return ((Project)obj).toString().equals(this.toString());
//		}
//		return false;
//	}
//	
//	@Override
//	public int hashCode() {
//		return this.toString().hashCode();
//	}
//	
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(leftSymbol.toString()+"->");
//		if(production.get(0).getName().equals("nil")) {
//			sb.append("·");
//		} else {
//			for(int i=0; i<production.size(); i++) {
//				if(i == pos) sb.append("·");
//				sb.append(production.get(i).toString());
//			}
//			if(pos == production.size()) {
//				sb.append("·");
//			}
//		}
//		sb.append(" [");
//		for(Symbol s : outlook) {
//			sb.append(s.toString() + " ");
//		}
//		sb.replace(sb.length()-1, sb.length(), "]");
//		return sb.toString();
//	}
//}
//
//class ProjectSet {
//	/**
//	 * 该类维护项目集
//	 */
//	
//	Set<Project> projects = null;
//	
//	public ProjectSet(Set<Project> projects) {
//		this.projects = projects;
//	}
//	
//	public Set<Project> getProjects() {
//		return projects;
//	}
//	
//	public Set<Symbol> canGoto() {
//		Set<Symbol> result = new HashSet<>();
//		for(Project p : projects) {
//			Symbol b = p.getPosSymbol();
//			if(b != null) result.add(b);
//		}
//		return result;
//	}
//	
//	public boolean isEmpty() {
//		return projects == null || projects.size() == 0;
//	}
//	
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		for(Project p : projects) {
//			sb.append(p.toString() + "\n");
//		}
//		return sb.toString();
//	}
//	
//	@Override
//	public boolean equals(Object o) {
//		if(o == this) return true;
//		if(o instanceof ProjectSet) {
//			return ((ProjectSet)o).toString().equals(this.toString());
//		}
//		return false;
//	}
//	
//	@Override
//	public int hashCode() {
//		return this.toString().hashCode();
//	}
//}

package grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Production {
	/**
	 * 记录一个产生式
	 * leftSymbol表示产生式左部的符号
	 * rightSymbols表示产生式右部的符号
	 * isNil表示该产生式是否是空产生式
	 * index表示该产生式的编号
	 */
	
	Symbol leftSymbol;
	List<Symbol> rightSymbols;
	boolean isNil;
	int index;
	
	/**
	 * 构建一个产生式
	 * @param leftSymbol 产生式左部的非终结符
	 * @param rightSymbols 产生式右部的符号集合；若是空产生式则传入null
	 * @param index 该产生式的编号，唯一
	 */
	public Production(Symbol leftSymbol, List<Symbol> rightSymbols, int index) {
		this.leftSymbol = leftSymbol;
		this.rightSymbols = rightSymbols;
		this.isNil = rightSymbols == null ? true : false;
		this.index = index;
	}
	
	/**
	 * 构建一个产生式，编号由ProductionSet类管理
	 * @param leftSymbol 产生式左部的非终结符
	 * @param rightSymbols 产生式右部的符号集合；若是空产生式则传入null
	 */
	public Production(Symbol leftSymbol, List<Symbol> rightSymbols) {
		this.leftSymbol = leftSymbol;
		this.rightSymbols = rightSymbols;
		this.isNil = rightSymbols == null ? true : false;
		this.index = -1; //表示未初始化
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSymbol.toString()+"->");
		if(rightSymbols == null) {
			sb.append("nil");
		} else {
			for(Symbol s : rightSymbols) {
				sb.append(s.toString());
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof Production) {
			return ((Production)obj).toString().equals(toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}

class ProductionList {
	/**
	 * 该类是产生式的集合类，包括了所有产生式
	 */
	
	List<Production> productions;
	private Set<Production> noRepeat; //用于去重的
	private Map<Symbol, Set<Integer>> symbol2ProductionIndex; //用于查找某个符号对应的产生式编号集合

//	/**
//	 * @param productionSet 产生式的集合，每个产生式的编号必须与其在List里的下标一致，并且产生式中不允许有重复
//	 */
//	public ProductionList(List<Production> productionList) {
//		this.productionList = productionList;
//		this.noRepeat.addAll(productionList);
//	}
	
	public ProductionList() {
		this.productions = new ArrayList<>();
		this.noRepeat = new HashSet<>();
		symbol2ProductionIndex = new HashMap<>();
	}
	
	/**
	 * 向产生式集中加入新的产生式
	 * @param p 加入的新产生式，该产生式的编号会被修改
	 * @return 若添加成功，则返回true；若该产生式已经在产生式集中，则返回false
	 */
	public boolean add(Production p) {
		if(noRepeat.contains(p)) return false;
		p.index = productions.size();
		productions.add(p);
		noRepeat.add(p);
		if(symbol2ProductionIndex.get(p.leftSymbol) == null)
			symbol2ProductionIndex.put(p.leftSymbol, new HashSet<>());
		symbol2ProductionIndex.get(p.leftSymbol).add(p.index);
		return true;
	}
	
	public Set<Production> symbol2Production(Symbol symbol) {
		Set<Integer> productionIndex = symbol2ProductionIndex.get(symbol);
		if(productionIndex == null) return null;
		Set<Production> result = new HashSet<>();
		for(Integer i : productionIndex) {
			result.add(productions.get(i));
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<productions.size(); i++) {
			sb.append("P"+i+": "+productions.get(i)+"\n");
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof Production) {
			return ((ProductionList)obj).toString().equals(toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}

class Project {
	/**
	 * 该类是一个项目类
	 * 每个项目中都含有一个产生式和一个位置信息
	 * leftSymbol代表产生式左部
	 * production=ArrayList<Symbol>代表产生式右部
	 * outlook表示展望符
	 * pos代表·的位置信息，空产生式则为-1
	 * productionIndex表示对应的产生式编号
	 * isNil表示该产生式是否是空产生式
	 * isReduce表示该项目是否是规约项目
	 * 如产生式A->·BCD
	 * 那么leftSymbol=A
	 * production=BCD
	 * pos=0
	 */
	
	Symbol leftSymbol;
	List<Symbol> production;
	Set<Symbol> outlook;
	int pos, productionIndex;
	boolean isNil, isReduce;
	
	/**
	 * 构造一个新的项目
	 * @param leftSymbol 产生式左部的符号
	 * @param production 产生式右部的符号List；若产生式为空，则传入null
	 * @param outlook 展望符集合
	 * @param pos 符号·所在的位置；如A->B·CD，pos=1；若是空产生式，则任意值均可(默认-1)
	 * @param productionIndex 该项目对应的产生式编号
	 */
	public Project(Symbol leftSymbol, List<Symbol> production, Set<Symbol> outlook, int pos, int productionIndex) {
		this.leftSymbol = leftSymbol;
		this.productionIndex = productionIndex;
		if(production == null) { //空产生式
			this.production = null;
			this.pos = -1; //表示空产生式
			this.isReduce = true;
			this.isNil = true;
		} else { //不是空产生式
			this.production = production;
			this.pos = pos;
			this.isReduce = this.pos < this.production.size() ? false : true;
			this.isNil = false;
		}
		this.outlook = outlook;
	}
	
	/**
	 * @return ·的下一个符号；如A->BC·D，将会返回D；若该项目是规约项目，则返回null
	 */
	public Symbol getPosSymbol() {
		if(isReduce) return null;
		return production.get(pos);
	}
	
	/**
	 * @return ·的下一个符号之后的符号序列，如A->B·CD，将会返回D；若该项目是规约项目，则返回null
	 */
	public List<Symbol> getBehindPosSymbols() {
		if(isReduce) return null;
		return production.subList(pos+1, production.size());
	}
	
	/**
	 * 判断两个状态是否是可合并的，即两个项目是否除了展望符都相同；
	 * 如A->B·C [a]和A->B·C [b]就可被合并
	 * @param p 另一个项目
	 * @return 可合并的返回true；否则返回false
	 */
	public boolean canMerge(Project p) {
		return p.toString().substring(0, p.toString().indexOf("[", 3)-1).equals(this.toString().substring(0, this.toString().indexOf("[", 3)-1));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj instanceof Project) {
			return ((Project)obj).toString().equals(toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(leftSymbol.toString()+"->");
		if(production == null) { //空产生式
			sb.append("·");
		} else {
			for(int i=0; i<production.size(); i++) {
				if(i == pos) sb.append("·");
				else sb.append(" ");
				sb.append(production.get(i).toString());
			}
			if(pos == production.size()) {
				sb.append("·");
			}
		}
		sb.append("   [");
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
	
	Set<Project> projects;
	int index;
	
	public ProjectSet(Set<Project> projects, int index) {
		this.projects = projects;
		this.index = index;
	}
	
	public ProjectSet(int index) {
		this.projects = new HashSet<>();
		this.index = index;
	}
	
	/**
	 * 向该项目集中加入新的项目p
	 * @param p 新的项目p
	 * @return 若添加成功，则返回true；若该项目已经在项目集中，则返回false
	 */
	public boolean add(Project p) {
		return projects.add(p);
	}

	public boolean addAll(Collection<? extends Project> c) {
		return projects.addAll(c);
	}
	
	/**
	 * 该状态集能够跳转的符号
	 * @return 能够跳转的符号集
	 */
	public Set<Symbol> canGoSymbols() {
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
	
	/**
	 * 合并一下项目集，如A->B·C [a]和A->B·C [b]会被合并成A->B·C [a b]
	 */
	public void merge() {
		if(projects == null || projects.size() == 0) return;
		
		Set<Project> resultMerge = new HashSet<>();
		Set<Project> rubbish = new HashSet<>(); //已经合并过的项目
		for(Project p1 : projects) {
			if(rubbish.contains(p1)) continue;
			Set<Symbol> mergeOutlook = new HashSet<>();
			mergeOutlook.add(p1.outlook.iterator().next());
			for(Project p2 : projects) {
				if(rubbish.contains(p2)) continue;
				if(p1.canMerge(p2)) {
					mergeOutlook.addAll(p2.outlook);
					rubbish.add(p2);
				}
			}
			rubbish.add(p1);
			Project mergeProject = new Project(p1.leftSymbol, p1.production, mergeOutlook, p1.pos, p1.productionIndex);
			resultMerge.add(mergeProject);
		}
		
		projects = resultMerge;
	}
	
	/**
	 * 查询项目集中是否包含某项目
	 * @param p 需要查询的项目p
	 * @return 若该项目集中包含p则返回true；否则返回false
	 */
	public boolean contains(Project p) {
		return projects.contains(p);
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
			return ((ProjectSet)o).toString().equals(toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}

class ProjectSetList {
	/**
	 * 该类记录所有的项目集以及它们的编号
	 */
	
	List<ProjectSet> projectSets;
	Set<ProjectSet> noRepeat; //用于去重
	
	/**
	 * @param productionSet 产生式的集合，每个产生式的编号必须与其在List里的下标一致，并且产生式中不允许有重复
	 */
	public ProjectSetList(List<ProjectSet> projectSets) {
		this.projectSets = projectSets;
		this.noRepeat.addAll(projectSets);
	}
	
	public ProjectSetList() {
		this.projectSets = new ArrayList<>();
		this.noRepeat = new HashSet<>();
	}
	
	/**
	 * 向项目集列表中加入新的项目集
	 * @param p 加入的新项目集，该项目集的编号会被修改
	 * @return 若添加成功，则返回true；若该产生式已经在产生式集中，则返回false
	 */
	public boolean add(ProjectSet p) {
		if(noRepeat.contains(p)) return false;
		p.index = projectSets.size();
		projectSets.add(p);
		noRepeat.add(p);
		return true;
	}

	public int size() {
		return projectSets.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<projectSets.size(); i++) {
			sb.append("  I"+i+":\n");
			for(Project p : projectSets.get(i).projects) {
				sb.append(p+"\n");
			}
			sb.append("\n");
		}
		sb.delete(sb.length()-2, sb.length());
		return sb.toString();
	}
	
}