package dev.excel.utils.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DataMapper {
    ArrayList<Class<?>> findAll();
    void insertData(List<Object> list);
    void deleteAll();
}
