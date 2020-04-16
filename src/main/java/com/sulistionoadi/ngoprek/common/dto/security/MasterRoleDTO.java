package com.sulistionoadi.ngoprek.common.dto.security;

import java.util.Date;
import java.util.Set;

import com.sulistionoadi.ngoprek.common.dto.BaseMasterDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class MasterRoleDTO extends BaseMasterDTO {
	
	private static final long serialVersionUID = -733340342573751196L;

	private String name;
	private Set<AccessMenuDTO> permittedMenu;

	@Builder
	public MasterRoleDTO(Long id, String createdBy, Date createdDate, String updatedBy, Date updatedDate,
			Boolean isDeleted, Boolean isActive, String name, Set<AccessMenuDTO> permittedMenu) {
		super(id, createdBy, createdDate, updatedBy, updatedDate, isDeleted, isActive);
		this.name = name;
		this.permittedMenu = permittedMenu;
	}
	
}
