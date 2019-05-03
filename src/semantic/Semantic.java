package semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import grammar.GrammarTree;

public class Semantic {
	/**
	 * 该类是一个语义分析工具类
	 * 
	 */
	private static Stack<SymbolTable> stack;
	private static SymbolTable curSymbolTable;
	
	private static StringBuilder error, info;
	
	private static List<Code> codes; //三地址码
	
	/* 所有的符号表 */
	private static Map<String, SymbolTable> tables;
	
	/* 将类型和宽度信息从语法分析树中的B节点传递到对应于产生式C->nil的结点 */
	private static String t;
	private static int w;
	
	/* 作为生成新的临时变量的下标 */
	private static int temp;
	
	
	/**
	 * 语义分析初始化
	 */
	public static void init() {
		stack = new Stack<>();
		codes = new ArrayList<>();
		SymbolTable mainTable = new SymbolTable("main", codes);
		tables = new HashMap<>();
		tables.put("main", mainTable);
		stack.push(mainTable);
		curSymbolTable = mainTable;
		error = new StringBuilder();
		info = new StringBuilder();
		curSymbolTable.setError(error);
		curSymbolTable.setInfo(info);
		temp = 1;
	}
	
	
	@SuppressWarnings("unchecked")
	public static void setProperty(GrammarTree gt) {
		//记得每个节点的孩子是从右往左存储的
		switch(gt.productionIndex) {
//		case 0: //P'->P
//			break;
//		case 1: //P->DP
//			break;
		case 2: { //P->SMP
			List<Integer> SnextList = (List<Integer>)gt.children.get(2).property.get("nextList");
			int Mquad = (int)gt.children.get(1).property.get("quad");
			backPatch(SnextList, Mquad);
		}break;
//		case 3: //P->nil
//			break;
//		case 4: //D->Dv
//			break;
		case 5: { //Dv'->DvDv'
			gt.property.put("num", (int)gt.children.get(0).property.get("num")+1);
		}break;
		case 6: { //Dv'->nil
			gt.property.put("num", 0);
		}break;
		case 7: { //D->procidMp(Dv'){P}
			String procName = gt.children.get(7).token.getValue();
			if(tables.containsKey(procName)) {
				stack.pop();
				curSymbolTable = stack.peek();
				codes = curSymbolTable.getCodes();
				error = curSymbolTable.getError();
				info = curSymbolTable.getInfo();
				curSymbolTable.delete();
				appendError(gt.children.get(7).token.getLineNumber(), "函数或结构体或变量"+procName+"已存在");
				break;
			}
			curSymbolTable.setName(procName);
			curSymbolTable.setParamNum((int)gt.children.get(4).property.get("num"));
			int width = curSymbolTable.getSpaceSum();
			SymbolTableRow procSymbolTableRow = (SymbolTableRow)gt.children.get(6).property.get("SymbolTableRow");
			procSymbolTableRow.setSpace(width);
			tables.put(procName, curSymbolTable);
			stack.pop();
			curSymbolTable = stack.peek();
			curSymbolTable.addSpace(width);
			codes = curSymbolTable.getCodes();
			error = curSymbolTable.getError();
			info = curSymbolTable.getInfo();
			curSymbolTable.changeVarName("nil", procName);
		}break;
		case 8: { //D->procidMp(){P}
			String procName = gt.children.get(6).token.getValue();
			if(tables.containsKey(procName)) {
				stack.pop();
				curSymbolTable = stack.peek();
				codes = curSymbolTable.getCodes();
				error = curSymbolTable.getError();
				info = curSymbolTable.getInfo();
				curSymbolTable.delete();
				appendError(gt.children.get(6).token.getLineNumber(), "函数或结构体或变量"+procName+"已存在");
				break;
			}
			curSymbolTable.setName(procName);
			int width = curSymbolTable.getSpaceSum();
			SymbolTableRow procSymbolTableRow = (SymbolTableRow)gt.children.get(5).property.get("SymbolTableRow");
			procSymbolTableRow.setSpace(width);
			tables.put(procName, curSymbolTable);
			stack.pop();
			curSymbolTable = stack.peek();
			curSymbolTable.addSpace(width);
			codes = curSymbolTable.getCodes();
			error = curSymbolTable.getError();
			info = curSymbolTable.getInfo();
			curSymbolTable.changeVarName("nil", procName);
		}break;
		case 9: { //D->recordidMp{Dv'}
			String recordName = gt.children.get(4).token.getValue();
			if(tables.containsKey(recordName)) {
				stack.pop();
				curSymbolTable = stack.peek();
				codes = curSymbolTable.getCodes();
				error = curSymbolTable.getError();
				info = curSymbolTable.getInfo();
				curSymbolTable.delete();
				appendError(gt.children.get(4).token.getLineNumber(), "函数或结构体或变量"+recordName+"已存在");
				break;
			}
			curSymbolTable.setName(recordName);
			int width = curSymbolTable.getSpaceSum();
			SymbolTableRow recordSymbolTableRow = (SymbolTableRow)gt.children.get(3).property.get("SymbolTableRow");
			recordSymbolTableRow.setSpace(width);
			recordSymbolTableRow.setType("record");
			tables.put(recordName, curSymbolTable);
			stack.pop();
			curSymbolTable = stack.peek();
			curSymbolTable.addSpace(width);
			codes = curSymbolTable.getCodes();
			error = curSymbolTable.getError();
			info = curSymbolTable.getInfo();
			curSymbolTable.changeVarName("nil", recordName);
		}break;
		case 10: { // Mp->nil
			codes = new ArrayList<>();
			SymbolTable t = new SymbolTable("nil", curSymbolTable, codes);
			curSymbolTable.enter("nil", "proc", 0, t);
			SymbolTableRow SymbolTableRow = curSymbolTable.lookUp("nil");
			stack.push(t);
			curSymbolTable = t;
			error = new StringBuilder();
			info = new StringBuilder();
			curSymbolTable.setError(error);
			curSymbolTable.setInfo(info);
			gt.property.put("SymbolTableRow", SymbolTableRow);
		}break;
		case 11: { //Dv->Tid;
			String idName = (String)gt.children.get(1).token.getValue();
			if(curSymbolTable.lookUp(idName) != null) {
				appendError(gt.lineNumber, "变量"+idName+"重复声明");
				break;
			}
			String Ttype = (String)gt.children.get(2).property.get("type");
			int Tspace = (int)gt.children.get(2).property.get("width");
			curSymbolTable.enter(idName, Ttype, Tspace, null);
			if(Ttype.charAt(Ttype.length()-1) == ')') { //数组
				Object idvalue;
				if(Ttype.charAt(Ttype.lastIndexOf(',')+2) == 'i') { //int数组
					idvalue = new int[Tspace/4];
				} else { //float数组
					idvalue = new float[Tspace/4];
				}
				curSymbolTable.setValue(idName, idvalue);
			}
		}break;
		case 12: { //T->XC'
			String C_type = (String)gt.children.get(0).property.get("type");
			int C_width = (int)gt.children.get(0).property.get("width");
			gt.property.put("type", C_type);
			gt.property.put("width", C_width);
		}break;
		case 13: { //X->int
			t = "int";
			w = 4;
			gt.property.put("type", "int");
			gt.property.put("width", 4);
		}break;
		case 14: { //X->float
			t = "float";
			w = 4;
			gt.property.put("type", "float");
			gt.property.put("width", 4);
		}break;
		case 15: { //C'->CC'
			int numVal = (int)gt.children.get(1).property.get("num");
			String C1_type = (String)gt.children.get(0).property.get("type");
			int C1_width = (int)gt.children.get(0).property.get("width");
			String C0_type = "array("+numVal+", "+C1_type+")";
			gt.property.put("type", C0_type);
			gt.property.put("width", numVal*C1_width);
		}break;
		case 16: { //C'->nil
			gt.property.put("type", t);
			gt.property.put("width", w);
		}break;
		case 17: { //C->[number]
			gt.property.put("num", Integer.valueOf(gt.children.get(1).token.getValue()));
		}break;
		case 18: { //S->id=E;
			String idName = gt.children.get(3).token.getValue();
			SymbolTableRow p = curSymbolTable.lookUp(idName);
			Map<String, Object> Eproperty = gt.children.get(1).property;
			String Etype = (String)Eproperty.get("type");
			String Ename = (String)Eproperty.get("name");
			Object Evalue = Eproperty.get("value");
			if(p == null) { //变量未经声明就使用
				appendError(gt.lineNumber, "变量"+idName+"未经声明就使用");
				break;
			}
			Object idvalue;
			if(!p.getType().equals(Etype)) { //两者类型不一样
				if(p.getType().equals("int")) { //id是int，表达式右边是float，则无法强转，出错
					appendError(gt.lineNumber, "类型\""+p.getType()+"\"不能强转为\""+Etype+"\"");
					break;
				} else { //id是float，表达式右边是int，则可以强转
					appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
					idvalue = (float)((int)Evalue);
				}
			} else { //类型一样
				idvalue = Evalue;
			}
			p.setValue(idvalue);
			Assign assign = new Assign(p.getVarName(), Ename);
			codes.add(assign);
		}break;
		case 19: { //E->E+E
			Map<String, Object> E1property = gt.children.get(2).property;
			Map<String, Object> E2property = gt.children.get(0).property;
			String E1type = (String)E1property.get("type");
			String E1name = (String)E1property.get("name");
			Object E1value = E1property.get("value");
			String E2type = (String)E2property.get("type");
			String E2name = (String)E2property.get("name");
			Object E2value = E2property.get("value");
			String E0type;
			Object E0value;
			if(E1type.equals(E2type)) { //同类型计算
				E0type = E1type;
				if(E1type.equals("int")) { //都是int
					E0value = (int)((int)E1value*(int)E2value);
				} else { //都是float
					E0value = (float)((float)E1value+(float)E2value);
				}
			} else if(E1type.equals("int")) { //E1是int，E2是float
				E0type = E2type;
				E0value = (float)((int)E1value+(float)E2value);
				appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
			} else { //E1是float，E2是int
				E0type = E1type;
				E0value = (float)((float)E1value*(int)E2value);
				appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
			}
			String t_ = newTemp();
			gt.property.put("type", E0type);
			gt.property.put("name", t_);
			gt.property.put("value", E0value);
			Assign assign = new Assign('+', t_, E1name, E2name);
			codes.add(assign);
		}break;
		case 20: { //E->E*E
			Map<String, Object> E1property = gt.children.get(2).property;
			Map<String, Object> E2property = gt.children.get(0).property;
			String E1type = (String)E1property.get("type");
			String E1name = (String)E1property.get("name");
			Object E1value = E1property.get("value");
			String E2type = (String)E2property.get("type");
			String E2name = (String)E2property.get("name");
			Object E2value = E2property.get("value");
			String E0type;
			Object E0value;
			if(E1type.equals(E2type)) { //同类型计算
				E0type = E1type;
				if(E1type.equals("int")) { //都是int
					E0value = (int)((int)E1value*(int)E2value);
				} else { //都是float
					E0value = (float)((float)E1value*(float)E2value);
				}
			} else if(E1type.equals("int")) { //E1是int，E2是float
				E0type = E2type;
				E0value = (float)((int)E1value*(float)E2value);
				appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
			} else { //E1是float，E2是int
				E0type = E1type;
				E0value = (float)((float)E1value*(int)E2value);
				appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
			}
			String t_ = newTemp();
			gt.property.put("type", E0type);
			gt.property.put("name", t_);
			gt.property.put("value", E0value);
			Assign assign = new Assign('*', t_, E1name, E2name);
			codes.add(assign);
		}break;
		case 21: { //E->-E
			String E1type = (String)gt.children.get(0).property.get("type");
			String E1name = (String)gt.children.get(0).property.get("name");
			Object E0value;
			if(E1type.equals("int")) {
				E0value = -(int)gt.children.get(0).property.get("value");
			} else if(E1type.equals("float")) {
				E0value = -(float)gt.children.get(0).property.get("value");
			} else {
				appendError(gt.lineNumber, "未对类型\""+E1type+"\"定义符号'-'");
				break;
			}
			String tempStr = newTemp();
			gt.property.put("name", tempStr);
			gt.property.put("value", E0value);
			gt.property.put("type", E1type);
			Assign assign = new Assign('-', tempStr, E1name);
			codes.add(assign);
		}break;
		case 22: { //E->(E)
			gt.property = gt.children.get(1).property;
		}break;
		case 23: { //E->id
			String idName = gt.children.get(0).token.getValue();
			SymbolTableRow p = curSymbolTable.lookUp(idName);
			if(p == null) { //变量未经声明就使用
				error.append("Error at Line ["+gt.lineNumber+"]: 变量"+idName+"未经声明就使用\n");
				break;
			}
			gt.property.put("name", idName);
			gt.property.put("value", p.getValue());
			gt.property.put("type", p.getType());
		}break;
		case 24: { //E->int_num
			int Evalue = (int)Integer.valueOf(gt.children.get(0).token.getValue());
			gt.property.put("name", String.valueOf(Evalue));
			gt.property.put("value", Evalue);
			gt.property.put("type", "int");
		}break;
		case 25: { //E->float_num
			float Evalue = (float)Float.valueOf(gt.children.get(0).token.getValue());
			gt.property.put("name", String.valueOf(Evalue));
			gt.property.put("value", Evalue);
			gt.property.put("type", "float");
		}break;
		case 26: { //E->L
			String t_ = newTemp();
			Map<String, Object> Lproperty = gt.children.get(0).property;
			
			if(Lproperty.get("offset") == null) break; //数组出错了
			
			int Loffset = (int)Lproperty.get("offset");
			SymbolTableRow array = curSymbolTable.lookUp((String)gt.children.get(0).property.get("array"));
			gt.property.put("name", t_);
			gt.property.put("type", Lproperty.get("type"));
			gt.property.put("value", ((int[])array.getValue())[Loffset/4]);
			Assign assign = new Assign(t_, array.getVarName(), (String)gt.children.get(0).property.get("offsetName"));
			codes.add(assign);
		}break;
		case 27: { //L->id[E]
			String idName = gt.children.get(3).token.getValue();
			SymbolTableRow p = curSymbolTable.lookUp(idName);
			Map<String, Object> Eproperty = gt.children.get(1).property;
			String Etype = (String)Eproperty.get("type");
			if(p == null) { //变量未经声明就使用
				appendError(gt.lineNumber, "变量"+idName+"未经声明就使用");
				break;
			} else if(!Etype.equals("int")) {
				appendError(gt.lineNumber, "数组"+idName+"的分量非整数");
				break;
			}
			
			if(p.getType().charAt(0) != 'a') {
				appendError(gt.lineNumber, "数组"+idName+"的维数过多");
				break;
			}
			int arrayLen = Integer.valueOf(p.getType().substring(p.getType().indexOf('(')+1, p.getType().indexOf(' ')-1));
			if((int)Eproperty.get("value") >= arrayLen) {
				appendError(gt.lineNumber, "数组"+idName+"越界访问");
				break;
			}
			String Ltype = p.getType().substring(p.getType().indexOf(' ')+1, p.getType().length()-1);
			int Loffset = 4 * (int)Eproperty.get("value");
			// 以下计算offset
			int index = -1;
			String temp = Ltype;
			while((index=temp.indexOf(',')) != -1) {
				Loffset = Loffset * Integer.valueOf(temp.substring(6, index));
				temp = temp.substring(index+2, temp.length()-1);
			}
			gt.property.put("offset", Loffset);
			gt.property.put("array", idName);
			gt.property.put("type", Ltype);
			String t_ = newTemp();
			Assign assign = new Assign('*', t_, (String)Eproperty.get("name"), String.valueOf(Loffset));
			codes.add(assign);
			gt.property.put("offsetName", t_);
		}break;
		case 28: { //L->L[E]
			Map<String, Object> L1property = gt.children.get(3).property;
			Map<String, Object> Eproperty = gt.children.get(1).property;
			String Etype = (String)Eproperty.get("type");
			SymbolTableRow arrayRow = curSymbolTable.lookUp((String)L1property.get("array"));
			if(!Etype.equals("int")) {
				appendError(gt.lineNumber, "数组"+L1property.get("array")+"的分量非整数");
				break;
			}
			String L1type = (String)L1property.get("type");
			if(L1type == null) break; //数组维数过少
			if(L1type.charAt(0) != 'a') {
				appendError(gt.lineNumber, "数组"+L1property.get("array")+"的维数过多");
				break;
			}
			int arrayLen = Integer.valueOf(L1type.substring(L1type.indexOf('(')+1, L1type.indexOf(' ')-1));
			if((int)Eproperty.get("value") >= arrayLen) {
				appendError(gt.lineNumber, "数组"+L1property.get("array")+"越界访问");
				break;
			}
			String L0type = L1type.substring(L1type.indexOf(' ')+1, L1type.length()-1);
			int L0offset = 4;
			// 以下计算offset
			int index = -1;
			String temp = L0type;
			while((index=temp.indexOf(',')) != -1) {
				L0offset = L0offset * Integer.valueOf(temp.substring(6, index));
				temp = temp.substring(index+2, temp.length()-1);
			}
			
			if(L0offset >= arrayRow.getSpace()) {
				appendError(gt.lineNumber, "数组"+L1property.get("array")+"越界访问");
				break;
			}
			
			gt.property.put("array", (String)L1property.get("array"));
			gt.property.put("type", L0type);
			String t_1 = newTemp();
			String t_2 = newTemp();
			Assign assign1 = new Assign('*', t_1, (String)gt.children.get(1).property.get("name"), String.valueOf(L0offset));
			Assign assign2 = new Assign('+', t_2, (String)L1property.get("offsetName"), t_1);
			codes.add(assign1);
			codes.add(assign2);
			L0offset = L0offset * (int)Eproperty.get("value") + (int)L1property.get("offset");
			gt.property.put("offset", L0offset);
			gt.property.put("offsetName", t_2);
		}break;
		case 29: { //S->L=E;
			Map<String, Object> Eproperty = gt.children.get(1).property;
			Map<String, Object> Lproperty = gt.children.get(3).property;
			String Etype = (String)Eproperty.get("type");
			String Ename = (String)Eproperty.get("name");
			Object Evalue = Eproperty.get("value");
			String Ltype = (String)Lproperty.get("type");
			String Larray = (String)Lproperty.get("array");
			
			if(Lproperty.get("offset") == null) break; //出现了数组越界错误

			int Loffset = (int)Lproperty.get("offset");
			
			SymbolTableRow array = curSymbolTable.lookUp(Larray);
			if(Etype == null) break; //计算分量有错
			if(Etype.equals(Ltype)) { //相同类型
				if(Ltype.equals("int")) { //都是int
					int[] L = (int[])array.getValue();
					L[Loffset/4] = (int)Evalue;
				} else { //都是float
					float[] L = (float[])array.getValue();
					L[Loffset/4] = (float)Evalue;
				}
			} else if(Ltype.equals("int")) { //数组是int，表达式是float
				appendError(gt.lineNumber, "类型\"float\"不能强转为\"int\"");
				break;
			} else if(Ltype.equals("float")) { //数组是float，表达式是int
				float[] L = (float[])array.getValue();
				L[Loffset/4] = (float)((int)Evalue);
				appendInfo(gt.lineNumber, "类型\"int\"强转为\"float\"");
			} else {
				appendError(gt.lineNumber, "类型\""+Etype+"\"不能强转为\""+Ltype+"\"");
				break;
			}
			ArrayAssign arrayAssign = new ArrayAssign(Larray, (String)Lproperty.get("offsetName"), Ename);
			codes.add(arrayAssign);
		}break;
		case 30: { //S'->SMS'
			List<Integer> S2nextList = (List<Integer>)gt.children.get(0).property.get("nextList");
			List<Integer> S1nextList = (List<Integer>)gt.children.get(2).property.get("nextList");
			int Mquad = (int)gt.children.get(1).property.get("quad");
			backPatch(S1nextList, Mquad);
			gt.property.put("nextList", S2nextList);
		}break;
		case 31: //S'->nil
			break;
		case 32: { //S->ifMBthen{MS'}
			Map<String, Object> Bproperty = gt.children.get(5).property;
			List<Integer> BfalseList = (List<Integer>)Bproperty.get("falseList");
			List<Integer> BtrueList = (List<Integer>)Bproperty.get("trueList");
			List<Integer> S1nextList = (List<Integer>)gt.children.get(1).property.get("nextList");
			List<Integer> S0nextList = merge(BfalseList, S1nextList);
			backPatch(BtrueList, (int)gt.children.get(2).property.get("quad"));
			gt.property.put("nextList", S0nextList);
		}break;
		case 33: { //S->ifMBthen{MS'}Nelse{MS'}
			Map<String, Object> Bproperty = gt.children.get(11).property;
			List<Integer> BfalseList = (List<Integer>)Bproperty.get("falseList");
			List<Integer> BtrueList = (List<Integer>)Bproperty.get("trueList");
			List<Integer> S1nextList = (List<Integer>)gt.children.get(7).property.get("nextList");
			List<Integer> S2nextList = (List<Integer>)gt.children.get(1).property.get("nextList");
			List<Integer> NnextList = (List<Integer>)gt.children.get(5).property.get("nextList");
			
			List<Integer> SnextList = merge(S1nextList, NnextList, S2nextList);
			backPatch(BtrueList, (int)gt.children.get(8).property.get("quad"));
			backPatch(BfalseList, (int)gt.children.get(2).property.get("quad"));
			gt.property.put("nextList", SnextList);
		}break;
		case 34: { //S->whileMBdo{MS'}
			Map<String, Object> Bproperty = gt.children.get(5).property;
			List<Integer> BfalseList = (List<Integer>)Bproperty.get("falseList");
			List<Integer> BtrueList = (List<Integer>)Bproperty.get("trueList");
			List<Integer> S1nextList = (List<Integer>)gt.children.get(1).property.get("nextList");
			int M1quad = (int)gt.children.get(6).property.get("quad");
			int M2quad = (int)gt.children.get(2).property.get("quad");
			List<Integer> SnextList = BfalseList;
			backPatch(S1nextList, M1quad);
			backPatch(BtrueList, M2quad);
			gt.property.put("nextList", SnextList);
			BoolExpression boolExpression = new BoolExpression();
			boolExpression.setQuad(M1quad);
			codes.add(boolExpression);
		}break;
		case 35: { //B->BorMB
			Map<String, Object> B1property = gt.children.get(3).property;
			Map<String, Object> B2property = gt.children.get(0).property;
			List<Integer> B1trueList = (List<Integer>)B1property.get("trueList");
			List<Integer> B1falseList = (List<Integer>)B1property.get("falseList");
			List<Integer> B2trueList = (List<Integer>)B2property.get("trueList");
			List<Integer> B2falseList = (List<Integer>)B2property.get("falseList");
			
			List<Integer> BtrueList = merge(B1trueList, B2trueList);
			List<Integer> BfalseList = B2falseList;
			int Mquad = (int)gt.children.get(1).property.get("quad");
			backPatch(B1falseList, Mquad);
			gt.property.put("trueList", BtrueList);
			gt.property.put("falseList", BfalseList);
		}break;
		case 36: { //B->BandMB
			Map<String, Object> B1property = gt.children.get(3).property;
			Map<String, Object> B2property = gt.children.get(0).property;
			List<Integer> B1trueList = (List<Integer>)B1property.get("trueList");
			List<Integer> B1falseList = (List<Integer>)B1property.get("falseList");
			List<Integer> B2trueList = (List<Integer>)B2property.get("trueList");
			List<Integer> B2falseList = (List<Integer>)B2property.get("falseList");
			
			List<Integer> BtrueList = B2trueList;
			List<Integer> BfalseList = merge(B1falseList, B2falseList);
			int Mquad = (int)gt.children.get(1).property.get("quad");
			backPatch(B1trueList, Mquad);
			gt.property.put("trueList", BtrueList);
			gt.property.put("falseList", BfalseList);
		}break;
		case 37: { //B->notB
			Map<String, Object> B1property = gt.children.get(2).property;
			List<Integer> B1trueList = (List<Integer>)B1property.get("trueList");
			List<Integer> B1falseList = (List<Integer>)B1property.get("falseList");
			gt.property.put("trueList", B1falseList);
			gt.property.put("falseList", B1trueList);
		}break;
		case 38: { //B->(B)
			gt.property = gt.children.get(1).property;
		}break;
		case 39: { //B->E<E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" < "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 40: { //B->E<=E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" <= "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 41: { //B->E>E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" > "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 42: { //B->E>=E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" >= "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 43: { //B->E==E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" == "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 44: { //B->E!=E
			int trueQuad = nextQuad();
			int falseQuad = nextQuad()+1;
			String condition = gt.children.get(2).property.get("name")+" != "+gt.children.get(0).property.get("name");
			List<Integer> trueList = new ArrayList<>();
			List<Integer> falseList = new ArrayList<>();
			trueList.add(trueQuad);
			falseList.add(falseQuad);
			gt.property.put("trueList", trueList);
			gt.property.put("falseList", falseList);
			BoolExpression trueExpression = new BoolExpression(condition);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(trueExpression);
			codes.add(falseExpression);
		}break;
		case 45: { //B->true
			int trueQuad = nextQuad();
			List<Integer> trueList = new ArrayList<>();
			trueList.add(trueQuad);
			gt.property.put("trueList", trueList);
			BoolExpression trueExpression = new BoolExpression();
			codes.add(trueExpression);
		}break;
		case 46: { //B->false
			int flaseQuad = nextQuad();
			List<Integer> falseList = new ArrayList<>();
			falseList.add(flaseQuad);
			gt.property.put("falseList", falseList);
			BoolExpression falseExpression = new BoolExpression();
			codes.add(falseExpression);
		}break;
		case 47: { //M->nil
			gt.property.put("quad", nextQuad());
		}break;
		case 48: { //N->nil
			List<Integer> nextList = new ArrayList<>();
			nextList.add(nextQuad());
			gt.property.put("nextList", nextList);
			BoolExpression boolExpression = new BoolExpression();
			codes.add(boolExpression);
		}break;
		case 49: { //S->callid(Elist);
			Stack<String> s = (Stack<String>)gt.children.get(2).property.get("stack");
			int n = 0;
			String procName = (String)gt.children.get(4).token.getValue();
			if(s != null) {
				n = s.size();
				while(!s.isEmpty()) {
					Common code = new Common("param " + s.pop());
					codes.add(code);
				}
				Common code = new Common("call "+procName+", "+n);
				codes.add(code);
			}
			SymbolTable procTable = (SymbolTable)(curSymbolTable.lookUp(procName).getValue());
			if(n != procTable.getParamNum()) {
				appendError(gt.lineNumber, "参数类型错误，应有"+procTable.getParamNum()+"个参数");
				break;
			}
		}break;
		case 50: { //Elist->E,Elist
			Stack<String> q = (Stack<String>)gt.children.get(0).property.get("stack");
			q.add((String)gt.children.get(2).property.get("name"));
			gt.property.put("stack", q);
		}break;
		case 51: { //Elist->nil
			Stack<String> q = new Stack<>();
			gt.property.put("stack", q);
		}break;
		}
	}
	
	private static String newTemp() {
		String t_ = "t" + temp;
		while(curSymbolTable.lookUp(t_) != null) {
			temp++;
			t_ = "t" + temp;
		}
		temp++;
//		curSymbolTable.enter(t_, type, space, value);
		return t_;
	}
	
	private static int nextQuad() {
		return codes.size()+1;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Integer> merge(final List<Integer>...list) {
		List<Integer> result = new ArrayList<>();
		for(List<Integer> l : list) {
			if(l == null) continue;
			result.addAll(l);
		}
		return result;
	}
	
	private static void backPatch(List<Integer> list, int quad) {
		if(list == null) return;
		for(int i : list) {
			BoolExpression boolExpression = (BoolExpression)codes.get(i-1);
			boolExpression.setQuad(quad);
		}
	}
	
	public static String getCode() {
		StringBuilder sb = new StringBuilder();
		for(int i=1; i<=codes.size(); i++) {
			sb.append(i+": "+codes.get(i-1)+"\n");
		}
		return sb.toString();
	}
	
	public static void appendError(int lineNumber, String errorInfo) {
		error.append("Error at Line ["+lineNumber+"]: "+errorInfo+"\n");
	}
	
	public static void appendInfo(int lineNumber, String info) {
		Semantic.info.append("Warning at Line ["+lineNumber+"]: "+info+"\n");
	}
	
	public static SymbolTable getSymbolTable() {return curSymbolTable;}
	
	public static Map<String, SymbolTable> getSymbolTables() {return tables;}
}
