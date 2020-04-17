package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface AccessMenuService {

	public void save(AccessMenuDTO dto) throws Exception;
	public void update(AccessMenuDTO dto) throws Exception;
	public Optional<AccessMenuDTO> findOne(Long id) throws Exception;
	public Long count(PssFilter filter, StatusActive statusActive) throws Exception;
	public List<AccessMenuDTO> filter(PssFilter filter, StatusActive statusActive) throws Exception;
	public void delete(Long id) throws Exception;
	public void setActive(Long id, Boolean bool) throws Exception;
	public Set<AccessMenuDTO> getPermittedAccess(Long roleid) throws Exception;
	
}
