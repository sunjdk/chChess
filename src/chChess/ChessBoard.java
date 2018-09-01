package chChess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ChessBoard extends JPanel implements Runnable{
	public static final short REDPLAYER=1;
	public static final short BLACKPLAYER=0;
	public Chess[] chess=new Chess[32];
	public int[][] Map=new int[9+1][10+1];
	public Image bufferImage;
	
	private Chess firstChess2=null;
	private Chess secondChess2=null;
	private boolean first=true;
	private int x1,y1,x2,y2;
	private int tempx,tempy;
	private int r;
	private boolean IsMyTurn=true;
	public short LocalPlayer=REDPLAYER;
	private String message="";
	//线程消亡标识位
	private boolean flag=false;
	private int otherport;//对方端口
	private int receiveport;//本机接受端口
	
	public void startJoin(String ip,int otherport,int receiveport){
		flag=true;
		this.otherport=otherport;
		this.receiveport=receiveport;
		send("join|");
		//创建一个线程
		Thread th=new Thread(this);
		//启动线程
		th.start();
		message="线程处于等待联机状态!";
	}
	
	public ChessBoard(){
		r=20;
		cls_map();
		/**
		 * 鼠标事件监听
		 */
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(IsMyTurn==false){
					message="该对方走棋";
					repaint();
					return ;
				}
				int x=e.getX();
				int y=e.getY();
				
				selectChess(e);
				System.out.println(x);
				repaint();
			}

			private void selectChess(MouseEvent e) {
				// TODO Auto-generated method stub
				int idx,idx2;
				if(first){
					//第一次单击棋子
					firstChess2=analyse(e.getX(),e.getY());
					x1=tempx;
					y1=tempy;
					if(firstChess2!=null){
						if(firstChess2.player!=LocalPlayer){
							message="单击成对方棋子了";
							return ;
						}
						first=false;
					}
				}else{
					//第二次单击棋子
					secondChess2=analyse(e.getX(),e.getY());
					x2=tempx;
					y2=tempy;
					//如果是自己的棋子，则换上次选择的棋子
					if(secondChess2!=null){
						if(secondChess2.player==LocalPlayer){
							//取消上次选择的棋子
							firstChess2=secondChess2;
							x1=tempx;
							y1=tempy;
							secondChess2=null;
							return;
						}
					}
					if(secondChess2==null){//目标处没棋子，移动棋子
						if(IsAbleToPut(firstChess2,x2,y2)){
							//在Map取掉原CurSelect棋子
							idx=Map[x1][y1];
							Map[x1][y1]=-1;
							Map[x2][y2]=idx;
							chess[idx].SetPos(x2, y2);
							//send
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							//CurSelect=0;
							first=true;
							repaint();
							SetMyTurn(false);//该对方了
							//toolStripStatusLabel1.Text="";
						}else{
							//错误走棋
							message="不符合走棋规则";
						}
						return ;
					}
					if(secondChess2!=null && IsAbleToPut(firstChess2,x2,y2)){//可以吃子
						first=true;
						//在map去掉原CurSelect棋子
						idx=Map[x1][y1];
						idx2=Map[x2][y2];
						Map[x1][y1]=-1;
						Map[x2][y2]=idx;
						chess[idx].SetPos(x2, y2);
						chess[idx2]=null;
						repaint();
						if(idx2==0){//0--------将
							message="红方赢了";
							JOptionPane.showConfirmDialog(null, message, "提示", JOptionPane.DEFAULT_OPTION);
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							send("succ"+"|"+"红方赢了"+"|");
							return;
						}
						if(idx==16){//16--------帅
							message="黑方赢了";
							JOptionPane.showConfirmDialog(null, message, "提示", JOptionPane.DEFAULT_OPTION);
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							send("succ"+"|"+message+"|");
							return;
						}
						send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
						SetMyTurn(false);//该对方了
					}else{//不能吃子
						message="不能吃子";
					}
				}				
			}			
		});
	}
	//解析鼠标之下，棋子对象
	private Chess analyse(int x, int y) {
		tempx=(int)Math.floor((double)x/40)+1;
		tempy=(int)Math.floor((double)(y-20)/40)+1;
		
		System.out.println(x+","+y);
		System.out.println(tempx+","+tempy);
		//防止超出范围
		if(tempx>9||tempy>10||tempx<0||tempy<0){
			return null;
		}else{
			int idx=Map[tempx][tempy];
			if(idx==-1){
				return null;
			}
			return chess[idx];
		}
	}
	
	private boolean IsMyChess(int idx){
		boolean functionReturnValue=false;
		if(idx>=0 && idx<16 && LocalPlayer==BLACKPLAYER){
			functionReturnValue=true;
		}
		if(idx>=16 && idx<32 && LocalPlayer==REDPLAYER){
			functionReturnValue=true;
		}
		return functionReturnValue;
	}
	
	
	private void SetMyTurn(boolean bollsMyTurn) {
		// TODO Auto-generated method stub
		IsMyTurn=bollsMyTurn;
		if(bollsMyTurn){
			message="请您开始走棋";
		}else{
			message="对方正在思考";
		}
	}
	
	
	
	public void send(String str) {
		// TODO Auto-generated method stub
		DatagramSocket s=null;
		try {	
			s=new DatagramSocket();
			byte[] buffer;
			buffer=new String(str).getBytes();
			InetAddress ia=InetAddress.getLocalHost();//本机地址
			//目的主机地址
			DatagramPacket dgp=new DatagramPacket(buffer,buffer.length,ia,otherport);
			s.send(dgp);
			System.out.println("发送信息："+str);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("未知主机异常："+e.toString());
		}catch (IOException ex) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("输入输出异常："+ex.toString());
		}finally{
			if(s!=null){
				s.close();
			}
		}
	}
	
	
	private void cls_map(){
		int i,j;
		for(i=1;i<=9;i++){
			for(j=1;j<=10;j++){
				Map[i][j]=-1;
			}
		}
	}
	
	
	public final void NewGame(short player){
		cls_map();
		InitChess();
		if(player==BLACKPLAYER){
			ReverseBoard();
		}
		repaint();
	}
	private void InitChess(){
		//布置黑方棋子
		chess[0]=new Chess(BLACKPLAYER,"将",new Point(5,1));
		Map[5][1]=0;
		
		chess[1]=new Chess(BLACKPLAYER,"士",new Point(4,1));
		Map[4][1]=1;
		chess[2]=new Chess(BLACKPLAYER,"士",new Point(6,1));
		Map[6][1]=2;
		
		chess[3]=new Chess(BLACKPLAYER,"象",new Point(3,1));
		Map[3][1]=3;
		chess[4]=new Chess(BLACKPLAYER,"象",new Point(7,1));
		Map[7][1]=4;
		
		chess[5]=new Chess(BLACKPLAYER,"马",new Point(2,1));
		Map[2][1]=5;
		chess[6]=new Chess(BLACKPLAYER,"马",new Point(8,1));
		Map[8][1]=6;
		
		chess[7]=new Chess(BLACKPLAYER,"车",new Point(1,1));
		Map[1][1]=7;
		chess[8]=new Chess(BLACKPLAYER,"车",new Point(9,1));
		Map[9][1]=8;
		
		chess[9]=new Chess(BLACKPLAYER,"炮",new Point(2,3));
		Map[2][3]=9;
		chess[10]=new Chess(BLACKPLAYER,"炮",new Point(8,3));
		Map[8][3]=10;
		
		for(int i=0;i<=4;i++){
			chess[11+i]=new Chess(BLACKPLAYER,"卒",new Point(1+i*2,4));
			Map[1+i*2][4]=11+i;
		}
		//布置红方棋子
		chess[16]=new Chess(REDPLAYER,"帅",new Point(5,10));
		Map[5][10]=16;
		
		chess[17]=new Chess(REDPLAYER,"仕",new Point(4,10));
		Map[4][10]=17;		
		chess[18]=new Chess(REDPLAYER,"仕",new Point(6,10));
		Map[6][10]=18;
		
		chess[19]=new Chess(REDPLAYER,"相",new Point(3,10));
		Map[3][10]=19;		
		chess[20]=new Chess(REDPLAYER,"相",new Point(7,10));
		Map[7][10]=20;
		
		chess[21]=new Chess(REDPLAYER,"马",new Point(2,10));
		Map[2][10]=21;		
		chess[22]=new Chess(REDPLAYER,"马",new Point(8,10));
		Map[8][10]=22;
		
		chess[23]=new Chess(REDPLAYER,"车",new Point(1,10));
		Map[1][10]=23;		
		chess[24]=new Chess(REDPLAYER,"车",new Point(9,10));
		Map[9][10]=24;
		
		chess[25]=new Chess(REDPLAYER,"炮",new Point(2,8));
		Map[2][8]=25;		
		chess[26]=new Chess(REDPLAYER,"炮",new Point(8,8));
		Map[8][8]=26;
		
		for(int i=0;i<4;i++){
			chess[27+i]=new Chess(REDPLAYER,"兵",new Point(1+i*2,7));
			Map[1+i*2][7]=27+i;
		}
	}
	
	
	//重画场景中所有对象
	public void paint(Graphics g){
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		Image bgImage=Toolkit.getDefaultToolkit().getImage("pic\\qipan.jpg");
		//绘制背景棋盘
		g.drawImage(bgImage, 1, 20, this);
		//画棋子
		for(int i=0;i<32;i++){
			if(chess[i]!=null){
				chess[i].paint(g, this);
			}
		}
		if(firstChess2!=null){
			firstChess2.DrawSelectedChess(g);
		}
		if(secondChess2!=null){
			secondChess2.DrawSelectedChess(g);
		}
		g.drawString(message, 0, 450);
	}
		
		
	//调转棋盘视角
	private void ReverseBoard(){
		int x,y,c;
		//对调(x,y)与(10-x,11-y)处棋子
		for(int i=0;i<32;i++){
			if(chess[i]!=null){
				chess[i].ReversePos();
			}
		}
			
		//对调Map记录的棋子索引号
		for(x=1;x<=9;x++){
			for(y=1;y<=5;y++){
				if(Map[x][y]!=-1){
					c=Map[10-x][11-y];
					Map[10-x][11-y]=Map[x][y];
					Map[x][y]=c;
				}
			}
		}		
	}
	
	
	
	public final boolean IsAbleToPut(Chess firstchess, int x, int y) {
		int i,j,c;
		int oldx,oldy;//在棋盘原坐标
		oldx=firstchess.pos.x;
		oldy=firstchess.pos.y;
		String qi_name=firstchess.typeName;
		//帅的走棋规则
		if(qi_name.equals("将")||qi_name.equals("帅")){
			if((x-oldx)*(y-oldy)!=0){
				return false;
			}
			if(Math.abs(x-oldx)>1||Math.abs(y-oldy)>1){
				return false;
			}
			if(x<4||x>6||(y>3 && y<8)){
				return false;
			}
			return true;
		}
		//士的走棋规则
		if(qi_name.equals("士")||qi_name.equals("仕")){
			if((x-oldx)*(y-oldy)==0){
				return false;
			}
			if(Math.abs(x-oldx)>1 || Math.abs(y-oldy)>1){
				return false;
			}
			if(x<4||x>6 || (y>3 && y<8)){
				return false;
			}
			return true;
		}
		//象的走棋规则
		
		if(qi_name.equals("象")||qi_name.equals("相")){
			if((x-oldx)*(y-oldy)==0){
				return false;
			}
			if(Math.abs(x-oldx)!=2 || Math.abs(y-oldy)!=2){
				return false;
			}
			if(y<6){
				return false;
			}
			//我认为此规则有错误
			i=0;j=0;
			if(x-oldx==2){
				i=x-1;
			}
			if(x-oldx==-2){
				i=x+1;
			}
			if(y-oldy==2){
				j=y-1;
			}
			if(y-oldy==-2){
				j=y+1;
			}
			if(Map[i][j]!=-1){
				return false;
			}
			//我认为此规则有错误
			return true;
		}
		if(qi_name.equals("马")||qi_name.equals("马")){
			if(Math.abs(x-oldx)*Math.abs(y-oldy)!=2){
				return false;
			}
			if(x-oldx==2){
				if(Map[x-1][oldy]!=-1){
					return false;
				}
			}
			if(x-oldx==-2){
				if(Map[x+1][oldy]!=-1){
					return false;
				}
			}
			if(y-oldy==2){
				if(Map[oldx][y-1]!=-1){
					return false;
				}
			}
			if(y-oldy==-2){
				if(Map[oldx][y+1]!=-1){
					return false;
				}
			}
			return true;
		}
		if(qi_name.equals("车")||qi_name.equals("车")){
			//是否直线
			if((x-oldx)*(y-oldy)!=0){
				return false;
			}
			//判断是否有隔棋子
			if(x!=oldx){
				if(oldx>x){
					int t=x;
					x=oldx;
					oldx=t;
				}
				for(i=oldx;i<=x;i+=1){
					if(i!=x && i!=oldx){
						if(Map[i][y]!=-1){
							return false;
						}
					}
				}
			}
			if(y!=oldy){
				if(oldy>y){
					int t=y;
					y=oldy;
					oldy=t;
				}
				for(j=oldy;j<=y;j+=1){
					if(j!=y && j!=oldy){
						if(Map[x][j]!=-1){
							return false;
						}
					}
				}
			}
			return true;
		}
		if(qi_name.equals("炮")||qi_name.equals("炮")){
			boolean swapflagx=false;
			boolean swapflagy=false;
			if((x-oldx)*(y-oldy)!=0){
				return false;
			}
			c=0;
			if(x!=oldx){
				if(oldx>x){
					int t=x;
					x=oldx;
					oldx=t;
					swapflagx=true;
				}
				for(i=oldx;i<=x;i++){
					if(i!=x && i!=oldx){
						if(Map[i][y]!=-1){
							c=c+1;
						}
					}
				}
			}
			if(y!=oldy){
				if(oldy>y){
					int t=y;
					y=oldy;
					oldy=t;
					swapflagy=true;
				}
				for(j=oldy;j<=y;j++){
					if(j!=y && j!=oldy){
						if(Map[x][j]!=-1){
							c=c+1;
						}
					}
				}
			}
			if(c>1){//与目标处间隔1个以上棋子
				return false;
			}
			if(c==0){//与目标处无间隔棋子
				if(swapflagx==true){
					int t=x;
					x=oldx;
					oldx=t;
				}
				if(swapflagy==true){
					int t=y;
					y=oldy;
					oldy=t;
				}
				if(Map[x][y]!=-1){
					return false;
				}
			}
			if(c==1){//与目标处间隔1个棋子
				if(swapflagx==true){
					int t=x;
					x=oldx;
					oldx=t;
				}
				if(swapflagy==true){
					int t=y;
					y=oldy;
					oldy=t;
				}
				if(Map[x][y]==-1){
					return false;
				}
			}
			return true;
		}
		if(qi_name.equals("卒")||qi_name.equals("兵")){
			if((x-oldx)*(y-oldy)!=0){
				return false;
			}
			if(Math.abs(x-oldx)>1||Math.abs(y-oldy)>1){
				return false;
			}
			if(y>=6 && (x-oldx)!=0){
				return false;
			}
			if(y-oldy>0){
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//指定接受端口
		try {
			DatagramSocket s=new DatagramSocket(receiveport);
			byte[] data=new byte[100];
			DatagramPacket dgp=new DatagramPacket(data,data.length);
			//进入一个无限循环中来接收消息
			while(flag==true){
				s.receive(dgp);
				String strData=new String(data);
				String[] a=new String[6];
				a=strData.split("\\|");
				System.out.println("接收数据信息："+strData+"分割命令："+a[0]);
				if(a[0].equals("join")){
					LocalPlayer=BLACKPLAYER;
					//显示棋子
					NewGame(LocalPlayer);
					if(LocalPlayer==REDPLAYER){
						SetMyTurn(true);//能走棋
					}else{
						SetMyTurn(false);
					}
					//发送联机成功信息
					send("conn|");
				}else if(a[0].equals("conn")){//联机成功
					LocalPlayer=REDPLAYER;
					//显示棋子
					NewGame(LocalPlayer);
					if(LocalPlayer==REDPLAYER){
						SetMyTurn(true);
					}else{
						SetMyTurn(false);
					}
				}else if(a[0].equals("succ")){
					//获取传送信息到本地端口号的远程计算机IP地址
					if(a[1].equals("黑方赢了")){
						JOptionPane.showConfirmDialog(null, "黑方赢了你可以重新开始","你输了",JOptionPane.DEFAULT_OPTION);
					}
					if(a[1].equals("红方赢了")){
						JOptionPane.showConfirmDialog(null, "红方赢了你可以重新开始","你输了",JOptionPane.DEFAULT_OPTION);
					}
					message="你可以重新开局！";
				}else if(a[0].equals("move")){
					//对方的走棋信息，move|棋子走动索引号|X|Y
					int idx=Short.parseShort(a[1]);
					x2=Short.parseShort(a[2]);
					y2=Short.parseShort(a[3]);
					
					String z=a[4];
					message=x2+":"+y2;
					Chess c=chess[idx];
					x1=c.pos.x;
					y1=c.pos.y;
					
					//修改棋子位置，显示对方走棋
					idx=Map[x1][y1];
					int idx2=Map[x2][y2];
					
					Map[x1][y1]=-1;
					Map[x2][y2]=idx;
					
					chess[idx].SetPos(x2, y2);
					if(idx2!=-1){
						chess[idx2]=null;
					}
					repaint();
					IsMyTurn=true;
				}else if(a[0].equals("quit")){
					//对方退出信息
					JOptionPane.showConfirmDialog(null, "对方退出了，游戏结束","提示",JOptionPane.DEFAULT_OPTION);
					message="对方退出了，游戏结束";
					flag=false;
				}else if(a[0].equals("lose")){
					//对方认输
					JOptionPane.showConfirmDialog(null, "恭喜你，对方认输了","你赢了",JOptionPane.DEFAULT_OPTION);
					SetMyTurn(false);
				}	
				System.out.println(new String(data));
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("线程异常run方法："+e.toString());
		}catch (IOException ex) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("输入输出异常run方法："+ex.toString());
		}
	}

}
