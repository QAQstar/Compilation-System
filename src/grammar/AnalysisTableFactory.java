package grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import lexical.DFA;
import lexical.DFAFactory;
import lexical.Token;

public class AnalysisTableFactory {
	/**
	 * 该类从文件中读取文法，并生成LALR分析的分析表
	 */
	
	private static Symbol nilSymbol = new Symbol("nil", true); //空串符号
	
	/**
	 * 从文件中读取文法，并生成分析表
	 * @param grammarPath 文法文件路径
	 * @param FAPath FA文件路径
	 * @return LR分析表
	 */
	public static AnalysisTable creator(String grammarPath, String FAPath) {
		List<Map<Symbol, Item>> table = new ArrayList<>(); //记录分析表，格式详情见AnalysisTable类
		ProductionList productions = new ProductionList(); //所有的产生式集
		ProjectSetList projectSetList = new ProjectSetList(); //所有的项目集(项目集列表)
		
		Map<String, Symbol> str2Symbol = new HashMap<>(); //记录String和Symbol的关系
		Map<Symbol, Set<Symbol>> firstSet = null; //记录每个非终结符对应的FIRST集
		
		Map<Symbol, Token> symbol2Token = new HashMap<>(); //记录终结符和Token的关系
		
		//文件的格式为:A->B c D
		File file = new File(grammarPath);
		try(FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)) {
			String line = null;
			while((line=br.readLine()) != null) {
				if(line.length() == 0 || line.charAt(0) == '#') continue;	//文本中的注释和空行不读取
				if(line.charAt(line.length()-1) == '>') { //终结符对应Token序列
					int index = line.indexOf(':');
					String symbolStr = line.substring(0, index);
					Token token = new Token(line.substring(index+1));
					symbol2Token.put(str2Symbol.get(symbolStr), token);
				} else {
					int index = line.indexOf("->");
					String left = line.substring(0, index);
					Symbol leftSymbol = str2Symbol.get(left);
					if(leftSymbol == null) { //第一次读到该非终结符的产生式
						leftSymbol = new Symbol(left, false);
						str2Symbol.put(left, leftSymbol);
					} else if(leftSymbol.isFinal() == true) { //被错误地设置成了终结符
						leftSymbol.setIsFinal(false);
					}
					
					String rightStr = line.substring(index+2).trim();
					List<Symbol> rightSymbols = new ArrayList<>();
					String right[] = rightStr.split(" ");
						
					for(String s : right) {
						Symbol rightSymbol = str2Symbol.get(s);
						if(rightSymbol == null) {
							rightSymbol = new Symbol(s, true);
							str2Symbol.put(s, rightSymbol);
						}
						rightSymbols.add(rightSymbol);
					}
					if(rightSymbols.get(0).getName().equals("nil")) //空产生式
						rightSymbols = null;
					Production newProduction = new Production(leftSymbol, rightSymbols);
					productions.add(newProduction);
				}
				
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		firstSet = FirstSet(productions);
		
		Set<Symbol> startLookOut = new HashSet<>();
		startLookOut.add(new Symbol("$", true));
		Project startProject = new Project(productions.productions.get(0).leftSymbol, productions.productions.get(0).rightSymbols, startLookOut, 0, 0);
		ProjectSet startProjectSet = new ProjectSet(0);
		startProjectSet.add(startProject);
		startProjectSet = CLOSURE(firstSet, productions, startProjectSet, 0);
		projectSetList.add(startProjectSet);
		int projectSetIndex = 1;
		
		Map<Integer, Map<Symbol, Integer>> GOTOtable = new HashMap<>(); //存储每个项目集之间的跳转
		Map<String, Integer> projectSetStr2Index = new HashMap<>(); //记录每个项目集在projectSetList中的编号
		Set<String> projectStr = new HashSet<>(); //用来去重的项目集，与projectSetList等价
		projectStr.add(startProjectSet.toString());
		
		Queue<ProjectSet> queue = new LinkedList<>();
		projectSetStr2Index.put(startProjectSet.toString(), 0);
		queue.offer(startProjectSet);
		
		while(!queue.isEmpty()) {
			ProjectSet I = queue.poll(); //C中的每个项集I
			for(Symbol X : I.canGoSymbols()) { //每个文法符号X
				ProjectSet nextProjectSet = GOTO(firstSet, productions, I, X, projectSetIndex); //形成一个新的后继项目集
				
				if(!projectStr.contains(nextProjectSet.toString())) { //GOTO(I, X)非空且不在C中
					projectSetList.add(nextProjectSet);
					projectStr.add(nextProjectSet.toString());
					queue.offer(nextProjectSet);
					projectSetStr2Index.put(nextProjectSet.toString(), projectSetIndex);
					projectSetIndex++; //项目集编号自增
					table.add(new HashMap<>());
				}
				if(GOTOtable.containsKey(projectSetStr2Index.get(I.toString()))) { //跳转表中有I了
					GOTOtable.get(projectSetStr2Index.get(I.toString())).put(X, projectSetStr2Index.get(nextProjectSet.toString()));
				} else { //跳转表中还没I
					Map<Symbol, Integer> mapTemp = new HashMap<>();
					mapTemp.put(X, projectSetList.size()-1);
					GOTOtable.put(projectSetStr2Index.get(I.toString()), mapTemp);
				}
			}
		}
		
		boolean isFindFinalStatus = false; //是否找到了移入$能够接收
		for(int i=0; i<table.size(); i++) {
			if(GOTOtable.get(i) == null) { //没有后继状态
				if(!isFindFinalStatus &&
				   projectSetList.projectSets.get(i).projects.size() == 1 &&
				   projectSetList.projectSets.get(i).projects.iterator().next().isNextProject(startProject)) { //为接收时赋值
					isFindFinalStatus = true;
					Item item = new Item(null, -1);
					table.get(i).put(new Symbol("$", true), item);
				}
				continue;
			}
			Set<Symbol> canGo = GOTOtable.get(i).keySet(); //从这个状态集能够接受啥样的符号
			for(Project p : projectSetList.projectSets.get(i).projects) { //看看项目集中有没有可归约的项目集
				if(p.isReduce) { //可归约的项目
					for(Symbol outlook : p.outlook) { //把每一个展望符加入到表中
						Item item = new Item(ItemType.REDUCE, p.productionIndex);
						table.get(i).put(outlook, item);
					}
				}
			}
			for(Symbol s : canGo) {
				Item item = null;
				if(s.isFinal()) { //该符号是终结符
					item = new Item(ItemType.SHIFT, GOTOtable.get(i).get(s));
				} else { //该符号是非终结符
					item = new Item(ItemType.GOTO, GOTOtable.get(i).get(s));
				}
				table.get(i).put(s, item);
			}
		}
		
		System.out.println(productions);
		
		System.out.println(projectSetList);
		
		for(int i=0; i<table.size(); i++) {
			System.out.println("  "+i+":");
			for(Symbol s : table.get(i).keySet()) {
				System.out.println(s+": "+table.get(i).get(s));
			}
			System.out.println();
		}
		
		DFA dfa;
		if(FAPath.charAt(FAPath.length()-3) == 'n') { //NFA
			dfa = DFAFactory.creatorUseNFA(FAPath);
		} else { //DFA
			dfa = DFAFactory.creator(FAPath);
		}
		
		return new AnalysisTable(table, productions, projectSetList, symbol2Token, dfa);
	}
	
	/**
	 * 计算所有符号的FIRST集
	 * @param productions 所有的产生式集合
	 * @return 所有符号的FIRST集
	 */
	private static Map<Symbol, Set<Symbol>> FirstSet(ProductionList productions) {
		Map<Symbol, Set<Symbol>> firstMap = new HashMap<>();
		Set<Production> set = new HashSet<>(); //右部以非终结符打头且无左递归的产生式
		
		for(Production p : productions.productions) {
			if(!firstMap.containsKey(p.leftSymbol)) {
				firstMap.put(p.leftSymbol, new HashSet<>());
			}
			if(p.isNil) { //空产生式
				firstMap.get(p.leftSymbol).add(nilSymbol);
			} else if(p.rightSymbols.get(0).isFinal()) { //右部以终结符打头
				firstMap.get(p.leftSymbol).add(p.rightSymbols.get(0));
			} else if(!p.leftSymbol.equals(p.rightSymbols.get(0))) { //非左递归
				set.add(p);
			}
		}

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
		
		//打印FIRST集
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
	
	/**
	 * 求项目I的闭包
	 * @param firstSet 所有非终结符的FIRST集
	 * @param productions 产生式集合
	 * @param I 需要求闭包的项目集
	 * @param index 该项目集的编号
	 * @return 经过合并后的项目集I的闭包
	 */
	private static ProjectSet CLOSURE(Map<Symbol, Set<Symbol>> firstSet, ProductionList productions, ProjectSet I, int index) {
		ProjectSet result = new ProjectSet(index);
		Queue<Project> queue = new LinkedList<>();
		
		result.addAll(I.projects);
		queue.addAll(I.projects);

		while(!queue.isEmpty()) {
			Project A = queue.poll();
			if(A.isReduce) continue; //规约项目不会有
			Symbol B = A.getPosSymbol();
			if(!B.isFinal()) { //非终结符
				for(Production Bproduction : productions.symbol2Production(B)) { //G'的每个产生式B->γ
					List<Symbol> list = new ArrayList<>();
					Set<Symbol> FIRST = new HashSet<>(); //FIRST(βa)
					if(A.getBehindPosSymbols() != null) {
						list.addAll(A.getBehindPosSymbols());
						FIRST = First(firstSet, list);
					}
					if(FIRST.size() == 0 || FIRST.contains(nilSymbol)) {
						FIRST.remove(nilSymbol);
						FIRST.addAll(A.outlook);
					}
					for(Symbol b : FIRST) { //FIRST(βa)中的每个符号b
						Set<Symbol> outlook = new HashSet<>();
						outlook.add(b);
						Project newProject = new Project(B, Bproduction.rightSymbols, outlook, 0, Bproduction.index);
						if(result.add(newProject)) { //将[B->·γ, b]加入到集合result中
							queue.offer(newProject);
						}
					}
				}
			}
		}
		result.merge();
		
		return result;
	}
	
	/**
	 * 某个项目集I输入符号X后的后继项目集
	 * @param firstSet 所有非终结符的FIRST集
	 * @param productions 所有产生式的列表
	 * @param I 跳转前项目集
	 * @param X 输入符号X
	 * @param index 跳转后的项目集编号index
	 * @return 跳转后项目集
	 */
	public static ProjectSet GOTO(Map<Symbol, Set<Symbol>> firstSet, ProductionList productions, ProjectSet I, Symbol X, int index) {
		ProjectSet result = new ProjectSet(index);
		for(Project p : I.projects) {
			if(p.isReduce || !p.getPosSymbol().equals(X)) continue; //规约项目没有后继项目集或·后的符号不是X
			result.add(new Project(p.leftSymbol, p.production, p.outlook, p.pos+1, p.productionIndex));
		}
		
		result = CLOSURE(firstSet, productions, result, index);
		result.merge();
		
		return result;
	}
	
	public static void main(String[] args) {
		creator("test.txt", "NFA.nfa");
	}
}