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
			//加载驱动
			Class.forName(driver);
			//连接Mysql数据库
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
			System.out.println("关闭数据库问题");
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
			System.out.println("查询数据库问题");
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
			System.out.println("插入数据库问题");
			e.printStackTrace();
		}
		return false;
	}
}
