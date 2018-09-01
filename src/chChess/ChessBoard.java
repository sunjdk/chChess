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
	//�߳�������ʶλ
	private boolean flag=false;
	private int otherport;//�Է��˿�
	private int receiveport;//�������ܶ˿�
	
	public void startJoin(String ip,int otherport,int receiveport){
		flag=true;
		this.otherport=otherport;
		this.receiveport=receiveport;
		send("join|");
		//����һ���߳�
		Thread th=new Thread(this);
		//�����߳�
		th.start();
		message="�̴߳��ڵȴ�����״̬!";
	}
	
	public ChessBoard(){
		r=20;
		cls_map();
		/**
		 * ����¼�����
		 */
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(IsMyTurn==false){
					message="�öԷ�����";
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
					//��һ�ε�������
					firstChess2=analyse(e.getX(),e.getY());
					x1=tempx;
					y1=tempy;
					if(firstChess2!=null){
						if(firstChess2.player!=LocalPlayer){
							message="�����ɶԷ�������";
							return ;
						}
						first=false;
					}
				}else{
					//�ڶ��ε�������
					secondChess2=analyse(e.getX(),e.getY());
					x2=tempx;
					y2=tempy;
					//������Լ������ӣ����ϴ�ѡ�������
					if(secondChess2!=null){
						if(secondChess2.player==LocalPlayer){
							//ȡ���ϴ�ѡ�������
							firstChess2=secondChess2;
							x1=tempx;
							y1=tempy;
							secondChess2=null;
							return;
						}
					}
					if(secondChess2==null){//Ŀ�괦û���ӣ��ƶ�����
						if(IsAbleToPut(firstChess2,x2,y2)){
							//��Mapȡ��ԭCurSelect����
							idx=Map[x1][y1];
							Map[x1][y1]=-1;
							Map[x2][y2]=idx;
							chess[idx].SetPos(x2, y2);
							//send
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							//CurSelect=0;
							first=true;
							repaint();
							SetMyTurn(false);//�öԷ���
							//toolStripStatusLabel1.Text="";
						}else{
							//��������
							message="�������������";
						}
						return ;
					}
					if(secondChess2!=null && IsAbleToPut(firstChess2,x2,y2)){//���Գ���
						first=true;
						//��mapȥ��ԭCurSelect����
						idx=Map[x1][y1];
						idx2=Map[x2][y2];
						Map[x1][y1]=-1;
						Map[x2][y2]=idx;
						chess[idx].SetPos(x2, y2);
						chess[idx2]=null;
						repaint();
						if(idx2==0){//0--------��
							message="�췽Ӯ��";
							JOptionPane.showConfirmDialog(null, message, "��ʾ", JOptionPane.DEFAULT_OPTION);
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							send("succ"+"|"+"�췽Ӯ��"+"|");
							return;
						}
						if(idx==16){//16--------˧
							message="�ڷ�Ӯ��";
							JOptionPane.showConfirmDialog(null, message, "��ʾ", JOptionPane.DEFAULT_OPTION);
							send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
							send("succ"+"|"+message+"|");
							return;
						}
						send("move"+"|"+idx+"|"+(10-x2)+"|"+String.valueOf(11-y2)+"|");
						SetMyTurn(false);//�öԷ���
					}else{//���ܳ���
						message="���ܳ���";
					}
				}				
			}			
		});
	}
	//�������֮�£����Ӷ���
	private Chess analyse(int x, int y) {
		tempx=(int)Math.floor((double)x/40)+1;
		tempy=(int)Math.floor((double)(y-20)/40)+1;
		
		System.out.println(x+","+y);
		System.out.println(tempx+","+tempy);
		//��ֹ������Χ
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
			message="������ʼ����";
		}else{
			message="�Է�����˼��";
		}
	}
	
	
	
	public void send(String str) {
		// TODO Auto-generated method stub
		DatagramSocket s=null;
		try {	
			s=new DatagramSocket();
			byte[] buffer;
			buffer=new String(str).getBytes();
			InetAddress ia=InetAddress.getLocalHost();//������ַ
			//Ŀ��������ַ
			DatagramPacket dgp=new DatagramPacket(buffer,buffer.length,ia,otherport);
			s.send(dgp);
			System.out.println("������Ϣ��"+str);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("δ֪�����쳣��"+e.toString());
		}catch (IOException ex) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("��������쳣��"+ex.toString());
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
		//���úڷ�����
		chess[0]=new Chess(BLACKPLAYER,"��",new Point(5,1));
		Map[5][1]=0;
		
		chess[1]=new Chess(BLACKPLAYER,"ʿ",new Point(4,1));
		Map[4][1]=1;
		chess[2]=new Chess(BLACKPLAYER,"ʿ",new Point(6,1));
		Map[6][1]=2;
		
		chess[3]=new Chess(BLACKPLAYER,"��",new Point(3,1));
		Map[3][1]=3;
		chess[4]=new Chess(BLACKPLAYER,"��",new Point(7,1));
		Map[7][1]=4;
		
		chess[5]=new Chess(BLACKPLAYER,"��",new Point(2,1));
		Map[2][1]=5;
		chess[6]=new Chess(BLACKPLAYER,"��",new Point(8,1));
		Map[8][1]=6;
		
		chess[7]=new Chess(BLACKPLAYER,"��",new Point(1,1));
		Map[1][1]=7;
		chess[8]=new Chess(BLACKPLAYER,"��",new Point(9,1));
		Map[9][1]=8;
		
		chess[9]=new Chess(BLACKPLAYER,"��",new Point(2,3));
		Map[2][3]=9;
		chess[10]=new Chess(BLACKPLAYER,"��",new Point(8,3));
		Map[8][3]=10;
		
		for(int i=0;i<=4;i++){
			chess[11+i]=new Chess(BLACKPLAYER,"��",new Point(1+i*2,4));
			Map[1+i*2][4]=11+i;
		}
		//���ú췽����
		chess[16]=new Chess(REDPLAYER,"˧",new Point(5,10));
		Map[5][10]=16;
		
		chess[17]=new Chess(REDPLAYER,"��",new Point(4,10));
		Map[4][10]=17;		
		chess[18]=new Chess(REDPLAYER,"��",new Point(6,10));
		Map[6][10]=18;
		
		chess[19]=new Chess(REDPLAYER,"��",new Point(3,10));
		Map[3][10]=19;		
		chess[20]=new Chess(REDPLAYER,"��",new Point(7,10));
		Map[7][10]=20;
		
		chess[21]=new Chess(REDPLAYER,"��",new Point(2,10));
		Map[2][10]=21;		
		chess[22]=new Chess(REDPLAYER,"��",new Point(8,10));
		Map[8][10]=22;
		
		chess[23]=new Chess(REDPLAYER,"��",new Point(1,10));
		Map[1][10]=23;		
		chess[24]=new Chess(REDPLAYER,"��",new Point(9,10));
		Map[9][10]=24;
		
		chess[25]=new Chess(REDPLAYER,"��",new Point(2,8));
		Map[2][8]=25;		
		chess[26]=new Chess(REDPLAYER,"��",new Point(8,8));
		Map[8][8]=26;
		
		for(int i=0;i<4;i++){
			chess[27+i]=new Chess(REDPLAYER,"��",new Point(1+i*2,7));
			Map[1+i*2][7]=27+i;
		}
	}
	
	
	//�ػ����������ж���
	public void paint(Graphics g){
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		Image bgImage=Toolkit.getDefaultToolkit().getImage("pic\\qipan.jpg");
		//���Ʊ�������
		g.drawImage(bgImage, 1, 20, this);
		//������
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
		
		
	//��ת�����ӽ�
	private void ReverseBoard(){
		int x,y,c;
		//�Ե�(x,y)��(10-x,11-y)������
		for(int i=0;i<32;i++){
			if(chess[i]!=null){
				chess[i].ReversePos();
			}
		}
			
		//�Ե�Map��¼������������
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
		int oldx,oldy;//������ԭ����
		oldx=firstchess.pos.x;
		oldy=firstchess.pos.y;
		String qi_name=firstchess.typeName;
		//˧���������
		if(qi_name.equals("��")||qi_name.equals("˧")){
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
		//ʿ���������
		if(qi_name.equals("ʿ")||qi_name.equals("��")){
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
		//����������
		
		if(qi_name.equals("��")||qi_name.equals("��")){
			if((x-oldx)*(y-oldy)==0){
				return false;
			}
			if(Math.abs(x-oldx)!=2 || Math.abs(y-oldy)!=2){
				return false;
			}
			if(y<6){
				return false;
			}
			//����Ϊ�˹����д���
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
			//����Ϊ�˹����д���
			return true;
		}
		if(qi_name.equals("��")||qi_name.equals("��")){
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
		if(qi_name.equals("��")||qi_name.equals("��")){
			//�Ƿ�ֱ��
			if((x-oldx)*(y-oldy)!=0){
				return false;
			}
			//�ж��Ƿ��и�����
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
		if(qi_name.equals("��")||qi_name.equals("��")){
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
			if(c>1){//��Ŀ�괦���1����������
				return false;
			}
			if(c==0){//��Ŀ�괦�޼������
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
			if(c==1){//��Ŀ�괦���1������
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
		if(qi_name.equals("��")||qi_name.equals("��")){
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
		//ָ�����ܶ˿�
		try {
			DatagramSocket s=new DatagramSocket(receiveport);
			byte[] data=new byte[100];
			DatagramPacket dgp=new DatagramPacket(data,data.length);
			//����һ������ѭ������������Ϣ
			while(flag==true){
				s.receive(dgp);
				String strData=new String(data);
				String[] a=new String[6];
				a=strData.split("\\|");
				System.out.println("����������Ϣ��"+strData+"�ָ����"+a[0]);
				if(a[0].equals("join")){
					LocalPlayer=BLACKPLAYER;
					//��ʾ����
					NewGame(LocalPlayer);
					if(LocalPlayer==REDPLAYER){
						SetMyTurn(true);//������
					}else{
						SetMyTurn(false);
					}
					//���������ɹ���Ϣ
					send("conn|");
				}else if(a[0].equals("conn")){//�����ɹ�
					LocalPlayer=REDPLAYER;
					//��ʾ����
					NewGame(LocalPlayer);
					if(LocalPlayer==REDPLAYER){
						SetMyTurn(true);
					}else{
						SetMyTurn(false);
					}
				}else if(a[0].equals("succ")){
					//��ȡ������Ϣ�����ض˿ںŵ�Զ�̼����IP��ַ
					if(a[1].equals("�ڷ�Ӯ��")){
						JOptionPane.showConfirmDialog(null, "�ڷ�Ӯ����������¿�ʼ","������",JOptionPane.DEFAULT_OPTION);
					}
					if(a[1].equals("�췽Ӯ��")){
						JOptionPane.showConfirmDialog(null, "�췽Ӯ����������¿�ʼ","������",JOptionPane.DEFAULT_OPTION);
					}
					message="��������¿��֣�";
				}else if(a[0].equals("move")){
					//�Է���������Ϣ��move|�����߶�������|X|Y
					int idx=Short.parseShort(a[1]);
					x2=Short.parseShort(a[2]);
					y2=Short.parseShort(a[3]);
					
					String z=a[4];
					message=x2+":"+y2;
					Chess c=chess[idx];
					x1=c.pos.x;
					y1=c.pos.y;
					
					//�޸�����λ�ã���ʾ�Է�����
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
					//�Է��˳���Ϣ
					JOptionPane.showConfirmDialog(null, "�Է��˳��ˣ���Ϸ����","��ʾ",JOptionPane.DEFAULT_OPTION);
					message="�Է��˳��ˣ���Ϸ����";
					flag=false;
				}else if(a[0].equals("lose")){
					//�Է�����
					JOptionPane.showConfirmDialog(null, "��ϲ�㣬�Է�������","��Ӯ��",JOptionPane.DEFAULT_OPTION);
					SetMyTurn(false);
				}	
				System.out.println(new String(data));
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("�߳��쳣run������"+e.toString());
		}catch (IOException ex) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("��������쳣run������"+ex.toString());
		}
	}

}
