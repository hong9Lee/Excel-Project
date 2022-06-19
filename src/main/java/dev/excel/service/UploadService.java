package dev.excel.service;

import dev.excel.dto.ColumnsVO;
import dev.excel.dto.SampleVO;
import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.JdbcRepository;
import dev.excel.repository.MybatisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;

import static dev.excel.utils.DataUtils.getDataList;
import static dev.excel.utils.connection.DBConnectionUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService { // jdbc, mybatis, jpa

    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;
    private final JdbcRepository jdbcRepository;

    public long excelUploadJpa(MultipartFile uploadFile) {
        log.info("EXCEL UPLOAD BY [JPA]");

        long start = System.currentTimeMillis();


        List<ColumnsVO> dataList = getDataList(uploadFile);
//        jpaRepository.saveAll(dataList);

        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
        return executeTime;
    }

    public void excelUploadJdbc(MultipartFile uploadFile) throws Exception {
        List<ColumnsVO> dataList = getDataList(uploadFile);

        String sql = "insert into excel_data (user_num, apply_date, apply_num, report, category, api_data, db_data) VALUES(?,?,?,?,?,?,?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            for (int i = 0; i < dataList.size(); i++) {
                pstmt.setString(1, dataList.get(i).getUserNum());
                pstmt.setString(2, dataList.get(i).getApplyDate());
                pstmt.setString(3, dataList.get(i).getApplyNum());
                pstmt.setString(4, dataList.get(i).getReport());
                pstmt.setString(5, dataList.get(i).getCategory());
                pstmt.setString(6, dataList.get(i).getApiData());
                pstmt.setString(7, dataList.get(i).getDbData());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    @Transactional
    public long excelUploadJdbcByBulkApi(MultipartFile uploadFile) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        long start = System.currentTimeMillis();

        String tableNm = "sample_data2";
        jdbcRepository.insertClazzData(uploadFile, tableNm, SampleVO.class);


        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
        return executeTime;
    }


    @Transactional
    public long excelInsertByMybatis(MultipartFile uploadFile) {
        long start = System.currentTimeMillis();
        mybatisRepository.insertData(getDataList(uploadFile));

        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
        return executeTime;
    }

    @Transactional
    public void deleteAll() {
        mybatisRepository.deleteAll();
    }


    //        String tableNm = "excel_data";
//        jdbcRepository.insertClazzData(uploadFile, tableNm, ColumnsVO.class);
//        String tableNm = "excel_data2";
//        String fieldStr = getStringQueryByAllFields(SampleVO.class);
//        jdbcRepository.insertClazzData(uploadFile, tableNm, fieldStr, SampleVO.class);
}
