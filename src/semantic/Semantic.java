package semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import grammar.GrammarTree;

public class Semantic {
	/**
	 * 该类是一个语义分析工具类
	 * 
	 */
	private static Stack<SymbolTable> stack;
	private static SymbolTable curSymbolTable;
	
	private static StringBuilder error;
	
	/* 将类型和宽度信息从语法分析树中的B节点传递到对应于产生式C->nil的结点 */
	private static String t;
	private static int w;
	
	/**
	 * 语义分析初始化
	 */
	public static void init() {
		stack = new Stack<>();
		SymbolTable mainTable = new SymbolTable("main");
		stack.push(mainTable);
		curSymbolTable = mainTable;
		error = new StringBuilder();
	}
	
	
	public static void setProperty(GrammarTree gt) {
		//记得每个节点的孩子是从右往左存储的
		switch(gt.productionIndex) {
		case 0: //P'->P
			break;
		case 1: //P->DP
			break;
		case 2: //P->SP
			break;
		case 3: //P->nil
			break;
		case 4: //D->Dv
			break;
		case 5: //Dv'->DvDv'
			break;
		case 6: //Dv'->nil
			break;
		case 7: //D->procid(Dv'){P}
			break;
		case 8: //D->procid(){P}
			break;
		case 9: //D->recordid{Dv'}
			break;
		case 10: { //Dv->Tid;
			String idLexeme = (String)gt.children.get(1).token.getValue();
			String Ttype = (String)gt.children.get(2).property.get("type");
			int Tspace = (int)gt.children.get(2).property.get("width");
			String idValue = (String)gt.children.get(1).token.getValue();
			curSymbolTable.enter(idLexeme, Ttype, Tspace, idValue);
		}break;
		case 11: { //T->XC'
			String C_type = (String)gt.children.get(0).property.get("type");
			int C_width = (int)gt.children.get(0).property.get("width");
			gt.property.put("type", C_type);
			gt.property.put("width", C_width);
		}break;
		case 12: { //T->X
			String Btype = (String)gt.children.get(0).property.get("type");
			int Bwidth = (int)gt.children.get(0).property.get("width");
			gt.property.put("type", Btype);
			gt.property.put("width", Bwidth);
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
			
		}break;
		case 19: //E->E+E
			break;
		case 20: //E->E*E
			break;
		case 21: //E->-E
			break;
		case 22: //E->(E)
			break;
		case 23: //E->id
			break;
		case 24: //E->number
			break;
		case 25: //E->L
			break;
		case 26: //L->id[E]
			break;
		case 27: //L->L[E]
			break;
		case 28: //S->L=E;
			break;
		case 29: //S'->SS'
			break;
		case 30: //S'->nil
			break;
		case 31: //S->ifBthen{S'}
			break;
		case 32: //S->ifBthen{S'}else{S'}
			break;
		case 33: //S->whileBdo{S'}
			break;
		case 34: //B->BorB
			break;
		case 35: //B->BandB
			break;
		case 36: //B->notB
			break;
		case 37: //B->(B)
			break;
		case 38: //B->E<E
			break;
		case 39: //B->E<=E
			break;
		case 40: //B->E>E
			break;
		case 41: //B->E>=E
			break;
		case 42: //B->E==E
			break;
		case 43: //B->E!=E
			break;
		case 44: //B->true
			break;
		case 45: //B->false
			break;
		case 46: //S->callid(Elist);
			break;
		case 47: //Elist->E,Elist
			break;
		case 48: //Elist->E
			break;
		case 49: //Elist->nil
			break;

		}
	}
	
	public static void main(String[] args) {
		File file = new File("Productions.txt");
		try(FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)) {
			String line = null;
			int i = 1;
			while((line = br.readLine()) != null) {
				System.out.println("case " + (i++)+": //"+line.substring(5)+"\n\tbreak;");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

class Code {
	/**
	 * 该类表示一个三地址码
	 */
	
	
}
