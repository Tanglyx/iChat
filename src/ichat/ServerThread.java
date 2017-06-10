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

// ������ÿ���߳�ͨ�ŵ��߳���
public class ServerThread implements Runnable{
	// ���嵱ǰ�߳��������Socket
	Socket socket = null;
	// ���߳��������Socket����Ӧ��������
	BufferedReader br = null;
	String[] getStr = null;
	OutputStream os = null;
	//������̰�������
	JSONObject jsonReply = new JSONObject();
	String user;//�û�ID
	//��������м�����
	RSAPublicKey pubKey;
	RSAPrivateKey priKey;
	Key aesKey;
	String mingKey;
	//���ݿ�
	Database masterDB = new Database();
	
	public ServerThread (Socket s) throws IOException {
		this.socket = s;
		// ��ʼ����Socket��Ӧ��������
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
			// ɾ����Socket��
			MyServer.socketList.remove(user); 
			System.out.println("socket�ѶϿ�");
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
		//����RSA��Կ�ԣ������͹�Կ
		HashMap<String, Object> map;
		try {
			map = RSA.getKeys();
			RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
			RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");			
			String modulus = publicKey.getModulus().toString();
			String public_exponent = publicKey.getPublicExponent().toString();
			String private_exponent = privateKey.getPrivateExponent().toString();
			//ʹ��ģ��ָ�����ɹ�Կ��˽Կ
			pubKey = RSA.getPublicKey(modulus, public_exponent);
			priKey = RSA.getPrivateKey(modulus, private_exponent);
			System.out.println("pubKey:"+pubKey);
			System.out.println("priKey:"+priKey);
			//����Կ
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
		System.out.println("����ǰ���ݣ�"+jsonObj.getString("EncodePackage"));
		try {
			//����ID��Ӧ��KEY����KEY����EncodePackage��
			byte[] encryptData=AES.hexToBytes(jsonObj.getString("EncodePackage"));
			byte[] decryptData = AES.decrypt(encryptData, aesKey);//���ݽ���
//			System.out.println("���ܺ����� byte[]:"+AES.showByteArray(decryptData));
			System.out.println("���ܺ����� :"+new String(decryptData,"UTF-8"));
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
		//RSA˽Կ���ܻ�öԳ���ԿKEY���洢����KEY����EncodePackage��
		//RSA˽Կ���ܻ��KEY
		String mi = jsonObj.get("EncodeKey").toString();
		System.out.println("�յ����ܵ�AES��Կ��"+mi);
		int len = mi.length();
		String mi1 = mi.substring(0,256);
		String mi2 = mi.substring(256,len);
		try {
			//��RSA˽Կ����Key�������
			String mingsecond1;
			mingsecond1 = RSA.decryptByPrivateKey(mi1, priKey);
			String mingsecond2 = RSA.decryptByPrivateKey(mi2, priKey);
			mingKey=mingsecond1+mingsecond2;
			System.err.println("��RSA˽Կ���ܺ��Key��"+mingKey);
			//KEY�����ݿ�?????
			//key����ܰ�
			byte[] encryptData=AES.hexToBytes(jsonObj.getString("EncodePackage"));
			String[] keyarray = new String[16];
			byte[] keybyte = new byte[16];
			for(int i=0; i<16; i++){
				keyarray[i] = mingKey.substring(i*8, (i+1)*8);
				keybyte[i] = (byte)Integer.parseInt(keyarray[i],2);
			}
		    System.out.println("AES��key��"+AES.showByteArray(keybyte));
		    aesKey = AES.toKey(keybyte);
			byte[] decryptData = AES.decrypt(encryptData, aesKey);//���ݽ���
//			System.out.println("���ܺ����� byte[]:"+AES.showByteArray(decryptData));
			System.out.println("���ܺ����� :"+new String(decryptData,"UTF-8"));
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
				//�����ID��socketList�б�
				MyServer.socketList.put(user, socket);
				System.out.println(MyServer.socketList);
				//KEY�����ݿ�
				masterDB.connSQL();
				//�����ھͲ��룬���ھ͸���
				sql = "insert into key_table(username,userkey) values('" 
				+ jsonObj.getString("ID") + "','" + mingKey + "')" + "on DUPLICATE KEY UPDATE userkey = '" + mingKey + "';";
				masterDB.insertSQL(sql);
				masterDB.closeSQL();
			}							
			else
				loginJson.put("FlagSign","No");
			masterDB.closeSQL();
			System.out.println("loginJson"+loginJson);
			//��key����
			byte[] encryptData = AES.encrypt(loginJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply���ܺ�"+jsonReply);
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
		//��Key����
		try {
			byte[] encryptData = AES.encrypt(registJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply���ܺ�"+jsonReply);
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
		ArrayList<Map<String,String>> friendList = new ArrayList<>();//������ϵ���б�   
		listJson.put("Flag","List");
    	//��ȡsocketList�е�key
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
		//��key����
		try {
			byte[] encryptData = AES.encrypt(listJson.toString().getBytes("utf-8"), aesKey);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply���ܺ�"+jsonReply);
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
			//��ȡ������Key
			masterDB.connSQL();
			String sql = "select * from Key_table where username = '" + jsonObj.getString("SendTo")+ "';";  
			ResultSet resultSet = masterDB.selectSQL(sql);  
			String sqlKey = new String();
			if(resultSet.next()){
				 sqlKey = resultSet.getString("userkey");
			}else{
				System.out.println("����aesKey����");
			}			
			masterDB.closeSQL();
			String[] keyarray = new String[16];
			byte[] keybyte = new byte[16];
			for(int i=0; i<16; i++){
				keyarray[i] = sqlKey.substring(i*8, (i+1)*8);
				keybyte[i] = (byte)Integer.parseInt(keyarray[i],2);
			}
		    System.out.println("AES��key��"+AES.showByteArray(keybyte));
		    Key keyto = AES.toKey(keybyte);
			//�����¼�����ݿ�
			masterDB.connSQL();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
			System.out.println(df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��
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
			//��������key����
			byte[] encryptData=AES.encrypt(sendJson.toString().getBytes("utf-8"), keyto);
			String data = AES.bytes2Hex(encryptData);
			jsonReply.put("EncodePackage", data);
			jsonReply.put("EncodeFlag", "1");
			System.out.println("jsonReply���ܺ�"+jsonReply);
			//��Ϣת��
			socket = MyServer.socketList.get(jsonObj.getString("SendTo"));
			os = socket.getOutputStream();
			os.write((jsonReply + "\n").getBytes("utf-8"));
			os.flush();
			//socket�ص�ԭ��½��ID
			socket = MyServer.socketList.get(user);
			os = socket.getOutputStream();						
		}catch(Exception e){						
			// ɾ����Socket��
			System.out.println(MyServer.socketList);
			System.out.println("�Է�������");
			e.printStackTrace();
		}
	}
}