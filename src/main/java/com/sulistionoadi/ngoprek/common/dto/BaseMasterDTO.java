package com.sulistionoadi.ngoprek.common.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseMasterDTO implements Serializable {

	private static final long serialVersionUID = 8458557714361897319L;

	private Long id;
	private String createdBy;
	private Date createdDate;
	private String updatedBy;
	private Date updatedDate;
	private Boolean isDeleted;
	private Boolean isActive;
	
}
