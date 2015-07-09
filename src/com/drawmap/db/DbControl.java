package com.drawmap.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;




public class DbControl {
	private static String dbName = "shp.db";
	private Connection conn ;
	
	public DbControl(String dbFile)
	{
		dbName = dbFile;
		init();
	}
	
	public synchronized void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+dbName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public Connection getConnection(){   //��ȡ��ݿ������
		
		if(conn == null)
		{
			try {
				conn = DriverManager.getConnection("jdbc:sqlite:"+dbName);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conn;
	}
	
	public int execute(String sql)   //ִ�д�������Ϊ�����?
	{
		Statement statement;
		int result = 0;
		try {
			statement = conn.createStatement();
			statement.setQueryTimeout(30);
			result = statement.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public ResultSet query(String sql)  //ִ�в�����䣬����һ��ResultSet���͵Ķ���
	{
		Statement statement;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			statement.setQueryTimeout(30);
			result = statement.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void close()   //�ر�����ݿ������
	{
		try {
			if(conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
