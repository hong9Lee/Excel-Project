package dev.excel.utils.resource;

import dev.excel.utils.annotation.ExcelColumn;
import dev.excel.utils.handler.ExcelRenderResource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.excel.utils.SuperClassReflectionUtils.getAllFields;

/**
 * ExcelRenderResourceFactory
 *
 */
public final class ExcelRenderResourceFactory {

	/**
	 * VO Field에 적용한 headerName 가져오는 메소드.
	 */
	public static ExcelRenderResource prepareRenderResource(Class<?> type) {
		Map<String, String> headerNamesMap = new LinkedHashMap<>();
		List<String> fieldNames = new ArrayList<>();

		for (Field field : getAllFields(type)) {
			if (field.isAnnotationPresent(ExcelColumn.class)) {
				ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

				fieldNames.add(field.getName());
				headerNamesMap.put(field.getName(), annotation.headerName());
			}
		}
		return new ExcelRenderResource(headerNamesMap, fieldNames);
	}

	/**
	 * VO Field에 적용한 dbFieldName 가져오는 메소드.
	 */
	public static String getDbField(Field field) {
		if (field.isAnnotationPresent(ExcelColumn.class)) {
			ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
			return annotation.dbFieldName();
		} else return "";
	}

}
