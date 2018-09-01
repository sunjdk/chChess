package chChess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramDemo {
	public static void main(String args[]){
		String host="localhost";
		DatagramSocket s=null;
		
		try {
			s=new DatagramSocket();
			
			byte[] buffer;
			buffer=new String("Datagram 发送的报文，这里是客户端，会循环1000次，每秒1次消息，直到发完为止").getBytes();

			InetAddress ia=InetAddress.getByName(host);
			DatagramPacket dgp=new DatagramPacket(buffer,buffer.length,ia,10000);
			for(int i=0;i<1000;i++){
				s.send(dgp);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("线程中断异常"+e.toString());
				}
			}
						
			byte[] buffer2=new byte[100];
			dgp=new DatagramPacket(buffer2,buffer.length,ia,10000);
			s.receive(dgp);
			System.out.println(new String(dgp.getData()));
		} catch (SocketException e) {
			//e.printStackTrace();
			System.out.println("socket 异常："+e.toString());
		}catch (UnknownHostException e) {
			//e.printStackTrace();
			System.out.println("未知主机异常："+e.toString());
		}catch (IOException e) {
			//e.printStackTrace();
			System.out.println("输入输出异常："+e.toString());
		}finally{
			if(s!=null){
				s.close();
			}
		}
		
	}
}
