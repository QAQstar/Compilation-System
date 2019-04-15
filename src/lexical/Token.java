package lexical;

public class Token {
	/**
	 * morpheme为识别出来的单词
	 * type为种别码，value为属性值
	 * 当value为null时，则该token无属性值
	 * path为从start转换至终结状态得到该token的路径
	 * lineNumber为该token所在的行号
	 */
	
	private String morpheme, type, value, path;
	private int lineNumber;
	
	/**
	 * 利用token字符串创建一个token
	 * 这类Token不具有具体意义的属性值和词素，因为代表的是一类Token
	 * 属性值一般为从接受字符串中提取的正则子串或为空
	 * @param token 格式为<种别码,属性值>；属性值为"-"或"null"则属性值为空
	 */
	public Token(String token) {
		this.morpheme = null;
		int index = token.indexOf(',');
		this.type = token.substring(1, index);
		String valueTemp = token.substring(index+1, token.length()-1);
		if(valueTemp.equals("-") || valueTemp.equals("null")) {
			this.value = null;
		} else {
			this.value = valueTemp;
		}
		this.path = null;
		this.lineNumber = 0;
	}
	
	/**
	 * 根据种别码和属性值构造Token
	 * 这类Token不具有具体意义的属性值和词素，因为代表的是一类Token
	 * 属性值一般为从接受字符串中提取的正则子串
	 * @param type 种别码
	 * @param value 属性值;"-"或null则属性值为空
	 */
	public Token(String type, String value) {
		this.morpheme = null;
		this.type = type;
		this.path = null;
		if(value == null || value.equals("-")) {
			this.value = null;
		} else {
			this.value = value;
		}
		this.lineNumber = 0;
	}
	
	/**
	 * 根据种别码和属性值构造Token
	 * 同时接收至对应的终结状态的路径记录在path中
	 * @param morpheme 单词
	 * @param type 种别码
	 * @param value 属性值;"-"或null则属性值为空
	 * @param path 对应的终结状态的路径记录
	 * @param lineNumber 该Token所在的行号
	 */
	public Token(String morpheme, String type, String value, String path, int lineNumber) {
		if(type.equals("COMMENT")) { //注释没必要保留内容
			this.morpheme = "/*...*/";
		} else if(morpheme.charAt(morpheme.length()-1) == '$') { //终止符$没必要记录
			this.morpheme = morpheme.substring(0, morpheme.length()-1);
		} else {
			this.morpheme = morpheme;
		}
		this.type = type;
		this.path = path;
		this.lineNumber = lineNumber;
		if(value == null || value.equals("-")) {
			this.value = null;
		} else {
			this.value = value;
		}
	}
	
	/**
	 * 得到该Token的单词
	 * @return 单词
	 */
	public String getMorpheme() {
		return morpheme;
	}
	
	/**
	 * 得到Token的种别码
	 * @return 种别码
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * 得到Token的属性值
	 * @return 属性值，若属性值为空则返回null
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * 得到Token的转移路径
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * 判断两个种别码是否是同一类
	 * @return 两个Token有着相同的种别码时返回true；否则返回false
	 */
	public boolean isSameType(Token tokenType) {
		if(!tokenType.type.equals(type)) return false;
		if(tokenType.value == null || value == null) return true; //不需要判断属性值
		return tokenType.value.equals(value);
	}
	
	public String forGrammar() {
		if(value != null) {
			return type+"("+value+")";
		}
		return type;
	}
	
	@Override
	public String toString() {
		if(value == null)
			return morpheme+": <"+type+", ->: "+path;
		return morpheme+": <"+type+", "+value+">: "+path;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Token)) {
			return false;
		}
		Token t = (Token)o;
		if(this.type.equals(t.type) && (this.value == t.value || this.value.equals(t.value))) {
			return true;
		}
		return false;
	}
	
//	@Override
//	public int hashCode() {
//		if(value == null) return type.hashCode();
//		return (type+value).hashCode();
//	}
}
