package com.sulistionoadi.ngoprek.common.login.rowmapper;

import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getBooleanValue;
import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getDateValue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;

public class AccessMenuRowMapper implements RowMapper<AccessMenuDTO> {

	@Override
	public AccessMenuDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Boolean isDeleted = getBooleanValue(rs, rowNum, "IS_DELETED");
		Boolean isActive = getBooleanValue(rs, rowNum, "IS_ACTIVE");
		
		Boolean actFilter = Boolean.FALSE;
		Boolean actCsv = Boolean.FALSE;
		Boolean actExcel = Boolean.FALSE;
		Boolean actPdf = Boolean.FALSE;
		Boolean actSave = Boolean.FALSE;
		Boolean actRemove = Boolean.FALSE;
		
		try {
			actFilter = getBooleanValue(rs, rowNum, "ACT_FILTER");
		} catch(Exception ex) {}
		try {
			actCsv = getBooleanValue(rs, rowNum, "ACT_CSV");
		} catch(Exception ex) {}
		try {
			actExcel = getBooleanValue(rs, rowNum, "ACT_EXCEL");
		} catch(Exception ex) {}
		try {
			actPdf = getBooleanValue(rs, rowNum, "ACT_PDF");
		} catch(Exception ex) {}
		try {
			actSave = getBooleanValue(rs, rowNum, "ACT_SAVE");
		} catch(Exception ex) {}
		try {
			actRemove = getBooleanValue(rs, rowNum, "ACT_REMOVE");
		} catch(Exception ex) {}
		
		return AccessMenuDTO.builder()
				.id(rs.getLong("ID"))
				.createdDate(getDateValue(rs, rowNum, "CREATED_DATE"))
				.createdBy(rs.getString("CREATED_BY"))
				.updatedDate(getDateValue(rs, rowNum, "UPDATED_DATE"))
				.updatedBy(rs.getString("UPDATED_BY"))
				.isActive(isActive)
				.isDeleted(isDeleted)
				.menuCode(rs.getString("MENUCODE"))
				.menuName(rs.getString("MENUNAME"))
				.priority(rs.getInt("PRIORITY"))
				.parentid(rs.getLong("PARENTID"))
				.actFilter(actFilter)
				.actCsv(actCsv)
				.actExcel(actExcel)
				.actPdf(actPdf)
				.actSave(actSave)
				.actRemove(actRemove)
				.build();
	}

}
