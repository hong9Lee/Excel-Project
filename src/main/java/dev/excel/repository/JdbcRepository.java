package dev.excel.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.SuperClassReflectionUtils.getStringQueryByAllFields;
import static dev.excel.utils.connection.DBConnectionUtil.close;
import static dev.excel.utils.connection.DBConnectionUtil.getConnection;

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
     * Data Insert By JDBC
     */
    public void insertClazzData(MultipartFile uploadFile, String tableNm, Class<?> clazz) {
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
                con = getConnection(dataSource);
                String sqls = sql + getAppendQueryByObj(dataList);
                pstmt = con.prepareStatement(sqls);
                pstmt.addBatch();
                pstmt.executeBatch();
            } catch (SQLException e) {
                throw exTranslator.translate("JDBCInsertData", sql, e);
            } finally {
                close(con, pstmt, null, dataSource);
            }

        }
    }


}
