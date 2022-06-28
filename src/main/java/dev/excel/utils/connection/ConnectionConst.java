package dev.excel.utils.connection;

public abstract class ConnectionConst {
    public static final String URL = "jdbc:mysql://localhost:3306/excel_data?characterEncoding=UTF-8";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "102030";

    public static final String JDBC_TABLE = "data_jdbc_1";
    public static final String MYBATIS_TABLE = "data_mybatis_1";
    public static final String JPA_TABLE = "data_jpa_1";

    public static final int JDBC_DOWNLOAD_SPLIT_SIZE = 5000;
    public static final int MYBATIS_UPLOAD_SPLIT_SIZE = 500;
}
