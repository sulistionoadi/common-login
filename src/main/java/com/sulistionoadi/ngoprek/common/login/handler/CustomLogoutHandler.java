package com.sulistionoadi.ngoprek.common.login.handler;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.login.service.LoginHistoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("customLogoutHandler")
public class CustomLogoutHandler implements LogoutSuccessHandler {

	private LoginHistoryService loginHitoryService;
	
	@Autowired
	public CustomLogoutHandler(LoginHistoryService loginHitoryService) {
		this.loginHitoryService = loginHitoryService;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		if(authentication!=null) {
			String username = authentication.getName();
			
			log.info("User {} Logging Out", username);
			LoginHistoryDTO dto = LoginHistoryDTO.builder().username(username).activityDate(new Date()).remark("Logout Success").build();
			try {
				loginHitoryService.log(dto);
			} catch (Exception e) {
				log.error("Cannot save Login Activity History, cause:{}", e.getMessage(), e);
			}
			
			response.sendRedirect("/login?loggedOut");			
		} else {
			log.warn("Authentication Session not found");
			response.sendRedirect("/login");
		}
	}

}
