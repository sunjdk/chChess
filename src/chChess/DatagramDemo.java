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
			buffer=new String("Datagram ���͵ı��ģ������ǿͻ��ˣ���ѭ��1000�Σ�ÿ��1����Ϣ��ֱ������Ϊֹ").getBytes();

			InetAddress ia=InetAddress.getByName(host);
			DatagramPacket dgp=new DatagramPacket(buffer,buffer.length,ia,10000);
			for(int i=0;i<1000;i++){
				s.send(dgp);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("�߳��ж��쳣"+e.toString());
				}
			}
						
			byte[] buffer2=new byte[100];
			dgp=new DatagramPacket(buffer2,buffer.length,ia,10000);
			s.receive(dgp);
			System.out.println(new String(dgp.getData()));
		} catch (SocketException e) {
			//e.printStackTrace();
			System.out.println("socket �쳣��"+e.toString());
		}catch (UnknownHostException e) {
			//e.printStackTrace();
			System.out.println("δ֪�����쳣��"+e.toString());
		}catch (IOException e) {
			//e.printStackTrace();
			System.out.println("��������쳣��"+e.toString());
		}finally{
			if(s!=null){
				s.close();
			}
		}
		
	}
}
