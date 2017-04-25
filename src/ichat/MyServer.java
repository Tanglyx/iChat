package ichat;
import java.net.*;
import java.io.*;
import java.util.*;

public class MyServer
{
	// 定义保存所有Socket的ArrayList
	public static Map<String,Socket> socketList = new HashMap<>();

    public static void main(String[] args)
		throws IOException
    {
		ServerSocket ss = new ServerSocket(12345);
		while(true)
		{
			// 此行代码会阻塞，将一直等待别人的连接
			Socket s = ss.accept();
			System.out.println("new socket come in");
			// 每当客户端连接后启动一条ServerThread线程为该客户端服务
			new Thread(new ServerThread(s)).start();
		}
    }
}
