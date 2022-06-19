package dev.excel.utils;

import dev.excel.dto.ColumnsVO;
import dev.excel.utils.handler.ExcelSheetHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

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

    public static List<ColumnsVO> getDataList(MultipartFile uploadFile) {
        ExcelSheetHandler excelSheetHandler = new ExcelSheetHandler();
        ExcelSheetHandler handler = excelSheetHandler.readExcel(uploadFile);
        List<List<String>> excelData = handler.getRows();

        log.info("GET DATA LIST SIZE ==> " + excelData.size());
        List<ColumnsVO> list = new ArrayList<>();
        for (List<String> data : excelData) {
            int iCol = 0;
            ColumnsVO columnsVO = new ColumnsVO();

            for (String datum : data) {
                if (datum == null || datum == "") datum = null;
                if (datum != null) datum = datum.replaceAll("\"", "'");
                if (datum != null) datum = datum.replaceAll("\'", "`");

                if (iCol == 0) columnsVO.setUserNum(datum);
                else if (iCol == 1) columnsVO.setApplyDate(datum);
                else if (iCol == 2) columnsVO.setApplyNum(datum);
                else if (iCol == 3) columnsVO.setReport(datum);
                else if (iCol == 4) columnsVO.setCategory(datum);
                else if (iCol == 5) columnsVO.setApiData(datum);
                else if (iCol == 6) columnsVO.setDbData(datum);
                iCol++;
            }
            list.add(columnsVO);
        }
        return list;
    }

    /**
     * MultipartFile 형태의 Input 엑셀파일을 Parsing 하여 List<List<String>>로 반환
     */
    public static List<List<String>> getExcelParsingData(MultipartFile uploadFile) {
        ExcelSheetHandler excelSheetHandler = new ExcelSheetHandler();
        ExcelSheetHandler handler = excelSheetHandler.readExcel(uploadFile);
        List<List<String>> excelData = handler.getRows();
        return excelData;
    }

    public static List<Object> getClazzDataList(MultipartFile uploadFile, Class<?> clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<List<String>> excelData = getExcelParsingData(uploadFile);
        log.info("GET DATA LIST SIZE ==> " + excelData.size());

        Class<?> inst = Class.forName(clazz.getName());
        Field[] declaredFields = clazz.getDeclaredFields();

        List<Object> list = new ArrayList<>();
        for (List<String> data : excelData) {
            Object obj = inst.newInstance();

            for (int i = 0; i < data.size(); i++) {
                Field declaredField = declaredFields[i + 1];
                declaredField.setAccessible(true);
                declaredField.set(obj, data.get(i));
            }
            list.add(obj);
        }
        return list;
    }

    public static Map<String, Object> beanProperties(final Object bean) {
        final Map<String, Object> result = new HashMap<String, Object>();

        try {
            final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                final Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    result.put(propertyDescriptor.getName(), readMethod.invoke(bean, (Object[]) null));
                }
            }
        } catch (Exception ex) {
            // ignore
        }

        return result;
    }


    /**
     * JDBC를 이용하여 데이터 Insert시 쿼리를 묶기위해 전처리하는 메소드
     * ex) insert into TABLE_NAME (column1, colum2) VALUES (1, A), (2, B), (3, C) ,,,;
     */
    public static String getAppendQueryByObj(List<Object> dataList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < dataList.size(); i++) {
            sb.append("(");
            Map<String, Object> valueMap = beanProperties(dataList.get(i));
            Class<?> clazz = dataList.get(i).getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (int j = 0; j < fields.length; j++) {
                String name = fields[j].getName();
                if (!name.equals("id")) {
                    sb.append("\'" + valueMap.get(name) + "\'");
                    if (j != fields.length - 1) sb.append(",");
                }
            }
            if(i != dataList.size() - 1) sb.append("),");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getAppendQuery(List<ColumnsVO> dataList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            sb.append("(" + "\'" + dataList.get(i).getUserNum() + "\',"
                    + "\'" + dataList.get(i).getApplyDate() + "\',"
                    + "\'" + dataList.get(i).getApplyNum() + "\',"
                    + "\'" + dataList.get(i).getReport() + "\',"
                    + "\'" + dataList.get(i).getCategory() + "\',"
                    + "\'" + dataList.get(i).getApiData() + "\',"
                    + "\'" + dataList.get(i).getDbData()
                    + "\'), ");

            if (i + 1 == dataList.size()) {
                sb.append("(" + "\'" + dataList.get(i).getUserNum() + "\',"
                        + "\'" + dataList.get(i).getApplyDate() + "\',"
                        + "\'" + dataList.get(i).getApplyNum() + "\',"
                        + "\'" + dataList.get(i).getReport() + "\',"
                        + "\'" + dataList.get(i).getCategory() + "\',"
                        + "\'" + dataList.get(i).getApiData() + "\',"
                        + "\'" + dataList.get(i).getDbData()
                        + "\');");
            }
        }
        return sb.toString();
    }

    /**
     * Upload File을 MultipartFile로 반환
     */
    public static MultipartFile getUploadFile(String path) throws IOException {
        File file = new File(path);
        FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
        try {
            InputStream input = new FileInputStream(file);
            OutputStream os = fileItem.getOutputStream();
            IOUtils.copy(input, os);
            // Or faster..
            // IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
        } catch (IOException ex) {
            // do something.
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

    public static List<Object> ResultSetToObj (ResultSet rs, Class<?> clazz) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        for(Field field: fields) {
            field.setAccessible(true);
        }

        List<Object> list = new ArrayList<>();
        while(rs.next()) {
            Object dto = clazz.getConstructor().newInstance();

            for(Field field: fields) {
                String name = getDbField(field);

                try{
                    if(!"id".equals(name) && name != "") {
                        if (isThere(rs, name)) {
                            String value = rs.getString(name);
                            field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            list.add(dto);
        }
        return list;
    }

    public static boolean isThere(ResultSet rs, String column)
    {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException sqlex) {
            log.error("columns doesn't exist {}", column, sqlex);
        }
        return false;
    }
}
