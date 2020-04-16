package com.sulistionoadi.ngoprek.common.dto.security;

import java.util.Date;

import com.sulistionoadi.ngoprek.common.dto.BaseMasterDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class MasterUserDTO extends BaseMasterDTO {
	
	private static final long serialVersionUID = -7715937163129643101L;
	
	private String username;
	private String password;
	private MasterRoleDTO role;

	@Builder
	public MasterUserDTO(Long id, String createdBy, Date createdDate, String updatedBy, Date updatedDate,
			Boolean isDeleted, Boolean isActive, String username, String password, MasterRoleDTO role) {
		super(id, createdBy, createdDate, updatedBy, updatedDate, isDeleted, isActive);
		this.username = username;
		this.password = password;
		this.role = role;
	}
	
}
