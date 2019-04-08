package lex;

public class Token {
	/**
	 * morpheme为识别出来的词素
	 * type为种别码，value为属性值
	 * 当value为null时，则该token无属性值
	 * path为从start转换至终结状态得到该token的路径
	 */
	
	private String morpheme, type, value, path;
	
	/**
	 * 利用token字符串创建一个token
	 * 这类Token不具有具体意义的属性值和词素，因为代表的是一类Token
	 * 属性值一般为从接受字符串中提取的正则子串
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
	}
	
	/**
	 * 根据种别码和属性值构造Token
	 * 同时接收至对应的终结状态的路径记录在path中
	 * @param type 种别码
	 * @param value 属性值;"-"或null则属性值为空
	 * @param path 对应的终结状态的路径记录
	 */
	public Token(String morpheme, String type, String value, String path) {
		if(type.equals("COMMENT")) { //注释没必要保留内容
			this.morpheme = "/*...*/";
		} else if(morpheme.charAt(morpheme.length()-1) == '$') { //终止符$没必要记录
			this.morpheme = morpheme.substring(0, morpheme.length()-1);
		} else {
			this.morpheme = morpheme;
		}
		this.type = type;
		this.path = path;
		if(value == null || value.equals("-")) {
			this.value = null;
		} else {
			this.value = value;
		}
	}
	
	/**
	 * 得到该token的词素
	 * @return 词素
	 */
	public String getMorpheme() {
		return morpheme;
	}
	
	/**
	 * 得到Token序列的种别码
	 * @return 种别码
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * 得到Token序列的属性值
	 * @return 属性值，若属性值为空则返回null
	 */
	public String getValue() {
		return value;
	}
	
	public String getPath() {
		return path;
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
}
