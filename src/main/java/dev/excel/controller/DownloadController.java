package dev.excel.controller;

import dev.excel.dto.SampleVO;
import dev.excel.service.DownloadService;
import dev.excel.utils.handler.SheetExcelFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j

public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping(value = "/download", produces = "application/text; charset=UTF-8")
    public void excelDownload(HttpServletResponse response,
                              @RequestParam("title") String title) throws SQLException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        log.info("============= [DOWNLOAD START] =============");
        log.info("TITLE => {}", title);
        long start = System.currentTimeMillis();

        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        if (title.equals("jdbc")) {

            downloadService.excelDownloadJdbc(response);

        } else if (title.equals("myBatis")) {

            SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
            ArrayList<Class<?>> mybatisData = downloadService.getMyBatisDataInfo();
            List<SampleVO> sam = (List<SampleVO>) (List<?>) mybatisData;

            System.out.println(mybatisData.size());

            excelFile.addRows(sam);
            excelFile.write(response.getOutputStream());

        } else if (title.equals("jpa")) {

            SheetExcelFile<SampleVO> excelFile = new SheetExcelFile<>(SampleVO.class);
            List<SampleVO> jpaDataInfo = downloadService.getJpaDataInfo();

            excelFile.addRows(jpaDataInfo);
            excelFile.write(response.getOutputStream());

        }



        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
    }
}
