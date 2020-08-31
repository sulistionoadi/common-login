package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;
import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.*;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.*;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MappingPrivilege;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.rowmapper.MasterRoleRowMapper;
import com.sulistionoadi.ngoprek.common.login.service.AccessMenuService;
import com.sulistionoadi.ngoprek.common.login.service.MasterRoleService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("masterRoleService")
public class MasterRoleServiceImpl extends DaoUtils implements MasterRoleService, Serializable {

	private static final long serialVersionUID = 5915454856023099621L;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired 
	private DataSource datasource;
	
	@Autowired
	private AccessMenuService accessMenuService;
	
	private void saveAccessMenu(MasterRoleDTO dto) throws CommonException {
		if(dto.getPermittedMenu()==null || dto.getPermittedMenu().isEmpty()) return;
		
		Set<MappingPrivilege> privileges = new HashSet<>();
		for (AccessMenuDTO menu : dto.getPermittedMenu()) {
			log.debug("Add MappingPrivilege with role_id:{} and menu_id:{}", dto.getId(), menu.getId());
			privileges.add(MappingPrivilege.builder()
					.role(dto).menu(menu)
					.actCsv(menu.getActCsv()?1:0).actExcel(menu.getActExcel()?1:0).actFilter(menu.getActFilter()?1:0)
					.actPdf(menu.getActPdf()?1:0).actRemove(menu.getActRemove()?1:0).actSave(menu.getActSave()?1:0)
					.build());
		}
		log.debug("Save MappingPrivilege for role_id:{} with size:{}", dto.getId(), privileges.size());
		accessMenuService.saveMappingPrivilege(dto, privileges);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(MasterRoleDTO dto) throws CommonException {
		Optional<MasterRoleDTO> exists = findByRolename(dto.getName());
		if(exists.isPresent()) {
			throw new CommonException(RC_DATA_ALREADY_EXIST, "Rolename already exists");
		}
		
		String sql = "INSERT INTO cm_sec_role (created_by, created_date, updated_by, updated_date, is_active, appname, rolename) "
				   + "VALUES (:createdBy, :createdDate, :updatedBy, :updatedDate, :isActive, :appname, :name)";

		try {
			NamedParameterJdbcTemplate jdbcTemplate = getNamedParameterJdbcTemplate(this.datasource);
			KeyHolder keyHolder = new GeneratedKeyHolder();
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
			
			Number newId = keyHolder.getKey();
			if(newId == null) throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get generated-id from database");
			
			log.info("Role {} saved with id:{}", dto.getName(), newId);
			dto.setId(newId.longValue());
			saveAccessMenu(dto);
			log.info("Save MasterRole successfully");
		} catch (Exception ex) {
			log.error("Cannot save MasterRole, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot save MasterRole", ex);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(MasterRoleDTO dto) throws CommonException {
		Optional<MasterRoleDTO> op = findOne(dto.getId());
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterRole with id:" + dto.getId() + " not found");
		}
		
		Optional<MasterRoleDTO> exists = findByRolename(dto.getName());
		if(exists.isPresent()) {
			if(!exists.get().getId().equals(dto.getId()))
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Rolename already exists");
		}
		
		validateRecordBeforeUpdate(op.get());
		String sql = "UPDATE cm_sec_role SET "
				   + "    updated_by=:updatedBy, updated_date=:updatedDate, is_active=:isActive, rolename=:name "
				   + "WHERE id=:id AND appname=:appname";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			saveAccessMenu(dto);
			log.info("Update MasterRole with id:{} successfully", dto.getId());
		} catch (Exception ex) {
			log.error("Cannot update MasterRole, cause:{}", ex.getMessage());
			if(ex.getMessage().toLowerCase().indexOf("unique constraint")>-1)
				throw new CommonException(RC_DATA_ALREADY_EXIST, "Data already exists");
			else
				throw new CommonException(RC_DB_QUERY_ERROR, "Cannot update MasterRole", ex);
		}
	}

	@Override
	public Optional<MasterRoleDTO> findOne(Long id) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_role m WHERE m.id=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get MasterRole with id:{}", id);
			MasterRoleDTO dto = getJdbcTemplate(datasource).queryForObject(sql, new Object[] { id, this.appname }, new MasterRoleRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("MasterRole with id:{} not found", id);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get MasterRole with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Cannot get MasterRole with id:{0}", 
					id != null ? id.toString() : "null"), ex);
		}
	}
	
	public Optional<MasterRoleDTO> findOneFetchEager(Long id) throws CommonException {
		Optional<MasterRoleDTO> op = findOne(id);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterRoleDTO dto = op.get();
		dto.setPermittedMenu(accessMenuService.getPermittedAccess(dto.getId()));
		return op;
	}
	
	@Override
	public Optional<MasterRoleDTO> findByRolename(String rolename) throws CommonException {
		String sql = "SELECT m.* FROM cm_sec_role m WHERE m.rolename=? AND m.appname=? AND m.is_deleted=0";
		try {
			log.debug("Get MasterRole with rolename:{}", rolename);
			MasterRoleDTO dto = getJdbcTemplate(datasource).queryForObject(sql, 
					new Object[] { rolename, this.appname }, new MasterRoleRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("MasterRole with rolename:{} not found", rolename);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get MasterRole with rolename:{}, cause:{}", rolename, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat.format("Cannot get MasterRole with rolename:{0}", rolename), ex);
		}
	}
	
	public Optional<MasterRoleDTO> findByRolenameFetchEager(String rolename) throws CommonException {
		Optional<MasterRoleDTO> op = findByRolename(rolename);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterRoleDTO dto = op.get();
		dto.setPermittedMenu(accessMenuService.getPermittedAccess(dto.getId()));
		return op;
	}

	@Override
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException {
		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);

		String sql = "SELECT COUNT(*) FROM cm_sec_role m WHERE m.appname=:appname AND m.is_deleted=0 ";
		if(statusActive!=null) {
			sql += "    AND m.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(m.rolename) LIKE :filter ";
			sql += "    ) ";
		}

		log.info("Count list data MasterRole filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data MasterRole, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get count list data MasterRole", ex);
		}
	}

	@Override
	public List<MasterRoleDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException {
		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);

		String[] orderableColums = new String[]{"id", "rolename"};
		String sql = "SELECT * FROM ( " 
				   + "    SELECT DT.*, " 
				   + "           row_number() over ( "
				   + "               ORDER BY DT." + getOrderBy(filter, "ID", orderableColums)
				   + "           ) line_number " 
				   + "    FROM ( "
				   + "        SELECT m.* FROM cm_sec_role m "
				   + "        WHERE m.appname = :appname AND m.is_deleted=0 ";
		if(statusActive!=null) {
			sql += "            AND m.is_active=:isActive ";
			param.put("isActive", statusActive.equals(StatusActive.YES) ? 1 : 0);
		}
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			  sql += "          AND ( ";
			  sql += "                 lower(m.rolename) LIKE :filter ";
			  sql += "          ) ";
		}
		      sql += "    ) DT ";
		      sql += ") WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";

		log.info("Get list data MasterRole filter by {}", param);

		try {
			List<MasterRoleDTO> datas = getNamedParameterJdbcTemplate(datasource).query(sql, param, new MasterRoleRowMapper());
			return datas;
		} catch (Exception ex) {
			log.error("Cannot get list data MasterRole, cause:{}", ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot get list data MasterRole", ex);
		}
	}
	
	public List<MasterRoleDTO> filterFetchEager(PssFilter filter, StatusActive statusActive) throws CommonException {
		List<MasterRoleDTO> list = filter(filter, statusActive);
		for (MasterRoleDTO dto : list) {
			dto.setPermittedMenu(accessMenuService.getPermittedAccess(dto.getId()));
		}
		return list;
	}
	
	public List<MasterRoleDTO> getForSelection() throws CommonException {
		PssFilter pss = new PssFilter();
		pss.setSearch(new HashMap<>());
		pss.setOrder(new ArrayList<>());
		
		Long countAll = count(pss, StatusActive.YES);
		
		pss.setStart(0);
		pss.setLength(countAll.intValue());
		
		HashMap<String, String> orderBy = new HashMap<>();
		orderBy.put(PSS_ORDER_COLUMN, "1");
		orderBy.put(PSS_ORDER_DIRECTION, "asc");
		pss.getOrder().add(orderBy);
		
		return filter(pss, StatusActive.YES);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delete(Long id) throws CommonException {
		Optional<MasterRoleDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterRole with id:" + id + " not found");
		}

		validateRecordBeforeUpdate(op.get());
		try {
			String q = "DELETE FROM cm_sec_role WHERE id=? AND appname=?";
			getJdbcTemplate(datasource).update(q, id, this.appname);
			log.info("Delete MasterRole with id:{} successfully", id);
		} catch (Exception ex) {
			log.error("Cannot delete MasterRole with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot delete MasterRole with id:" + id, ex);
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setAsDelete(Long id, String updatedBy) throws CommonException {
		Optional<MasterRoleDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterRole with id:" + id + " not found");
		}

		JdbcTemplate jdbcTemplate = getJdbcTemplate(datasource);
		validateRecordBeforeUpdate(op.get());

		try {
			String q = "UPDATE cm_sec_role SET is_deleted=1, rolename=CONCAT(rolename, CONCAT('-', id)), updated_by=?, updated_date=? "
					 + "WHERE id=? AND appname=?";
			
			log.warn("MasterRole with id:{} will be flag as isDeleted", id);
			jdbcTemplate.update(q, updatedBy, new Date(), id, this.appname);
			log.info("Flag isDeleted for MasterRole with id:{} successfully", id);
		} catch(Exception ex) {
			log.error("Cannot flag isDeleted for MasterRole with id:{}, cause:{}", id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, "Cannot flag isDeleted for MasterRole with id:" + id, ex);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setActive(Long id, StatusActive statusActive, String updatedBy) throws CommonException {
		Optional<MasterRoleDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "MasterRole with id:" + id + " not found");
		}
		
		validateRecordBeforeUpdate(op.get());
		String q = "UPDATE cm_sec_role SET is_active=?, updated_by=?, updated_date=? WHERE id=? AND appname=?";
		try {
			Integer boolVal = statusActive!=null && statusActive.equals(StatusActive.YES) ? 1:0;
			getJdbcTemplate(datasource).update(q, boolVal, updatedBy, new Date(), id, this.appname);
			log.info("Flag isActive={} for MasterRole with id:{} successfully", statusActive, id);
		} catch (Exception ex) {
			log.error("Cannot update isActive={} for MasterRole with id:{}, cause:{}", statusActive, id, ex.getMessage());
			throw new CommonException(RC_DB_QUERY_ERROR, MessageFormat
					.format("Cannot update flag isActive={0} for MasterRole with id:{1}", statusActive, id.toString()), ex);
		}
	}

}
