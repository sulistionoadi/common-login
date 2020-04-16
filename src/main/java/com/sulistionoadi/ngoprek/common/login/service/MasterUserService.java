package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;
import java.util.Optional;

import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface MasterUserService {

	public void save(MasterUserDTO dto) throws Exception;
	public void update(MasterUserDTO dto) throws Exception;
	public Optional<MasterUserDTO> findOne(Long id) throws Exception;
	public Optional<MasterUserDTO> findOneFetchEager(Long id) throws Exception;
	public Optional<MasterUserDTO> findByUsername(String username) throws Exception;
	public Optional<MasterUserDTO> findByUsernameFetchEager(String username) throws Exception;
	public Long count(PssFilter filter) throws Exception;
	public List<MasterUserDTO> filter(PssFilter filter) throws Exception;
	public List<MasterUserDTO> filterFetchEager(PssFilter filter) throws Exception;
	public void delete(Long id) throws Exception;
	public void setActive(Long id, Boolean bool) throws Exception;
	
}
