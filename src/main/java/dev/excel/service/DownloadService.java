package dev.excel.service;

import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.JdbcRepository;
import dev.excel.repository.MybatisRepository;
import dev.excel.utils.handler.SheetExcelFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static dev.excel.utils.connection.ConnectionConst.JDBC_DOWNLOAD_SPLIT_SIZE;
import static dev.excel.utils.connection.ConnectionConst.JDBC_TABLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadService {

    private final JdbcRepository jdbcRepository;
    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;

    /**
     * [JDBC] 엑셀 다운로드
     */
    public void excelDownloadJdbc(HttpServletResponse response, Class<?> clazz) {
        String tableNm = JDBC_TABLE;
        int splitSize = JDBC_DOWNLOAD_SPLIT_SIZE;

        SheetExcelFile<T> excelFile = new SheetExcelFile(clazz);
        int jdbcDataSize = jdbcRepository.getDataSize(tableNm);
        int iterSize = (jdbcDataSize / splitSize) + 1;

        for (int i = 0; i < iterSize; i++) {
            List<T> dataInfo = jdbcRepository.getDataInfo(i, splitSize, tableNm, clazz);
            excelFile.addRows(dataInfo);
        }
        excelWrite(excelFile, response);
    }

    /**
     * [Mybatis] 엑셀 다운로드
     */
    public void excelDownloadMybatis(HttpServletResponse response, Class<?> clazz) {
        SheetExcelFile<T> excelFile = new SheetExcelFile(clazz);

        excelFile.addRows(getMyBatisDataInfo());
        excelWrite(excelFile, response);
    }

    /**
     * [JPA] 엑셀 다운로드
     */
    public void excelDownloadJpa(HttpServletResponse response, Class<?> clazz) {
        SheetExcelFile<T> excelFile = new SheetExcelFile(clazz);

        excelFile.addRows(getJpaDataInfo());
        excelWrite(excelFile, response);
    }

    public <T> List<T> getMyBatisDataInfo() {
        return mybatisRepository.findAll();
    }

    public <T> List<T> getJpaDataInfo() {
        return (List<T>) jpaRepository.findAll();
    }

    /**
     * excel write
     */
    public void excelWrite(SheetExcelFile<?> excelFile, HttpServletResponse response) {
        try {
            excelFile.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("IOException", e);
            e.printStackTrace();
        }
    }
}
