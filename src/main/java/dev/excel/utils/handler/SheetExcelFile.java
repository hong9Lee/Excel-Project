package dev.excel.utils.handler;

import dev.excel.utils.exception.ExcelHandlingException;

import java.util.Iterator;
import java.util.List;

public final class SheetExcelFile<T> extends SXSSFExcelFile<T>{

    private static final int ROW_START_INDEX = 0;
    private static final int COLUMN_START_INDEX = 0;
    private int currentRowIndex = ROW_START_INDEX;

    public SheetExcelFile(Class<T> type) { super(type); }

    @Override
    protected void renderExcel(List<T> data) {
        // 1. Create sheet and renderHeader
        sheet = wb.createSheet();
        renderHeadersWithNewSheet(sheet, currentRowIndex++, COLUMN_START_INDEX);

        if (data.isEmpty()) return;

        // 2. Render Body
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            T renderedData = iterator.next();
            renderBody(renderedData, currentRowIndex++, COLUMN_START_INDEX);
        }
    }

    @Override
    protected void validateDate(List<T> data) {
        int maxRows = supplyExcelVersion.getMaxRows();
        if (data.size() > maxRows) {
            throw new ExcelHandlingException(String.format("This concrete ExcelFile does not support over %s rows", maxRows));
        }
    }

    @Override
    public void addRows(List<T> data) {
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            T renderedData = iterator.next();
            renderBody(renderedData, ++currentRowIndex, COLUMN_START_INDEX);
        }
    }
}
