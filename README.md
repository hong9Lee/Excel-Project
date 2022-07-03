# ExcelHandler

Excel File Upload & Download Web Application
- 다양한 데이터 접근기술을 활용한 Excel Handling 목적으로 개발
- Upload(Excel To DB), Download(DB To Excel) 2가지 기능
- Jdbc, MyBatis, Jpa, Spring, MySql, Apache POI 사용


## 1. Excel Upload ( Excel To DB )
#### &nbsp; 1.1 Excel File을 Split하여 서버로 전송 ( JavaScript )
```
let name = file.name; // 파일 이름
let size = file.size; // 총합 크기
let shardSize = 10 * 1024 * 1024; // 파일 슬라이스 10MB
let shardCount = Math.ceil(size / shardSize); // 파일 슬라이스의 총 수

for (let i = 0; i < shardCount; ++i) {

    //시작과 끝 위치 각각에 대해 계산
    let start = i * shardSize
    let end = Math.min(size, start + shardSize);
    let partFile = file.slice(start, end); // shardSize만큼 파일을 나누어 서버로 전송
    this.partUpload(GUID, partFile, name, shardCount, i);

}
```


#### &nbsp; 1.2 분할된 chunk file을 merge ( Server )
```
for (int i = 0; i < parentFileDir.listFiles().length; i++) {
    File partFile = new File(parentFileDir, guid + "_" + i + ".part");
    FileOutputStream destTempFos = new FileOutputStream(destTempFile, true);
    FileUtils.copyFile(partFile, destTempFos); // file merge
    destTempFos.close();
}

FileUtils.deleteDirectory(parentFileDir); // chunk 임시 디렉토리 삭제
```


#### &nbsp; 1.3 Server에 업로드된 파일을 SAX 방식으로 Read
```
OPCPackage opc = OPCPackage.open(path);
XSSFReader xssfReader = new XSSFReader(opc);

// Get Sheet
InputStream inputStream = xssfReader.getSheetsData().next();
InputSource inputSource = new InputSource(inputStream);

XMLReader xmlReader = SAXHelper.newXMLReader();
xmlReader.parse(inputSource);
```





## 2. Excel Download ( DB To Excel )

#### &nbsp; 2.1 Excel Column과 데이터를 순서대로 매핑하기 위해 Custom Annotation 생성

```
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    String headerName() default "";
    String dbFieldName() default "";
    
}
```
```
for (Field field : getAllFields(type)) {
  if (field.isAnnotationPresent(ExcelColumn.class)) {
      ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

      fieldNames.add(field.getName());
      headerNamesMap.put(field.getName(), annotation.headerName()); // Field와 Excel Column Name을 매핑
  }
}
```

#### &nbsp; 2.2 해당 Column Cell에 Data 렌더링

```
for (String dataFieldName : resource.getDataFieldNames()) {
  try {
  
      Cell cell = row.createCell(columnIndex++);
      Field field = getField(data.getClass(), dataFieldName);
      field.setAccessible(true);
      Object cellValue = field.get(data);
      renderCellValue(cell, cellValue);
      
  } catch (IllegalAccessException e) {
      log.error("IllegalAccessException", e);
  }
}
```



#### &nbsp; 2.3 DTO를 동적으로 사용하기 위해 Reflection 사용
```
-- controller

/**
* SET VO.class
*/
Class<SampleVO> clazz = SampleVO.class;
downloadService.excelDownloadJdbc(response, clazz);
```

```
-- service

SheetExcelFile<T> excelFile = new SheetExcelFile(clazz);
...
Object dto = clazz.getConstructor().newInstance();
field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
...
excelFile.addRows(data);
excelWrite(excelFile, response);
```




## 3. Web 페이지를 통해 기능 테스트
![main_img](https://user-images.githubusercontent.com/94272140/176992766-3c30555e-e3d6-4f3b-9162-c16f6ef87864.png)





## 4. 성능
#### &nbsp; 4.1 rows * 24column 테스트 진행

[DOWNLOAD]
|rows|JDBC|MyBatis|JPA
|------|---|---|--|
|100|0.08s|0.04s|0.08s
|1000|0.25s|0.13s|0.15s
|10000|1.54s|1.06s|1.17s
|100000|13.36s|9.53s|10.07s

[UPLOAD]
|rows|JDBC|MyBatis|JPA
|------|---|---|--|
|100|0.1s|0.08s|0.04s
|1000|0.78s|0.48s|0.27s
|10000|7.24s|3.92s|2.55s
|100000|71.43s|40.20s|24.43s

#### &nbsp; 4.2 MultiRow Bulk Insert 방식으로 성능 개선 ( 10000 row 테스트 )


||execute|Batch|
|------|---|---|
|JDBC|20.78s|7.24s
|MyBatis|7.78s|3.92s
|JPA|5.72s|2.55s


