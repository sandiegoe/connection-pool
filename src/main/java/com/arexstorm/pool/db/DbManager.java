package com.arexstorm.pool.db;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 提供给大数据埋点统计指标同步使用，大数据埋点存储在hive中，使用同步程序将hive中增量数据同步到指定库中
 * 打算使用Hikari数据库连接池，但是hive-jdbc-1.1.0.jar中HiveConnection.java 中有部分方法未实现，直接抛出Method not support exception
 * 造成连接池方式使用有问题，升级jar包和大数据cdh环境版本不匹配
 * 1) 这里通过为每个db维护若干个连接，供查询时使用
 * 2) 使用时自动检查每个连接的状态，如果不可用自动切换重连接
 * 3) 使用完毕释放连接
 */
public class DbManager {

    private static ConcurrentHashMap<String, ConnectionInfo> connectionInfos = new ConcurrentHashMap<>();

    // 启动后台线程监控connection状态
    private static boolean daemonMonitorThreadEnable = true;

    private volatile static boolean isRunning = true;

    static {
        if (daemonMonitorThreadEnable) {
            DbManager.daemonMonitor();
        }
    }

    public static void setConnectionInfo(String alias, ConnectionInfo connectionInfo) {
        DbManager.connectionInfos.put(alias, connectionInfo);
    }

    public static boolean hasConnectionInfo(String alias) {
        return DbManager.connectionInfos.containsKey(alias);
    }

    public static Optional<ConnectionInfo> getConnectionInfo(String alias) {
        return Optional.ofNullable(DbManager.connectionInfos.get(alias));
    }

    public static void shutdown() {
        DbManager.connectionInfos.values().stream().forEach(connectionInfo -> connectionInfo.close());
        isRunning = false;
    }

    public static void removeConnectionInfo(String alias) {
        DbManager.connectionInfos.remove(alias);
    }

    // 单独一个线程来检测连接状态
    // 不足的话，自动补充新的连接
    public static void daemonMonitor() {
        new Thread(() -> {
            while (isRunning) {
                DbManager.connectionInfos.values().stream().forEach(info -> info.monitor());
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
