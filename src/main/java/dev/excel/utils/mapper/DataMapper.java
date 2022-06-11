package dev.excel.utils.mapper;

import dev.excel.dto.ColumnsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface DataMapper {
    ArrayList<HashMap<String, Object>> findAll();
    void insertData(List<ColumnsVO> list);
    void deleteAll();
}
