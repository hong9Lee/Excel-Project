package dev.excel.repository;

import java.util.ArrayList;
import java.util.List;

public interface DataRepository {
    ArrayList<Class<?>> findAll(String tableNm);
    void insertData(List<Object> list);
    void deleteAll();
}
