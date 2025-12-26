//private static final String URL = "jdbc:mysql://192.168.12.54:3306/data_collection?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8";
//private static final String USER = "renjie"; // 改为远程授权的用户名renjie
//private static final String PASSWORD = "909166980lrj"; // 改为新密码

//private static final String URL = "jdbc:mysql://localhost:3306/data_collection?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8";
//	private static final String USER = "root"; // 改为远程授权的用户名renjie
	//private static final String PASSWORD = "13332543179lrj"; // 改为新密码

package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	// 原URL
	// 核心修改点：
	// 1. localhost → 192.168.12.54（远程MySQL服务器IP）
	private static final String URL = "jdbc:mysql://localhost:3306/data_collection?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8";
	private static final String USER = "renjie"; // 改为远程授权的用户名renjie
		private static final String PASSWORD = "909166980lrj"; // 改为新密码


	
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动找不到");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}