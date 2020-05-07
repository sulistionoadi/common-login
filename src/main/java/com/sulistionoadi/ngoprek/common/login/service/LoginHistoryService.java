package com.sulistionoadi.ngoprek.common.login.service;

import java.util.List;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface LoginHistoryService {

	public void log(LoginHistoryDTO dto) throws Exception;
	public Long count(PssFilter filter) throws Exception;
	public List<MasterRoleDTO> filter(PssFilter filter) throws Exception;
	
}
