AnalysisTableFactory.class
工厂类，构造出LR分析表；同时一些重要的准备函数也在该类中完成，如求FIRST集、CLOSURE函数、GOTO函数等

AnalysisTable.class
LR分析表，所有的分析工作在此完成。若要运行语法分析，请在该类的main方法中运行，代码中已给出示例。

Grammar.class
存放与语法分析的准备工作有关的数据结构，包括产生式、产生式集、项目、项目集等等，每个数据结构中均有详细注释

GrammarTree.class
语法树的数据结构

Symbol.class
文法符号的数据结构

测试用例见code.txt

代码中都有详细注释，真的很详细

环境：windows10 e(fx)clipse