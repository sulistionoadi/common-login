package com.sulistionoadi.ngoprek.common.login.rowmapper;

import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getBooleanValue;
import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getDateValue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MasterUserRowMapper implements RowMapper<MasterUserDTO> {

	@Override
	public MasterUserDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Boolean isDeleted = getBooleanValue(rs, rowNum, "IS_DELETED");
		Boolean isActive = getBooleanValue(rs, rowNum, "IS_ACTIVE");
		
		Long roleId = null;
		String roleName = null;
		try {
			roleId = rs.getLong("ROLEID");
		} catch(Exception ex) {
			log.trace("Cannot get value from field ROLEID, Cause:{}", ex.getMessage(), ex);
		}
		
		try {
			roleName = rs.getString("ROLENAME");
		} catch(Exception ex) {
			log.trace("Cannot get value from field ROLENAME, Cause:{}", ex.getMessage(), ex);
		}
		
		MasterRoleDTO role = null;
		if(roleId!=null || roleName!=null) {
			role = MasterRoleDTO.builder().id(roleId).name(roleName).build();
		}
		
		return MasterUserDTO.builder()
				.id(rs.getLong("ID"))
				.createdDate(getDateValue(rs, rowNum, "CREATED_DATE"))
				.createdBy(rs.getString("CREATED_BY"))
				.updatedDate(getDateValue(rs, rowNum, "UPDATED_DATE"))
				.updatedBy(rs.getString("UPDATED_BY"))
				.isActive(isActive)
				.isDeleted(isDeleted)
				.username(rs.getString("USERNAME"))
				.password(rs.getString("PASSWORD"))
				.role(role)
				.build();
	}

}
