package com.arexstorm.pool.db;

public enum DriverClass {

	/**
	 * MySQL
	 */
	MYSQL("com.mysql.jdbc.Driver"),

	/**
	 * Hive
	 */
	HIVE("org.apache.hive.jdbc.HiveDriver"),

	/**
	 * Oracle
	 */
	ORACLE("oracle.jdbc.driver.OracleDriver"),

	/**
	 * SQLSERVER
	 */
	SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver");

	String driver;

	DriverClass(String driver) {
		this.driver = driver;
	}

	public String getDriver() {
		return driver;
	}
}
