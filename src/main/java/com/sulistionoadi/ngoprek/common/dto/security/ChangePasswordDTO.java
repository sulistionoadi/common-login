package com.sulistionoadi.ngoprek.common.dto.security;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class ChangePasswordDTO implements Serializable {

	private static final long serialVersionUID = -2856359344303242480L;

	private MasterUserDTO userDto;
	private String oldPassword;
	private String newPassword;
	private String confirmPassword;

	@Builder
	public ChangePasswordDTO(MasterUserDTO userDto, String oldPassword, String newPassword, String confirmPassword) {
		this.userDto = userDto;
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
	}
	
}
