package dev.excel.utils.exception;

public class ExcelException extends RuntimeException{

    public ExcelException(String msg, Throwable cause) { super(msg, cause); }

    public ExcelException(String msg) { super(msg); }
}
