package com.sulistionoadi.ngoprek.common.login.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sulistionoadi.ngoprek.common.builder.DefaultResponseBuilder;
import com.sulistionoadi.ngoprek.common.dto.DefaultResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler, Serializable {
	
	private static final long serialVersionUID = -8330732037716240260L;

	@Autowired
    private ObjectMapper objectMapper;
    
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    
	@Override
	public void handle(HttpServletRequest req, HttpServletResponse res,
			AccessDeniedException ex) throws IOException, ServletException {
		log.error("Handle Error, cause:{}", ex.getMessage(), ex);
        
        if(req.getRequestURI().startsWith("/api")) {
        	HttpStatus status = HttpStatus.FORBIDDEN;
        	
        	res.setStatus(status.value());
        	res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        	DefaultResponse response = DefaultResponseBuilder.builder()
        			.setCode(String.valueOf(status.value()))
        			.setMessage(ex.getMessage())
        			.build();
        	
        	try (PrintWriter writer = res.getWriter()) {
        		writer.write(objectMapper.writeValueAsString(response));
        		writer.flush();
        		writer.close();
        	} catch(Exception e) {
        		log.error(e.getMessage(), e);
        	}
        } else {
        	HttpSession session = req.getSession();
        	session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, ex.getMessage());
        	redirectStrategy.sendRedirect(req, res, "/403");
        }
	}

}
