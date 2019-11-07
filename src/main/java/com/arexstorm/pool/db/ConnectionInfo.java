package com.arexstorm.pool.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionInfo {

    private final static Logger logger = LoggerFactory.getLogger(ConnectionInfo.class);

    private ConcurrentHashMap<Connection, ConnectionStatus> connectionStatus = new ConcurrentHashMap<Connection, ConnectionStatus>();

    private String host;
    private String port;
    private String user;
    private String password;
    private String defaultDBName;
    private String dbType;
    private String monitorTable;

    public ConcurrentHashMap<Connection, ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConcurrentHashMap<Connection, ConnectionStatus> connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultDBName() {
        return defaultDBName;
    }

    public void setDefaultDBName(String defaultDBName) {
        this.defaultDBName = defaultDBName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getMonitorTable() {
        return monitorTable;
    }

    public void setMonitorTable(String monitorTable) {
        this.monitorTable = monitorTable;
    }

    /**
     * 添加连接
     * @param connection
     */
    public void addConnection(Connection connection) {
        this.addConnection(connection, ConnectionStatus.FREE);
    }

    public void addConnection(Connection connection, ConnectionStatus status) {
        this.connectionStatus.put(connection, status);
    }

    /**
     * 获取一个连接
     * @return
     */
    public synchronized Connection obtainConnection() throws SQLException {
        for (Map.Entry<Connection, ConnectionStatus> entry : connectionStatus.entrySet()) {
            if (ConnectionStatus.FREE.equals(entry.getValue()) && !entry.getKey().isClosed()) {
                entry.setValue(ConnectionStatus.BUSY);
                return entry.getKey();
            }
        }

        throw new SQLException("obtainConnection failed, no free connection avaliable");

        //TODO 超时等待机制
    }

    /**
     * 释放一个连接
     * @param connection
     */
    public synchronized void releaseConnection(Connection connection) {
        connectionStatus.put(connection, ConnectionStatus.FREE);
    }

    /**
     * 关闭连接
     */
    public void close() {
        connectionStatus.entrySet().stream()
            .filter(m -> !ConnectionStatus.CLOSED.equals(m.getValue()))
            .map(m -> m.getKey())
            .forEach(conn -> {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("release connection failed! {}", e.getMessage());
                }
        });
        connectionStatus.clear();
    }

    /**
     * 监控连接池
     */
    public void monitor() {
        int[] reNew = {0};
        connectionStatus.entrySet().stream().filter(m -> ConnectionStatus.FREE.equals(m.getValue())).map(m -> m.getKey()).forEach(conn -> {
            if (!isValid(conn)) {
                ++reNew[0];
                connectionStatus.put(conn, ConnectionStatus.CLOSED);
            }
        });

        // 重新分配
        if (reNew[0] > 0) {
            logger.info("[ConnectionInfo] monitor: {}:{}/{}, reNew: {}", host, port, dbType, reNew[0]);
            ConnectionInfoBuilder
                .init(this.host, this.port, this.user, this.password, this.defaultDBName, this.monitorTable)
                .buildConnection(dbType, reNew[0])
                .stream()
                .forEach(this::addConnection);
        }

    }

    /**
     * 检测连接是否还可用
     * @param connection
     * @return
     */
    private boolean isValid(Connection connection) {
       String validTemplate = String.format("select * from %s where 1 = 2 ", monitorTable);

       try (PreparedStatement pstmt = connection.prepareStatement(validTemplate)) {
           pstmt.executeQuery();
       } catch (SQLException e) {
           logger.error("[ConnectionInfo] isValid failed, {}", e.getMessage());
           return false;
       }

        return true;
    }
}
