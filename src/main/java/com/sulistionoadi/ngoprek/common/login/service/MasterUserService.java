package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;

import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface MasterUserService {

	public void save(MasterUserDTO dto) throws CommonException;
	public void update(MasterUserDTO dto) throws CommonException;
	public Optional<MasterUserDTO> findOne(Long id) throws CommonException;
	public Optional<MasterUserDTO> findOneFetchEager(Long id) throws CommonException;
	public Optional<MasterUserDTO> findByUsername(String username) throws CommonException;
	public Optional<MasterUserDTO> findByUsernameFetchEager(String username) throws CommonException;
	public Long count(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<MasterUserDTO> filter(PssFilter filter, StatusActive statusActive) throws CommonException;
	public List<MasterUserDTO> filterFetchEager(PssFilter filter, StatusActive statusActive) throws CommonException;
	public void delete(Long id) throws CommonException;
	public void setAsDelete(Long id, String updatedBy) throws CommonException;
	public void setActive(Long id, StatusActive statusActive, String updatedBy) throws CommonException;
	
}
