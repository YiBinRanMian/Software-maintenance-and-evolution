package ws.test.ws.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ws.test.ws.wsdl.WSDLParser;

/**
 * @author ：kai
 * @date ：Created in 2020/6/21 16:24
 * @description：database access
 */
public class DBHelper {
    static final String DB_URL = "jdbc:mysql://39.107.140.102:3306/param_matching";
    /** MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL */
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final Logger logger = Logger.getLogger(WSDLParser.class);

    static final String PASS = "WStesting123";
    /** 数据库的用户名与密码，需要根据自己的设置. */
    static final String USER = "param_matching";

    /** 从数据库中获取，返回一个字符串list（以便生成多个测试用例）. */
    public static ArrayList<String> retriveFromDataBase(String type, String param, int number){
        List<String> usualTypes = Arrays.asList("int","double","float","boolean"); //TODO: 有待进一步完善
        ArrayList<String> params = new ArrayList<>();
        DBHelper dbHelper = new DBHelper();
        dbHelper.getConnection();
        Connection conn = dbHelper.getConn();
        //传入处理过的参数值和occurs
        System.out.println("#################当前匹配的参数："+param);
        String sqlString1 = "select code,value from parameter_semantic where tag like \"%"+param+"%\" order by rand() limit "+number+";";
        String sqlString2 = "select value from parameter_semantic where tag like \"%"+param+"%\" order by rand() limit "+number+";";
        String sqlString3 = "select value from usual_types where type ='"+type+"' order by rand() limit "+number+";";
        try {
            PreparedStatement psta = null;
            if(usualTypes.contains(type)) {  //如果是常见非String类型int, double, float等等，查询usual_types表
                psta=conn.prepareStatement(sqlString3);
            }
            else {  //对于一般的String类型参数
                psta=conn.prepareStatement(sqlString2);
            }
            ResultSet rs = psta.executeQuery();
            while (rs.next()) {
                logger.info("retriving from database: " +  rs.getString(1));
                params.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(params.isEmpty()) {
            logger.info("Failed to retrive value from database.");
        }
        //不管params中有没有结果，都返回，交给后面来判断
        return params;
    }

    private Connection conn;

    private Statement stmt;

    /** 执行增删改. */
    public void execete(String sqlString) {
        getConnection();
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            conn=null;
        }
    }

    public Connection getConn() {
        return conn;
    }

    public void getConnection() {
        //1.加载驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("未能成功加载驱动程序，请检查是否导入驱动程序！");
            //添加一个语句，如果加载驱动异常，检查是否添加驱动，或者添加驱动字符串是否错误
            e.printStackTrace();
        }

        //2.连接数据库
        try {
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
//		    System.out.println("获取数据库连接成功！");
        } catch (SQLException e) {
            System.out.println("获取数据库连接失败！");
            //添加一个语句，如果连接失败，检查连接字符串或者登录名以及密码是否错误
            e.printStackTrace();
        }
    }

    /** 执行查询. */
    public int getResultNum(String sqlString) {
        getConnection();
        ResultSet rs = null;
        int count = 0;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlString);
            while(rs.next()) {
                count+=1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            conn=null;
        }
        return count;
    }

    public Statement getStmt() {
        return stmt;
    }

    /** 测试方法，没有用处. */
    public void QueryTest()
    {
        getConnection();
        //3.进行查询
        try {
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tag,value,code from parameter_semantic";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String tag  = rs.getString("tag");
                String value = rs.getString("value");
                String code = rs.getString("code");

                // 输出数据
                System.out.print("tag: " + tag);
                System.out.print(", value: " + value);
                System.out.print(", code: " + code);
                System.out.print("\n");
            }
        } catch(SQLException e) {
            System.out.println("查询失败！请检查语句！");
            //添加一个语句，如果查询失败，检查SQL语句是否错误
            e.printStackTrace();
        }
        //查询完后就要关闭
        if(conn!=null)
        {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                conn=null;
            }
        }
    }
}
