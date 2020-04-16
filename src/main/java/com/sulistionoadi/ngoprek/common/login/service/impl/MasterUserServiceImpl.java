package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.PSS_SEARCH_VAL;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generateCountPssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generatePssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.getOrderBy;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.login.rowmapper.MasterUserRowMapper;
import com.sulistionoadi.ngoprek.common.login.service.MasterRoleService;
import com.sulistionoadi.ngoprek.common.login.service.MasterUserService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("masterUserService")
public class MasterUserServiceImpl extends DaoUtils implements MasterUserService, Serializable {

	private static final long serialVersionUID = 5915454856023099621L;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired 
	private DataSource datasource;
	
	@Autowired
	private MasterRoleService roleService;

	@Override
	public void save(MasterUserDTO dto) throws Exception {
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
			log.error("Cannot save MasterUser, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot save MasterUser, cause:" + ex.getMessage());
		}
	}

	@Override
	public void update(MasterUserDTO dto) throws Exception {
		Optional<MasterUserDTO> op = findOne(dto.getId());
		if (!op.isPresent()) {
			throw new Exception("MasterUser with id:" + dto.getId() + " not found");
		}
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
			log.error("Cannot update MasterUser, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot update MasterUser, cause:" + ex.getMessage());
		}
	}

	@Override
	public Optional<MasterUserDTO> findOne(Long id) throws Exception {
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
			log.error("Cannot get MasterUser with id:{}, cause:{}", id, ex.getMessage(), ex);
			throw new Exception(MessageFormat.format("Cannot get MasterUser with id:{0}", 
					id != null ? id.toString() : "null"), ex);
		}
	}
	
	public Optional<MasterUserDTO> findOneFetchEager(Long id) throws Exception {
		Optional<MasterUserDTO> op = findOne(id);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterUserDTO dto = op.get();
		Optional<MasterRoleDTO> role = roleService.findOneFetchEager(dto.getRole().getId());
		dto.setRole(role.isPresent() ? role.get() : null);

		return op;
	}
	
	public Optional<MasterUserDTO> findByUsername(String username) throws Exception {
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
			log.error("Cannot get MasterUser with username:{}, cause:{}", username, ex.getMessage(), ex);
			throw new Exception(MessageFormat.format(
					"Cannot get MasterUser with username:{0}", username), ex);
		}
	}
	
	public Optional<MasterUserDTO> findByUsernameFetchEager(String username) throws Exception {
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
	public Long count(PssFilter filter) throws Exception {
		String sql = "SELECT COUNT(u.id) FROM cm_sec_user u " 
				   + "INNER JOIN cm_sec_role r ON r.id = u.roleid "
				   + "WHERE u.appname = :appname AND u.is_deleted=0 "
				   + "  AND r.appname = :appname AND r.is_deleted=0 ";
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(u.username) LIKE :filter ";
			sql += "        OR  lower(r.rolename) LIKE :filter ";
			sql += "    ) ";
		}

		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);
		log.info("Count list data MasterUser filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data MasterUser, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot get count list data MasterUser");
		}
	}

	@Override
	public List<MasterUserDTO> filter(PssFilter filter) throws Exception {
		String[] orderableColums = new String[]{"ID", "USERNAME", "ROLENAME"};
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
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "          AND ( ";
			sql += "            lower(u.username) LIKE :filter ";
			sql += "        OR  lower(r.rolename) LIKE :filter ";
			sql += "          ) ";
		}
		
		sql += "          ) DT ";
		sql += "     ) WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";

		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);
		log.info("Get list data MasterUser filter by {}", param);

		try {
			List<MasterUserDTO> datas = getNamedParameterJdbcTemplate(datasource).query(sql, param,
					new MasterUserRowMapper());
			return datas;
		} catch (Exception ex) {
			log.error("Cannot get list data MasterUser, cause:{}", ex.getMessage(), ex);
			throw new Exception(MessageFormat.format("Cannot get list data MasterUser, cause:{0}", ex.getMessage()));
		}
	}
	
	public List<MasterUserDTO> filterFetchEager(PssFilter filter) throws Exception {
		List<MasterUserDTO> list = filter(filter);
		for (MasterUserDTO dto : list) {
			Optional<MasterRoleDTO> role = roleService.findOneFetchEager(dto.getRole().getId());
			dto.setRole(role.isPresent() ? role.get() : null);
		}
		return list;
	}
	
	@Override
	public void delete(Long id) throws Exception {
		Optional<MasterUserDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new Exception("MasterUser with id:" + id + " not found");
		}

		validateRecordBeforeUpdate(op.get());
		String q = "DELETE FROM cm_sec_user WHERE id=? AND appname=?";
		try {
			getJdbcTemplate(datasource).update(q, id, this.appname);
			log.info("Delete MasterUser with id:{} successfully", id);
		} catch (Exception ex) {
			if (ex.getMessage().toLowerCase().contentEquals("constraint")) {
				q = "UPDATE cm_sec_user SET is_deleted=1 WHERE id=? AND appname=?";
				try {
					log.warn("MasterUser with id:{} will be flag as isDeleted", id);
					getJdbcTemplate(datasource).update(q, id, this.appname);
					log.info("Flag isDeleted for MasterUser with id:{} successfully", id);
				} catch(Exception ex1) {
					log.error("Cannot flag isDeleted for MasterUser with id:{}, cause:{}", id, ex.getMessage(), ex);
					throw new Exception("Cannot flag isDeleted for MasterUser with id:" + id);
				}
			} else {
				log.error("Cannot delete MasterUser with id:{}, cause:{}", id, ex.getMessage(), ex);
				throw new Exception("Cannot delete MasterUser with id:" + id);
			}
		}
	}

	@Override
	public void setActive(Long id, Boolean bool) throws Exception {
		Optional<MasterUserDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new Exception("MasterUser with id:" + id + " not found");
		}
		
		validateRecordBeforeUpdate(op.get());
		String q = "UPDATE cm_sec_user SET is_active=? WHERE id=? AND appname=?";
		try {
			Integer boolVal = bool ? 1:0;
			getJdbcTemplate(datasource).update(q, boolVal, id, this.appname);
			log.info("Flag active status for MasterUser with id:{} successfully", id);
		} catch (Exception ex) {
			log.error("Cannot update flag active for MasterUser with id:{}, cause:{}", id, ex.getMessage(), ex);
			throw new Exception("Cannot update flag active for MasterUser with id:" + id);
		}
	}
	
}
