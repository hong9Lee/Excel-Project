package dev.excel;

import dev.excel.dto.SampleVO;
import dev.excel.service.DownloadService;
import dev.excel.utils.handler.SheetExcelFile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DownloadTest {


    @Autowired
    private DownloadService downloadService;

    @Test
    @Description("엑셀 다운로드 테스트")
    @Transactional
    public void excelDownloadTest() throws SQLException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
        String tableNm = "excel_data";
        int jdbcDataSize = downloadService.getJdbcDataSize(tableNm);

        int sp = 10000;
        List<Object> dataList = new ArrayList<>();
        if(jdbcDataSize < 10000) {
            dataList = downloadService.getJdbcDataInfo(tableNm);
        } else {
            for (int i = 0; i < jdbcDataSize / sp; i++) {
//                dataList = downloadService.getJdbcDataInfo(i, sp, tableNm);
            }
        }

        System.out.println(dataList.size());

        List<SampleVO> retList = (List<SampleVO>) (List<?>) dataList;
        excelFile.addRows(retList);
        FileOutputStream stream = new FileOutputStream("/Users/ihong-gyu/Desktop/sample1.xlsx");
        excelFile.write(stream);
    }

}
