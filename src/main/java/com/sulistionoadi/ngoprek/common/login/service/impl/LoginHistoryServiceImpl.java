package com.sulistionoadi.ngoprek.common.login.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.login.service.LoginHistoryService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("loginHistoryService")
public class LoginHistoryServiceImpl extends DaoUtils implements LoginHistoryService, Serializable{
	
	private static final long serialVersionUID = -1168028208422574208L;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired 
	private DataSource datasource;

	@Override
	public void log(LoginHistoryDTO dto) throws Exception {
		if(dto.getRemark().length() > 250) {
			dto.setRemark(dto.getRemark().substring(0, 250));
		}
		
		String sql = "INSERT INTO cm_login_history ("
				   + "    username, activity_date, remark, appname "
				   + ") VALUES ("
				   + "    :username, :activityDate, :remark, :appname "
				   + ")";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Log Activity Login successfully");
		} catch (Exception ex) {
			log.error("Cannot Log Activity Login, cause:{}", ex.getMessage(), ex);
		}
	}

	@Override
	public Long count(PssFilter filter) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MasterRoleDTO> filter(PssFilter filter) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
