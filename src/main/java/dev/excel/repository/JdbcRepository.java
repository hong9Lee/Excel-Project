package dev.excel.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collections;
import java.util.List;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.SuperClassReflectionUtils.getStringQueryByAllFields;
import static dev.excel.utils.connection.ConnectionConst.*;
import static dev.excel.utils.connection.DBConnectionUtil.*;
import static dev.excel.utils.connection.DBConnectionUtil.getConnectionByBulkApi;

@Slf4j
@Repository
public class JdbcRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    /**
     * #INSERT
     * Data Insert By JDBC Bulk API
     * ex) insert into TABLE_NAME (column1, colum2) VALUES (1, A), (2, B), (3, C) ,,,;
     */
    public void insertClazzData(String path, Class<?> clazz) {
        String tableNm = JDBC_TABLE;
        deleteAll(tableNm); // 기존 데이터 삭제

        List<T> dataList = getClazzDataList(path, clazz);

        List<List<T>> ret = split(dataList, JDBC_UPLOAD_SPLIT_SIZE);
        String fieldStr = getStringQueryByAllFields(clazz);

        for (int j = 0; j < ret.size(); j++) {
            dataList = ret.get(j);

            Connection con = null;
            PreparedStatement pstmt = null;
            String sql = "insert into " + tableNm + " " + fieldStr + " VALUES ";

            try {
                con = getConnection(dataSource);
                String sqls = sql + getAppendQueryByObj(dataList, clazz);
                pstmt = con.prepareStatement(sqls);
                pstmt.addBatch();
                pstmt.executeBatch();
            } catch (SQLException e) {
                log.error("SQLException", e);
                throw exTranslator.translate("JDBCInsertData", sql, e);
            } finally {
                close(con, pstmt, null, dataSource);
            }
        }
    }

    /**
     * #SELECT
     * Get Number of data in the table
     */
    public int getDataSize(String tableNm) {
        String sql = "select count(*) from " + tableNm + ";";
        int rowCount = 0;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnectionByBulkApi();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) rowCount = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowCount;
    }

    /**
     * #SELECT
     * [JDBC] 데이터 가져오는 메서드 - Paging( OFFSET )
     */
    public <T> List<T> getDataInfo(int num, int limit, String tableNm, Class<?> clazz) {
        if (num != 0) num = num * limit;
//        String sql = "select * from " + tableNm + " LIMIT " + limit + " OFFSET " + num + ";";
        String sql = "SELECT * FROM " + tableNm + " as p JOIN ( SELECT id FROM " + tableNm + " LIMIT " + limit + " OFFSET " + num + " ) AS t ON p.id = t.id;";

        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnectionByBulkApi();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            pstmt.setFetchSize(JDBC_SELECT_FETCH_SIZE);

            List<T> result = resultSetToObj(rs, clazz);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * #DELETE
     * 테이블의 모든 데이터 삭제
     */
    public void deleteAll(String tableNm) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "delete from " + tableNm + ";";
        log.info("SQL => {}", sql);

        try {
            con = getConnection(dataSource);
            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("SQLException", e);
            throw exTranslator.translate("JDBCInsertData", sql, e);
        } finally {
            close(con, pstmt, null, dataSource);
        }
    }
}
