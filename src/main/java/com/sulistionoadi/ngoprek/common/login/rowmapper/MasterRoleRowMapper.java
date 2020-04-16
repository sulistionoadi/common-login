package com.sulistionoadi.ngoprek.common.login.rowmapper;

import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getBooleanValue;
import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getDateValue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;

public class MasterRoleRowMapper implements RowMapper<MasterRoleDTO> {

	@Override
	public MasterRoleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Boolean isDeleted = getBooleanValue(rs, rowNum, "IS_DELETED");
		Boolean isActive = getBooleanValue(rs, rowNum, "IS_ACTIVE");
		
		return MasterRoleDTO.builder()
				.id(rs.getLong("ID"))
				.createdDate(getDateValue(rs, rowNum, "CREATED_DATE"))
				.createdBy(rs.getString("CREATED_BY"))
				.updatedDate(getDateValue(rs, rowNum, "UPDATED_DATE"))
				.updatedBy(rs.getString("UPDATED_BY"))
				.isActive(isActive)
				.isDeleted(isDeleted)
				.name(rs.getString("ROLENAME"))
				.build();
	}

}
