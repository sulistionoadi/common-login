package com.sulistionoadi.ngoprek.common.login.rest;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sulistionoadi.ngoprek.common.builder.DefaultResponseBuilder;
import com.sulistionoadi.ngoprek.common.dto.DefaultResponse;
import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.service.AccessMenuService;
import com.sulistionoadi.ngoprek.common.pss.builder.DatatableResponseBuilder;
import com.sulistionoadi.ngoprek.common.pss.constant.PssConstant;
import com.sulistionoadi.ngoprek.common.pss.dto.DatatableResponse;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;

import lombok.extern.slf4j.Slf4j;

@RestController	
@RequestMapping("/api/menu")
@Slf4j
public class MasterMenuRestApi {

	private AccessMenuService service;
	
	@Autowired
	public MasterMenuRestApi(AccessMenuService service) {
		this.service = service;
	}

	@GetMapping("/draw")
	@PreAuthorize("hasAuthority('API_LIST_MENU')")
	public ResponseEntity<DatatableResponse> draw(PssFilter filter) throws Exception{
		log.debug("draw master menu, filter:[{}]", filter.toString());
		
		Long recordTotal = service.count(filter, null);
		List<AccessMenuDTO> listData = service.filter(filter, null);
		return ResponseEntity.ok(DatatableResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS)
				.setMessage("Success")
				.setDraw(filter.getDraw())
				.setData(listData)
				.setRecordsFiltered(Long.valueOf(listData.size()))
				.setRecordsTotal(recordTotal)
				.setSearch(filter.getSearch().get(PssConstant.PSS_SEARCH_VAL))
				.build());
	}
	
	@PostMapping("")
	@PreAuthorize("hasAuthority('API_SAVE_MENU')")
	public ResponseEntity<DefaultResponse> save(@RequestBody AccessMenuDTO dto) throws CommonException {
		log.debug("save AccessMenu {}", dto);
		
		dto.setIsActive(Boolean.TRUE);
		if(dto.getId()==null) {
			service.save(dto);
		} else {
			service.update(dto);
		}
		
		return ResponseEntity.ok(DefaultResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
				.build());
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('API_DELETE_MENU')")
	public ResponseEntity<DefaultResponse> delete(@PathVariable Long id) throws CommonException {
		log.debug("delete master role with id:[{}]", id);
		
		ResponseEntity<DefaultResponse> resp = ResponseEntity.ok(DefaultResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
				.build());
		
		try {
			service.delete(id);
			return resp;
		} catch (Exception ex) {
			if (ex.getMessage().toLowerCase().indexOf("constraint")>-1) {
				service.setAsDelete(id, null);
				return resp;
			} else {
				throw ex;
			}
		}
	}
	
	@GetMapping("/findOne/{id}")
	@PreAuthorize("hasAuthority('API_LIST_MENU')")
	public ResponseEntity<DefaultResponse> findOne(@PathVariable Long id) throws CommonException {
		Optional<AccessMenuDTO> op = service.findOne(id);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("AccessMenu with id:"+id+" not found")
					.build());
		}
	}
	
	@GetMapping("/findByCode/{code}")
	@PreAuthorize("hasAuthority('API_LIST_MENU')")
	public ResponseEntity<DefaultResponse> findByRolename(@PathVariable String code) throws CommonException {
		Optional<AccessMenuDTO> op = service.findByCode(code);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("AccessMenu with code:"+code+" not found")
					.build());
		}
	}
	
	@PutMapping("/setStatus/{id}")
	@PreAuthorize("hasAuthority('API_SAVE_MENU')")
	public ResponseEntity<DefaultResponse> setStatus(@PathVariable Long id, 
			@RequestParam(name="is_active", required=false) Boolean isActive) throws CommonException {
		if(isActive==null) isActive=Boolean.FALSE;
		service.setActive(id, isActive ? StatusActive.YES : StatusActive.NO, null);
		return ResponseEntity.ok(DefaultResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
				.build());
	}
	
}
