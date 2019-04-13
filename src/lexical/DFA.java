package lexical;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DFA {
	/**
	 * 该类主要是维护一个转换表
	 * 输入当前状态和输入，输出下一状态
	 */
	
	//记录词法分析时读到哪个字符了
	private int index = 0;
	private int startIndex = 0; //上一次开始状态时读取到的字符
	private int lineNumber = 1; //记录当前的行号
	private char[] chars = null;
	
	private StringBuilder path = null; //记录状态转移路径
	
	//转换表的格式为<状态, <输入, 下一状态>>
	private GotoTable gotoTable = null;
	
	//终止状态表，记录了终止状态序号、token序列
	//注意token里存的是<种别码，属性值的正则表达式>
	private Map<Integer, Token> tokenMap = null;
	
	/**
	 * 构造有穷自动机
	 * @param gotoTable 转换表
	 * @param tokenMap 终结状态序号对应的Token序列
	 * @param isDFA 若传入的是由DFA原生构建的转换表，则为true；若是由NFA构建的转换表，则为false
	 */
	protected DFA(GotoTable gotoTable, Map<Integer, Token> tokenMap) {
		this.gotoTable = gotoTable;
		this.tokenMap = tokenMap;
		this.lineNumber = 1;
		this.path = new StringBuilder(gotoTable.getStatus(0));
	}
	
	/**
	 * 初始化DFA，向DFA的输入流中写入待分析的字符串
	 * @param str 待分析的字符串
	 */
	public void init(String str) {
		index = 0;
		startIndex = 0;
		lineNumber = 1;
		chars = (str+'$').toCharArray(); //在代码末尾自动添加一个终止符
	}
	
	/**
	 * 在初始化分析字符串后，返回下一个token
	 * @return 下一个token；分析结束或错误后返回null
	 */
	public Token getNext() {
		int curStatus = 0; //记录当前状态，开始时初始状态的序号为0
		for(; index < chars.length; index++) {
			if(chars[index] == ' ' || chars[index] == '\n' || chars[index] == '\t'|| chars[index] == '\r') { //将所有的回车、空格和制表符换成终止符$
				if(chars[index] == '\n') lineNumber++;
				chars[index] = '$';
			}
			
			curStatus = gotoTable.nextStatus(curStatus, chars[index]); //执行跳转动作
			
			if(curStatus == -1) { //没有此跳转，输出一个代表错误的token
				String morpheme = String.valueOf(chars, startIndex, index-startIndex+1);
				Token errorToken = new Token(morpheme, "ERROR", String.valueOf(lineNumber), null, lineNumber);
				index++;
				startIndex = index;
				curStatus = 0;
				return errorToken;
			}
			
			if(curStatus == 0) { //跳转的时候遇到空格
				startIndex = index+1;
				continue;
			}
			
			path.append("-"+chars[index]+"->"+gotoTable.getStatus(curStatus)); //正常跳转，并将该跳转添加到路径中
			
			//跳转后的状态是终结状态并且如果还有下一个状态，那么就继续走，不返回当前的Token
			if(tokenMap.containsKey(curStatus)) {
				if(index < chars.length-1) {
					if(chars[index+1] == ' ' || chars[index+1] == '\n' || chars[index+1] == '\t' || chars[index+1] == '\r') //将所有的回车、空格和制表符换成终止符$
						if(gotoTable.nextStatus(curStatus, '$') != -1)//如果还有下一个状态，那么就继续走，不返回当前的Token
							continue;
					if(gotoTable.nextStatus(curStatus, chars[index+1]) != -1)//如果还有下一个状态，那么就继续走，不返回当前的Token
						continue;
				}
				
				Token tokenType = tokenMap.get(curStatus); //取出该终结状态对应的token类
				Token result = null;
				String morpheme = String.valueOf(chars, startIndex, index-startIndex+1);
				if(tokenType.getValue() != null) { //此时属性值非空
					Pattern pattern = Pattern.compile(tokenType.getValue());
					Matcher matcher = pattern.matcher(morpheme);
					matcher.find();
					result = new Token(morpheme, tokenType.getType(), matcher.group(matcher.groupCount()), path.toString(), lineNumber);
				} else { //此时属性值为空
					result = new Token(morpheme, tokenType.getType(), null, path.toString(), lineNumber);;
				}
				curStatus = 0; //重置到开始状态
				path = new StringBuilder(gotoTable.getStatus(0)); //重置路径
				index++;
				startIndex = index;
				return result;
			}
		}
		
		return null; //读取完字符串后没找到终结状态
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<gotoTable.getStatusNum(); i++) {
			String statusStr = gotoTable.getStatus(i);
			sb.append(statusStr+": ");
			int len = statusStr.length()+2;
			String padding = String.format("%"+len+"s","");
			for(int j=32; j<126; j++) {
				int nextStatus = gotoTable.nextStatus(i, j);
				if(nextStatus != -1) {
					sb.append("--"+(char)j+"->"+gotoTable.getStatus(nextStatus)+"\n" + padding);
				}
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * 得到状态序号为status的转换表
	 * String[i]表示状态status在遇到字符(char)(i+32)时的下一个状态；
	 * String[0]表示状态序号为status对应的状态名；
	 * String[]共有95个元素
	 * @param status 状态序号
	 * @return 若status状态不存在，则返回null
	 */
	public String[] getTable(int statusIndex) {
		return gotoTable.getTable(statusIndex);
	}
	
	/**
	 * 得到状态总数
	 * @return 状态总数
	 */
	public int getStatusNum() {
		return gotoTable.getStatusNum();
	}
	
	/**
	 * 得到该token所在的行号
	 * @return 该token所在的行号
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * 将DFA转换表写入文件中
	 * @param file 要写入的文件
	 */
	public void write2File(File file) {
		gotoTable.write2File(tokenMap, file);
	}
	
	public static void main(String[] args) {
		DFA dfa = DFAFactory.creator("DFA.dfa");
//		DFA dfa = LexFactory.creator("NFA.nfa");
		dfa.init("record test_Record{\r\n" + 
				"	int a = 0;\r\n" + 
				"	int b = 044;\r\n" + 
				"	int c = 0x8F;\r\n" + 
				"	char d = 't';\r\n" + 
				"	string s = \"String\";\r\n" + 
				"	int array[10];\r\n" + 
				"	float e = 3.1415926;\r\n" + 
				"}\r\n" + 
				"while(a <=@ 10) {\r\n" + 
				"    a++; /* test the comment */\r\n" + 
				"}");
		Token t;
		while((t=dfa.getNext()) != null) {
			System.out.println(t);
		}
	}
}

