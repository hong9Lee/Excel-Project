package dev.excel.repository;

import dev.excel.dto.ColumnsVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface DataRepository {
    ArrayList<HashMap<String, Object>> findAll();
    void insertData(List<ColumnsVO> list);
    void deleteAll();
}
