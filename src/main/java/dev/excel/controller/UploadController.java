package dev.excel.controller;

import dev.excel.dto.Result;
import dev.excel.dto.SampleVO;
import dev.excel.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.excel.utils.DataUtils.*;
import static dev.excel.utils.DataUtils.removeTempUploadFiles;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload") // MultipartFile => 큰 파일을 청크 단위로 쪼개서 업로드
    public ModelAndView upload(@RequestParam("title") String title, @RequestParam("filePath") String path, Model model) {
        log.info("FILE PATH => " + path);
        log.info("DB TITLE : " + title);

        ModelAndView modelAndView = getModelAndView();

        /**
         * SET VO.class
         */
        Class<SampleVO> clazz = SampleVO.class;

        if (title.equals("jdbc")) {
            model.addAttribute("jdbc", uploadService.excelUploadJdbcByBulkApi(path, clazz));
        } else if (title.equals("myBatis")) {
            model.addAttribute("myBatis", uploadService.excelInsertByMybatis(path, clazz));
        } else if (title.equals("jpa")) {
            model.addAttribute("jpa", uploadService.excelUploadJpa(path, clazz));
        }

        removeTempUploadFiles(getUploadDirectoryPath()); // 업로드 파일 삭제
        return modelAndView;
    }

    @PostMapping("/part")
    @ResponseBody
    public Result bigFile(HttpServletRequest request, String guid, Integer chunk, MultipartFile file) {
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (isMultipart) {
                if (chunk == null) chunk = 0; // 임시 디렉토리는 모든 조각난 파일을 저장하는 데 사용
                String tempFileDir = getUploadDirectoryPath() + guid;
                File parentFileDir = new File(tempFileDir);

                if (!parentFileDir.exists()) parentFileDir.mkdirs();

                File tempPartFile = new File(parentFileDir, guid + "_" + chunk + ".part"); // 분열 과정은, 프론트 데스크가 인터페이스를 업로드 할 여러 번 호출하면 파일의 각 부분은 배경에 업로드됩니다
                FileUtils.copyInputStreamToFile(file.getInputStream(), tempPartFile);
            }
        } catch (IOException e) {
            log.error("IOException", e);
            return Result.failMessage(400, e.getMessage());
        }
        return Result.successMessage(200, "", "");
    }


    @RequestMapping("merge")
    @ResponseBody
    public Result mergeFile(String guid, String fileName) {
        try {
            Date currentTime = new Date(); //시간 형식 형식
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

            String newName = simpleDateFormat.format(currentTime) + fileName.substring(fileName.lastIndexOf(".")); //타임 스탬프로 현재 시간 + 새 파일 이름
            simpleDateFormat = new SimpleDateFormat(YM_DATE_FORMAT);
            String tmp = simpleDateFormat.format(currentTime);

            String path = getUploadDirectoryPath();
            File parentFileDir = new File(path + guid);

            if (parentFileDir.isDirectory()) {
                File destTempFile = new File(path + tmp, newName);

                if (!destTempFile.exists()) { //상위 디렉토리의 파일을 얻고, 부모 디렉토리를 만들려면 파일을 생성
                    destTempFile.getParentFile().mkdir();
                    try {
                        destTempFile.createNewFile();
                    } catch (IOException e) {
                        log.error("IOException", e);
                    }
                }

                log.info("Chunk Size => {}", parentFileDir.listFiles().length);

                for (int i = 0; i < parentFileDir.listFiles().length; i++) {
                    File partFile = new File(parentFileDir, guid + "_" + i + ".part");
                    FileOutputStream destTempFos = new FileOutputStream(destTempFile, true); //"최종 문서"에서에 "모든 조각난 파일"을 걸어
                    FileUtils.copyFile(partFile, destTempFos);
                    destTempFos.close();
                }

                FileUtils.deleteDirectory(parentFileDir); // chunk 임시 디렉토리 삭제
                return Result.successMessage(200, "success", destTempFile.getPath());
            } else {
                return Result.failMessage(400, "merge error");
            }
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
        } catch (IOException e) {
            log.error("IOException", e);
        }

        return Result.failMessage(400, "mergeFile error");
    }
}
