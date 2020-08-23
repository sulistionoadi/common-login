package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MappingPrivilege;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface AccessMenuService {

	public void save(AccessMenuDTO dto) throws CommonException;
	public void update(AccessMenuDTO dto) throws CommonException;
	public Optional<AccessMenuDTO> findOne(Long id) throws CommonException;
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<AccessMenuDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException;
	public void delete(Long id) throws CommonException;
	public void setAsDelete(Long id, String updatedBy) throws CommonException;
	public void setActive(Long id, Boolean bool, String updatedBy) throws CommonException;
	public Set<AccessMenuDTO> getPermittedAccess(Long roleid) throws CommonException;
	public void saveMappingPrivilege(MasterRoleDTO role, Set<MappingPrivilege> privileges) throws CommonException;
	
}
