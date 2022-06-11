package dev.excel.service;

import dev.excel.dto.ColumnsVO;
import dev.excel.repository.DataJpaRepository;
import dev.excel.repository.MybatisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.excel.utils.handler.ExcelSheetHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static dev.excel.utils.connection.DBConnectionUtil.getConnection;
import static dev.excel.utils.connection.DBConnectionUtil.getConnectionByBulkApi;

// jdbc, mybatis, jpa

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadService {


    private final MybatisRepository mybatisRepository;
    private final DataJpaRepository jpaRepository;

    public void excelUploadJpa(MultipartFile uploadFile) throws Exception {
        log.info("EXCEL UPLOAD BY [JPA]");
        List<ColumnsVO> dataList = getDataList(uploadFile);
        jpaRepository.saveAll(dataList);
    }

    public List<ColumnsVO> getDataList(MultipartFile uploadFile) throws Exception {
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

    public void excelUploadJdbc(MultipartFile uploadFile) throws Exception {
        List<ColumnsVO> dataList = getDataList(uploadFile);

        String sql = "insert into excel_data (user_num, apply_date, apply_num, report, category, api_data, db_data) VALUES(?,?,?,?,?,?,?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            for (int i = 0; i < dataList.size(); i++) {
                pstmt.setString(1, dataList.get(i).getUserNum());
                pstmt.setString(2, dataList.get(i).getApplyDate());
                pstmt.setString(3, dataList.get(i).getApplyNum());
                pstmt.setString(4, dataList.get(i).getReport());
                pstmt.setString(5, dataList.get(i).getCategory());
                pstmt.setString(6, dataList.get(i).getApiData());
                pstmt.setString(7, dataList.get(i).getDbData());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

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

    public void excelUploadJdbcByBulkApi(MultipartFile uploadFile) throws Exception {
        long start = System.currentTimeMillis();

        List<ColumnsVO> dataList = getDataList(uploadFile);
        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        log.info("EXECUTE TIME (excelUploadJdbcByBulkApi) ===> {}", executeTime);
        log.info("GET excelUploadJdbcByBulkApi LIST SIZE ==> {}", dataList.size());

        List<List<ColumnsVO>> ret = split(dataList, 10000);

        for (int j = 0; j < ret.size(); j++) {
            dataList = ret.get(j);


            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                String sql = "insert into excel_data (user_num, apply_date, apply_num, report, category, api_data, db_data) VALUES ";


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


                con = getConnectionByBulkApi();
                String sqls = sql + sb.toString();
                pstmt = con.prepareStatement(sqls);
//            pstmt.executeUpdate();
                pstmt.addBatch();
                pstmt.executeBatch();
            } catch (SQLException e) {
                log.error("db error", e);
                throw e;
            } finally {
                close(con, pstmt, null);
            }

        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) { // 커넥션 얻은 역순으로 close()

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    @Transactional
    public void excelInsertByMybatis(MultipartFile uploadFile) throws Exception {
        mybatisRepository.insertData(getDataList(uploadFile));
    }

    @Transactional
    public void deleteAll() {
        mybatisRepository.deleteAll();
    }

    @Transactional
    public void findAll() {
        ArrayList<HashMap<String, Object>> all = mybatisRepository.findAll();
        System.out.println(all);
    }
}
