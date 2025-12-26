package com.listener;//处理tomcat内存泄露

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

@WebListener // 注解注册监听器，无需手动修改web.xml
public class AppCleanupListener implements ServletContextListener {

    // 应用启动时执行（无需处理）
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    // 应用停止/重新加载时执行（核心清理逻辑）
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 1. 注销所有JDBC驱动（避免驱动残留）
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("已注销JDBC驱动：" + driver.getClass().getName());
            } catch (SQLException e) {
                System.err.println("注销JDBC驱动失败：" + e.getMessage());
            }
        }

        // 2. 强制终止MySQL的连接清理线程（解决核心错误）
        try {
            // 中断线程
            AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("已终止MySQL连接清理线程");
        } catch (Exception e) {
            System.err.println("终止MySQL连接清理线程失败：" + e.getMessage());
        }

        // 3. 清理线程池残留（可选，进一步避免内存泄漏）
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread thread : threadArray) {
            if (thread.getName().contains("mysql-cj-abandoned-connection-cleanup")) {
                thread.interrupt();
                System.out.println("已中断残留的MySQL线程：" + thread.getName());
            }
        }
    }
}