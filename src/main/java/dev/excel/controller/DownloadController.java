package dev.excel.controller;

import dev.excel.dto.SampleVO;
import dev.excel.service.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping(value = "/download", produces = "application/text; charset=UTF-8")
    public void excelDownload(HttpServletResponse response, @RequestParam("title") String title, Model model) {

        long start = System.currentTimeMillis();
        log.info("============= [DOWNLOAD START] =============");
        log.info("TITLE => {}", title);

        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        /**
         * SET VO.class
         */
        Class<SampleVO> clazz = SampleVO.class;


        if (title.equals("jdbc")) {
            downloadService.excelDownloadJdbc(response, clazz);
        } else if (title.equals("myBatis")) {
            downloadService.excelDownloadMybatis(response, clazz);
        } else if (title.equals("jpa")) {
            downloadService.excelDownloadJpa(response, clazz);
        }

        long end = System.currentTimeMillis();
        long executeTime = (end - start) / 1000;
        System.out.println("실행시간(m) : " + executeTime);
    }
}
