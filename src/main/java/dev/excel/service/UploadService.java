package dev.excel.service;

import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.JdbcRepository;
import dev.excel.repository.MybatisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.connection.ConnectionConst.MYBATIS_UPLOAD_SPLIT_SIZE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService {

    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;
    private final JdbcRepository jdbcRepository;

    @Transactional
    public String excelUploadJdbcByBulkApi(String path, Class<?> clazz) { // JDBC
        log.info("EXCEL UPLOAD BY [JDBC]");
        long start = System.currentTimeMillis();

        jdbcRepository.insertMultiRowData(path, clazz);
//        jdbcRepository.insertBatchData(path, clazz);

        long end = System.currentTimeMillis();
        return executeTime(start, end);
    }

    @Transactional
    public String excelInsertByMybatis(String path, Class<?> clazz) { // MyBatis
        log.info("EXCEL UPLOAD BY [MYBATIS]");
        long start = System.currentTimeMillis();

        mybatisRepository.deleteAll();
        List<T> clazzDataList = getClazzDataList(path, clazz);
        List<List<T>> split = split(clazzDataList, MYBATIS_UPLOAD_SPLIT_SIZE);

        for (int j = 0; j < split.size(); j++) {
            mybatisRepository.insertData(split.get(j));
        }

        long end = System.currentTimeMillis();
        return executeTime(start, end);
    }

    public String excelUploadJpa(String path, Class<?> clazz) { // JPA
        log.info("EXCEL UPLOAD BY [JPA]");
        long start = System.currentTimeMillis();

        jpaRepository.deleteAllInBatch();
        jpaRepository.saveAll(getClazzDataList(path, clazz));

        long end = System.currentTimeMillis();
        return executeTime(start, end);
    }

}
