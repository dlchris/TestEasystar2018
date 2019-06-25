package com.tskj.core.system.consts;

public class DBConsts {
    /**
     * 数据库配置文件说明
     * 如果是普通文件，则此配置文件必须在classes目录下的config/db子目录中，例如：default.xml
     * 如果是url，则需要把core单独部署，例如：http://127.0.0.1/core/GetDBConfigAction，主机地址必须是IP
     * 如果是空，则从core的DBConnection类中获取
     */
    public static final String DB_CONFIG_FILENAME = "default.properties";
    //public static final String DB_CONFIG_URL = "http://127.0.0.1/core/GetDBConfigAction";

    public static final byte DB_SQLSERVER = 0;
    public static final byte DB_MYSQL = 1;
    public static final byte DB_ORACLE = 2;

    /**
     * 数据库配置文件用到的参数定义
     * START
     */
    public static final String MAX_ACTIVE = "maxActive";
    public static final String MAX_IDLE = "maxIdle";
    public static final String MIN_IDLE = "minIdle";
    public static final String INITIAL_SIZE = "initialSize";
    public static final String VALIDATION_QUERY = "validationQuery";
    public static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
    public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
    public static final String SUSPECT_TIMEOUT ="suspectTimeout";
    public static final String TEST_ON_BORROW = "testOnBorrow";
    public static final String TEST_WHILE_IDLE = "testWhileIdle";

    public static final String SQL_SERVER = "sqlserver";
    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    public static final String URL = "druid.url";
    public static final String USERNAME = "druid.username";
    public static final String PASSWORD = "druid.password";
    public static final String DRIVER_CLASS_NAME = "druid.driverClassName";

    /**
     * END
     */
}
