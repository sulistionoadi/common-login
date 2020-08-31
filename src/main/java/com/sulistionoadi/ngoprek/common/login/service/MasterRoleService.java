package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface MasterRoleService {

	public void save(MasterRoleDTO dto) throws CommonException;
	public void update(MasterRoleDTO dto) throws CommonException;
	public Optional<MasterRoleDTO> findOne(Long id) throws CommonException;
	public Optional<MasterRoleDTO> findByRolename(String rolename) throws CommonException;
	public Optional<MasterRoleDTO> findOneFetchEager(Long id) throws CommonException;
	public Optional<MasterRoleDTO> findByRolenameFetchEager(String rolename) throws CommonException;
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<MasterRoleDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<MasterRoleDTO> filterFetchEager(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<MasterRoleDTO> getForSelection() throws CommonException;
	public void delete(Long id) throws CommonException;
	public void setAsDelete(Long id, String updatedBy) throws CommonException;
	public void setActive(Long id, StatusActive statusActive, String updatedBy) throws CommonException;
	
}
