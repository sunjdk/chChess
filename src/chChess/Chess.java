package chChess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

class Chess {
	public static final short REDPLAYER=1;
	public static final short BLACKPLAYER=0;
	public short player; //����Ϊ REDPLAYER  ����ΪBLACKPLAYER
	public String typeName;//������ʿ˧
	public Point pos; //λ��
	private Image chessImage;
	
	public Chess(short player,String typeName,Point chesspos){
		this.player=player;
		this.typeName=typeName;
		this.pos=chesspos;
		//��ʼ������ͼ��
		//����ʹ��switch��switch��֧��String����
		if(player==REDPLAYER){//����
			if(typeName.equals("˧"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\˧.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\ʿ.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��.png");
		}else{//����
			if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("ʿ"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
			else if(typeName.equals("��"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\��1.png");
		}
	}
	public void SetPos(int x,int y){
		pos.x=x;
		pos.y=y;
	}
	public void ReversePos(){
		pos.x=10-pos.x;
		pos.y=11-pos.y;
	}
	//��ָ����JPanel �ϻ�����
	protected void paint(Graphics g,JPanel i){
		g.drawImage(chessImage, pos.x*40-40, pos.y*40-20, 40,40,(ImageObserver) i);		
	}
	public void DrawSelectedChess(Graphics g) {
		// TODO Auto-generated method stub
		g.drawRect(pos.x*40-40, pos.y*40-20, 40, 40);
	}
}
