package com.tskj.core.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.sun.istack.internal.NotNull;
import com.tskj.core.config.ConfigUtility;
import com.tskj.core.system.consts.DBConsts;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库连接池
 * public方法：
 * getConnection()
 * getConnection(String fileName)
 *
 * @author LeonSu
 * @version v1.0.0.2
 * <p>
 * v1.0.0.2
 * 1.去掉了初始化连接
 * 2.修改了url不能正确识别的bug
 * 3.重新整理了目录结构，所有的配置文件都放在src/config下，db表示数据库配置文件，system表示系统配置文件
 * 4.重新定义了常量
 * @date 2018-09-26
 */
public class DbConnection {

    /**
     * 数据库类型
     */
    private static byte dbType = -1;

    public static void setType(byte value) {
        dbType = value;
    }

    public static byte getType() {
        return dbType;
    }

    //private static Properties properties = null;

    private static Properties loadPropertiesFromFile(String fileName) {
        ConfigUtility configUtility = new ConfigUtility();
        InputStream inputStream = configUtility.readDbConfig(fileName);
        if (null == inputStream) {
            return null;
        }
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(bis);
            setDbType(properties);
//            properties.loadFromXML(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static Properties loadPropertiesFromUrl(String urlName) {
        try {
            URL url = new URL(urlName);
            //打开连接获取连接对象
            URLConnection connection = url.openConnection();
            //能够进行远程写操作
            connection.setDoOutput(true);

            //接收返回响应信息
            StringBuilder response = new StringBuilder();
            Scanner in = new Scanner(connection.getInputStream());
            while (in.hasNextLine()) {
                response.append(in.nextLine());
                response.append("\n");
            }
            ByteArrayInputStream stream = new ByteArrayInputStream(response.toString().getBytes());
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PoolProperties setPoolProperties(Properties properties) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDbProperties(properties);

        poolProperties.setUrl(properties.getProperty(DBConsts.URL));
        poolProperties.setUsername(properties.getProperty(DBConsts.USERNAME));
        poolProperties.setPassword(properties.getProperty(DBConsts.PASSWORD));
        poolProperties.setDriverClassName(properties.getProperty(DBConsts.DRIVER_CLASS_NAME));

        //（整形值）池同时能分配的活跃连接的最大数目。默认为 100
        poolProperties.setMaxActive(Integer.parseInt(properties.getProperty(DBConsts.MAX_ACTIVE, "100")));

        //（整型值）池始终都应保留的连接的最大数目。默认为maxActive:100。会周期性检查空闲连接（如果启用该功能），
        // 留滞时间超过 minEvictableIdleTimeMillis 的空闲连接将会被释放。（请参考 testWhileIdle）
        poolProperties.setMaxIdle(Integer.parseInt(properties.getProperty(DBConsts.MAX_IDLE, "20")));

        //（整型值）池始终都应保留的连接的最小数目。如果验证查询失败，则连接池会缩减该值。默认值取自 initialSize:10
        poolProperties.setMinIdle(Integer.parseInt(properties.getProperty(DBConsts.MIN_IDLE, "10")));

        //（整型值）连接器启动时创建的初始连接数。默认为 10
        poolProperties.setInitialSize(Integer.parseInt(properties.getProperty(DBConsts.INITIAL_SIZE, "10")));

        //（整形值）把空闲时间超过minEvictableIdleTimeMillis毫秒的连接断开, 直到连接池中的连接数到minIdle为止 连接池中连接可空闲的时间,毫秒
        poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(properties.getProperty(DBConsts.MIN_EVICTABLE_IDLE_TIME_MILLIS, "60000")));

        //（整形值）毫秒秒检查一次连接池中空闲的连接
        poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(properties.getProperty(DBConsts.TIME_BETWEEN_EVICTION_RUNS_MILLIS, "5000")));

        poolProperties.setSuspectTimeout(Integer.parseInt(properties.getProperty(DBConsts.SUSPECT_TIMEOUT, "60")));

        //在连接返回给调用者前是否检测连接有效
        poolProperties.setTestOnBorrow(Boolean.getBoolean(properties.getProperty(DBConsts.TEST_ON_BORROW, "true")));

        //在连接返回给调用者前用于校验连接是否有效的SQL语句
        poolProperties.setValidationQuery(properties.getProperty(DBConsts.VALIDATION_QUERY, "SELECT 1"));

        //指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.
        poolProperties.setTestWhileIdle(Boolean.getBoolean(properties.getProperty(DBConsts.TEST_WHILE_IDLE, "true")));

        poolProperties.setValidationInterval(5000);

        poolProperties.setFairQueue(true);

        setDbType(properties);

        return poolProperties;
    }

    private static void setDbType(Properties properties) {
        if (properties.getProperty(DBConsts.URL).contains(DBConsts.SQL_SERVER)) {
            setType(DBConsts.DB_SQLSERVER);
        }
        if (properties.getProperty(DBConsts.URL).contains(DBConsts.MYSQL)) {
            setType(DBConsts.DB_MYSQL);
        }
        if (properties.getProperty(DBConsts.URL).contains(DBConsts.ORACLE)) {
            setType(DBConsts.DB_ORACLE);
        }
    }

    private static boolean isURL(String url) {
        //转换为小写
        url = url.toLowerCase();
        String regex = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~" +
                "\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$";
        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(url.trim());
        return mat.matches();
    }

    private static Properties getProperties(String fileName) {
        if (!fileName.isEmpty()) {
            if (!isURL(fileName)) {
                return loadPropertiesFromFile(fileName);
            } else {
                return loadPropertiesFromUrl(fileName);
            }
        } else {
            return loadPropertiesFromFile(DBConsts.DB_CONFIG_FILENAME);
        }
    }

    private static PoolProperties getPoolProperties(String fileName) {
        Properties properties = getProperties(fileName);
        return setPoolProperties(properties);
    }

    /**
     * 从连接池列表中获取第一个数据库连接池
     *
     * @return 成功返回Connection对象，失败返回null;
     */
    public static Connection getConnection() {
        return getConnection("");
    }

    private static HashMap<String, DruidDataSource> dataSources;

    static {
        dataSources = new HashMap<>();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setConnectProperties(getProperties(DBConsts.DB_CONFIG_FILENAME));
        addDataSource(DBConsts.DB_CONFIG_FILENAME, dataSource);
    }

    private static void addDataSource(@NotNull String fileName, DruidDataSource dataSource) {
        dataSources.put(fileName, dataSource);
    }

    /**
     * 获取数据库连接池
     *
     * @param fileName 数据库的配置文件名，包括扩展名，不含路径，比如：default.xml
     *                 获取数据库配置的完整url，如果此配置文件不在连接池列表中，则创建一个
     *                 连接池，并加入到列表中，配置文件是主键
     * @return 成功返回Connection对象，失败返回null;
     */
    public static Connection getConnection(@NotNull String fileName) {
        try {
            String tmpFileName;
            if (fileName.isEmpty()) {
                tmpFileName = DBConsts.DB_CONFIG_FILENAME;
            } else {
                tmpFileName = fileName;
            }
            if (dataSources.containsKey(tmpFileName) && dataSources.get(tmpFileName) != null) {
                Connection conn = dataSources.get(tmpFileName).getConnection();
                return conn;
            }
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setConnectProperties(getProperties(fileName));
            addDataSource(tmpFileName, dataSource);
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet rs, Statement st) {
        close(rs);
        close(st);
    }

    public static void close(Connection conn, ResultSet rs, Statement st) {
        close(rs);
        close(st);
        close(conn);
    }

    public static void main(String[] args) {
        Connection connection = getConnection("db.xml");
    }
}
