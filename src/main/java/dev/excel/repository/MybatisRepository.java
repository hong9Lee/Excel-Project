package dev.excel.repository;

import dev.excel.utils.mapper.DataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MybatisRepository {

    private final DataMapper dataMapper;

    public ArrayList<Class<?>> findAll() { return dataMapper.findAll(); }

    public void insertData(List<Object> list) {
        dataMapper.insertData(list);
    }

    public void deleteAll() {
        dataMapper.deleteAll();
    }
}
