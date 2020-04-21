package com.sulistionoadi.ngoprek.common.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RowMapperUtils {

	public static Boolean getBooleanValue(ResultSet rs, int rowNum, String columnName) throws SQLException {
		Boolean bool = Boolean.FALSE;
		Integer columnVal = rs.getInt(columnName);
		if (columnVal != null) {
			switch (columnVal) {
			case 0:
				bool = Boolean.FALSE;
				break;
			case 1:
				bool = Boolean.TRUE;
				break;
			default:
				throwError(columnName, columnVal, rowNum);
			}
		}
		return bool;
	}
	
	public static Date getDateValue(ResultSet rs, int rowNum, String columnName) throws SQLException {
		java.sql.Date columnVal = rs.getDate(columnName);
		return columnVal!=null ? new Date(columnVal.getTime()) : null;
	}
	
	public static void throwError(String columnName, Object columnVal, int rowNum) {
		log.warn("Invalid value {}={} at row:{}", columnName, columnVal, rowNum);
		throw new IllegalStateException(MessageFormat.format("Invalid value {0}={1} at row:{2}", columnName,
				columnVal.toString(), String.valueOf(rowNum)));
	}

}
