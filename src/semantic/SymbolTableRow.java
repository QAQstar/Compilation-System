package semantic;

public class SymbolTableRow {
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
	public SymbolTableRow(String varName, String type, Object value, int addr, int space) {
		this.varName = varName;
		this.type = type;
		this.value = value;
		this.addr = addr;
		this.space = space;
	}
	
	public void setValue(Object value) {this.value = value;}
	
	public void setSpace(int space) {this.space = space;}
	
	public void setVarName(String varName) {this.varName = varName;}
	
	public void setType(String type) {this.type = type;}
	
	public String getVarName() {return this.varName;}
	
	public String getType() {return this.type;}
	
	public Object getValue() {return this.value;}
	
	public int getAddr() {return this.addr;}
	
	public int getSpace() {return this.space;}
}
