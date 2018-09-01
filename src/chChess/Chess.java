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
	public short player; //红子为 REDPLAYER  黑子为BLACKPLAYER
	public String typeName;//车马象士帅
	public Point pos; //位置
	private Image chessImage;
	
	public Chess(short player,String typeName,Point chesspos){
		this.player=player;
		this.typeName=typeName;
		this.pos=chesspos;
		//初始化棋子图案
		//不能使用switch，switch不支持String类型
		if(player==REDPLAYER){//红棋
			if(typeName.equals("帅"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\帅.png");
			else if(typeName.equals("仕"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\士.png");
			else if(typeName.equals("相"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\相.png");
			else if(typeName.equals("马"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\马.png");
			else if(typeName.equals("车"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\车.png");
			else if(typeName.equals("炮"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\炮.png");
			else if(typeName.equals("兵"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\兵.png");
		}else{//黑棋
			if(typeName.equals("将"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\将1.png");
			else if(typeName.equals("士"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\仕1.png");
			else if(typeName.equals("象"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\象1.png");
			else if(typeName.equals("马"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\马1.png");
			else if(typeName.equals("车"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\车1.png");
			else if(typeName.equals("炮"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\炮1.png");
			else if(typeName.equals("卒"))
				chessImage=Toolkit.getDefaultToolkit().getImage("pic\\卒1.png");
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
	//在指定的JPanel 上画棋子
	protected void paint(Graphics g,JPanel i){
		g.drawImage(chessImage, pos.x*40-40, pos.y*40-20, 40,40,(ImageObserver) i);		
	}
	public void DrawSelectedChess(Graphics g) {
		// TODO Auto-generated method stub
		g.drawRect(pos.x*40-40, pos.y*40-20, 40, 40);
	}
}
