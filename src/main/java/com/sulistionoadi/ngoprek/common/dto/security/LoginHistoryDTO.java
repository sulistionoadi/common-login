package com.sulistionoadi.ngoprek.common.dto.security;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class LoginHistoryDTO implements Serializable {

	private static final long serialVersionUID = -7431750475354990957L;

	private Long lineNum;
	private String username;
	private Date activityDate;
	private String remark;

	@Builder
	public LoginHistoryDTO(Long lineNum, String username, Date activityDate, String remark) {
		this.lineNum = lineNum;
		this.username = username;
		this.activityDate = activityDate;
		this.remark = remark;
	}
	
}
