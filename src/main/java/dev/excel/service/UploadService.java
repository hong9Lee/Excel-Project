package dev.excel.service;

import dev.excel.handler.ExcelSheetHandler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class UploadService {

    public void excelUpload() throws Exception {
        System.out.println("Test");

        String filePath = "/Users/ihong-gyu/Desktop/test1.xlsx";
        File file = new File(filePath);

        ExcelSheetHandler excelSheetHandler = new ExcelSheetHandler();
        ExcelSheetHandler handler = excelSheetHandler.readExcel(file);
        List<List<String>> excelData = handler.getRows();

        System.out.println();




    }
}
