package grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import lexical.Token;

public class GrammarTree {
	/**
	 * 作为注释分析树中的一个节点
	 * symbol 它对应的符号
	 * token 它对应的token，若是非终结符则为null
	 * lineNumber 对应的行号
	 * productionIndex 该符号所对应的产生式编号，若该符号是终结符，则为-1
	 * children 它的孩子节点列表，倒序
	 * property 它的属性，包括继承属性和综合属性
	 * isVisited 用来递归遍历的时候作为是否访问过的标记
	 */
	
	public Symbol symbol;
	public Token token;
	public int lineNumber, productionIndex;
	public List<GrammarTree> children;
	public Map<String, Object> property;
	public boolean isVisited = false;
	
	public GrammarTree(Symbol symbol, Token token) {
		this.symbol = symbol;
		this.token = token;
		if(token == null) {
			lineNumber = -1;
			this.property = new HashMap<>();
		} else {
			this.lineNumber = token.getLineNumber();
		}
		this.productionIndex = -1;
		this.children = null;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}
	
	public int lineNumber() {
		return lineNumber;
	}
	
	public void addChild(GrammarTree child) {
		if(children == null) {
			children = new ArrayList<>();
		}
		if(child.lineNumber != -1) { //父节点符号的行号为第一个非空产生式的子节点的行号
			this.lineNumber = child.lineNumber;
		}
		children.add(child);
	}
	
	public void setProductionIndex(int productionIndex) {
		this.productionIndex = productionIndex;
	}
	
	public String getResultString() {
		StringBuffer sb = new StringBuffer();
		Stack<GrammarTree> stack = new Stack<>();
		stack.push(this);
		StringBuffer tab = new StringBuffer();
		
		while(!stack.isEmpty()) {
			GrammarTree top = stack.peek();
			if(top.isVisited) { //子树已经压入栈中
				stack.pop();
				top.isVisited = false; //重置
				tab.delete(tab.length()-2, tab.length());
			} else { //子树还没压入栈
				if(top.children == null) { //终结符或者是空产生式
					if(top.symbol.isFinal()) { //终结符
						sb.append(tab.toString()+top.symbol.getName()+"("+top.lineNumber+"):"+top.token.forGrammar()+"\n");
						top.isVisited = false;
					} else { //空产生式
						sb.append(tab.toString()+top.symbol.getName()+"("+top.lineNumber+")\n");
						sb.append(tab.toString()+"  nil\n");
						top.isVisited = false;
					}
					stack.pop();
				} else { //非空产生式
					sb.append(tab.toString()+top.symbol.getName()+"("+top.lineNumber+")\n");
					tab.append("  ");
					top.isVisited = true;
					for(GrammarTree st : top.children) { //展开子树
						stack.push(st);
					}
				}
			}
		}
		sb.delete(sb.length()-1, sb.length());//去掉最后的换行
		return sb.toString();
	}
	
	public String getGraphvizCode() {
		StringBuffer sb = new StringBuffer("digraph GrammarTree {\n    node [shape=circle]\n");
		class GrammarTreeAndIndex {
			GrammarTree gt;
			int index;
			public GrammarTreeAndIndex(GrammarTree gt, int index) {
				this.gt = gt;
				this.index = index;
			}
		}
		
		Queue<GrammarTreeAndIndex> queue = new LinkedList<>();
		queue.offer(new GrammarTreeAndIndex(this, 0));
		int index = 1;
		String tab = "    ";
		sb.append(tab+"0 [label=\" "+this.symbol.getName()+"("+this.lineNumber+")\"]\n");
		
		while(!queue.isEmpty()) {
			GrammarTreeAndIndex top = queue.poll();
			if(top.gt.children != null) { //非空产生式
				for(int i=top.gt.children.size()-1; i>=0; i--) {
					GrammarTree child = top.gt.children.get(i);
					if(child.children == null) { //空产生式或终结符
						if(child.symbol.isFinal()) { //终结符
							sb.append(tab+index+" [fontcolor=blue,shape=none,label=\" "+child.symbol.getName()+"("+child.lineNumber+"):"+child.token.forGrammar()+"\"]\n");
							sb.append(tab+top.index+"->"+index+"\n");
						} else { //空产生式
							sb.append(tab+index+" [label=\" "+child.symbol.getName()+"("+child.lineNumber+")\"]\n");
							sb.append(tab+top.index+"->"+index+"\n");
							sb.append(tab+(index+1)+" [fontcolor=blue,shape=none,label=\" nil\"]\n");
							sb.append(tab+index+"->"+(index+1));
							index++;
						}
					} else { //非空产生式
						GrammarTreeAndIndex childTemp = new GrammarTreeAndIndex(child, index);
						queue.offer(childTemp);
						sb.append(tab+index+" [label=\" "+child.symbol.getName()+"("+child.lineNumber+")\"]\n");
						sb.append(tab+top.index+"->"+index+"\n");
					}
					index++;
				}
			}
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return symbol.toString();
	}
}