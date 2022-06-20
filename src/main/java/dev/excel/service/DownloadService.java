package dev.excel.service;

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

    /**
     * JDBC를 이용한 엑셀 다운로드
     */
    public void excelDownloadJdbc(HttpServletResponse response) {

        /**
         * SET
         * [tableNm, VO.Class, splitSize]
         */
        String tableNm = "excel_data2";
        SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
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
        List<SampleVO> data = (List<SampleVO>) (List<?>) dataList;
        try {
            excelFile.addRows(data);
            excelFile.write(response.getOutputStream());
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mybatis를 이용한 엑셀 다운로드
     */
    public void excelDownloadMybatis(HttpServletResponse response) {
        try {
            SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
            ArrayList<Class<?>> mybatisData = getMyBatisDataInfo();
            List<SampleVO> dataList = (List<SampleVO>) (List<?>) mybatisData;

            System.out.println(mybatisData.size());

            excelFile.addRows(dataList);
            excelFile.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Jpa를 이용한 엑셀 다운로드
     */
    public void excelDownloadJpa(HttpServletResponse response) {
        try {
            SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
            List<SampleVO> dataList = (List<SampleVO>) (List<?>) getJpaDataInfo();

            excelFile.addRows(dataList);
            excelFile.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Number of data in the table
     */
    public int getJdbcDataSize(String tableNm) {
        String sql = "select count(*) from " + tableNm + ";";
        int rowcount = 0;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnectionByBulkApi();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) rowcount = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowcount;
    }

    /**
     * [JDBC] 데이터 가져오는 메서드 - Paging( OFFSET )
     */
    public List<Object> getJdbcDataInfo(int num, int limit, String tableNm) {
        if (num != 0) num = num * limit;
        String sql = "select * from " + tableNm + " LIMIT " + limit + " OFFSET " + num + ";";

        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnectionByBulkApi();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            pstmt.setFetchSize(20000);

            List<Object> result = ResultSetToObj(rs, SampleVO.class);
            return result;
        } catch (SQLException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * [JDBC] 데이터 가져오는 메서드
     */
    public List<Object> getJdbcDataInfo(String tableNm) {
        String sql = "select * from " + tableNm + ";";
        System.out.println(sql);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        con = getConnectionByBulkApi();
        try {
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            pstmt.setFetchSize(500);

            List<Object> result = ResultSetToObj(rs, SampleVO.class);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }






    public ArrayList<Class<?>> getMyBatisDataInfo() {
        return mybatisRepository.findAll();
    }

    public List<SampleVO> getJpaDataInfo() {
         return jpaRepository.findAll();
    }
}
