#chChess
#java 版中国象棋项目
#实现局域网俩人对弈
#pic下面是棋盘和棋子素材图片

#中国象棋相关的类是下面的三个类：

#Chess.java	棋子类
#ChessBoard.java	棋盘+棋子事件+通信
#Frmchess.java    	象棋界面创建类

#下面这俩个类是 UDP 通信相关的类，和中国象棋没有依赖关系，部署代码时不用管
#DatagramDemo.java	源码提交	4 minutes ago
#DatagramServerDemo.java	源码提交	4 minutes ago

#如果是只有一台计算机，可以运行2个实例，其中将地址127.0.0.1，这样一方作为红方，另一方作为黑方，便可以自己和自己对弈了。注意对方端口一个是3003，另一个填3004，如果是在不同的局域网机器，就可以都用3003。
