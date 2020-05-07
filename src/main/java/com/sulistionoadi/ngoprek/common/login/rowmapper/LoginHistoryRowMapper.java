package com.sulistionoadi.ngoprek.common.login.rowmapper;

import static com.sulistionoadi.ngoprek.common.utils.RowMapperUtils.getDateValue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;

public class LoginHistoryRowMapper implements RowMapper<LoginHistoryDTO> {

	@Override
	public LoginHistoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		return LoginHistoryDTO.builder()
				.lineNum(rs.getLong("line_number"))
				.username(rs.getString("username"))
				.activityDate(getDateValue(rs, rowNum, "activity_date"))
				.remark(rs.getString("remark"))
				.build();
	}

}
