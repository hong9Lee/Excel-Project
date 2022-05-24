package dev.excel.handler;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    private int currentCol = -1;
    private int currRowNum =  0;

    String filePath = "";

    private List<List<String>> rows = new ArrayList<List<String>>();    //실제 엑셀을 파싱해서 담아지는 데이터
    private List<String>       row    = new ArrayList<String>();
    private List<String>     header    = new ArrayList<String>();

    public ExcelSheetHandler readExcel(File file) throws Exception {

        ExcelSheetHandler sheetHandler = new ExcelSheetHandler();

        OPCPackage opc = OPCPackage.open(file);
        XSSFReader xssfReader = new XSSFReader(opc);
        StylesTable styles = xssfReader.getStylesTable();

        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
        InputStream inputStream = xssfReader.getSheetsData().next();

        InputSource inputSource = new InputSource(inputStream);
        ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);

        XMLReader xmlReader = SAXHelper.newXMLReader();
        xmlReader.setContentHandler(handle);

        xmlReader.parse(inputSource);
        inputStream.close();
        opc.close();

        return sheetHandler;
    }






    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        XSSFSheetXMLHandler.SheetContentsHandler.super.headerFooter(text, isHeader, tagName);
    }

    @Override
    public void endSheet() {
        XSSFSheetXMLHandler.SheetContentsHandler.super.endSheet();
    }

    @Override
    public void startRow(int rowNum) {

    }

    @Override
    public void endRow(int rowNum) {

    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {

    }

    public List<List<String>> getRows(){
        return rows;
    }
}
