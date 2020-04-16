package com.sulistionoadi.ngoprek.common.utils;

import java.beans.Expression;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DaoUtils {

	public JdbcTemplate getJdbcTemplate(DataSource datasource) {
		return new JdbcTemplate(datasource);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource datasource) {
		return new NamedParameterJdbcTemplate(datasource);
	}
	
	public void validateRecordBeforeUpdate(Object o) throws Exception {
		String className = o.getClass().getSimpleName();
		className = className.replaceAll("(?i)dto", "");
		
		Expression expr;

		//Get ID
		expr = new Expression(o, "getId", new Object[0]);
		Long id = (Long) expr.getValue();
		
		//Get IS_DELETED
		expr = new Expression(o, "getIsDeleted", new Object[0]);
		Boolean isDeleted = (Boolean) expr.getValue();
				
		if (isDeleted)
			throw new Exception(MessageFormat.format("{0} with id:{1,number,#} already deleted", className, id));
	}
	
}
