package com.sulistionoadi.ngoprek.common.login.config;

import java.beans.Statement;
import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.sulistionoadi.ngoprek.common.utils.SessionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class DataAuditorAspect {

	@Before("execution(* com.sulistionoadi.ngoprek.common.login.service.impl..*.save(..))")
	public void beforeSave(JoinPoint joinPoint) {
		Statement stmt;
		Object theDTO = joinPoint.getArgs()[0];
		String dtoClass = theDTO.getClass().getSimpleName();
		log.debug("Before Save {} ", dtoClass);
		
		String principal = SessionUtils.getPrincipalName();
		
		try {
			log.debug("Set CreatedBy in object {} as {}", dtoClass, principal);
			stmt = new Statement(theDTO, "setCreatedBy", new Object[] {principal});
			stmt.execute();
		} catch (Exception e) {
			throw new RuntimeException("Failed inject CreatedBy value", e);
		}
		
		try {
			log.debug("Set CreatedDate in object {} as {}", dtoClass, new Date());
			stmt = new Statement(theDTO, "setCreatedDate", new Object[] {new Date()});
			stmt.execute();
		} catch (Exception e) {
			throw new RuntimeException("Failed inject CreatedDate value", e);
		}
	
	}
	
	@Before("execution(* com.sulistionoadi.ngoprek.common.login.service.impl..*.update(..))")
	public void beforeUpdate(JoinPoint joinPoint) {
		Statement stmt;
		Object theDTO = joinPoint.getArgs()[0];
		String dtoClass = theDTO.getClass().getSimpleName();
		log.debug("Before Update {} ", dtoClass);
		
		String principal = SessionUtils.getPrincipalName();
		
		try {
			log.debug("Set UpdatedBy in object {} as {}", dtoClass, principal);
			stmt = new Statement(theDTO, "setUpdatedBy", new Object[] {principal});
			stmt.execute();
		} catch (Exception e) {
			throw new RuntimeException("Failed inject UpdatedBy value", e);
		}
		
		try {
			log.debug("Set UpdatedDate in object {} as {}", dtoClass, new Date());
			stmt = new Statement(theDTO, "setUpdatedDate", new Object[] {new Date()});
			stmt.execute();
		} catch (Exception e) {
			throw new RuntimeException("Failed inject UpdatedDate value", e);
		}
	}
	
}
