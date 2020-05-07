package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.PSS_SEARCH_VAL;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generateCountPssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generatePssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.getOrderBy;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.login.rowmapper.LoginHistoryRowMapper;
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
	public Long count(PssFilter filter, Date startDate, Date endDate) throws Exception {
		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);
		param.put("startDate", startDate);
		param.put("endDate", endDate);

		String sql = "SELECT COUNT(*) FROM cm_login_history a "
				   + "WHERE a.appname=:appname "
				   + "  AND a.activity_date BETWEEN :startDate AND :endDate ";
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(a.username) LIKE :filter ";
			sql += "       OR   lower(a.remark) LIKE :filter ";
			sql += "    ) ";
		}

		log.info("Count list data Login Activity History filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data Login Activity History, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot get count list data Login Activity History");
		}
	}

	@Override
	public List<LoginHistoryDTO> filter(PssFilter filter, Date startDate, Date endDate) throws Exception {
		String[] orderableColums = new String[]{
			"", "username", "activity_date"
		};
		
		String q = "";
		q += "SELECT * FROM ( SELECT Z.*, ROWNUM line_number FROM (";
		q += "    SELECT username, activity_date, remark ";
		q += "    FROM cm_login_history "; 
		q += "    WHERE activity_date BETWEEN :startDate AND :endDate and appname = :appname ";
		
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			q += "  AND ( ";
			q += "         lower(username) LIKE :filter ";
			q += "    OR   lower(remark) LIKE :filter ";
			q += "  ) ";
		}
		
		q += "    ORDER BY " + getOrderBy(filter, "activity_date", orderableColums);
		q += ") Z ) WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";
		
		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);
		param.put("startDate", startDate);
		param.put("endDate", endDate);
		
		log.info("Get list data Login Activity History filter by {}", param);
		
		try {
			List<LoginHistoryDTO> datas = getNamedParameterJdbcTemplate(this.datasource).query(q, param, new LoginHistoryRowMapper());
			return datas;
		} catch(Exception ex) {
			log.error("Cannot get list data Login Activity History, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot get list data Login Activity History");
		}
	}

}
