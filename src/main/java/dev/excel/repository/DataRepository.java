package dev.excel.repository;

import dev.excel.dto.ColumnsVO;
import dev.excel.dto.SampleVO;

import java.util.ArrayList;
import java.util.List;

public interface DataRepository {
    ArrayList<Class<?>> findAll();
    void insertData(List<ColumnsVO> list);
    void deleteAll();
}
