 package ichat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.PreparedStatement;

public class Database {
	private static Connection con;
	private final String driver = "com.mysql.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:3306/ichat";
	private final String user = "root";
	private final String pw = "hello";
	private PreparedStatement statement = null;  
	
	public void connSQL(){
		try {
			//��������
			Class.forName(driver);
			//����Mysql���ݿ�
			con = DriverManager.getConnection(url,user,pw);
			if(!con.isClosed())
				System.out.println("Succeeded connecting to the Database!");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Sorry, can't find the Driver!");
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Sorry, can't connect the DataBase!");
			e.printStackTrace();
		}
	}
	
	public void closeSQL(){
		try {
			if(con!=null)
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("�ر����ݿ�����");
			e.printStackTrace();
		}
	}
	
	public ResultSet selectSQL(String sql){
		ResultSet rs = null;
		try {
			statement = (PreparedStatement) con.prepareStatement(sql);
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("��ѯ���ݿ�����");
			e.printStackTrace();
		}
		return rs;		
	}
	
	public boolean insertSQL(String sql){
		try {
			statement = (PreparedStatement) con.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("�������ݿ�����");
			e.printStackTrace();
		}
		return false;
	}
}
