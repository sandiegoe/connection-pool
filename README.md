# pool

## 1. 配置db连接信息
修改src/main/resource/config目录下的application-dev.yml配置文件
```
datasource:
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:mysql://localhost:3306/pool?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: passw0rd

修改数据库ip地址
创建pool数据库
修改数据库用户名/密码信息
```

## 2. 自动创建相关表
在src/main/java/PoolApp.java 中右击run，会启动应用，并自动创建相关表：monitor(用于监控连接状态使用), pool_test(用于测试使用)   
在出现如下信息时，表示自动创建成功，此时可以关闭应用
```
...
Liquibase has updated your database in 1094 ms
...
```

## 3. 测试connection-pool
Test class：src/test/java/com/arexstorm/pool/db/SQLSepc.java   
右击运行
