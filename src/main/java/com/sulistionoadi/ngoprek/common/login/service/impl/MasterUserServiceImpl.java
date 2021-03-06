package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;
import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.*;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.*;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.rowmapper.MasterUserRowMapper;
import com.sulistionoadi.ngoprek.common.login.service.MasterRoleService;
import com.sulistionoadi.ngoprek.common.login.service.MasterUserService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("masterUserService")
public class MasterUserServiceImpl extends DaoUtils implements MasterUserService, Serializable {

	private static final long serialVersionUID = 5915454856023099621L;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired 
	private DataSource datasource;
	
	@Autowired
	private MasterRoleService roleService;

	private void validateRole(MasterUserDTO dto) throws CommonException {
		if(dto.getRole()==null || dto.getRole().getId()==null) {
			throw new CommonException(RC_INVALID_PARAMETER, "Parameter role.id is Required");
		}
		
		Optional<MasterRoleDTO> role = roleService.findOne(dto.getRole().getId());
		if(!role.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterRole with id:" + dto.getRole().getId() + " not found");
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(MasterUserDTO dto) throws CommonException {
		Optional<MasterUserDTO> exists = findByUsername(dto.getUsername());
		if(exists.isPresent()) {
			throw new CommonException(RC_DATA_ALREADY_EXIST, "Username already exists");
		}
		validateRole(dto);
		
		String sql = "INSERT INTO cm_sec_user ("
				   + "    id, created_by, created_date, updated_by, updated_date,"
				   + "    is_active, appname, username, password, roleid "
				   + ") VALUES ("
				   + "    :id, :createdBy, :createdDate, :updatedBy, :updatedDate, "
				   + "    :isActive, :appname, :username, :password, :role_id "
				   + ")";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("role_id", dto.getRole().getId());
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Save MasterUser successfully");
		} catch (Exception ex) {
			log.error("Cannot save MasterUser, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot save MasterUser", ex);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(MasterUserDTO dto) throws CommonException {
		Optional<MasterUserDTO> op = findOne(dto.getId());
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterUser with id:" + dto.getId() + " not found");
		}
		
		Optional<MasterUserDTO> existsByUsername = findByUsername(dto.getUsername());
		if(existsByUsername.isPresent()) {
			if(!existsByUsername.get().getId().equals(dto.getId()))
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Username already exists");
		}
		
		validateRole(dto);
		validateRecordBeforeUpdate(op.get());

		if (!StringUtils.hasText(dto.getPassword())) {
			dto.setPassword(op.get().getPassword());
		}
		
		String sql = "UPDATE cm_sec_user SET "
				   + "    updated_by=:updatedBy, updated_date=:updatedDate, is_active=:isActive, "
				   + "    username=:username, password=:password, roleid=:role_id "
				   + "WHERE id=:id "
				   + "  AND appname=:appname";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("role_id", dto.getRole().getId());
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Update MasterUser with id:{} successfully", dto.getId());
		} catch (Exception ex) {
			log.error("Cannot update MasterUser, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot update MasterUser", ex);
		}
	}

	@Override
	public Optional<MasterUserDTO> findOne(Long id) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_user m WHERE m.id=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get MasterUser with id:{}", id);
			MasterUserDTO dto = getJdbcTemplate(datasource).queryForObject(sql, 
					new Object[] { id, this.appname }, new MasterUserRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("MasterUser with id:{} not found", id);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get MasterUser with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(MessageFormat.format("Cannot get MasterUser with id:{0}", 
					id != null ? id.toString() : "null"), ex);
		}
	}
	
	public Optional<MasterUserDTO> findOneFetchEager(Long id) throws CommonException {
		Optional<MasterUserDTO> op = findOne(id);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterUserDTO dto = op.get();
		Optional<MasterRoleDTO> role = roleService.findOneFetchEager(dto.getRole().getId());
		dto.setRole(role.isPresent() ? role.get() : null);

		return op;
	}
	
	public Optional<MasterUserDTO> findByUsername(String username) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_user m WHERE m.username=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get MasterUser with username:{}", username);
			MasterUserDTO dto = getJdbcTemplate(datasource).queryForObject(sql, 
					new Object[] { username, this.appname }, new MasterUserRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("MasterUser with username:{} not found", username);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get MasterUser with username:{}, cause:{}", username, ex.getMessage());
			throw new CommonException(MessageFormat.format("Cannot get MasterUser with username:{0}", username), ex);
		}
	}
	
	public Optional<MasterUserDTO> findByUsernameFetchEager(String username) throws CommonException {
		Optional<MasterUserDTO> op = findByUsername(username);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterUserDTO dto = op.get();
		Optional<MasterRoleDTO> role = roleService.findOneFetchEager(dto.getRole().getId());
		dto.setRole(role.isPresent() ? role.get() : null);

		return op;
	}

	@Override
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException {
		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);

		String sql = "SELECT COUNT(u.id) FROM cm_sec_user u " 
				   + "INNER JOIN cm_sec_role r ON r.id = u.roleid "
				   + "WHERE u.appname = :appname AND u.is_deleted=0 "
				   + "  AND r.appname = :appname AND r.is_deleted=0 ";
		if(statusActive!=null) {
			sql += "    AND u.is_active=:isActive AND r.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(u.username) LIKE :filter ";
			sql += "        OR  lower(r.rolename) LIKE :filter ";
			sql += "    ) ";
		}

		log.info("Count list data MasterUser filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data MasterUser, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get count list data MasterUser", ex);
		}
	}

	@Override
	public List<MasterUserDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException {
		String[] orderableColums = new String[]{"username", "rolename"};
		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);

		String sql = "SELECT * FROM ( " 
				   + "    SELECT DT.*, " 
				   + "           row_number() over ( "
				   + "               ORDER BY DT." + getOrderBy(filter, "ID", orderableColums)
				   + "           ) line_number "  
				   + "    FROM ( "
				   + "        SELECT u.*, r.rolename FROM cm_sec_user u "
				   + "        INNER JOIN cm_sec_role r ON r.id = u.roleid " 
				   + "        WHERE u.appname = :appname AND u.is_deleted=0 "
				   + "          AND r.appname = :appname AND r.is_deleted=0 ";
		if(statusActive!=null) {
			sql += "            AND u.is_active=:isActive AND r.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "          AND ( ";
			sql += "            lower(u.username) LIKE :filter ";
			sql += "        OR  lower(r.rolename) LIKE :filter ";
			sql += "          ) ";
		}
		
		sql += "          ) DT ";
		sql += "     ) WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";

		log.info("Get list data MasterUser filter by {}", param);

		try {
			List<MasterUserDTO> datas = getNamedParameterJdbcTemplate(datasource).query(sql, param,
					new MasterUserRowMapper());
			return datas;
		} catch (Exception ex) {
			log.error("Cannot get list data MasterUser, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get list data MasterUser", ex);
		}
	}
	
	public List<MasterUserDTO> filterFetchEager(PssFilter filter, StatusActive statusActive) throws CommonException {
		List<MasterUserDTO> list = filter(filter, statusActive);
		for (MasterUserDTO dto : list) {
			Optional<MasterRoleDTO> role = roleService.findOneFetchEager(dto.getRole().getId());
			dto.setRole(role.isPresent() ? role.get() : null);
		}
		return list;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delete(Long id) throws CommonException {
		Optional<MasterUserDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterUser with id:" + id + " not found");
		}

		validateRecordBeforeUpdate(op.get());
		String q = "DELETE FROM cm_sec_user WHERE id=? AND appname=?";
		try {
			getJdbcTemplate(datasource).update(q, id, this.appname);
			log.info("Delete MasterUser with id:{} successfully", id);
		} catch (Exception ex) {
			log.error("Cannot delete MasterUser with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot delete MasterUser with id:" + id, ex);
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setAsDelete(Long id, String updatedBy) throws CommonException {
		Optional<MasterUserDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterUser with id:" + id + " not found");
		}

		JdbcTemplate jdbcTemplate = getJdbcTemplate(datasource);
		validateRecordBeforeUpdate(op.get());

		try {
			String q = "UPDATE cm_sec_user SET "
					 + "       is_deleted=1, "
					 + "       username=CONCAT(username, CONCAT('-', id)), "
					 + "       updated_by=?, updated_date=? "
					 + "WHERE id=? AND appname=?";
			
			log.warn("MasterUser with id:{} will be flag as isDeleted", id);
			jdbcTemplate.update(q, updatedBy, new Date(), id, this.appname);
			log.info("Flag isDeleted for MasterUser with id:{} successfully", id);
		} catch(Exception ex) {
			log.error("Cannot flag isDeleted for MasterUser with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot flag isDeleted for MasterUser with id:" + id, ex);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setActive(Long id, StatusActive statusActive, String updatedBy) throws CommonException {
		Optional<MasterUserDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterUser with id:" + id + " not found");
		}
		
		validateRecordBeforeUpdate(op.get());
		String q = "UPDATE cm_sec_user SET is_active=?, updated_by=?, updated_date=? WHERE id=? AND appname=?";
		try {
			Integer boolVal = statusActive!=null && statusActive.equals(StatusActive.YES) ? 1:0;
			getJdbcTemplate(datasource).update(q, boolVal, updatedBy, new Date(), id, this.appname);
			log.info("Flag isActive={} for MasterUser with id:{} successfully", statusActive, id);
		} catch (Exception ex) {
			log.error("Cannot update isActive={} for MasterUser with id:{}, cause:{}", statusActive, id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat
					.format("Cannot update flag isActive={0} for MasterUser with id:{1}", statusActive, id.toString()), ex);
		}
	}

}
