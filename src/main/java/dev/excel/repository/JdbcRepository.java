package dev.excel.repository;

import dev.excel.dto.ColumnsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.SuperClassReflectionUtils.getStringQueryByAllFields;

@Slf4j
@Repository
public class JdbcRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
//        long start = System.currentTimeMillis();
//        long end = System.currentTimeMillis();
//        long executeTime = (end - start) / 1000;
//        log.info("EXECUTE TIME (excelUploadJdbcByBulkApi) ===> {}", executeTime);
    }

    /**
     * JDBC Get Connection (DataSource)
     * 트랜잭션 동기를 사용하려면 DataSourceUtils를 사용해야 한다.
     */
    private Connection getConnection() throws SQLException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("GET connection={}, class={}", con, con.getClass());
        return con;
    }

    /**
     * JDBC Connection Close
     */
    private void close(Connection con, Statement stmt, ResultSet rs) { // 커넥션 얻은 역순으로 close()
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야한다.
        DataSourceUtils.releaseConnection(con, dataSource);
        log.info("CLOSE connection={}, class={}", con, con.getClass());
    }

    public void insertData(MultipartFile uploadFile, String tableNm, String fieldStr) {
        List<ColumnsVO> dataList = getDataList(uploadFile);
        log.info("GET excelUploadJdbcByBulkApi LIST SIZE ==> {}", dataList.size());

        List<List<ColumnsVO>> ret = split(dataList, 10000);


        for (int j = 0; j < ret.size(); j++) {
            dataList = ret.get(j);

            Connection con = null;
            PreparedStatement pstmt = null;
            String sql = "insert into " + tableNm + " " + fieldStr + " VALUES ";

            System.out.println(sql);

            try {
                con = getConnection();
                String sqls = sql + getAppendQuery(dataList);
                pstmt = con.prepareStatement(sqls);
                pstmt.addBatch();
                pstmt.executeBatch();
            } catch (SQLException e) {
                throw exTranslator.translate("JDBCInsertData", sql, e);
            } finally {
                close(con, pstmt, null);
            }
        }
    }


    /**
     * Data Insert By JDBC
     */
    public void insertClazzData(MultipartFile uploadFile, String tableNm,
                                Class<?> clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<Object> dataList = getClazzDataList(uploadFile, clazz);
        log.info("GET excelUploadJdbcByBulkApi LIST SIZE ==> {}", dataList.size());

        List<List<Object>> ret = split(dataList, 200);
        String fieldStr = getStringQueryByAllFields(clazz);
        for (int j = 0; j < ret.size(); j++) {
            dataList = ret.get(j);

            Connection con = null;
            PreparedStatement pstmt = null;
            String sql = "insert into " + tableNm + " " + fieldStr + " VALUES ";
            log.info("SQL => {}", sql);

            try {
                con = getConnection();
                String sqls = sql + getAppendQueryByObj(dataList);
                pstmt = con.prepareStatement(sqls);
                pstmt.addBatch();
                pstmt.executeBatch();
            } catch (SQLException e) {
                throw exTranslator.translate("JDBCInsertData", sql, e);
            } finally {
                close(con, pstmt, null);
            }

        }
    }


}
