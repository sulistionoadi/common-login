package com.sulistionoadi.ngoprek.common.login.service.impl;

import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.PSS_ORDER_COLUMN;
import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.PSS_ORDER_DIRECTION;
import static com.sulistionoadi.ngoprek.common.pss.constant.PssConstant.PSS_SEARCH_VAL;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generateCountPssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.generatePssParameter;
import static com.sulistionoadi.ngoprek.common.pss.utils.PssUtils.getOrderBy;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.sulistionoadi.ngoprek.common.login.rowmapper.MasterRoleRowMapper;
import com.sulistionoadi.ngoprek.common.login.service.AccessMenuService;
import com.sulistionoadi.ngoprek.common.login.service.MasterRoleService;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.utils.CombinedSqlParameterSource;
import com.sulistionoadi.ngoprek.common.utils.DaoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("masterRoleService")
public class MasterRoleServiceImpl extends DaoUtils implements MasterRoleService, Serializable {

	private static final long serialVersionUID = 5915454856023099621L;
	
	@Value("${app.name:MYAPP}")
	private String appname;
	
	@Autowired 
	private DataSource datasource;
	
	@Autowired
	private AccessMenuService accessMenuService;

	@Override
	public void save(MasterRoleDTO dto) throws Exception {
		String sql = "INSERT INTO cm_sec_role ("
				   + "    id, created_by, created_date, updated_by, updated_date,"
				   + "    is_deleted, is_active, appname, rolename "
				   + ") VALUES ("
				   + "    :id, :createdBy, :createdDate, :updatedBy, :updatedDate, "
				   + "    :isDeleted, :isActive, :appname, :name"
				   + ")";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Save MasterRole successfully");
		} catch (Exception ex) {
			log.error("Cannot save MasterRole, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot save MasterRole, cause:" + ex.getMessage());
		}
	}

	@Override
	public void update(MasterRoleDTO dto) throws Exception {
		Optional<MasterRoleDTO> op = findOne(dto.getId());
		if (!op.isPresent()) {
			throw new Exception("MasterRole with id:" + dto.getId() + " not found");
		}
		
		String sql = "UPDATE cm_sec_role SET "
				   + "    updated_by=:updatedBy, updated_date=:updatedDate, "
				   + "    is_deleted=:isDeleted, is_active=:isActive, "
				   + "    rolename=:name "
				   + "WHERE id=:id "
				   + "  AND appname=:appname";

		try {
			CombinedSqlParameterSource params = new CombinedSqlParameterSource(dto);
			params.addValue("appname", this.appname);
			
			getNamedParameterJdbcTemplate(this.datasource).update(sql, params);
			log.info("Update MasterRole with id:{} successfully", dto.getId());
		} catch (Exception ex) {
			log.error("Cannot update MasterRole, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot update MasterRole, cause:" + ex.getMessage());
		}
	}

	@Override
	public Optional<MasterRoleDTO> findOne(Long id) throws Exception {
		String sql = "SELECT m.* FROM cm_sec_role m WHERE m.id=? AND m.appname=?";
		try {
			log.debug("Get MasterRole with id:{}", id);
			MasterRoleDTO dto = getJdbcTemplate(datasource).queryForObject(sql, 
					new Object[] { id, this.appname }, new MasterRoleRowMapper());
			return Optional.of(dto);
		} catch (EmptyResultDataAccessException ex) {
			log.warn("MasterRole with id:{} not found", id);
			return Optional.empty();
		} catch (Exception ex) {
			log.error("Cannot get MasterRole with id:{}, cause:{}", id, ex.getMessage(), ex);
			throw new Exception(MessageFormat.format("Cannot get MasterRole with id:{0}", 
					id != null ? id.toString() : "null"), ex);
		}
	}
	
	public Optional<MasterRoleDTO> findOneFetchEager(Long id) throws Exception {
		Optional<MasterRoleDTO> op = findOne(id);
		if(!op.isPresent()) {
			return Optional.empty();
		}
		
		MasterRoleDTO dto = op.get();
		dto.setPermittedMenu(accessMenuService.getPermittedAccess(dto.getId()));
		return op;
	}

	@Override
	public Long count(PssFilter filter) throws Exception {
		String sql = "SELECT COUNT(*) FROM cm_sec_role m WHERE m.appname=:appname ";
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			sql += "    AND ( ";
			sql += "            lower(m.ROLENAME) LIKE :filter ";
			sql += "    ) ";
		}

		Map<String, Object> param = generateCountPssParameter(filter);
		param.put("appname", this.appname);
		log.info("Count list data MasterRole filter by {}", param);

		try {
			return getNamedParameterJdbcTemplate(datasource).queryForObject(sql, param, Long.class);
		} catch (Exception ex) {
			log.error("Cannot get count list data MasterRole, cause:{}", ex.getMessage(), ex);
			throw new Exception("Cannot get count list data MasterRole");
		}
	}

	@Override
	public List<MasterRoleDTO> filter(PssFilter filter) throws Exception {
		String[] orderableColums = new String[]{"ID", "ROLENAME"};
		String sql = "SELECT * FROM ( " 
				   + "    SELECT DT.*, " 
				   + "           row_number() over ( "
				   + "               ORDER BY DT." + getOrderBy(filter, "ID", orderableColums)
				   + "           ) line_number " 
				   + "    FROM ( "
				   + "        SELECT m.* FROM cm_sec_role m "
				   + "        WHERE appname = :appname ";
		if (StringUtils.hasText(filter.getSearch().get(PSS_SEARCH_VAL))) {
			  sql += "          AND ( ";
			  sql += "                 lower(m.ROLENAME) LIKE :filter ";
			  sql += "          ) ";
		}
		      sql += "    ) DT ";
		      sql += ") WHERE line_number BETWEEN :start_row AND :end_row ORDER BY line_number";

		Map<String, Object> param = generatePssParameter(filter);
		param.put("appname", this.appname);
		log.info("Get list data MasterRole filter by {}", param);

		try {
			List<MasterRoleDTO> datas = getNamedParameterJdbcTemplate(datasource).query(sql, param,
					new MasterRoleRowMapper());
			return datas;
		} catch (Exception ex) {
			log.error("Cannot get list data MasterRole, cause:{}", ex.getMessage(), ex);
			throw new Exception(MessageFormat.format("Cannot get list data MasterRole, cause:{0}", ex.getMessage()));
		}
	}
	
	public List<MasterRoleDTO> filterFetchEager(PssFilter filter) throws Exception {
		List<MasterRoleDTO> list = filter(filter);
		for (MasterRoleDTO dto : list) {
			dto.setPermittedMenu(accessMenuService.getPermittedAccess(dto.getId()));
		}
		return list;
	}
	
	public List<MasterRoleDTO> getAllRole() throws Exception {
		PssFilter pss = new PssFilter();
		pss.setSearch(new HashMap<>());
		pss.setOrder(new ArrayList<>());
		
		Long countAll = count(pss);
		
		pss.setStart(0);
		pss.setLength(countAll.intValue());
		
		HashMap<String, String> orderBy = new HashMap<>();
		orderBy.put(PSS_ORDER_COLUMN, "1");
		orderBy.put(PSS_ORDER_DIRECTION, "asc");
		pss.getOrder().add(orderBy);
		
		return filter(pss);
	}
	
	@Override
	public void delete(Long id) throws Exception {
		Optional<MasterRoleDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new Exception("MasterRole with id:" + id + " not found");
		}

		String q = "DELETE FROM cm_sec_role WHERE id=? AND appname=?";
		try {
			getJdbcTemplate(datasource).update(q, id, this.appname);
			log.info("Delete MasterRole with id:{} successfully", id);
		} catch (Exception ex) {
			if (ex.getMessage().toLowerCase().contentEquals("constraint")) {
				q = "UPDATE cm_sec_role SET is_deleted=1 WHERE id=? AND appname=?";
				try {
					log.warn("MasterRole with id:{} will be flag as isDeleted", id);
					getJdbcTemplate(datasource).update(q, id, this.appname);
					log.info("Flag isDeleted for MasterRole with id:{} successfully", id);
				} catch(Exception ex1) {
					log.error("Cannot flag isDeleted for MasterRole with id:{}, cause:{}", id, ex.getMessage(), ex);
					throw new Exception("Cannot flag isDeleted for MasterRole with id:" + id);
				}
			} else {
				log.error("Cannot delete MasterRole with id:{}, cause:{}", id, ex.getMessage(), ex);
				throw new Exception("Cannot delete MasterRole with id:" + id);
			}
		}
	}

	@Override
	public void setActive(Long id, Boolean bool) throws Exception {
		Optional<MasterRoleDTO> op = this.findOne(id);
		if (!op.isPresent()) {
			throw new Exception("MasterRole with id:" + id + " not found");
		}
		
		String q = "UPDATE cm_sec_role SET is_active=? WHERE id=? AND appname=?";
		try {
			Integer boolVal = bool ? 1:0;
			getJdbcTemplate(datasource).update(q, boolVal, id, this.appname);
			log.info("Flag active status for MasterRole with id:{} successfully", id);
		} catch (Exception ex) {
			log.error("Cannot update flag active for MasterRole with id:{}, cause:{}", id, ex.getMessage(), ex);
			throw new Exception("Cannot update flag active for MasterRole with id:" + id);
		}
	}
	
}
