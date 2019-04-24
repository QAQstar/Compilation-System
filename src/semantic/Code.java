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
	private StringBuilder quad;
	
	/**
	 * goto _
	 */
	public BoolExpression(String quad) {
		condition = null;
		this.quad = new StringBuilder(quad);
	}
	
	/**
	 * if E1 relop E2 goto _
	 */
	public BoolExpression(String condition, String quad) {
		this.condition = condition;
		this.quad = new StringBuilder(quad);
	}
	
	/**
	 * 重新设置指令标号，即回填
	 * @param newQuad 新的指令标号
	 */
	public void setQuad(StringBuilder newQuad) {
		this.quad = newQuad;
	}
	
	/**
	 * 得到指令标号，为了以后的回填
	 */
	public StringBuilder getQuad() {return this.quad;}
	
	@Override
	public String toString() {
		if(condition == null) return "goto "+quad;
		return "if "+condition+" goto "+quad;
	}
}