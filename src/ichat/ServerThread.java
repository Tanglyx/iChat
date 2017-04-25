package ichat;
import java.awt.List;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

// 负责处理每个线程通信的线程类
public class ServerThread implements Runnable
{
	String u ;//登陆人账号ID
	ArrayList<Map<String,String>> friendList = new ArrayList<>();//在线联系人列表    
	// 定义当前线程所处理的Socket
	Socket s = null;
	// 该线程所处理的Socket所对应的输入流
	BufferedReader br = null;
	String[] getStr = null;
	OutputStream os = null;
	JSONObject jsonReply = new JSONObject();
	JSONArray jsonA;
	Database masterDB = new Database();
	
	public ServerThread (Socket s) throws IOException
	{
		this.s = s;
		// 初始化该Socket对应的输入流
		os = s.getOutputStream();
		br = new BufferedReader(new InputStreamReader(s.getInputStream() , "utf-8"));   // ②  
	}
	public void run()
	{			
		String Data = null;
		// 采用循环不断从Socket中读取客户端发送过来的数据
		while ((Data = readFromClient()) != null)
			{
		        System.out.println(Data);
		        JSONObject jsonObj = JSONObject.fromObject(Data);  
		        String Flag = jsonObj.getString("Flag");
		        //登陆请求处理
		        if(Flag.equals("Login")){
					try {
						jsonReply.put("Flag","Login");
						masterDB.connSQL();
						String sql = "select * from user_table where username = '" + jsonObj.getString("ID") 
								+ "' and password = '" + jsonObj.getString("PW") + "';";  
						ResultSet resultSet = masterDB.selectSQL(sql);  
						if(resultSet.next() == true){
							jsonReply.put("FlagSign","Yes");
							//添加新ID到socketList列表
							MyServer.socketList.put(jsonObj.getString("ID"), s);
							u = jsonObj.getString("ID");
							System.out.println(MyServer.socketList);
						}							
						else
							jsonReply.put("FlagSign","No");
						masterDB.closeSQL();
						System.out.println(jsonReply);
						os.write((jsonReply+ "\n").getBytes("utf-8"));
						os.flush();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
		        //注册请求处理
		        else if (Flag.equals("Regist")){
					try {
						jsonReply.put("Flag","Regist");
						masterDB.connSQL();
						String sql = "insert into user_table(username,password) values('" 
						+ jsonObj.getString("ID") + "','" + jsonObj.getString("PW") + "');";   
						if(masterDB.insertSQL(sql) == true)
							jsonReply.put("FlagSign","Yes");			
						else
							jsonReply.put("FlagSign","No");
						masterDB.closeSQL();						
						System.out.println(jsonReply);
						os.write((jsonReply+ "\n").getBytes("utf-8"));
						os.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		        //联系人列表List请求
		        else if (Flag.equals("List")){
					try {
			        	jsonReply.put("Flag","List");
			        	//获取socketList中的key
			    		Iterator<String> friend = MyServer.socketList.keySet().iterator();
			    		jsonA = new JSONArray();
			    		while(friend.hasNext()){	
			    			Map<String,String> friendData = new HashMap<>();
			    			friendData.put("text", friend.next());
			    			jsonA.add(friendData);
			    		}
//			    		System.out.println("frendList:"+friendList);
			        	jsonReply.put("FriendList", jsonA);
						System.out.println(jsonReply);
						os.write((jsonReply+ "\n").getBytes("utf-8"));
						os.flush();
						jsonA = null;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
		        }
		        //聊天请求处理
		        else{
					String content = jsonObj.getString("Content");
					System.out.println(u+" send to "+jsonObj.getString("SendTo")+":" + content);
					try{
						//消息转发
						s = MyServer.socketList.get(jsonObj.getString("SendTo"));
						os = s.getOutputStream();
						os.write((jsonObj + "\n").getBytes("utf-8"));
						os.flush();
						//聊天记录存数据库
						masterDB.connSQL();
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
						System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
						String sql = "insert into chat_table(src_user,dest_user,content,time) values('" 
								+ u + "','" + jsonObj.getString("SendTo") + 
								"','" + jsonObj.getString("Content") +
								"','" + df.format(new Date())+"');";   
						if(masterDB.insertSQL(sql) == true)
							System.out.println("chat_table insert succeed");							
						else
							System.out.println("chat_table insert fail");
						masterDB.closeSQL();
						//socket回到原登陆人ID
						s = MyServer.socketList.get(u);
						os = s.getOutputStream();						
					}catch(Exception e){						
						// 删除该Socket。
						System.out.println(MyServer.socketList);
						System.out.println("对方已下线");
						e.printStackTrace();
					}
				}
			}
		// 删除该Socket。
		MyServer.socketList.remove(u); 
		System.out.println("socket已断开");
		System.out.println(MyServer.socketList);
	}
	 
	private String readFromClient()
	{
		try
		{
			return br.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
