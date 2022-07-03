package dev.excel.utils.mapper;

import dev.excel.dto.SampleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Mapper
public interface DataMapper {
    <T> List<T> findAll(String tableNm);
    void insertData(List<T> list, String tableNm);
    void deleteAll(String tableNm);
}
