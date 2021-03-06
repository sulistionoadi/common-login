package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;
import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.*;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.*;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MappingPrivilege;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.rowmapper.AccessMenuRowMapper;
import com.sulistionoadi.ngoprek.common.login.service.AccessMenuService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("accessMenuService")
public class AccessMenuServiceImpl extends DaoUtils implements AccessMenuService, Serializable {

	private static final long serialVersionUID = 5915454856023099621L;
	
	@Value("${app.max.jdbc.batch:10}")
	private Integer jdbcBatchSize;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired
	private DataSource datasource;

	@Override
	public void save(AccessMenuDTO dto) throws CommonException {
		Optional<AccessMenuDTO> exists = findByCode(dto.getMenuCode());
		if(exists.isPresent()) {
			throw new CommonException(RC_DATA_ALREADY_EXIST, "Menu already exists");
		}
		
		String sql = "INSERT INTO cm_sec_menu ("
				   + "    created_by, created_date, updated_by, updated_date, is_active, appname, "
				   + "    menucode, menuname, priority, parentid"
				   + ") VALUES ("
				   + "    :createdBy, :createdDate, :updatedBy, :updatedDate, :isActive, :appname, "
				   + "    :menuCode, :menuName, :priority, :parentid"
				   + ")";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Save AccessMenu successfully");
		} catch (Exception ex) {
			log.error("Cannot save AccessMenu, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot save AccessMenu", ex);				
		}
	}

	@Override
	public void update(AccessMenuDTO dto) throws CommonException {
		Optional<AccessMenuDTO> op = findOne(dto.getId());
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "AccessMenu with id:" + dto.getId() + " not found");
		}
		
		Optional<AccessMenuDTO> exists = findByCode(dto.getMenuCode());
		if(exists.isPresent()) {
			if(!exists.get().getId().equals(dto.getId()))
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Menu already exists");
		}
		
		validateRecordBeforeUpdate(op.get());
		String sql = "UPDATE cm_sec_menu SET "
				   + "    updated_by=:updatedBy, updated_date=:updatedDate, is_active=:isActive, "
				   + "    menucode=:menuCode, menuname=:menuName, priority=:priority, parentid=:parentid "
				   + "WHERE id=:id AND appname=:appname";
		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Update AccessMenu with id:{} successfully", dto.getId());
		} catch (Exception ex) {
			log.error("Cannot update AccessMenu, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot update AccessMenu", ex);
		}
	}

	@Override
	public Optional<AccessMenuDTO> findOne(Long id) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_menu m WHERE m.id=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get AccessMenu with id:{}", id);
			AccessMenuDTO dto = getJdbcTemplate(datasource).queryForObject(sql, new Object[] { id, this.appname }, new AccessMenuRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("AccessMenu with id:{} not found", id);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get AccessMenu with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Cannot get AccessMenu with id:{0}", 
					id != null ? id.toString() : "null"), ex);
		}
	}
	
	@Override
	public Optional<AccessMenuDTO> findByCode(String code) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_menu m WHERE m.menucode=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get AccessMenu with code:{}", code);
			AccessMenuDTO dto = getJdbcTemplate(datasource).queryForObject(sql, new Object[] { code, this.appname }, new AccessMenuRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("AccessMenu with code:{} not found", code);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get AccessMenu with code:{}, cause:{}", code, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Cannot get AccessMenu with code:{0}", code), ex);
		}
	}

	@Override
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException {
		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);

		String sql = "SELECT COUNT(c.id) FROM cm_sec_menu c "
				   + "INNER JOIN cm_sec_menu p ON p.id = c.parentid " 
				   + "WHERE c.appname = :appname and c.is_deleted=0 "
				   + "  AND p.appname = :appname and p.is_deleted=0 ";
		
		if(statusActive!=null) {
			sql += "    AND c.is_active=:isActive AND p.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(c.MENUCODE) LIKE :filter ";
			sql += "        OR  lower(c.MENUNAME) LIKE :filter ";
			sql += "    ) ";
		}
		
		log.info("Count list data AccessMenu filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data AccessMenu, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get count list data AccessMenu", ex);
		}
	}

	@Override
	public List<AccessMenuDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException {
		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);

		String sql = "SELECT * FROM ( " 
				   + "    SELECT DT.*, " 
				   + "           row_number() over ( "
				   + "               ORDER BY DT.priority ASC, DT.p_priority ASC NULL FIRST "
				   + "           ) line_number " 
				   + "    FROM ( "
				   + "        SELECT c.*, p.priority p_priority FROM cm_sec_menu c "
				   + "        INNER JOIN cm_sec_menu p ON p.id = c.parentid " 
				   + "        WHERE c.appname = :appname and c.is_deleted=0 "
				   + "          AND p.appname = :appname and p.is_deleted=0 ";
		
		if(statusActive!=null) {
			sql += "    AND c.is_active=:isActive AND p.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			  sql += "          AND ( ";
			  sql += "                 lower(c.MENUCODE) LIKE :filter ";
			  sql += "             OR  lower(c.MENUNAME) LIKE :filter ";
			  sql += "          ) ";
		}
		      sql += "    ) DT ";
		      sql += ") WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";

		log.info("Get list data AccessMenu filter by {}", param);

		try {
			List<AccessMenuDTO> datas = getNamedParameterJdbcTemplate(datasource).query(sql, param,
					new AccessMenuRowMapper());
			return datas;
		} catch (Exception ex) {
			log.error("Cannot get list data AccessMenu, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get list data AccessMenu", ex);
		}
	}

	@Override
	public void delete(Long id) throws CommonException {
		Optional<AccessMenuDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "AccessMenu with id:" + id + " not found");
		}
		
		validateRecordBeforeUpdate(op.get());
		String q = "DELETE FROM cm_sec_menu WHERE id=? AND appname=?";
		try {
			getJdbcTemplate(datasource).update(q, id, this.appname);
			log.info("Delete AccessMenu with id:{} successfully", id);
		} catch (Exception ex) {
			log.error("Cannot delete AccessMenu with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot delete AccessMenu with id:" + id, ex);
		}
	}
	
	@Override
	public void setAsDelete(Long id, String updatedBy) throws CommonException {
		Optional<AccessMenuDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "AccessMenu with id:" + id + " not found");
		}

		validateRecordBeforeUpdate(op.get());
		try {
			String q = "UPDATE cm_sec_menu SET is_deleted=1, menucode=CONCAT(menucode, CONCAT('-', id)), updated_by=?, updated_date=? "
					 + "WHERE id=? AND appname=?";
			
			log.warn("AccessMenu with id:{} will be flag as isDeleted", id);
			getJdbcTemplate(datasource).update(q, updatedBy, new Date(), id, this.appname);
			log.info("Flag isDeleted for AccessMenu with id:{} successfully", id);
		} catch(Exception ex) {
			log.error("Cannot flag isDeleted for AccessMenu with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot flag isDeleted for AccessMenu with id:" + id, ex);
		}
	}

	@Override
	public void setActive(Long id, StatusActive statusActive, String updatedBy) throws CommonException {
		Optional<AccessMenuDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "AccessMenu with id:" + id + " not found");
		}
		
		validateRecordBeforeUpdate(op.get());
		String q = "UPDATE cm_sec_menu SET is_active=?, updated_by=?, updated_date=? WHERE id=? AND appname=?";
		try {
			Integer boolVal = statusActive!=null && statusActive.equals(StatusActive.YES) ? 1:0;
			getJdbcTemplate(datasource).update(q, boolVal, updatedBy, new Date(), id, this.appname);
			log.info("Flag isActive={} for AccessMenu with id:{} successfully", statusActive, id);
		} catch (Exception ex) {
			log.error("Cannot update isActive={} for AccessMenu with id:{}, cause:{}", statusActive, id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat
					.format("Cannot update flag isActive={0} for AccessMenu with id:{1}", statusActive, id.toString()), ex);
		}
	}
	
	public Set<AccessMenuDTO> getPermittedAccess(Long roleid) throws CommonException {
		String sql = "SELECT menu.*, "
				   + "       map.act_filter, map.act_csv, map.act_excel, "
				   + "       map.act_pdf, map.act_save, map.act_remove "
				   + "FROM cm_sec_menu menu, cm_sec_role_menu map "
				   + "WHERE menu.id = map.menuid AND map.roleid = ? AND menu.parentid IS NULL "
				   + "  AND menu.is_deleted=0 AND menu.appname=? AND menu.is_active=1 "
				   + "ORDER BY menu.priority ASC";
		try {
			List<AccessMenuDTO> parents = getJdbcTemplate(datasource).query(sql, new Object[] { roleid, this.appname },
					new AccessMenuRowMapper());
			for (AccessMenuDTO amd : parents) {
				amd.setChilds(getPermittedChildMenu(roleid, amd.getId()));
			}
			return parents.stream().collect(Collectors.toSet());
		} catch (Exception ex) {
			log.error("Cannot get Permitted Menu for role_id {}, cause:{}", roleid, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Cannot get Permitted Menu for role_id {0,number,#}",
					roleid), ex);
		}
	}

	private Set<AccessMenuDTO> getPermittedChildMenu(Long roleid, Long parentid) throws Exception {
		String sql = "SELECT menu.*, "
				   + "       map.act_filter, map.act_csv, map.act_excel, "
				   + "       map.act_pdf, map.act_save, map.act_remove "
				   + "FROM cm_sec_menu menu, cm_sec_role_menu map "
				   + "WHERE menu.id = map.menuid AND map.roleid = ? AND menu.parentid = ? "
				   + "  AND menu.is_deleted=0 AND menu.appname=? AND menu.is_active=1 "
				   + "ORDER BY menu.priority ASC";
		try {
			List<AccessMenuDTO> parents = getJdbcTemplate(datasource).query(sql, new Object[] { roleid, parentid, this.appname},
					new AccessMenuRowMapper());
			for (AccessMenuDTO amd : parents) {
				if (amd.getParentid() != null) {
					this.getPermittedChildMenu(roleid, amd.getId());
				}
			}
			return parents.stream().collect(Collectors.toSet());
		} catch (Exception ex) {
			log.error("Cannot get Permitted Child Menu for role id {}, cause:{}", roleid, ex.getMessage());
			throw new Exception("Cannot get Permitted Child Menu for role_id:" + roleid + " and parent_id:" + parentid);
		}
	}

	@Override
	@Transactional(rollbackFor = {Exception.class})
	public void saveMappingPrivilege(MasterRoleDTO role, Set<MappingPrivilege> privileges) throws CommonException {
		JdbcTemplate jdbcTemplate = getJdbcTemplate(this.datasource);
		try {
			log.info("Trying to reset Mapping Privilege for Role:{}", role.getName());
			jdbcTemplate.update("DELETE FROM cm_sec_role_menu WHERE roleid = ?", role.getId());
			log.info("Success to reset Mapping Privilege for Role:{}", role.getName());
		} catch(Exception ex) {
			log.error("Failed to reset Mapping Privilege for Role:{}, cause:{}", role.getName(), ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Failed to reset Mapping Privilege for Role:{0}", role.getName()), ex);
		}
		
		try {
			log.info("Trying to save Mapping Privilege for Role:{} batch_size:{}", role.getName(), privileges.size());
			String q= "INSERT INTO cm_sec_role_menu ( "
					+ "    roleid, menuid, act_filter, act_csv, act_excel, act_pdf, act_save, act_remove "
					+ ") values (?, ?, ?, ?, ?, ?, ?, ?) ";
			jdbcTemplate.batchUpdate(q, privileges, this.jdbcBatchSize, 
					new ParameterizedPreparedStatementSetter<MappingPrivilege>() {
				@Override
				public void setValues(PreparedStatement ps, MappingPrivilege map) throws SQLException {
					ps.setLong(1, map.getRole().getId());
					ps.setLong(2, map.getMenu().getId());
					ps.setInt(3, map.getActFilter());
					ps.setInt(4, map.getActCsv());
					ps.setInt(5, map.getActExcel());
					ps.setInt(6, map.getActPdf());
					ps.setInt(7, map.getActSave());
					ps.setInt(8, map.getActRemove());
				}
			});
			
			log.info("Success to save Mapping Privilege for Role:{}", role.getName());
		} catch(Exception ex) {
			log.error("Failed to save Mapping Privilege for Role:{}, cause:{}", role.getName(), ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Failed to save Mapping Privilege for Role:{0}", role.getName()), ex);
		}
	}
	
}
