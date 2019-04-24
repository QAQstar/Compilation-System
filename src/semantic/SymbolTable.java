package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	/**
	 * 该类维护一个符号表
	 * content 是符号表中的内容，包含符号表中的每一行
	 * index 表示某个变量名是在符号表的第几行
	 * out 表示外围符号表
	 * spaceSum 表示该符号表所有符号占用的总空间
	 */
	
	private List<Row> content;
	private Map<String, Integer> index;
	private SymbolTable out;
	private int spaceSum;
	
	/**
	 * 初始化一个无外围的符号表
	 * @param name 该符号表的名字
	 */
	public SymbolTable(String name) {
		content = new ArrayList<>();
		index = new HashMap<>();
		out = null;
		spaceSum = 0;
	}
	
	/**
	 * 初始化一个符号表
	 * @param name 该符号表的名字
	 * @param parent 该符号表的外围符号表
	 */
	public SymbolTable(String name, SymbolTable parent) {
		content = new ArrayList<>();
		index = new HashMap<>();
		out = parent;
		spaceSum = 0;
	}

	/**
	 * 在符号表中创建一条记录
	 * @param varName 记录的名字
	 * @param type 类型
	 * @param space 该变量占用的字节数
	 * @param value 变量的值
	 */
	public void enter(String varName, String type, int space, Object value) {
		Row row = new Row(varName, type, value, spaceSum, space);
		spaceSum = spaceSum + space;
		index.put(varName, content.size());
		content.add(row);
	}
	
	/**
	 * 为变量赋值
	 * @param varName 变量名
	 * @param value 值
	 */
	public void setValue(String varName, Object value) {
		Integer rowNum = index.get(varName);
		if(rowNum == null) { //符号表中不存在该变量
			return;
		}
		content.get(rowNum).setValue(value);;
	}
	
	/**
	 * 查询符号表中某一个变量名对应的行
	 * @param varName 变量名
	 * @return 若存在该变量，则返回该变量的行；反之返回null
	 */
	public Row lookUp(String varName) {
		Integer rowNum = index.get(varName);
		if(rowNum == null) { //符号表中不存在该变量
			return null;
		}
		return content.get(rowNum);
	}
	
	/**
	 * 查询符号表中的某一行
	 * @param addr 所在的地址，也就是表中第几行
	 * @return 若存在该行则返回该行；反之返回null
	 */
	public Row lookUp(int addr) {
		if(addr > content.size()) return null;
		return content.get(addr-1);
	}
	
	/**
	 * 得到某行在表中的地址，也就是行数
	 * @param p 需要查询的行
	 * @return 该行在表中的地址，不存在则返回-1
	 */
	public int getAddr(Row p) {
		Integer addr = index.get(p.getVarName());
		if(addr == null) return -1;
		return addr;
	}
	
	/**
	 * 得到某个变量存储在表中的地址，也就是行数
	 * @param p 需要查询的行
	 * @return 该行在表中的地址，不存在则返回-1
	 */
	public int getAddr(String varName) {
		Integer addr = index.get(varName);
		if(addr == null) return -1;
		return addr;
	}
}

class Row {
	/**
	 * 该类维护符号表中的一行
	 * varName 表示变量名
	 * type 表示数据类型
	 * value 表示变量的值
	 * space 表示变量的占用空间
	 */
	
	private String varName, type;
	private Object value;
	private int addr, space;
	
	/**
	 * 生成符号表中的一行
	 * @param varName 变量名
	 * @param type 数据类型
	 * @param value 变量值
	 * @param addr 变量地址
	 * @param space 占用空间
	 */
	public Row(String varName, String type, Object value, int addr, int space) {
		this.varName = varName;
		this.type = type;
		this.value = value;
		this.space = space;
	}
	
	public void setValue(Object value) {this.value = value;}
	
	public String getVarName() {return this.varName;}
	
	public String getType() {return this.type;}
	
	public Object getValue() {return this.value;}
	
	public int getAddr() {return this.addr;}
	
	public int getSpace() {return this.space;}
}