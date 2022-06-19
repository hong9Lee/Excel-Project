package dev.excel.service;

import dev.excel.dto.ColumnsVO;
import dev.excel.dto.SampleVO;
import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.MybatisRepository;
import dev.excel.utils.handler.SheetExcelFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static dev.excel.utils.DataUtils.ResultSetToObj;
import static dev.excel.utils.connection.DBConnectionUtil.getConnectionByBulkApi;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadService {

    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;

    public void excelDownloadJdbc(HttpServletResponse response)
            throws SQLException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        /**
         * SET
         * [tableNm, VO.Class, splitSize]
         */
        String tableNm = "excel_data2";
        SheetExcelFile<ColumnsVO> excelFile = new SheetExcelFile<>(ColumnsVO.class);
        int jdbcDataSize = getJdbcDataSize(tableNm);
        int splitSize = 50000;


        List<Object> dataList = new ArrayList<>();
        if (jdbcDataSize < splitSize) {
            dataList = getJdbcDataInfo(tableNm);
        } else {
            for (int i = 0; i < jdbcDataSize / splitSize; i++) {
                dataList = getJdbcDataInfo(i, splitSize, tableNm);
            }
        }

        log.info("DATA SIZE => {}", dataList.size());

        /**
         * SET
         * [VO.Class]
         */
        List<ColumnsVO> retList = (List<ColumnsVO>) (List<?>) dataList;

        excelFile.addRows(retList);
        excelFile.write(response.getOutputStream());
        response.getOutputStream().close();
    }

    /**
     * Get Number of data in the table
     */
    public int getJdbcDataSize(String tableNm) throws SQLException {
        String sql = "select count(*) from " + tableNm + ";";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnectionByBulkApi();
        pstmt = con.prepareStatement(sql);
        rs = pstmt.executeQuery();

        int rowcount = 0;
        if (rs.next()) rowcount = rs.getInt(1);
        return rowcount;
    }

    /**
     * [JDBC] 데이터 가져오는 메서드 - Paging( OFFSET )
     */
    public List<Object> getJdbcDataInfo(int num, int limit, String tableNm) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (num != 0) num = num * limit;
        String sql = "select * from " + tableNm + " LIMIT " + limit + " OFFSET " + num + ";";

        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnectionByBulkApi();
        pstmt = con.prepareStatement(sql);
        rs = pstmt.executeQuery();
        pstmt.setFetchSize(20000);

        List<Object> result = ResultSetToObj(rs, ColumnsVO.class);
        return result;
    }

    /**
     * [JDBC] 데이터 가져오는 메서드
     */
    public List<Object> getJdbcDataInfo(String tableNm) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String sql = "select * from " + tableNm + ";";
        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnectionByBulkApi();
        pstmt = con.prepareStatement(sql);
        rs = pstmt.executeQuery();
        pstmt.setFetchSize(500);

        List<Object> result = ResultSetToObj(rs, ColumnsVO.class);
        return result;
    }






    public ArrayList<Class<?>> getMyBatisDataInfo() {
        return mybatisRepository.findAll();
    }

    public List<SampleVO> getJpaDataInfo() {
         return jpaRepository.findAll();
    }




}
