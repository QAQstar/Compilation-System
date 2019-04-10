package grammar;

public class Symbol {
	/**
	 * 文法符号类
	 * isFinal指示了该状态是否是终结符
	 * name指示了该状态的名字
	 */
	
	private boolean isFinal;
	private String name;
	
	/**
	 * 构造一个文法符号
	 * @param isFinal 指示该状态是否是终结符
	 * @param name 该状态的名字
	 */
	public Symbol(boolean isFinal, String name) {
		this.isFinal = isFinal;
		this.name = name;
	}
	
	public boolean isFinal() {
		return isFinal;
	}
	
	public void setIsFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Symbol)) {
			return o.toString().equals(name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
