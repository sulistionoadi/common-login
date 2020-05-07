package com.sulistionoadi.ngoprek.common.login.service;

import java.util.Date;
import java.util.List;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

public interface LoginHistoryService {

	public void log(LoginHistoryDTO dto) throws Exception;
	public Long count(PssFilter filter, Date startDate, Date endDate) throws Exception;
	public List<LoginHistoryDTO> filter(PssFilter filter, Date startDate, Date endDate) throws Exception;
	
}
