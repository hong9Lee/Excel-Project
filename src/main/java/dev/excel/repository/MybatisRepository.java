package dev.excel.repository;

import dev.excel.dto.ColumnsVO;
import dev.excel.dto.SampleVO;
import dev.excel.utils.mapper.DataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MybatisRepository implements DataRepository {

    private final DataMapper dataMapper;

    @Override
    public ArrayList<Class<?>> findAll() {
        return dataMapper.findAll();
    }

    @Override
    public void insertData(List<ColumnsVO> list) {
        dataMapper.insertData(list);
    }

    @Override
    public void deleteAll() {
        dataMapper.deleteAll();
    }
}
