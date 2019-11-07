package com.arexstorm.pool.db;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class SQL {

    private static Logger logger = LoggerFactory.getLogger(SQL.class);
    private String dataSourceAlias;
    private ConnectionInfo connectionInfo;

    private SQL() {
    }

    public static SQL forDS(String alias) throws InvalidConnectionInfoNameException, SQLException {
        SQL sql = new SQL();
        if (!DbManager.hasConnectionInfo(alias)) {
            throw new InvalidConnectionInfoNameException("No connection info bound to: " + alias);
        } else {
            sql.dataSourceAlias = alias;
            sql.connectionInfo = DbManager.getConnectionInfo(alias).orElseThrow(() -> new SQLException("obtain connection info failed[alias: " + alias + "]!"));
            return sql;
        }
    }

    public Map<String, Object> getMetaData(String table) throws SQLException {
        Map<String, Object> metas = new HashMap<>();

        Connection connection = connectionInfo.obtainConnection();
        String sql = String.format("select * from %s limit 1", table);
        try (PreparedStatement pstms = connection.prepareStatement(sql)) {
            ResultSet rs = pstms.executeQuery();
            ResultSetMetaData metaRs = rs.getMetaData();
            for (int index = 1; index < metaRs.getColumnCount() + 1; ++index) {
                metas.put(metaRs.getColumnName(index), metaRs.getColumnTypeName(index));
            }
            connectionInfo.releaseConnection(connection);
        }

        return metas;
    }

    public List<Map<String, Object>> fetchData(String sql, Function<Map<String, String>, List<Map<String, Object>>> op) throws SQLException {
        Connection connection = connectionInfo.obtainConnection();
        try (PreparedStatement pstms = connection.prepareStatement(sql)) {
            List ret = new Results(Optional.ofNullable(pstms.executeQuery()), m -> m).toMap();
            connectionInfo.releaseConnection(connection);
            return ret;
        }
    }

    public Optional<Map<String, Object>> fetchOneRecord(String sql, Function<Map<String, String>, List<Map<String, Object>>> op) throws SQLException {
        sql = sql + " limit 1";
        List<Map<String, Object>> results = this.fetchData(sql, op);

        Map<String, Object> t = null;
        if (results.size() >= 1) {
            t = results.get(0);
        }

        return Optional.ofNullable(t);
    }

    private int executeUpdate(String sql) throws SQLException {
        Connection connection = connectionInfo.obtainConnection();
        try (PreparedStatement pstms = connection.prepareStatement(sql)) {
            int ret = pstms.executeUpdate();
            connectionInfo.releaseConnection(connection);
            return ret;
        }
    }

    public int insert(String sql) throws SQLException {
        return this.executeUpdate(sql);
    }

    public int update(String sql) throws SQLException {
        return this.executeUpdate(sql);
    }

    public void execute(String sql) throws SQLException {
        Connection connection = connectionInfo.obtainConnection();
        try (PreparedStatement pstms = connection.prepareStatement(sql)) {
            pstms.execute();
            connectionInfo.releaseConnection(connection);
        }
    }

    class Results<T> implements Iterator<T> {
        private int columnCount;
        private String[] columnNames;
        private String[] columnTypeNames;
        private final Set<String> numberTypes = Sets.newHashSet(new String[]{"FLOAT", "DOUBLE", "BIGINT", "INT", "DECIMAL"});
        private Function<Map<String, String>, T> op;
        private Optional<ResultSet> rs;

        public Results(Optional<ResultSet> rs, Function<Map<String, String>, T> op) {
            this.op = op;
            this.rs = rs;
            rs.ifPresent((resultSet) -> {
                try {
                    this.columnCount = resultSet.getMetaData().getColumnCount();
                    this.columnNames = new String[this.columnCount];
                    this.columnTypeNames = new String[this.columnCount];

                    for(int i = 0; i < this.columnCount; ++i) {
                        this.columnNames[i] = resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase();
                        int pos = this.columnNames[i].indexOf(46);
                        if (pos > 0) {
                            this.columnNames[i] = this.columnNames[i].substring(pos + 1);
                        }

                        String columnTypeName = resultSet.getMetaData().getColumnTypeName(i + 1);
                        this.columnTypeNames[i] = this.numberTypes.contains(columnTypeName) ? "NUMBER" : columnTypeName;
                    }
                } catch (SQLException e) {
                    logger.error("Cannot get metadata: {}" + e.getMessage());
                }

            });
        }

        public boolean hasNext() {
            boolean hasNext = false;
            if (this.rs.isPresent()) {
                try {
                    hasNext = ((ResultSet)this.rs.get()).next();
                } catch (SQLException e) {
                }
            }
            return hasNext;
        }

        public T next() {
            Map<String, String> result = new HashMap();
            this.rs.ifPresent((rs) -> {
                try {
                    for(int i = 0; i < this.columnCount; ++i) {
                        if (this.columnTypeNames[i].equals("NUMBER")) {
                            result.put(this.columnNames[i], rs.getBigDecimal(i + 1) == null ? "" : rs.getBigDecimal(i + 1).toPlainString());
                        } else {
                            result.put(this.columnNames[i], rs.getObject(i + 1) == null ? "" : rs.getObject(i + 1).toString());
                        }
                    }
                } catch (SQLException e) {
                    logger.error("Cannot map data: {}", e.getMessage());
                }

            });
            return this.op.apply(result);
        }

        public List<Map<String, Object>> toMap() {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> ret = new HashMap<>();
            this.forEachRemaining(item -> list.add((Map<String, Object>) item));
            return list;
        }
    }
}
