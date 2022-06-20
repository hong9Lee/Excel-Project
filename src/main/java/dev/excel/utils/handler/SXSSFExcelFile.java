package dev.excel.utils.handler;

import dev.excel.utils.resource.ExcelRenderResourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import static dev.excel.utils.SuperClassReflectionUtils.getField;

@Slf4j
public abstract class SXSSFExcelFile<T> implements ExcelFile<T> {

    protected static final SpreadsheetVersion supplyExcelVersion = SpreadsheetVersion.EXCEL2007;
    protected SXSSFWorkbook wb;
    protected Sheet sheet;
    protected ExcelRenderResource resource;

    public SXSSFExcelFile(Class<T> type) { this(Collections.emptyList(), type);}

    public SXSSFExcelFile(List<T> data, Class<T> type) {
        validateDate(data);
        this.wb = new SXSSFWorkbook();
        this.resource = ExcelRenderResourceFactory.prepareRenderResource(type);
        renderExcel(data);
    }

    protected void validateDate(List<T> data) {}

    protected abstract void renderExcel(List<T> data);

    protected void renderHeadersWithNewSheet(Sheet sheet, int rowIndex, int columnStartIndex) {
        Row row = sheet.createRow(rowIndex);
        int columnIndex = columnStartIndex;
        for (String dataFieldName : resource.getDataFieldNames()) {
            Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(resource.getExcelHeaderNames(dataFieldName));
        }
    }

    protected void renderBody(T data, int rowIndex, int columnStartIndex) {
        Row row = sheet.createRow(rowIndex);
        int columnIndex = columnStartIndex;

        for (String dataFieldName : resource.getDataFieldNames()) {
            Cell cell = row.createCell(columnIndex++);
            try {
                    Field field = getField(data.getClass(), dataFieldName);
                    field.setAccessible(true);
                    Object cellValue = field.get(data);
                    renderCellValue(cell, cellValue);
            } catch (Exception e) {
//                throw new ExcelInternalException(e.getMessage(), e);
                log.info("RENDER BODY ERROR => {}", e);
            }
        }
    }

    private void renderCellValue(Cell cell, Object cellValue) {
        if (cellValue instanceof Number) {
            Number numberValue = (Number) cellValue;
            cell.setCellValue(numberValue.doubleValue());
            return;
        }
        cell.setCellValue(cellValue == null ? "" : cellValue.toString());
    }

    /**
     * 엑셀 Sheet write 메소드
     */
    @Override
    public void write(OutputStream stream) throws IOException {
        wb.write(stream);
        wb.close();
        wb.dispose();
    }

}
