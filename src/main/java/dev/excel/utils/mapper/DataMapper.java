package dev.excel.utils.mapper;

import dev.excel.dto.ColumnsVO;
import dev.excel.dto.SampleVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DataMapper {
    ArrayList<Class<?>> findAll();
    void insertData(List<ColumnsVO> list);
    void deleteAll();
}
