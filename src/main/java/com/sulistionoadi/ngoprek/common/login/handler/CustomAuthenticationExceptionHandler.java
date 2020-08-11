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
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sulistionoadi.ngoprek.common.builder.DefaultResponseBuilder;
import com.sulistionoadi.ngoprek.common.dto.DefaultResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationExceptionHandler implements AuthenticationEntryPoint, Serializable {

	private static final long serialVersionUID = -4252602766237358017L;

	@Autowired
    private ObjectMapper objectMapper;
    
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ae) throws IOException, ServletException {
        log.error("Authentication Error, cause:{}", ae.getMessage(), ae);
        
        if(req.getRequestURI().startsWith("/api")) {
        	HttpStatus status = HttpStatus.OK;
        	if(ae instanceof InsufficientAuthenticationException) {
        		status = HttpStatus.UNAUTHORIZED;
        	} else {
        		status = HttpStatus.FORBIDDEN;
        	}
        	
        	res.setStatus(status.value());
        	DefaultResponse response = DefaultResponseBuilder.builder()
        			.setCode(String.valueOf(status.value()))
        			.setMessage(ae.getMessage())
        			.build();
        	
        	try (PrintWriter writer = res.getWriter()) {
        		writer.write(objectMapper.writeValueAsString(response));
        		writer.flush();
        		writer.close();
        	} catch(Exception ex) {
        		log.error(ex.getMessage(), ex);
        	}
        } else {
        	HttpSession session = req.getSession();
        	session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, ae.getMessage());
        	if(ae instanceof InsufficientAuthenticationException) {
        		redirectStrategy.sendRedirect(req, res, "/login");
        	} else {
        		redirectStrategy.sendRedirect(req, res, "/403");
        	}
        }
    }
    
}
