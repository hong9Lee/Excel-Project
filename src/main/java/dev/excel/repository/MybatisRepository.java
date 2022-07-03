package dev.excel.repository;

import dev.excel.dto.SampleVO;
import dev.excel.utils.mapper.DataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.excel.utils.connection.ConnectionConst.MYBATIS_TABLE;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MybatisRepository {

    private final DataMapper dataMapper;

    public <T> List<T> findAll() {
        return dataMapper.findAll(MYBATIS_TABLE);
    }

    public void insertData(List<T> list) {
        dataMapper.insertData(list, MYBATIS_TABLE);
    }

    public void deleteAll() {
        dataMapper.deleteAll(MYBATIS_TABLE);
    }
}
