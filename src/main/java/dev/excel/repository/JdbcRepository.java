package dev.excel.repository;

import dev.excel.utils.exception.DataSqlException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.SuperClassReflectionUtils.getStringQueryByAllFields;
import static dev.excel.utils.connection.ConnectionConst.*;
import static dev.excel.utils.connection.DBConnectionUtil.*;

@Slf4j
@Repository
public class JdbcRepository {

    private final DataSource dataSource;

    private String tableNm = JDBC_TABLE;

    public JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * PreparedStatement에 setValue 후 addBatch, executeBatch 성능 테스트에 사용
     */
    public void insertBatchData(String path, Class<?> clazz) {
        deleteAll(tableNm); // 기존 데이터 삭제

        List<T> dataList = getClazzDataList(path, clazz);

        List<List<T>> ret = split(dataList, JDBC_UPLOAD_SPLIT_SIZE);
        String fieldStr = getStringQueryByAllFields(clazz);


        Connection con = null;
        PreparedStatement pstmt = null;
        CallableStatement cs = null;
        con = getConnection(dataSource);
        String sql = "insert into " + tableNm + " " + fieldStr + " VALUES " +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            pstmt = con.prepareStatement(sql);

            for (int j = 0; j < ret.size(); j++) {
                dataList = ret.get(j);
                for (int i = 0; i < ret.get(j).size(); i++) {
                    Object t = dataList.get(i);

                    pstmt = setPreparedStatement(t, clazz, pstmt);
//                    pstmt.execute();
                    pstmt.addBatch();
                    pstmt.clearParameters();

                }

                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        } catch (SQLException e) {
            throw new DataSqlException("insertBatchData SQL error", e);
        } finally {
            close(con, pstmt, null, dataSource);
        }
    }

    /**
     * #INSERT
     * Data Insert By JDBC Bulk API
     * ex) insert into TABLE_NAME (column1, colum2) VALUES (1, A), (2, B), (3, C) ,,,;
     */
    public void insertMultiRowData(String path, Class<?> clazz) {
        deleteAll(tableNm); // 기존 데이터 삭제

        List<T> dataList = getClazzDataList(path, clazz);

        List<List<T>> ret = split(dataList, JDBC_UPLOAD_SPLIT_SIZE);
        String fieldStr = getStringQueryByAllFields(clazz);


        Connection con = getConnection(dataSource);
        PreparedStatement pstmt = null;
        String sql = "insert into " + tableNm + " " + fieldStr + " VALUES ";

        try {
            for (int j = 0; j < ret.size(); j++) {
                dataList = ret.get(j);

                String sqls = sql + getAppendQueryByObj(dataList, clazz);
                pstmt = con.prepareStatement(sqls);
                pstmt.addBatch();
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new DataSqlException("insertMultiRowData SQL error", e);
        } finally {
            close(con, pstmt, null, dataSource);
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
        con = getConnection(dataSource);
        try {
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) rowCount = rs.getInt(1);
        } catch (SQLException e) {
            throw new DataSqlException("getDataSize SQL error", e);
        } finally {
            close(con, pstmt, null, dataSource);
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
//        String sql = "select * from " + tableNm;
//        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = getConnection(dataSource);
        try {
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            pstmt.setFetchSize(JDBC_SELECT_FETCH_SIZE);

            List<T> result = resultSetToObj(rs, clazz);
            return result;
        } catch (SQLException e) {
            throw new DataSqlException("getDataInfo SQL error", e);
        } finally {
            close(con, pstmt, null, dataSource);
        }
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
            throw new DataSqlException("deleteAll SQL error", e);
        } finally {
            close(con, pstmt, null, dataSource);
        }
    }
}
