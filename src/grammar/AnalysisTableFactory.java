package grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.sun.prism.PhongMaterial.MapType;

public class AnalysisTableFactory {
	/**
	 * 该类从文件中读取文法，并生成LALR分析的分析表
	 */
	
	/**
	 * 从文件中读取文法，并生成分析表
	 * @param path 文件路径
	 * @return LALR分析表
	 */
	public static AnalysisTable creator(String path) {
		List<Map<Symbol, Item>> table = new ArrayList<>(); //记录分析表，格式详情见AnalysisTable类
		List<Production> productions = new ArrayList<>(); //记录所有的产生式
		Map<Production, Integer> productionIndex = new HashMap<>(); //记录产生式对应的标号
		Map<Symbol, Set<List<Symbol>>> productionMap = new HashMap<>(); //记录非终结符和产生式右部的关系
		Map<String, Symbol> symbolMap = new HashMap<>(); //记录String和Symbol的关系
		Map<Symbol, Set<Symbol>> firstSet = null; //记录每个非终结符对应的FIRST集
		List<ProjectSet> projectSetList = new ArrayList<>(); //记录项目集合
		
		//文件的格式为:A->B c D
		File file = new File(path);
		try(FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)) {
			String line = null;
			while((line=br.readLine()) != null) {
				if(line.length() == 0 || line.charAt(0) == '#') continue;	//文本中的注释和空行不读取
				
				int index = line.indexOf("->");
				String left = line.substring(0, index);
				Symbol leftSymbol = symbolMap.get(left);
				if(leftSymbol == null) { //第一次读到该非终结符的产生式
					leftSymbol = new Symbol(false, left);
					symbolMap.put(left, leftSymbol);
					productionMap.put(leftSymbol, new HashSet<>());
				} else if(leftSymbol.isFinal() == true) { //被错误地设置成了终结符
					leftSymbol.setIsFinal(false);
					productionMap.put(leftSymbol, new HashSet<>());
				}
				
				String rightStr = line.substring(index+2).trim();
				List<Symbol> rightSymbols = new ArrayList<>();
				String right[] = rightStr.split(" ");
					
				for(String s : right) {
					Symbol rightSymbol = symbolMap.get(s);
					if(rightSymbol == null) {
						rightSymbol = new Symbol(true, s);
						symbolMap.put(s, rightSymbol);
					}
					rightSymbols.add(rightSymbol);
				}
				Production newProduction = new Production(leftSymbol, rightSymbols);
				productions.add(newProduction);
				productionIndex.put(newProduction, productions.size()-1);
				productionMap.get(leftSymbol).add(rightSymbols);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		firstSet = FirstSet(productions);
		
		Set<Symbol> startLookOut = new HashSet<>();
		startLookOut.add(new Symbol(true, "$"));
		Project start = new Project(productions.get(0).leftSymbol, productions.get(0).rightSymbols, startLookOut, 0);
		Set<Project> startProject = new HashSet<>();
		startProject.add(start);
		startProject = CLOSURE(firstSet, productionMap, startProject);
		ProjectSet startProjectSet = new ProjectSet(startProject);
		Map<Integer, Map<Symbol, Integer>> GOTOtable = new HashMap<>(); //存储每个项目集之间的跳转
		Map<String, Integer> indexMap = new HashMap<>(); //记录每个项目集在projects中的编号
		
		projectSetList.add(startProjectSet);
		Set<String> projectStr = new HashSet<>(); //用来去重的项目集，与projects等价
		projectStr.add(startProjectSet.toString());
		Queue<ProjectSet> queue = new LinkedList<>();
		indexMap.put(startProjectSet.toString(), 0);
		queue.offer(startProjectSet);
		
		while(!queue.isEmpty()) {
			ProjectSet I = queue.poll(); //C中的每个项集I
			for(Symbol X : I.canGoto()) { //每个文法符号X
				ProjectSet nextProjectSet = new ProjectSet(GOTO(firstSet, productionMap, I.getProjects(), X));
				if(!projectStr.contains(nextProjectSet.toString())) { //GOTO(I, X)非空且不在C中
					projectSetList.add(nextProjectSet);
					projectStr.add(nextProjectSet.toString());
					queue.offer(nextProjectSet);
					indexMap.put(nextProjectSet.toString(), projectSetList.size()-1);
					table.add(new HashMap<>());
				}
				if(GOTOtable.containsKey(indexMap.get(I.toString()))) { //跳转表中有I了
					GOTOtable.get(indexMap.get(I.toString())).put(X, indexMap.get(nextProjectSet.toString()));
				} else { //跳转表中还没I
					Map<Symbol, Integer> mapTemp = new HashMap<>();
					mapTemp.put(X, projectSetList.size()-1);
					GOTOtable.put(indexMap.get(I.toString()), mapTemp);
				}
			}
		}
		
		for(int i=0; i<table.size(); i++) {
			Set<Symbol> canGo = GOTOtable.get(i).keySet(); //从这个状态集能够接受啥样的符号
			for(Symbol s : canGo) {
				Item item = null;
				if(s.isFinal()) { //该符号是终结符
					item = new Item(-1, GOTOtable.get(i).get(s));
				} else { //该符号是非终结符
					item = new Item(0, GOTOtable.get(i).get(s));
				}
				table.get(i).put(s, item);
				for(Project p : projectSetList.get(i).projects) { //看看项目集中有没有可归约的项目集
					if(p.isReduce()) { //可归约的项目
						for(Symbol outlook : p.getOutlook()) { //把每一个展望符加入到表中
							Item item2 = new Item(1, productionIndex.get(p.production()));
							table.get(i).put(outlook, item2);
						}
					}
				}
			}
		}
		
		for(int i=0; i<projectSetList.size(); i++)
			System.out.println("  I"+i+":\n"+projectSetList.get(i).toString()+"\n");
		
		for(Integer i : GOTOtable.keySet()) {
			System.out.println("  I"+i);
			for(Symbol b : GOTOtable.get(i).keySet()) {
				System.out.println("--"+b+"-> I"+GOTOtable.get(i).get(b));
			}
		}
		
		
		return new AnalysisTable(table);
	}
	
	/**
	 * 计算所有符号的FIRST集
	 * @param productions 所有的产生式集合
	 * @return 所有符号的FIRST集
	 */
	private static Map<Symbol, Set<Symbol>> FirstSet(List<Production> productions) {
		Map<Symbol, Set<Symbol>> firstMap = new HashMap<>();
		Set<Production> set = new HashSet<>(); //右部以非终结符打头且无左递归的产生式
		Symbol nilSymbol = null; //空产生式的符号
		
		for(Production p : productions) {
			if(!firstMap.containsKey(p.leftSymbol)) {
				firstMap.put(p.leftSymbol, new HashSet<>());
			}
			if(p.rightSymbols.get(0).isFinal()) { //右部以终结符打头，则将该终结符加入到左部的FIRST集中
				firstMap.get(p.leftSymbol).add(p.rightSymbols.get(0));
				if(nilSymbol == null && p.rightSymbols.get(0).getName().equals("nil")) {
					nilSymbol = p.rightSymbols.get(0);
				}
			} else if(!p.leftSymbol.equals(p.rightSymbols.get(0))) { //非左递归
				set.add(p);
			}
		}
		
		if(nilSymbol == null)
			nilSymbol = new Symbol(true, "nil");
		
		boolean flag = true;
		while(flag) {
			flag = false;
			for(Production p : set) {
				for(Symbol s : p.rightSymbols) {
					if(firstMap.get(s).size() > 0) {
						flag = firstMap.get(p.leftSymbol).addAll(firstMap.get(s)); //直到FIRST集不再发生变化
						if(!firstMap.get(s).contains(nilSymbol))
							break;
					} else {
						break;
					}
				}
			}
		}
		
//		for(Symbol s : firstMap.keySet())
//			System.out.println("FIRST("+s+") = "+firstMap.get(s));
		
		return firstMap;
	}
	
	/**
	 * 计算符号串的FIRST集
	 * @param firstSet 已经计算好的所有单个符号的FIRST集
	 * @param symbols 需要计算FIRST集的符号串
	 * @return 所有符号的FIRST集
	 */
	private static Set<Symbol> First(Map<Symbol, Set<Symbol>> firstSet, List<Symbol> symbols) {
		Set<Symbol> result = new HashSet<>();
		Symbol nilSymbol = new Symbol(true, "nil");
		for(Symbol s : symbols) {
			Set<Symbol> temp = firstSet.get(s);
			if(temp == null) { //s是终结符
				result.add(s);
				break;
			}
			result.addAll(temp);
			if(!temp.contains(nilSymbol))
				break;
		}
		
		return result;
	}
	
	private static Set<Project> CLOSURE(Map<Symbol, Set<Symbol>> firstSet, Map<Symbol, Set<List<Symbol>>> productionMap, Set<Project> I) {
		Set<Project> result = new HashSet<>();
		Queue<Project> queue = new LinkedList<>();
		
		result.addAll(I);
		queue.addAll(I);
		Symbol nilSymbol = new Symbol(true, "nil");
		while(!queue.isEmpty()) {
			Project A = queue.poll();
			if(A.isReduce()) continue; //规约项目
			Symbol B = A.getPosSymbol();
			if(!B.isFinal()) { //非终结符
				for(List<Symbol> l : productionMap.get(B)) { //G'的每个产生式B->γ
					List<Symbol> list = new ArrayList<>();
					Set<Symbol> FIRST = new HashSet<>(); //FIRST(βa)
					if(A.getBehindPos() != null) {
						list.addAll(A.getBehindPos());
						FIRST = First(firstSet, list);
					}
					if(FIRST.size() == 0 || FIRST.contains(nilSymbol)) {
						FIRST.remove(nilSymbol);
						FIRST.addAll(A.getOutlook());
					}
					for(Symbol b : FIRST) { //FIRST(βa)中的每个符号b
						Set<Symbol> outlook = new HashSet<>();
						outlook.add(b);
						Project newProject = new Project(B, l, outlook, 0);
						if(result.add(newProject)) { //将[B->·γ, b]加入到集合result中
							queue.offer(newProject);
						}
					}
				}
			}
		}
		
		return compress(result);
	}
	
	public static Set<Project> GOTO(Map<Symbol, Set<Symbol>> firstSet, Map<Symbol, Set<List<Symbol>>> productionMap, Set<Project> I, Symbol X) {
		Set<Project> result = new HashSet<>();
		for(Project p : I) {
			if(p.isReduce() || !p.getPosSymbol().equals(X)) continue; //规约项目没有后继项目集或·后的符号不是X
			result.add(new Project(p.getLeftSymbol(), p.getProduction(), p.getOutlook(), p.getPos()+1));
		}
		
		return compress(CLOSURE(firstSet, productionMap, result));
	}
	
	/**
	 * 压缩一下项目集，如A->B·C [a]和A->B·C [b]会被压缩成A->B·C [a b]
	 * @return 压缩后等价的项目集
	 */
	public static Set<Project> compress(Set<Project> projectSet) {
		if(projectSet == null || projectSet.size() == 0) return null;
		
		Set<Project> resultMerge = new HashSet<>();
		Set<Project> rubbish = new HashSet<>(); //已经合并过的项目
		for(Project p1 : projectSet) {
			if(rubbish.contains(p1)) continue;
			Set<Symbol> mergeOutlook = new HashSet<>();
			mergeOutlook.add(p1.getOutlook().iterator().next());
			for(Project p2 : projectSet) {
				if(rubbish.contains(p2)) continue;
				if(p1.canMerge(p2)) {
					mergeOutlook.addAll(p2.getOutlook());
					rubbish.add(p2);
				}
			}
			rubbish.add(p1);
			Project mergeProject = new Project(p1.getLeftSymbol(), p1.getProduction(), mergeOutlook, p1.getPos());
			resultMerge.add(mergeProject);
		}
		
//		for(Project p : resultMerge) {
//			System.out.println(p);
//		}
		
		return resultMerge;
	}
	
	public static void main(String[] args) {
		creator("test.txt");
	}
}