package com.sulistionoadi.ngoprek.common.dto.security;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true, of = {"role", "menu"})
public class MappingPrivilege implements Serializable {

	private static final long serialVersionUID = 5013573892996272685L;

	private MasterRoleDTO role;
	private AccessMenuDTO menu;
	private Integer actFilter;
	private Integer actCsv;
	private Integer actExcel;
	private Integer actPdf;
	private Integer actSave;
	private Integer actRemove;
	
}
