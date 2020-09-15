package com.sulistionoadi.ngoprek.common.login.rest;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;

import java.util.Collections;
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
import com.sulistionoadi.ngoprek.common.builder.Select2ResponseBuilder;
import com.sulistionoadi.ngoprek.common.dto.DefaultResponse;
import com.sulistionoadi.ngoprek.common.dto.Select2Response;
import com.sulistionoadi.ngoprek.common.dto.StatusActive;
import com.sulistionoadi.ngoprek.common.dto.security.MasterRoleDTO;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.service.MasterRoleService;
import com.sulistionoadi.ngoprek.common.pss.builder.DatatableResponseBuilder;
import com.sulistionoadi.ngoprek.common.pss.constant.PssConstant;
import com.sulistionoadi.ngoprek.common.pss.dto.DatatableResponse;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.pss.helper.PssHelper;

import lombok.extern.slf4j.Slf4j;

@RestController	
@RequestMapping("/api/role")
@Slf4j
public class MasterRoleRestApi {

	private MasterRoleService service;
	
	@Autowired
	public MasterRoleRestApi(MasterRoleService service) {
		this.service = service;
	}

	@GetMapping("/draw")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DatatableResponse> draw(PssFilter filter) throws Exception{
		log.debug("draw master role, filter:[{}]", filter.toString());
		
		Long recordTotal = service.count(filter, null);
		List<MasterRoleDTO> listData = service.filter(filter, null);
		return ResponseEntity.ok(DatatableResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS)
				.setMessage("Success")
				.setDraw(filter.getDraw())
				.setData(listData)
				.setRecordsFiltered(recordTotal)
				.setRecordsTotal(recordTotal)
				.setSearch(filter.getSearch().get(PssConstant.PSS_SEARCH_VAL))
				.build());
	}
	
	@GetMapping("/fetch")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DatatableResponse> fetch(PssFilter filter) throws Exception{
		log.debug("fetch master role, filter:[{}]", filter.toString());
		
		Long recordTotal = service.count(filter, null);
		List<MasterRoleDTO> listData = service.filterFetchEager(filter, null);
		return ResponseEntity.ok(DatatableResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS)
				.setMessage("Success")
				.setDraw(filter.getDraw())
				.setData(listData)
				.setRecordsFiltered(recordTotal)
				.setRecordsTotal(recordTotal)
				.setSearch(filter.getSearch().get(PssConstant.PSS_SEARCH_VAL))
				.build());
	}
	
	@PostMapping("")
	@PreAuthorize("hasAuthority('API_SAVE_ROLE')")
	public ResponseEntity<DefaultResponse> save(@RequestBody MasterRoleDTO dto) throws CommonException {
		log.debug("save MasterRole {}", dto);
		
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
	@PreAuthorize("hasAuthority('API_DELETE_ROLE')")
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
	
	@GetMapping("/select2")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<Select2Response> select2 (
			@RequestParam("q") String q, @RequestParam("page") Integer page) {
		
		PssFilter filter = PssHelper.buildSelect2Filter(q, page, 1, "asc");
		log.debug("filter data role for select2, filter:[{}]", filter.toString());
		
		String errCode = RC_OTHER_ERROR;
		String message = "Error";
		List<MasterRoleDTO> items = Collections.emptyList();
		Long recordTotal = 0L;
		Boolean incompleteResult = Boolean.TRUE;
		
		try {
			errCode = RC_TRANSACTION_SUCCESS;
			message = "Success";
			recordTotal = service.count(filter, StatusActive.YES);
			items = service.filter(filter, StatusActive.YES);
			incompleteResult = (page*10) >= recordTotal;
		} catch(CommonException ex) {
			errCode = RC_DB_QUERY_ERROR;
			message = ex.getMessage();
			items = Collections.emptyList();
			recordTotal = 0L;
			incompleteResult = Boolean.TRUE;
		} catch(Exception ex) {
			errCode = RC_OTHER_ERROR;
			message = ex.getMessage();
			items = Collections.emptyList();
			recordTotal = 0L;
			incompleteResult = Boolean.TRUE;
		}
		
		return ResponseEntity.ok(Select2ResponseBuilder.builder()
				.setCode(errCode).setMessage(message)
				.setItems(items)
				.setTotalCount(recordTotal)
				.setIncompleteResults(incompleteResult)
				.build());
	}
	
	@GetMapping("/findOne/{id}")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DefaultResponse> findOne(@PathVariable Long id) throws CommonException {
		Optional<MasterRoleDTO> op = service.findOne(id);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterRole with id:"+id+" not found")
					.build());
		}
	}
	
	@GetMapping("/fetch/id/{id}")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DefaultResponse> fetchById(@PathVariable Long id) throws CommonException {
		Optional<MasterRoleDTO> op = service.findOneFetchEager(id);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterRole with id:"+id+" not found")
					.build());
		}
	}
	
	@GetMapping("/findByRolename/{name}")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DefaultResponse> findByRolename(@PathVariable String name) throws CommonException {
		Optional<MasterRoleDTO> op = service.findByRolename(name);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterRole with rolename:"+name+" not found")
					.build());
		}
	}
	
	@GetMapping("/fetch/rolename/{name}")
	@PreAuthorize("hasAuthority('API_LIST_ROLE')")
	public ResponseEntity<DefaultResponse> fetchByRolename(@PathVariable String name) throws CommonException {
		Optional<MasterRoleDTO> op = service.findByRolenameFetchEager(name);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterRole with rolename:"+name+" not found")
					.build());
		}
	}
	
	@PutMapping("/setStatus/{id}")
	@PreAuthorize("hasAuthority('API_SAVE_ROLE')")
	public ResponseEntity<DefaultResponse> setStatus(@PathVariable Long id, 
			@RequestParam(name="is_active", required=false) Boolean isActive) throws CommonException {
		
		if(isActive==null) isActive=Boolean.FALSE;
		service.setActive(id, isActive ? StatusActive.YES : StatusActive.NO, null);

		return ResponseEntity.ok(DefaultResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
				.build());
	}
	
}
