package lexical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class LexFactory {
	/**
	 * 该工厂类用于根据转换文件来构造有穷自动机
	 * 支持根据NFA转换文件以及DFA转换文件
	 * 文件里每一行的格式为
	 * [状态 输入 非终结状态]
	 * [状态 输入 终结状态 <种别码,属性值>]
	 */
	
	/**
	 * 根据DFA转换表来构建有穷自动机
	 * @param filePath DFA转换表文件路径
	 * @return 生成好的有穷自动机
	 */
	public static DFA creator(String filePath) {
		//文件格式必须为[状态 输入 下一状态]
		File file = new File(filePath);
		
		List<Integer[]> table = new ArrayList<>();
		List<String> status = new ArrayList<>();
		Set<String> statusSet = new HashSet<>();
		Map<String, Integer> statusMap = new HashMap<>();
		Map<Integer, Token> tokenMap = new HashMap<>();
		
		//第0个状态一定是初始状态
		table.add(new Integer[94]);
		status.add("start");
		statusSet.add("start");
		statusMap.put("start", 0);
		int index = 1; //所以状态序列从1开始计算
		
		try(FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)) {
			String line = null;
			while((line=br.readLine()) != null) {
				//[状态 输入 下一状态 <种别码,属性值>(可选)]
				if(line.length() == 0 || line.charAt(0) == '#') continue;	//文本中的注释和空行不读取
				
				String[] segment = line.trim().split(" ");
				
				if(segment.length == 3) { //状态转移
					if(!statusSet.contains(segment[0])) { //转换表中没有保存该状态
						table.add(new Integer[94]);
						status.add(segment[0]);
						statusSet.add(segment[0]);
						statusMap.put(segment[0], index);
						index++;
					}
					if(!statusSet.contains(segment[2])) { //转换表中没有保存该状态
						table.add(new Integer[94]);
						status.add(segment[2]);
						statusSet.add(segment[2]);
						statusMap.put(segment[2], index);
						index++;
					}
					
					int without = segment[1].indexOf('-');
					Set<Character> withoutChars = null;
					
					if(without > 0) { //表示有需要排除的字符
						withoutChars = new HashSet<>();
						for(char c : segment[1].substring(without+2, segment[1].length()-1).toCharArray()) {
							withoutChars.add(c);
						}
						segment[1] = segment[1].substring(0, without);
					}
					
					switch(segment[1]) {
					case "any"://如果输入任何字符
						for(int i=32; i<=125; i++) {
							if((withoutChars != null && withoutChars.contains((char)i)) || i == 35) continue; //跳过排除的字符和空串'#'
							table.get(statusMap.get(segment[0]))[(char)(i-32)] = statusMap.get(segment[2]);
						}
						break;
					case "letter"://如果输入的是字母
						for(int i=65; i<=90; i++) {
							if(withoutChars == null || !withoutChars.contains((char)i)) //跳过排除的字符
								table.get(statusMap.get(segment[0]))[(char)(i-32)] = statusMap.get(segment[2]);
							if(withoutChars == null || !withoutChars.contains((char)(i+32))) //跳过排除的字符
								table.get(statusMap.get(segment[0]))[(char)i] = statusMap.get(segment[2]);
						}
						break;
					case "number"://如果输入的是数字
						for(int i=48; i<=57; i++) {
							if(withoutChars != null && withoutChars.contains((char)i)) continue; //跳过排除的字符
							table.get(statusMap.get(segment[0]))[(char)(i-32)] = statusMap.get(segment[2]);
						}
						break;
					case "$"://如果输入的是终止符
						table.get(statusMap.get(segment[0]))['$'-32] = statusMap.get(segment[2]);
						break;
					case "null"://如果输入的是空串
						table.get(statusMap.get(segment[0]))['#'-32] = statusMap.get(segment[2]);
						break;
					default:
						table.get(statusMap.get(segment[0]))[(segment[1]).charAt(0)-32] = statusMap.get(segment[2]);
						
					}
				} else {
					segment = line.split(";");
					if(segment.length != 2) { //格式有错
						System.out.println("文件中该行格式错误: "+line);
						continue;
					}
					
					Token token = new Token(segment[1]);
					tokenMap.put(statusMap.get(segment[0]), token);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return new DFA(new GotoTable(table, status, statusMap), tokenMap);
	}
	
	/**
	 * 根据NFA转换表来构建有穷自动机
	 * * @param filePath NFA转换表文件路径
	 * @return 生成好的有穷自动机
	 */
	public static DFA creatorUseNFA(String filePath) {
		//[状态 输入 下一状态 <种别码,属性值>(可选)]
		File file = new File(filePath);
		Map<String, Map<Character, Set<String>>> convertTable = new HashMap<>();
		Map<String, Token> tokenMap = new HashMap<>();
		Map<Token, Integer> tokenWeight = new HashMap<>();
		
		try(FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)) {
			String line = null;
			while((line=br.readLine()) != null) {
				//[状态 输入 下一状态 <种别码,属性值>(可选)]
				if(line.length() == 0 || line.charAt(0) == '#') continue;	//文本中的注释和空行不读取
				
				String[] segment = line.trim().split(" ");
				
				if(segment.length == 3) { //状态转移
					if(!convertTable.containsKey(segment[0])) { //转换表中没有保存该状态
						convertTable.put(segment[0], new HashMap<Character, Set<String>>());
					}
					if(!convertTable.containsKey(segment[2])) { //转换表中没有保存该状态
						convertTable.put(segment[2], new HashMap<Character, Set<String>>());
					}
					
					int without = segment[1].indexOf('-');
					Set<Character> withoutChars = null;
					
					if(without > 0) { //表示有需要排除的字符
						withoutChars = new HashSet<>();
						for(char c : segment[1].substring(without+2, segment[1].length()-1).toCharArray()) {
							withoutChars.add(c);
						}
						segment[1] = segment[1].substring(0, without);
					}

					switch(segment[1]) {
					case "any"://如果输入任何字符
						for(int i=32; i<=125; i++) {
							if((withoutChars != null && withoutChars.contains((char)i)) || i == 35) { //跳过排除的字符和空串'#'
//								System.out.println(segment[0]+"-x-x-"+(char)i+"-->"+segment[2]);
								continue;
							}
							addJumpToNFA(convertTable, segment[0], (char)i, segment[2]);
						}
						break;
					case "letter"://如果输入的是字母
						for(int i=65; i<=90; i++) {
							if(withoutChars == null || !withoutChars.contains((char)i)) //跳过排除的字符
								addJumpToNFA(convertTable, segment[0], (char)i, segment[2]);
							if(withoutChars == null || !withoutChars.contains((char)(i+32))) //跳过排除的字符
								addJumpToNFA(convertTable, segment[0], (char)(i+32), segment[2]);
						}
						break;
					case "number"://如果输入的是数字
						for(int i=48; i<=57; i++) {
							if(withoutChars != null && withoutChars.contains((char)i)) continue; //跳过排除的字符
							addJumpToNFA(convertTable, segment[0], (char)i, segment[2]);
						}
						break;
					case "$"://如果输入的是终止符
						addJumpToNFA(convertTable, segment[0], '$', segment[2]);
						break;
					case "null"://如果输入的是空串
						addJumpToNFA(convertTable, segment[0], '#', segment[2]);
						break;
					default:
						addJumpToNFA(convertTable, segment[0], segment[1].charAt(0), segment[2]);
					}
				} else { //绑定token
					segment = line.split(";");
					if(segment.length != 2 && segment.length != 3) { //格式有错
						System.out.println("文件中该行格式错误: "+line);
						continue;
					}
					
					Token token = new Token(segment[1]);
					tokenMap.put(segment[0], token);
					
					if(segment.length == 3) { //token有特定的优先度
						int weight = Integer.parseInt(segment[2]);
						tokenWeight.put(token, weight);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return NFA2DFA(convertTable, tokenMap, tokenWeight);
	}
	
	/**
	 * 向一个NFA转换表convertTable中添加新的跳转项
	 * @param convertTable NFA转换表
	 * @param curStatus 跳转前的状态
	 * @param c 遇到的字符c
	 * @param nextStatus 遇到字符c跳转后的状态
	 */
	private static void addJumpToNFA(Map<String, Map<Character, Set<String>>> convertTable,
			String curStatus, char c, String nextStatus) {
		if(!convertTable.get(curStatus).keySet().contains(c)) {
			convertTable.get(curStatus).put(c, new HashSet<String>());
		}
		convertTable.get(curStatus).get(c).add(nextStatus);
	}
	
	/**
	 * 从NFA转换到DFA
	 * @param NFAGotoTable
	 * @return
	 */
	private static DFA NFA2DFA(Map<String, Map<Character, Set<String>>> NFAGotoTable,
									 Map<String, Token> tokenMap,
									 Map<Token, Integer> tokenWeight) {
		List<Integer[]> table = new ArrayList<>();
		List<String> status = new ArrayList<>();
		Set<String> statusSet = new HashSet<>();
		Map<String, Integer> statusMap = new HashMap<>();
		
		Set<String> DstatesString = new HashSet<>();
		Stack<Set<String>> DstatesSet = new Stack<>();
		Map<Integer, Token> newTokenMap = new HashMap<>(); //作为转换后DFA的新的终结符对应的token表
		
		Set<String> startSet = closure(NFAGotoTable, "start");
		DstatesString.add(startSet.toString());
		DstatesSet.push(startSet);
		
		int index = 0;
		
		while(!DstatesSet.isEmpty()) {
			Set<String> T = DstatesSet.pop();
			
			int Tindex;
			if(!statusSet.contains(T.toString())) {
				table.add(new Integer[94]);
				status.add(T.toString());
				statusSet.add(T.toString());
				statusMap.put(T.toString(), index);
				Tindex = index;
				index++;
			} else {
				Tindex = statusMap.get(T.toString());
			}
			
			for(int i=32; i<=125; i++) {
				char c = (char)i;

				Set<String> U = closure(NFAGotoTable, move(T, NFAGotoTable, c));
				
				if(U == null) continue;
				
				if(!statusSet.contains(U.toString())) { //还没有状态U
					table.add(new Integer[94]);
					status.add(U.toString());
					statusSet.add(U.toString());
					statusMap.put(U.toString(), index);
					index++;
				}
				
				if(!DstatesString.contains(U.toString())) {
					DstatesSet.push(U);
					DstatesString.add(U.toString());
				}
				
				table.get(Tindex)[i-32] = statusMap.get(U.toString()); //T就是上一个状态，(char)i是输入字符，U是能达到的下一个状态
				
				//若状态集U中含有终结状态，那么就找到其中优先值最大的终结符的token作为状态集U的token
				Token maxWeightToken = null; //U中优先度最高的token
				int maxWeight = Integer.MIN_VALUE;
				for(String s : U) {
					Token temp = tokenMap.get(s);
					if(temp != null) { //U中有终结状态
						int tempWeight = tokenWeight.containsKey(temp) ? tokenWeight.get(temp) : 0; //该终结状态的优先值
						if(maxWeight < tempWeight) {//U中的终结符优先值更高
							maxWeightToken = temp;
							maxWeight = tempWeight;
						}
					}
				}
				//U中的确有终结状态，且优先度最高的token为maxWeightToken
				if(maxWeightToken != null) {
					newTokenMap.put(statusMap.get(U.toString()), maxWeightToken);
				}
			}
		}
		
		return new DFA(new GotoTable(table, status, statusMap), newTokenMap);
	}
	
	/**
	 * 计算某个状态的ε闭包
	 * @param NFAGotoTable NFA转换表
	 * @param status 需要计算ε闭包的状态
	 * @return 状态status的ε闭包
	 */
	private static Set<String> closure(Map<String, Map<Character, Set<String>>> NFAGotoTable, String status) {
		Set<String> tempSet = new HashSet<>();
		tempSet.add(status);
		return closure(NFAGotoTable, tempSet);
	}
	
	/**
	 * 计算某个状态集的ε闭包
	 * @param NFAGotoTable NFA转换表
	 * @param status 需要计算ε闭包的状态集
	 * @return 状态集status的ε闭包
	 */
	private static Set<String> closure(Map<String, Map<Character, Set<String>>> NFAGotoTable, Set<String> status) {
		if(status == null || status.size() == 0) return null;
		
		Set<String> result = new HashSet<>();
		Stack<String> stack = new Stack<>();
		
		for(String s : status) {
			stack.push(s);
			result.add(s);
		}
		
		while(!stack.isEmpty()) {
			String t = stack.pop();
			Set<String> tClosure = NFAGotoTable.get(t).get('#');
			
			if(tClosure == null || tClosure.size() == 0) continue;
			
			for(String s : tClosure) {
				if(!result.contains(s)) {
					stack.push(s);
					result.add(s);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 在指定状态集T、指定NFA状态转换表、指定输入字符c的情况下，
	 * 得到能够到达的状态集
	 * @param T 当前的状态集
	 * @param NFAGotoTable NFA状态转换表
	 * @param c 输入的一个字符c，输入空时则为'#'
	 * @return 能从T(状态集)中的某个状态输入字符c后能够达到的状态集；若不存在则返回null
	 */
	private static Set<String> move(Set<String> T, Map<String, Map<Character, Set<String>>> NFAGotoTable, char c) {
		Set<String> result = new HashSet<>();
		for(String s : T) {
			Set<String> sNextStatus = NFAGotoTable.get(s).get(c);
			if(sNextStatus != null)
				result.addAll(sNextStatus);
		}
		if(result.size() == 0) return null;
		
		return result;
	}
}

