package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	/**
	 * 该类维护一个符号表
	 * name 表示该符号表的名字
	 * content 是符号表中的内容，包含符号表中的每一行
	 * index 表示某个变量名是在符号表的第几行
	 * out 表示外围符号表
	 * spaceSum 表示该符号表所有符号占用的总空间
	 * paramNum 表示参数的个数
	 * codes 表示该函数的三地址码
	 * error info 分别为解析代码时的错误信息和一般信息
	 */
	
	private String name;
	private List<SymbolTableRow> content;
	private Map<String, Integer> index;
	private SymbolTable out;
	private int spaceSum, paramNum;
	private List<Code> codes;
	private StringBuilder error, info;
	
	/**
	 * 初始化一个无外围的符号表
	 * @param name 该符号表的名字
	 * @param codes 该函数对应的三地址码
	 */
	public SymbolTable(String name, List<Code> codes) {
		this.name = name;
		this.paramNum = 0;
		content = new ArrayList<>();
		index = new HashMap<>();
		out = null;
		spaceSum = 0;
		this.codes = codes;
	}
	
	/**
	 * 初始化一个符号表
	 * @param name 该符号表的名字
	 * @param parent 该符号表的外围符号表
	 * @param codes 该函数对应的三地址码
	 */
	public SymbolTable(String name, SymbolTable parent, List<Code> codes) {
		this.name = name;
		this.paramNum = 0;
		content = new ArrayList<>();
		index = new HashMap<>();
		out = parent;
		spaceSum = 0;
		this.codes = codes;
	}
	
	public void setError(StringBuilder error) {this.error = error;}
	
	public void setInfo(StringBuilder info) {this.info = info;}
	
	public StringBuilder getError() {return this.error;}
	
	public StringBuilder getInfo() {return this.info;}
	
	public String getName() {return this.name;}
	
	public SymbolTable getParent() {return this.out;}
	
	public List<SymbolTableRow> getRows() {return this.content;}
	
	public void setParamNum(int paramNum) {this.paramNum = paramNum;}
	
	public int getParamNum() {return this.paramNum;}
	
	public void setName(String name) {this.name = name;}
	
	public List<Code> getCodes() {return this.codes;}

	/**
	 * 在符号表中创建一条记录
	 * @param varName 记录的名字
	 * @param type 类型
	 * @param space 该变量占用的字节数
	 * @param value 变量的值
	 */
	public void enter(String varName, String type, int space, Object value) {
		SymbolTableRow SymbolTableRow = new SymbolTableRow(varName, type, value, spaceSum, space);
		spaceSum = spaceSum + space;
		index.put(varName, content.size());
		content.add(SymbolTableRow);
	}
	
	public void changeVarName(String oldVarName, String newVarName) {
		Integer SymbolTableRowNum = index.get(oldVarName);
		if(SymbolTableRowNum == null) return;
		SymbolTableRow row = content.get(SymbolTableRowNum);
		row.setVarName(newVarName);
		index.remove(oldVarName);
		index.put(newVarName, SymbolTableRowNum);
	}
	
	/**
	 * 为变量赋值
	 * @param varName 变量名
	 * @param value 值
	 */
	public void setValue(String varName, Object value) {
		Integer SymbolTableRowNum = index.get(varName);
		if(SymbolTableRowNum == null) { //符号表中不存在该变量
			return;
		}
		content.get(SymbolTableRowNum).setValue(value);;
	}
	
	/**
	 * 查询符号表中某一个变量名对应的行
	 * @param varName 变量名
	 * @return 若存在该变量，则返回该变量的行；反之返回null
	 */
	public SymbolTableRow lookUp(String varName) {
		Integer SymbolTableRowNum = index.get(varName);
		if(SymbolTableRowNum == null) { //符号表中不存在该变量
			return null;
		}
		return content.get(SymbolTableRowNum);
	}
	
	/**
	 * 查询符号表中的某一行
	 * @param addr 所在的地址，也就是表中第几行
	 * @return 若存在该行则返回该行；反之返回null
	 */
	public SymbolTableRow lookUp(int addr) {
		if(addr > content.size()) return null;
		return content.get(addr-1);
	}
	
	/**
	 * 得到某行在表中的地址，也就是行数
	 * @param p 需要查询的行
	 * @return 该行在表中的地址，不存在则返回-1
	 */
	public int getAddr(SymbolTableRow p) {
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
	
	public int getSpaceSum() {return this.spaceSum;}
	
	public void addSpace(int space) {
		this.spaceSum = this.spaceSum + space;
	}
}