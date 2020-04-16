package com.sulistionoadi.ngoprek.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.dto.security.UserLogin;

public class AccessControlLevelUtils {

	public static final String HAS_FILTER = "has_actFilter";
	public static final String HAS_CSV = "has_actCsv";
	public static final String HAS_XLS = "has_actXls";
	public static final String HAS_PDF = "has_actPdf";
	public static final String HAS_SAVE = "has_actSave";
	public static final String HAS_REMOVE = "has_actRemove";
	
	public static Map<String, Boolean> getAccessButton(String menuCode){
		UserLogin session = (UserLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(session == null) {
			throw new AuthenticationCredentialsNotFoundException("Invalid Session");
		}
		
		AccessMenuDTO menuDto = session.findAccessMenuByCode(menuCode);
		
		Map<String, Boolean> mapAccess = new HashMap<String, Boolean>();
		mapAccess.put(HAS_FILTER, menuDto.getActFilter());
		mapAccess.put(HAS_CSV, menuDto.getActCsv());
		mapAccess.put(HAS_XLS, menuDto.getActExcel());
		mapAccess.put(HAS_PDF, menuDto.getActPdf());
		mapAccess.put(HAS_SAVE, menuDto.getActSave());
		mapAccess.put(HAS_REMOVE, menuDto.getActRemove());
		
		return mapAccess;
	}
	
}
