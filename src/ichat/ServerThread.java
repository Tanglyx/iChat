package ichat;

import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import encode.AES;
import encode.RSA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

// 负责处理每个线程通信的线程类
public class ServerThread implements Runnable{
	// 定义当前线程所处理的Socket
	Socket socket = null;
	// 该线程所处理的Socket所对应的输入流
	BufferedReader br = null;
	String[] getStr = null;
	OutputStream os = null;
	//处理过程包的数据
	JSONObject jsonReply = new JSONObject();
	String user;//用户ID
	//处理过程中间数据
	RSAPublicKey pubKey;
	RSAPrivateKey priKey;
	Key aesKey;
	String mingKey;
	//数据库
	Database masterDB = new Database();
	
	public ServerThread (Socket s) throws IOException {
		this.socket = s;
		// 初始化该Socket对应的输入流
		os = s.getOutputStream();
		br = new BufferedReader(new InputStreamReader(s.getInputStream() , "utf-8")); 
	}
	
	public void run() {
		String data = new String();
		try {
			while(((data = br.readLine())!=null)){
				JSONObject jsonObj = new JSONObject();
				jsonObj = packageHandle(data);
				if(jsonObj != null){
					if(jsonObj.getString("Flag").equals("Login")){
						actionLogin(jsonObj);
					}else if(jsonObj.getString("Flag").equals("Regist")){
						actionRegist(jsonObj);
					}else if(jsonObj.getString("Flag").equals("List")){
						actionList(jsonObj);
					}else if(jsonObj.getString("Flag").equals("Send")){
						actionSend(jsonObj);
					}
				}else {
					continue;
				}				
			}
			// 删除该Socket。
			MyServer.socketList.remove(user); 
			System.out.println("socket已断开");
			System.out.println(MyServer.socketList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONObject packageHandle(String string){
		JSONObject jsonDecode = new JSONObject();
		JSONObject jsonObj = JSONObject.fromObject(string);
		System.out.println(jsonObj);
		user = jsonObj.getString("ID");
		if(jsonObj.get("EncodeFlag").equals("0")){
			handle0();
			jsonDecode = null;
			return jsonDecode;
		}else if(jsonObj.get("EncodeFlag").equals("1")){
			jsonDecode = handle1(jsonObj);
			return jsonDecode;
		}else if(jsonObj.get("EncodeFlag").equals("2")){
			jsonDecode = handle2(jsonObj);
			return jsonDecode;
		}else{
			return null;
		}
	}
	
	private void handle0(){
		//产生RSA密钥对，并发送公钥
		HashMap<String, Object> map;
		try {
			map = RSA.getKeys();
			RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
			RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");			
			String modulus = publicKey.getModulus().toString();
			String public_exponent = publicKey.getPublicExponent().toString();
			String private_exponent = privateKey.getPrivateExponent().toString();
			//使用模和指数生成公钥和私钥
			pubKey = RSA.getPublicKey(modulus, public_exponent);
			priKey = RSA.getPrivateKey(modulus, private_exponent);
			System.out.println("pubKey:"+pubKey);
			System.out.println("priKey:"+priKey);
			//传公钥
			JSONObject jsonobj = new JSONObject();
			jsonobj.put("EncodeFlag", 0);
			jsonobj.put("public_exponent", public_exponent);
			jsonobj.put("modulus", modulus);
			os.write((jsonobj+ "\n").getBytes("utf-8"));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	private JSONObject handle1(JSONObject jsonObj){
		JSONObject jsonDecode = new JSONObject();
		System.out.println("解密前数据："+jsonObj.getString("EncodePackage"));
		try {
			//查找ID对应的KEY，用KEY解密EncodePackage包
			byte[] encryptData=AES.hexToBytes(jsonObj.getString("EncodePackage"));
			byte[] decryptData = AES.decrypt(encryptData, aesKey);//数据解密
//			System.out.println("解密后数据 byte[]:"+AES.showByteArray(decryptData));
			System.out.println("解密后数据 :"+new String(decryptData,"UTF-8"));
			jsonDecode = JSONObject.fromObject(new String(decryptData,"UTF-8"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return jsonDecode;
	}
	
	private JSONObject handle2(JSONObject jsonObj){
		JSONObject jsonDecode = new JSONObject();
		//RSA私钥解密获得对称密钥KEY并存储，用KEY解密EncodePackage包
		//RSA私钥解密获得KEY
		String mi = jsonObj.get("EncodeKey").toString();
		System.out.println("收到加密的AES密钥："+mi);
		int len = mi.length();
		String mi1 = mi.substring(0,256);
		String mi2 = mi.substring(256,len);
		try {
			//用RSA私钥解密Key后的明文
			String mingsecond1;
			mingsecond1 = RSA.decryptByPrivateKey(mi1, priKey);
			String mingsecond2 = RSA.decryptByPrivateKey(mi2, priKey);
			mingKey=mingsecond1+mingsecond2;
			System.err.println("用RSA私钥解密后的Key："+mingKey);
			//KEY存数据库?????
			//key解加密包
			byte[] encryptData=AES.hexToBytes(jsonObj.getString("EncodePackage"));
			String[] keyarray = new String[16];
			byte[] keybyte = new byte[16];
			for(int i=0; i<16; i++){
				keyarray[i] = mingKey.substring(i*8, (i+1)*8);
				keybyte[i] = (byte)Integer.parseInt(keyarray[i],2);
			}
		    System.out.println("AES的key："+AES.showByteArray(keybyte));
		    aesKey = AES.toKey(keybyte);
			byte[] decryptData = AES.decrypt(encryptData, aesKey);//数据解密
//			System.out.println("解密后数据 byte[]:"+AES.showByteArray(decryptData));
			System.out.println("解密后数据 :"+new String(decryptData,"UTF-8"));
			jsonDecode = JSONObject.fromObject(new String(decryptData,"UTF-8"));			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonDecode;
	}
	
	private JSONObject actionLogin (JSONObject jsonObj){
		JSONObject loginJson =new JSONObject();
		try {
			loginJson.put("Flag","Login");
			masterDB.connSQL();
			String sql = "select * from user_table where username = '" + user 
					+ "' and password = '" + jsonObj.getString("PW") + "';";  
			ResultSet resultSet = masterDB.selectSQL(sql);  
			if(resultSet.next() == true){
				loginJson.put("FlagSign","Yes");
				//添加新ID到socketList列表
				MyServer.socketList.put(user, socket);
				System.out.println(MyServer.socketList);
				//KEY存数据库
				masterDB.connSQL();
				//不存在就插入，存在就更新
				sql = "insert into key_table(username,userkey) values('" 
				+ jsonObj.getString("ID") + "','" + mingKey + "')" + "on DUPLICATE KEY UPDATE userkey = '" + mingKey + "';";
				masterDB.insertSQL(sql);
				masterDB.closeSQL();
			}							
			else
				loginJson.put("FlagSign","No");
			masterDB.closeSQL();
			System.out.println("loginJson"+loginJson);
			//用key加密
			byte[] encryptData = AES.encrypt(loginJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply加密后："+jsonReply);
			os.write((jsonReply+ "\n").getBytes("utf-8"));
			os.flush();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loginJson;
	}
	
	private void actionRegist (JSONObject jsonObj){
		JSONObject registJson =new JSONObject();
		registJson.put("Flag","Regist");
		masterDB.connSQL();
		String sql = "insert into user_table(username,password) values('" 
		+ user + "','" + jsonObj.getString("PW") + "');";   
		if(masterDB.insertSQL(sql) == true)
			registJson.put("FlagSign","Yes");			
		else
			registJson.put("FlagSign","No");
		masterDB.closeSQL();						
		System.out.println("registJson"+registJson);
		//用Key加密
		try {
			byte[] encryptData = AES.encrypt(registJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply加密后："+jsonReply);
			os.write((jsonReply+ "\n").getBytes("utf-8"));
			os.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void actionList (JSONObject jsonObj){
		JSONObject listJson =new JSONObject();
		JSONArray jsonA = new JSONArray();
		ArrayList<Map<String,String>> friendList = new ArrayList<>();//在线联系人列表   
		listJson.put("Flag","List");
    	//获取socketList中的key
		Iterator<String> friend = MyServer.socketList.keySet().iterator();
		jsonA = new JSONArray();
		while(friend.hasNext()){	
			Map<String,String> friendData = new HashMap<>();
			friendData.put("text", friend.next());
			jsonA.add(friendData);
		}
//		System.out.println("frendList:"+friendList);
		listJson.put("FriendList", jsonA);
		System.out.println("listJson"+listJson);
		//用key加密
		try {
			byte[] encryptData = AES.encrypt(listJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply加密后："+jsonReply);
			os.write((jsonReply+ "\n").getBytes("utf-8"));
			os.flush();
			jsonA = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void actionSend (JSONObject jsonObj){
		JSONObject sendJson =new JSONObject();
		String content = jsonObj.getString("Content");
		System.out.println(user+" send to "+jsonObj.getString("SendTo")+":" + content);
		try{
			//获取收信人Key
			masterDB.connSQL();
			String sql = "select * from Key_table where username = '" + jsonObj.getString("SendTo")+ "';";  
			ResultSet resultSet = masterDB.selectSQL(sql);  
			String sqlKey = new String();
			if(resultSet.next()){
				 sqlKey = resultSet.getString("userkey");
			}else{
				System.out.println("查找aesKey出错！");
			}			
			masterDB.closeSQL();
			String[] keyarray = new String[16];
			byte[] keybyte = new byte[16];
			for(int i=0; i<16; i++){
				keyarray[i] = sqlKey.substring(i*8, (i+1)*8);
				keybyte[i] = (byte)Integer.parseInt(keyarray[i],2);
			}
		    System.out.println("AES的key："+AES.showByteArray(keybyte));
		    Key keyto = AES.toKey(keybyte);
			//聊天记录存数据库
			masterDB.connSQL();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
			System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
			sql = "insert into chat_table(src_user,dest_user,content,time) values('" 
					+ user + "','" + jsonObj.getString("SendTo") + 
					"','" + jsonObj.getString("Content") +
					"','" + df.format(new Date())+"');";   
			if(masterDB.insertSQL(sql) == true)
				System.out.println("chat_table insert succeed");							
			else
				System.out.println("chat_table insert fail");
			masterDB.closeSQL();
			sendJson = jsonObj;
			System.out.println("sendJson:"+sendJson);
			//用收信人key加密
			byte[] encryptData=AES.encrypt(sendJson.toString().getBytes("utf-8"), keyto);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply加密后："+jsonReply);
			//消息转发
			socket = MyServer.socketList.get(jsonObj.getString("SendTo"));
			os = socket.getOutputStream();
			os.write((jsonReply + "\n").getBytes("utf-8"));
			os.flush();
			//socket回到原登陆人ID
			socket = MyServer.socketList.get(user);
			os = socket.getOutputStream();						
		}catch(Exception e){						
			// 删除该Socket。
			System.out.println(MyServer.socketList);
			System.out.println("对方已下线");
			e.printStackTrace();
		}
	}
}