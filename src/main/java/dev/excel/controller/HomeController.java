package dev.excel.controller;

import dev.excel.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UploadService uploadService;

    @GetMapping("/")
    public String main() {
        return "home/index.html";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile uploadFile) throws Exception { // MultipartFile => 큰 파일을 청크 단위로 쪼개서 업로드함.
        if (uploadFile.isEmpty()) {
            System.out.println("empty !");
            uploadService.excelUpload();
//            return new ResponseEntity("file is empty", HttpStatus.OK);
        }

//        String s = request.getSession().getServletContext().getRealPath("/") + "uploads" + File.separator;
//        System.out.println(s);



        return "redirect:/";
    }


}
