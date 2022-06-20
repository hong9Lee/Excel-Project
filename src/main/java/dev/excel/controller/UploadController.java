package dev.excel.controller;

import dev.excel.dto.Result;
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
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.excel.utils.DataUtils.getModelAndView;
import static dev.excel.utils.DataUtils.getUploadFile;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload") // MultipartFile => 큰 파일을 청크 단위로 쪼개서 업로드
    public ModelAndView upload(@RequestParam("title") String title, @RequestParam("filePath") String path, Model model) {
        log.info("FILE PATH => " + path);
        log.info("DB TITLE : " + title);

        MultipartFile uploadFile = getUploadFile(path);
        ModelAndView modelAndView = getModelAndView();

        if (uploadFile.isEmpty()) {
            log.info("=== UPLOAD FILE EMPTY ===");
            return modelAndView;
        }

        if(title.equals("jdbc")){

            model.addAttribute("jdbc", uploadService.excelUploadJdbcByBulkApi(uploadFile));
            return modelAndView;

        } else if(title.equals("myBatis")){

            uploadService.deleteAll();
            model.addAttribute("myBatis", uploadService.excelInsertByMybatis(uploadFile));
            return modelAndView;

        } else if (title.equals("jpa")) {

            model.addAttribute("jpa", uploadService.excelUploadJpa(uploadFile));
            return modelAndView;

        }
        return modelAndView;
    }

    @PostMapping("/part")
    @ResponseBody
    public Result bigFile(HttpServletRequest request, HttpServletResponse response, String guid, Integer chunk, MultipartFile file, Integer chunks) {
        try {
            String projectUrl = System.getProperty("user.dir").replaceAll("\\\\", "/");
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (isMultipart) {
                if (chunk == null) chunk = 0; // 임시 디렉토리는 모든 조각난 파일을 저장하는 데 사용
                String tempFileDir = projectUrl + "/upload/" + guid;
                File parentFileDir = new File(tempFileDir);

                if (!parentFileDir.exists()) parentFileDir.mkdirs();
                File tempPartFile = new File(parentFileDir, guid + "_" + chunk + ".part"); // 분열 과정은, 프론트 데스크가 인터페이스를 업로드 할 여러 번 호출하면 파일의 각 부분은 배경에 업로드됩니다
                FileUtils.copyInputStreamToFile(file.getInputStream(), tempPartFile);
            }

        } catch (Exception e) {
            return Result.failMessage(400,e.getMessage());
        }
        return Result.successMessage(200,"", "");
    }


    @RequestMapping("merge")
    @ResponseBody
    public Result mergeFile(String guid, String fileName) {
        String projectUrl = System.getProperty("user.dir").replaceAll("\\\\", "/");
        try {
            String sname = fileName.substring(fileName.lastIndexOf("."));

            Date currentTime = new Date(); //시간 형식 형식
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

            String timeStamp = simpleDateFormat.format(currentTime); //타임 스탬프로 현재 시간을 가져옵니다

            String newName = timeStamp + sname; //접합 새 파일 이름
            simpleDateFormat = new SimpleDateFormat("yyyyMM");
            String path = projectUrl + "/upload/";
            String tmp = simpleDateFormat.format(currentTime);
            File parentFileDir = new File(path + guid);
            if (parentFileDir.isDirectory()) {
                File destTempFile = new File(path + tmp, newName);
                if (!destTempFile.exists()) { //상위 디렉토리의 파일을 얻고, 부모 디렉토리를 만들려면 파일을 생성
                    destTempFile.getParentFile().mkdir();
                    try {
                        destTempFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                log.info("Chunk Size => {}", parentFileDir.listFiles().length);

                for (int i = 0; i < parentFileDir.listFiles().length; i++) {
                    File partFile = new File(parentFileDir, guid + "_" + i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(destTempFile, true); //"최종 문서"에서에 "모든 조각난 파일"을 걸어
                    FileUtils.copyFile(partFile, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(parentFileDir); // 조각난 파일은 임시 디렉토리를 삭제
                return Result.successMessage(200,"합병 성공~", destTempFile.getPath());
            }else{
                return Result.failMessage(400,"하지 디렉토리를 찾을");
            }
        } catch (Exception e) {
            return Result.failMessage(400,e.getMessage());
        }
    }
}
