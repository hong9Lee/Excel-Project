package dev.excel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
//@RequestMapping("/api/upload")
public class HomeController {

    @GetMapping("/")
    public String main() {return "home/index.html";}


}
