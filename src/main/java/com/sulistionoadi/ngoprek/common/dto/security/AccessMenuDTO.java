package com.sulistionoadi.ngoprek.common.dto.security;

import java.util.Date;
import java.util.Set;

import com.sulistionoadi.ngoprek.common.dto.BaseMasterDTO;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, doNotUseGetters = true, of = {"menuCode"})
public class AccessMenuDTO extends BaseMasterDTO {

	private static final long serialVersionUID = 3800566267254712419L;
	
	private String menuCode;
	private String menuName;
	private Integer priority;
	private Long parentid;
	private Boolean actFilter;
	private Boolean actCsv;
	private Boolean actExcel;
	private Boolean actPdf;
	private Boolean actSave;
	private Boolean actRemove;
	private Set<AccessMenuDTO> childs;

	@Builder
	public AccessMenuDTO(Long id, String createdBy, Date createdDate, String updatedBy, Date updatedDate,
			Boolean isDeleted, Boolean isActive, String menuCode, String menuName, Integer priority, Long parentid,
			Boolean actFilter, Boolean actCsv, Boolean actExcel, Boolean actPdf, Boolean actSave, Boolean actRemove,
			Set<AccessMenuDTO> childs) {
		super(id, createdBy, createdDate, updatedBy, updatedDate, isDeleted, isActive);
		this.menuCode = menuCode;
		this.menuName = menuName;
		this.priority = priority;
		this.parentid = parentid;
		this.actFilter = actFilter;
		this.actCsv = actCsv;
		this.actExcel = actExcel;
		this.actPdf = actPdf;
		this.actSave = actSave;
		this.actRemove = actRemove;
		this.childs = childs;
	}
	
}
