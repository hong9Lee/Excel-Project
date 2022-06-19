package dev.excel.utils.handler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
//import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class ExcelSheetHandler implements SheetContentsHandler{
    private int currentCol = -1;
    private int currRowNum = 0;
    String filePath = "";

    private List<List<String>> rows = new ArrayList<List<String>>();    //실제 엑셀을 파싱해서 담아지는 데이터
    private List<String> row = new ArrayList<String>();
    private List<String> header = new ArrayList<String>();

    public static ExcelSheetHandler readExcel(MultipartFile file) {

        ExcelSheetHandler sheetHandler = new ExcelSheetHandler();
        try{
            //org.apache.poi.openxml4j.opc.OPCPackage
            OPCPackage opc = OPCPackage.open(file.getInputStream());

            //org.apache.poi.xssf.eventusermodel.XSSFReader
            XSSFReader xssfReader = new XSSFReader(opc);

            //org.apache.poi.xssf.model.StylesTable
            StylesTable styles = xssfReader.getStylesTable();

            //org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);

            // 엑셀의 시트를 하나만 가져오기
            // 여러개일경우 while문으로 추출 필요
            InputStream inputStream = xssfReader.getSheetsData().next();

            // org.xml.sax.InputSource
            InputSource inputSource = new InputSource(inputStream);

            // org.xml.sax.Contenthandler
            ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);

            XMLReader xmlReader = SAXHelper.newXMLReader();
            xmlReader.setContentHandler(handle);

            xmlReader.parse(inputSource);

            inputStream.close();
            opc.close();
        }catch(Exception e) { //에러 발생했을때 하시고 싶은 TO-DO
            System.out.println(e);
            throw new IllegalStateException();
        }
        return sheetHandler;
    }//readExcel - end

    public List<List<String>> getRows(){
        return rows;
    }

    @Override
    public void startRow(int arg0){
        this.currentCol = -1;
        this.currRowNum = arg0;
    }

    @Override
    public void cell(String columnName, String value, XSSFComment var3){
        int iCol = (new CellReference(columnName)).getCol();
        int emptyCol = iCol - currentCol -1;

        for(int i=0; i< emptyCol; i++){
            row.add("");
        }
        currentCol = iCol;
        row.add(value);
    }

    @Override
    public void headerFooter(String arg0, boolean arg1, String arg2){}

    @Override
    public void endRow(int rowNum){
        if(rowNum == 0){
            header = new ArrayList(row);
        } else{
            if(row.size() < header.size()){
                for(int i = row.size(); i<header.size(); i++){
                    row.add("");
                }
            }
            rows.add(new ArrayList(row));
        }
        row.clear();
    }
}

