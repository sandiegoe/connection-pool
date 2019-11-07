package com.arexstorm.pool.db;

/**
 * JDBC connection URL templates
 */
public enum JDBCUrl {
    /**
     * MySQL
     */
    MYSQL("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true"),

    /**
     * Hive
     */
    HIVE("jdbc:hive2://%s:%s/%s"),

	/**
	 * Oracle
	 */
	ORACLE("jdbc:oracle:thin:@%s:%s:%s"),

	/**
	 * SQLSERVER
	 */
	SQLSERVER("jdbc:sqlserver://%s:%s;DatabaseName=%s");

	/**
	 * Url template
     */
    private String urlTemplate;

    private JDBCUrl(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    /**
     * Get template string  of connection URL
     * @return
     */
    public String getUrlTemplate() {
        return this.urlTemplate;
    }

    /**
     * Get connection URL
     * @param host
     * @param port
     * @param dbName
     * @return
     */
    public String getUrl(String host, String port, String dbName) {
        return String.format(getUrlTemplate(), host, port, dbName);
    }
}
