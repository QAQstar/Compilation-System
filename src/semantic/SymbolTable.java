package semantic;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
	/**
	 * 该类维护一个符号表
	 * content 是符号表中的内容，包含符号表中的每一行
	 * out 表示外围符号表
	 * spaceSum 表示该符号表所有符号占用的总空间
	 * offset 
	 * 
	 */
	
	private List<Row> content;
	private SymbolTable out;
	private int spaceSum, offset;
	
	/**
	 * 初始化一个无外围的符号表
	 * @param name 该符号表的名字
	 */
	public SymbolTable(String name) {
		content = new ArrayList<>();
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
		content.add(row);
	}
	
	public Row lookUp()
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
	
	public String getVarName() {return this.varName;}
	
	public String getType() {return this.type;}
	
	public Object getValue() {return this.value;}
	
	public int getAddr() {return this.addr;}
	
	public int getSpace() {return this.space;}
}