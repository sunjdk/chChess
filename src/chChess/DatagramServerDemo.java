package chChess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramServerDemo {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("服务器启动中 ...\n");
		DatagramSocket s=new DatagramSocket(10000);
		byte[] data=new byte[100];
		DatagramPacket dgp=new DatagramPacket(data,data.length);
		
		while(true){
			s.receive(dgp);
			System.out.println(new String(data));
			s.send(dgp);
		}
	}

}
