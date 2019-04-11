package lexical;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GotoTable {
	/**
	 * 该类维护了一个DFA或NFA转移表
	 * table[i][j] = k
	 * 代表第i个状态在输入字符char(j)时会转移到第k个状态
	 * status<String>表示第i个状态叫什么
	 * statusMap<String, Integer>表示某个状态在status的第几个位置
	 */
	
	private List<Integer[]> table = new ArrayList<>();
	private List<String> status = new ArrayList<>();
	private Map<String, Integer> statusMap = new HashMap<>();
	
	/**
	 * 初始化一个DFA转换表
	 * table[i][j] = k
	 * 代表第i个状态在输入字符char(j)时会转移到第k个状态
	 * status<String>表示第i个状态叫什么
	 * statusMap<String, Integer>表示某个状态在status的第几个位置
	 * @param table 转换表
	 * @param status 序号与状态对应表
	 * @param statusMap 状态与序号对应表
	 */
	public GotoTable(List<Integer[]> table, List<String> status, Map<String, Integer> statusMap) {
		this.table = table;
		this.status = status;
		this.statusMap = statusMap;
	}
	
	/**
	 * 根据上一个状态序号和一个输入字符input，来获得下一个状态序号
	 * lastStatus 上一个状态序号
	 * @param input 输入字符
	 * @return 下一个状态序号；若没有合法的下一个状态，则返回-1
	 */
	public int nextStatus(int lastStatus, char input) {
//		if(input == ' ' || input == '\n' || input == '\r') //将所有的回车和空格换成终止符$
//			input = '$';
		if(input >= 126 || input <= 32) return -1;
		Integer nextStatus = table.get(lastStatus)[input-32];
		return nextStatus == null ? -1 : nextStatus;
	}
	
	/**
	 * 根据上一个状态序号和一个输入字符input的ascii值，来获得下一个状态序号
	 * lastStatus 上一个状态序号
	 * @param input 输入字符的ascii值
	 * @return 下一个状态序号；若没有合法的下一个状态，则返回-1
	 */
	public int nextStatus(int lastStatus, int input) {
//		if(input == 32 || input == 10 || input == 13) //将所有的回车和空格换成终止符$
//			input = 36;
		if(input >= 126 || input <= 32) return -1;
		Integer nextStatus = table.get(lastStatus)[input-32];
		return nextStatus == null ? -1 : nextStatus;
	}
	
	/**
	 * 通过状态序号得到状态名
	 * @param index 状态序号
	 * @return 对应的状态名；若状态序号错误，则返回null
	 */
	public String getStatus(int index) {
		if(index >= status.size()) {
			return null;
		}
		return status.get(index);
	}
	
	/**
	 * 通过状态名得到状态序号
	 * @param status 状态名
	 * @return 对应的状态序号；若没有该状态则为null
	 */
	public int getStatusIndex(String status) {
		return statusMap.get(status);
	}
	
	/**
	 * 得到状态总数
	 * @return 状态总数
	 */
	public int getStatusNum() {
		return status.size();
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
		if(statusIndex >= status.size()) {
			return null;
		}
		
		String[] result = new String[95];
		result[0] = status.get(statusIndex);
		
		for(int i=1; i<95; i++) {
			Integer nextStatus = table.get(statusIndex)[i-1];
			if(nextStatus == null) {
				result[i] = null;
			} else {
				result[i] = status.get(nextStatus);
			}
		}
		return result;
	}
	
	/**
	 * 将DFA转换表写入文件中
	 * @param tokenMap 终结状态序号对应的token表
	 * @param file 要写入的文件
	 */
	public void write2File(Map<Integer,Token> tokenMap, File file) {
		try(FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw)) {
			//写转换表
			bw.write("# 以下是转换表\n");
			//初始状态不用写成[start]
			Integer[] startTable = table.get(0);
			for(int i=1; i<94; i++) { //空格(ascii码32)会被当成终止符$存储，所以不需要考虑
				if(startTable[i] != null) {
					String nextStatus = null;
					if(startTable[i] == 0) { //下一个跳转的状态仍然是开始状态
						nextStatus = "start";
					} else {
						nextStatus = status.get(startTable[i]).replace(" ", "");
					}
					bw.write("start "+(char)(i+32)+" "+nextStatus+"\n");
				}
			}
			
			for(int i=1; i<status.size(); i++) {
				Integer[] Is = table.get(i);
				for(int j=1; j<94; j++) { //空格(ascii码32)会被当成终止符$存储，所以不需要考虑
					if(Is[j] != null) {
						bw.write(status.get(i).replace(" ", "")+" "+(char)(j+32)+" "+status.get(Is[j]).replace(" ", "")+"\n");
					}
				}
			}
			bw.flush();
			
			//写终结状态对应token序列
			bw.write("\n\n# 以下是终结状态对应的token\n");
			Token temp = null;
			for(Integer i : tokenMap.keySet()) {
				temp = tokenMap.get(i);
				bw.write(status.get(i).replace(" ", "")+";<"+temp.getType()+","+(temp.getValue()==null?"-":temp.getValue())+">\n");
			}
			bw.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int i,a=1;
		for(i=1;i<=10;i++) a*=i;
		System.out.println(i);
	}
}
