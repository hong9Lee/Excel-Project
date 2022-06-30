package dev.excel.utils;

import dev.excel.utils.handler.ExcelSheetHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static dev.excel.utils.resource.ExcelRenderResourceFactory.getDbField;

@Slf4j
public class DataUtils {

    public static String SIMPLE_DATE_FORMAT = "yyyyMMddHHmmssSSS";
    public static String YM_DATE_FORMAT = "yyyyMM";

    /**
     * 지정한 Count만큼 List split
     */
    public static <T> List<List<T>> split(List<T> resList, int count) {
        if (resList == null || count < 1)
            return null;
        List<List<T>> ret = new ArrayList<List<T>>();
        int size = resList.size();
        if (size <= count) {
            // 데이터 부족 count 지정 크기
            ret.add(resList);
        } else {
            int pre = size / count;
            int last = size % count;
            // 앞 pre 개 집합, 모든 크기 다 count 가지 요소
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<T>();
                for (int j = 0; j < count; j++) {
                    itemList.add(resList.get(i * count + j));
                }
                ret.add(itemList);
            }
            // last 진행이 처리
            if (last > 0) {
                List<T> itemList = new ArrayList<T>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * count + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    /**
     * get upload file directory path
     */
    public static String getUploadDirectoryPath() {
        return System.getProperty("user.dir").replaceAll("\\\\", "/") + "/upload/";
    }

    /**
     * 임시 파일 삭제
     */
    public static void removeTempUploadFiles(String path) {
        File deleteFolder = new File(path);

        if (deleteFolder.exists()) {
            File[] deleteFolderList = deleteFolder.listFiles();

            for (int i = 0; i < deleteFolderList.length; i++) {
                if (deleteFolderList[i].isFile()) {
                    deleteFolderList[i].delete();
                } else {
                    removeTempUploadFiles(deleteFolderList[i].getPath());
                }
                deleteFolderList[i].delete();
            }
            deleteFolder.delete();
        }
    }

    /**
     * MultipartFile 형태의 Input 엑셀파일을 Parsing 하여 List<List<String>>로 반환
     */
    public static List<List<String>> getExcelParsingData(String path) {
        ExcelSheetHandler excelSheetHandler = new ExcelSheetHandler();
        ExcelSheetHandler handler = excelSheetHandler.readExcel(path);
        List<List<String>> excelData = handler.getRows();
        return excelData;
    }

    /**
     * Excel 파일을 Parsing 하여 Class<?> clazz형의 List<Object>로 변환
     */
    public static <T> List<T> getClazzDataList(String path, Class<?> clazz) {
        List<List<String>> excelData = getExcelParsingData(path); // OOM 확인필요.

        log.info("GET DATA LIST SIZE ==> " + excelData.size());

        try {
            Class<?> inst = Class.forName(clazz.getName());
            Field[] declaredFields = clazz.getDeclaredFields();
            List<T> list = new ArrayList<>();

            for (int k = 0; k < excelData.size(); k++) {
                Object obj = inst.newInstance();
                for (int i = 0; i < excelData.get(k).size(); i++) {
                    Field declaredField = declaredFields[i];
                    declaredField.setAccessible(true);

                    if (i == 0) declaredField.set(obj, Long.valueOf(k));
                    else declaredField.set(obj, excelData.get(k).get(i));
                }
                list.add((T) obj);
            }
            return list;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Object to Map
     */
    public static Map<String, Object> beanProperties(final Object bean) {
        final Map<String, Object> result = new HashMap<>();

        try {
            final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                final Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    result.put(propertyDescriptor.getName(), readMethod.invoke(bean, (Object[]) null));
                }
            }
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
        } catch (IntrospectionException e) {
            log.error("IntrospectionException", e);
        }
        return result;
    }

    /**
     * JDBC를 이용하여 데이터 Insert 쿼리를 묶기위해 전처리하는 메소드
     * ex) insert into TABLE_NAME (column1, colum2) VALUES (1, A), (2, B), (3, C) ,,,;
     */
    public static String getAppendQueryByObj(List<T> dataList, Class<?> clazz) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < dataList.size(); i++) {
            sb.append("(");

            Map<String, Object> valueMap = beanProperties(dataList.get(i));
            Field[] fields = clazz.getDeclaredFields();

            for (int j = 0; j < fields.length; j++) {
                String name = fields[j].getName();
                if (!name.equals("id")) {
                    sb.append("\'" + valueMap.get(name) + "\'");
                    if (j != fields.length - 1) sb.append(",");
                }
            }
            if (i != dataList.size() - 1) sb.append("),");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Upload File을 MultipartFile로 반환
     */
    public static MultipartFile getUploadFile(String path) {
        File file = new File(path);
        FileItem fileItem = null;

        try {
            fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
            IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
        } catch (IOException e) {
            log.error("IOException", e);
        }
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * ModelAndView 반환
     */
    public static ModelAndView getModelAndView() {
        ModelAndView modelAndView = new ModelAndView();
        MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
        modelAndView.setView(jsonView);
        return modelAndView;
    }

    /**
     * [JDBC] ResultSet -> List<Object> 변환
     */
    public static <T> List<T> resultSetToObj(ResultSet rs, Class<?> clazz) {
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        for (Field field : fields) {
            field.setAccessible(true);
        }

        List<T> list = new ArrayList<>();
        try {
            while (rs.next()) {
                Object dto = clazz.getConstructor().newInstance();

                for (Field field : fields) {
                    String name = getDbField(field);

                    if (!"id".equals(name) && name != "") {
                        if (isThere(rs, name)) {
                            String value = rs.getString(name);
                            field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
                        }
                    }
                }
                list.add((T) dto);
            }
        } catch (SQLException e) { //
            log.error("SQLException", e);
        } catch (InvocationTargetException e) { //
            log.error("InvocationTargetException", e);
        } catch (InstantiationException e) { // newInstance()로 객체를 생성할 수 없을 때 발생하는 exception
            log.error("InstantiationException", e);
        } catch (IllegalAccessException e) { //
            log.error("IllegalAccessException", e);
        } catch (NoSuchMethodException e) { // Method를 찾지 못할 때 발생
            log.error("NoSuchMethodException", e);
        }
        return list;
    }

    /**
     * [JDBC] ResultSet 에서 Column 존재 여부 확인
     */
    public static boolean isThere(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            log.error("columns doesn't exist {}", column, e);
        }
        return false;
    }
}
