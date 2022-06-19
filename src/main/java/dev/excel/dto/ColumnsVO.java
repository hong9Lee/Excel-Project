package dev.excel.dto;

import dev.excel.utils.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "excel_data")
public class ColumnsVO {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ExcelColumn(headerName = "사업자", dbFieldName = "user_num")
    private String userNum;

    @ExcelColumn(headerName = "접수일", dbFieldName = "apply_date")
    private String applyDate;

    @ExcelColumn(headerName = "접수순번", dbFieldName = "apply_num")
    private String applyNum;

    @ExcelColumn(headerName = "보고서", dbFieldName = "report")
    private String report;

    @ExcelColumn(headerName = "구분", dbFieldName = "category")
    private String category;

    @ExcelColumn(headerName = "API값", dbFieldName = "api_data")
    private String apiData;

    @ExcelColumn(headerName = "DB값", dbFieldName = "db_data")
    private String dbData;

}
