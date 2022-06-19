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
@Table(name = "sample_data2")
public class SampleVO {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ExcelColumn(headerName = "유저아이디", dbFieldName = "user_id")
    private String userId;

    @ExcelColumn(headerName = "샘플1", dbFieldName = "sample1")
    private String sample1;

    @ExcelColumn(headerName = "샘플2", dbFieldName = "sample2")
    private String sample2;

    @ExcelColumn(headerName = "샘플3", dbFieldName = "sample3")
    private String sample3;

    @ExcelColumn(headerName = "샘플4", dbFieldName = "sample4")
    private String sample4;

    @ExcelColumn(headerName = "샘플5", dbFieldName = "sample5")
    private String sample5;

    @ExcelColumn(headerName = "샘플6", dbFieldName = "sample6")
    private String sample6;

    @ExcelColumn(headerName = "샘플7", dbFieldName = "sample7")
    private String sample7;

    @ExcelColumn(headerName = "샘플8", dbFieldName = "sample8")
    private String sample8;

    @ExcelColumn(headerName = "샘플9", dbFieldName = "sample9")
    private String sample9;

    @ExcelColumn(headerName = "샘플10", dbFieldName = "sample10")
    private String sample10;

    @ExcelColumn(headerName = "샘플11", dbFieldName = "sample11")
    private String sample11;

    @ExcelColumn(headerName = "샘플12", dbFieldName = "sample12")
    private String sample12;

    @ExcelColumn(headerName = "샘플13", dbFieldName = "sample13")
    private String sample13;

    @ExcelColumn(headerName = "샘플14", dbFieldName = "sample14")
    private String sample14;

    @ExcelColumn(headerName = "샘플15", dbFieldName = "sample15")
    private String sample15;

    @ExcelColumn(headerName = "샘플16", dbFieldName = "sample16")
    private String sample16;

    @ExcelColumn(headerName = "샘플17", dbFieldName = "sample17")
    private String sample17;

    @ExcelColumn(headerName = "샘플18", dbFieldName = "sample18")
    private String sample18;

    @ExcelColumn(headerName = "샘플19", dbFieldName = "sample19")
    private String sample19;

    @ExcelColumn(headerName = "샘플20", dbFieldName = "sample20")
    private String sample20;

    @ExcelColumn(headerName = "샘플21", dbFieldName = "sample21")
    private String sample21;

    @ExcelColumn(headerName = "샘플22", dbFieldName = "sample22")
    private String sample22;

    @ExcelColumn(headerName = "샘플23", dbFieldName = "sample23")
    private String sample23;

    @ExcelColumn(headerName = "샘플24", dbFieldName = "sample24")
    private String sample24;

}
