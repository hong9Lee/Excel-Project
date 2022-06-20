package dev.excel.service;

import dev.excel.dto.SampleVO;
import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.JdbcRepository;
import dev.excel.repository.MybatisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import static dev.excel.utils.DataUtils.getClazzDataList;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService { // jdbc, mybatis, jpa

    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;
    private final JdbcRepository jdbcRepository;

    @Transactional
    public long excelUploadJdbcByBulkApi(MultipartFile uploadFile) {
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

        mybatisRepository.insertData(getClazzDataList(uploadFile, SampleVO.class));

        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
        return executeTime;
    }

    public long excelUploadJpa(MultipartFile uploadFile) {
        log.info("EXCEL UPLOAD BY [JPA]");
        long start = System.currentTimeMillis();

        List<Object> clazzDataList = getClazzDataList(uploadFile, SampleVO.class);
        List<SampleVO> dataList = (List<SampleVO>) (List<?>) clazzDataList;

        jpaRepository.saveAll(dataList);

        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
        return executeTime;
    }

    @Transactional
    public void deleteAll() {
        mybatisRepository.deleteAll();
    }
}
