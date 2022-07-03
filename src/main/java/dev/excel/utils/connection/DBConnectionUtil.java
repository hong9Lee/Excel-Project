package dev.excel.utils.connection;

import dev.excel.utils.exception.DataSqlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;

@Slf4j
public class DBConnectionUtil {

    /**
     * JDBC Get Connection (DataSource)
     * 트랜잭션 동기를 사용하려면 DataSourceUtils를 사용해야 한다.
     */
    public static Connection getConnection(DataSource dataSource) {
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("GET connection={}, class={}", con, con.getClass());
        return con;
    }

    /**
     * JDBC Connection Close
     */
    public static void close(Connection con, Statement stmt, ResultSet rs, DataSource dataSource) { // 커넥션 얻은 역순으로 close()
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야한다.
        DataSourceUtils.releaseConnection(con, dataSource);
        log.info("CLOSE connection={}, class={}", con, con.getClass());
    }
}
