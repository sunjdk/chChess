package chChess;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Frmchess extends JFrame{
	ChessBoard panel2=new ChessBoard();
	JButton button1=new JButton("认输");
	JButton button2=new JButton("开始");
	
	JTextField jTextField1=new JTextField();
	JTextField jTextField2=new JTextField();
	
	public static final short REDPLAYER=1;
	public static final short BLACKPLAYER=0;
	
	public Frmchess(){
		JPanel panel1=new JPanel(new BorderLayout());
		JPanel panel3=new JPanel(new BorderLayout());
		String urlString="pic\\帅.png";
		JLabel label=new JLabel(new ImageIcon(urlString));
		
		panel1.add(label,BorderLayout.CENTER);
		panel2.setLayout(new BorderLayout());
		panel3.setLayout(new FlowLayout());
		
		JLabel jLabel1=new JLabel("输入IP");
		JLabel jLabel2=new JLabel("输入对方端口");
		
		panel3.add(jLabel1);
		panel3.add(jLabel2);
		
		jTextField1.setText("127.0.0.1");
		jTextField2.setText("3004");
		
		panel3.add(jLabel1);
		panel3.add(jTextField1);
		panel3.add(jLabel2);
		panel3.add(jTextField2);
		panel3.add(button1);
		panel3.add(button2);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel1, BorderLayout.NORTH);
		this.getContentPane().add(panel2, BorderLayout.CENTER);
		this.getContentPane().add(panel3, BorderLayout.SOUTH);
		
		this.setSize(380, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("网络中国象棋游戏");
		
		this.setVisible(true);
		button1.setEnabled(false);
		button2.setEnabled(true);
		setVisible(true);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try{
					panel2.send("quit|");
					System.exit(0);
				}catch(Exception ex){
					
				}
			}
		});
		/**
		 * 鼠标监听事件
		 */
		button1.addMouseListener(new MouseAdapter(){//认输按钮
			public void mouseClicked(MouseEvent e){
				panel2.send("lose|");
			}
		});
		
		button2.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				String ip=jTextField1.getText();
				int remoteport=Integer.parseInt(jTextField2.getText());
				int receiveport;
				if(remoteport==3003)
					receiveport=3004;
				else
					receiveport=3003;
				panel2.startJoin(ip, remoteport, receiveport);
				button1.setEnabled(true);
				button2.setEnabled(true);
			}
		});
	}
	public static void main(String[] args) {
		Frmchess f=new Frmchess();
	}

}
