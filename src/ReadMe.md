### 词法分析

源代码见[lexical文件夹](https://github.com/978025302/Compilation-System/tree/master/src/lexical "词法分析")

- **DFAFactory.class** 工厂类，接收FA转换文件的路径，并构造出DFA，FA转换文件的格式已经在文件中用注释说明

- **DFA.class** 有穷自动机，通过init方法对DFA进行初始化，也就是告诉DFA输入流；然后就可以用getNext得到Token序列，得到最后一个序列时返回空；write2File是将DFA转换表写入文件中

- **GOTOTable**和**Token**分别是维护转换表和Token的两个数据结构

    

### 语法分析

源代码见[grammar文件夹](https://github.com/978025302/Compilation-System/tree/master/src/grammar "语法分析")

- **AnalysisTableFactory.class** 工厂类，构造出LR分析表；同时一些重要的准备函数也在该类中完成，如求FIRST集、CLOSURE函数、GOTO函数等

- **AnalysisTable.class** LR分析表，所有的分析工作在此完成。若要运行语法分析，请在该类的main方法中运行，代码中已给出示例。

- **Grammar.class** 存放与语法分析的准备工作有关的数据结构，包括产生式、产生式集、项目、项目集等等，每个数据结构中均有详细注释

- **GrammarTree.class** 语法树的数据结构

- **Symbol.class** 文法符号的数据结构

    

### 语义分析

源代码见[semantic文件夹](https://github.com/978025302/Compilation-System/tree/master/src/semantic "语义分析")

- **Semantic.class** 语义分析的工具类，利用此类中的setProperty进行属性赋值

- **SymbolTable.class** 符号表数据结构类

- **SymbolTableRow.class** 符号表中的单行数据结构类

- **Code.class** 生成三地址码的工具类

    

### 环境

- Windows10 x64

- e(fx)clipse
