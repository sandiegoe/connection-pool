package com.arexstorm.pool.db;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionInfoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionInfoBuilder.class);

    private String host;
    private String port;
    private String user;
    private String password;
    private String defaultDBName;
    private String jdbcUrl;
    private Integer poolSize = 5;
    private String monitorTable;

    private ConnectionInfoBuilder() {
    }

    public static ConnectionInfoBuilder init(String host, String port, String user, String password, String defaultDBName, String monitorTable) {
        return init(host, port, user, password, defaultDBName, null, monitorTable);
    }

    public static ConnectionInfoBuilder init(String host, String port, String user, String password, String defaultDBName, String jdbcUrl, String monitorTable) {
        ConnectionInfoBuilder builder = new ConnectionInfoBuilder();
        builder.setHost(host);
        builder.setPort(port);
        builder.setUser(user);
        builder.setPassword(password);
        builder.setDefaultDBName(defaultDBName);
        builder.setJdbcUrl(jdbcUrl);
        builder.setMonitorTable(monitorTable);
        return builder;
    }

    public ConnectionInfoBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public ConnectionInfoBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    public ConnectionInfoBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public ConnectionInfoBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public ConnectionInfoBuilder setDefaultDBName(String defaultDBName) {
        this.defaultDBName = defaultDBName;
        return this;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setMonitorTable(String monitorTable) {
        this.monitorTable = monitorTable;
    }

    public ConnectionInfo build(String dbType) {

        ConnectionInfo connectionInfo = new ConnectionInfo();

        buildConnection(dbType, poolSize).stream().forEach(connectionInfo::addConnection);

        copyProperties(connectionInfo, dbType);

        return connectionInfo;
    }

    private void copyProperties(ConnectionInfo connectionInfo, String dbType) {
        connectionInfo.setHost(this.host);
        connectionInfo.setPort(this.port);
        connectionInfo.setUser(this.user);
        connectionInfo.setPassword(this.password);
        connectionInfo.setDefaultDBName(this.defaultDBName);
        connectionInfo.setDbType(dbType);
        connectionInfo.setMonitorTable(this.monitorTable);
    }

    public Connection buildConnection(String dbType) {
        return buildConnection(dbType, 1).get(0);
    }

    public List<Connection> buildConnection(String dbType, int require) {
        List<Connection> connections = new ArrayList<>();

        try {
            String url = StringUtils.isNotBlank(this.jdbcUrl) ?
                this.jdbcUrl : JDBCUrl.valueOf(dbType).getUrl(this.host, this.port, this.defaultDBName);
            Class.forName(DriverClass.valueOf(dbType).getDriver());
            for (int i = 0; i < require; ++i) {
                connections.add(DriverManager.getConnection(url, this.user, this.password));
            }

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("connection info build failed, msg: {}", e.getMessage());
            e.printStackTrace();
        }

        return connections;
    }
}
