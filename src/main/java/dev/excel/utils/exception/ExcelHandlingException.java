package dev.excel.utils.exception;

public class ExcelHandlingException extends ExcelException{
    public ExcelHandlingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ExcelHandlingException(String msg) {
        super(msg);
    }
}
