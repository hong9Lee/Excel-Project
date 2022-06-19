package dev.excel.utils.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(ConnectionConst.URL, ConnectionConst.USERNAME, ConnectionConst.PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }

    public static Connection getConnectionByBulkApi() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/excel_data?user=root&password=102030&useBulkCopyForBatchInsert=true;");
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }

    public static void close(Connection con, Statement stmt, ResultSet rs) { // 커넥션 얻은 역순으로 close()
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }
}
