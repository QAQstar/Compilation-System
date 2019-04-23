DFAFactory.class是一个工厂类，接收FA转换文件的路径，并构造出DFA
	FA转换文件的格式已经在文件中用注释说明

DFA.class是有穷自动机，通过init方法对DFA进行初始化，也就是告诉DFA输入流
	然后就可以用getNext得到Token序列，得到最后一个序列时返回空
	write2File是将DFA转换表写入文件中

GOTOTable和Token分别是维护转换表和Token的两个数据结构

测试用例见code.txt

里边类的作用、属性、方法均有详细的注释

环境：windows10 e(fx)clipse

