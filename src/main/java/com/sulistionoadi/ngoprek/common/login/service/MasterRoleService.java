package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;

import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface MasterRoleService {

	public void save(MasterRoleDTO dto) throws Exception;
	public void update(MasterRoleDTO dto) throws Exception;
	public Optional<MasterRoleDTO> findOne(Long id) throws Exception;
	public Optional<MasterRoleDTO> findOneFetchEager(Long id) throws Exception;
	public Long count(PssFilter filter) throws Exception;
	public List<MasterRoleDTO> filter(PssFilter filter) throws Exception;
	public List<MasterRoleDTO> filterFetchEager(PssFilter filter) throws Exception;
	public List<MasterRoleDTO> getAllRole() throws Exception;
	public void delete(Long id) throws Exception;
	public void setActive(Long id, Boolean bool) throws Exception;
	
}
