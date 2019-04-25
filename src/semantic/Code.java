package semantic;

public interface Code {
	/**
	 * 代表三地址码的父类
	 */
}

class Assign implements Code {
	/**
	 * x = y op z
	 * x = op z
	 * x = y
	 * x = y[z]
	 */
	
	private char op;
	private String x, y, z;
	
	/**
	 * x = y op z
	 */
	public Assign(char op, String x, String y, String z) {
		this.op = op;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * x = op y
	 */
	public Assign(char op, String x, String y) {
		this.op = op;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * x = y
	 */
	public Assign(String x, String y) {
		this.op = '\0';
		this.x = x;
		this.y = y;
	}
	
	/**
	 * x = y[z]
	 */
	public Assign(String x, String y, String z) {
		this.op = '\0';
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		if(op == '\0') {
			if(z == null) return x+" = "+y;
			return x+" = "+y+"["+z+"]";
		}
		if(z != null) return x+" = "+y+" "+op+" "+z;
		return x+" = "+op+" "+z;
	}
}

class ArrayAssign implements Code {
	/**
	 * x[y] = z
	 */
	
	private String x, y, z;
	
	/**
	 * x[y] = z
	 */
	public ArrayAssign(String x, String y, String z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		return x+"["+z+"] = "+y;
	}
}

class BoolExpression implements Code {
	/**
	 * goto _
	 * if E1 relop E2 goto _
	 */
	
	private String condition;
	private String quad;
	
	/**
	 * goto _
	 */
	public BoolExpression() {
		condition = null;
		this.quad = "_";
	}
	
	/**
	 * if E1 relop E2 goto _
	 */
	public BoolExpression(String condition) {
		this.condition = condition;
		this.quad = "_";
	}
	
	/**
	 * 重新设置指令标号，即回填
	 * @param newQuad 新的指令标号
	 */
	public void setQuad(int newQuad) {
		this.quad = String.valueOf(newQuad);
	}
	
	/**
	 * 得到指令标号，为了以后的回填
	 */
	public String getQuad() {return this.quad;}
	
	@Override
	public String toString() {
		if(condition == null) return "goto "+quad;
		return "if "+condition+" goto "+quad;
	}
}