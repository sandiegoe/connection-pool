package com.arexstorm.pool.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SQLSpec {

    public static void main(String[] args) throws InvalidConnectionInfoNameException, SQLException {

        // 初始化连接信息
        DbManager.setConnectionInfo("mysql", ConnectionInfoBuilder.init(
            "localhost",
            "3306",
            "root",
            "passw0rd",
            "pool",
            "monitor"
        ).build("MYSQL"));

        // 获取操作的SQL对象
        SQL mysql = SQL.forDS("mysql");

        // 测试获取数据
        List<Map<String, Object>> results = mysql.fetchData("select * from pool_test", null);
        results.stream().forEach(System.out::println);

        // 测试获取单条数据
        System.out.println(mysql.fetchOneRecord("select * from pool_test", null));

        // 测试插入
        mysql.insert("insert into monitor(id, test) values (1, 'test')");

        // 关闭
        DbManager.shutdown();
    }
}
