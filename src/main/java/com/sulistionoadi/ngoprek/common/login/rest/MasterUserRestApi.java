package com.sulistionoadi.ngoprek.common.login.rest;

import static com.sulistionoadi.ngoprek.common.constant.ErrorCode.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.sulistionoadi.ngoprek.common.dto.security.ChangePasswordDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.dto.security.UserLogin;
import com.sulistionoadi.ngoprek.common.exception.CommonException;
import com.sulistionoadi.ngoprek.common.login.service.MasterUserService;
import com.sulistionoadi.ngoprek.common.pss.builder.DatatableResponseBuilder;
import com.sulistionoadi.ngoprek.common.pss.constant.PssConstant;
import com.sulistionoadi.ngoprek.common.pss.dto.DatatableResponse;
import com.sulistionoadi.ngoprek.common.pss.dto.PssFilter;
import com.sulistionoadi.ngoprek.common.pss.helper.PssHelper;

import lombok.extern.slf4j.Slf4j;

@RestController	
@RequestMapping("/api/user")
@Slf4j
public class MasterUserRestApi {

	private MasterUserService service;
	private PasswordEncoder encoder;
	
	@Autowired
	public MasterUserRestApi(MasterUserService service, PasswordEncoder encoder) {
		this.service = service;
		this.encoder = encoder;
	}

	@GetMapping("/draw")
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DatatableResponse> draw(PssFilter filter) throws Exception{
		log.debug("draw master user, filter:[{}]", filter.toString());
		
		Long recordTotal = service.count(filter, null);
		List<MasterUserDTO> listData = service.filter(filter, null);
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
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DatatableResponse> fetch(PssFilter filter) throws Exception{
		log.debug("fetch master user, filter:[{}]", filter.toString());
		
		Long recordTotal = service.count(filter, null);
		List<MasterUserDTO> listData = service.filterFetchEager(filter, null);
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
	@PreAuthorize("hasAuthority('API_SAVE_USER')")
	public ResponseEntity<DefaultResponse> save(@RequestBody MasterUserDTO dto) throws CommonException {
		log.debug("save MasterUser {}", dto);
		
		if(StringUtils.isNoneBlank(dto.getPassword())) {
			dto.setPassword(encoder.encode(dto.getPassword()));
		}
		
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
	@PreAuthorize("hasAuthority('API_DELETE_USER')")
	public ResponseEntity<DefaultResponse> delete(@PathVariable Long id) throws CommonException {
		log.debug("delete master user with id:[{}]", id);
		
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
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<Select2Response> select2 (
			@RequestParam("q") String q, @RequestParam("page") Integer page) {
		
		PssFilter filter = PssHelper.buildSelect2Filter(q, page, 1, "asc");
		log.debug("filter data user for select2, filter:[{}]", filter.toString());
		
		String errCode = RC_OTHER_ERROR;
		String message = "Error";
		List<MasterUserDTO> items = Collections.emptyList();
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
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DefaultResponse> findOne(@PathVariable Long id) throws CommonException {
		Optional<MasterUserDTO> op = service.findOne(id);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterUser with id:"+id+" not found")
					.build());
		}
	}
	
	@GetMapping("/fetch/id/{id}")
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DefaultResponse> fetchById(@PathVariable Long id) throws CommonException {
		Optional<MasterUserDTO> op = service.findOneFetchEager(id);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterUser with id:"+id+" not found")
					.build());
		}
	}
	
	@GetMapping("/findByUsername/{name}")
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DefaultResponse> findByUsername(@PathVariable String name) throws CommonException {
		Optional<MasterUserDTO> op = service.findByUsername(name);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterUser with username:"+name+" not found")
					.build());
		}
	}
	
	@GetMapping("/fetch/username/{name}")
	@PreAuthorize("hasAuthority('API_LIST_USER')")
	public ResponseEntity<DefaultResponse> fetchByUsername(@PathVariable String name) throws CommonException {
		Optional<MasterUserDTO> op = service.findByUsernameFetchEager(name);
		
		if(op.isPresent()) {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
					.setData(op.get())
					.build());
		} else {
			return ResponseEntity.ok(DefaultResponseBuilder.builder()
					.setCode(RC_DATA_NOT_FOUND).setMessage("MasterUser with username:"+name+" not found")
					.build());
		}
	}
	
	@PutMapping("/setStatus/{id}")
	@PreAuthorize("hasAuthority('API_SAVE_USER')")
	public ResponseEntity<DefaultResponse> setStatus(@PathVariable Long id, 
			@RequestParam(name="is_active", required=false) Boolean isActive) throws CommonException {
		
		if(isActive==null) isActive=Boolean.FALSE;
		service.setActive(id, isActive ? StatusActive.YES : StatusActive.NO, null);

		return ResponseEntity.ok(DefaultResponseBuilder.builder()
				.setCode(RC_TRANSACTION_SUCCESS).setMessage("Success")
				.build());
	}
	
	@PreAuthorize("hasAuthority('MNU_CHANGE_PASSWORD')")
	@PostMapping("/changepass")
	public ResponseEntity<DefaultResponse> changePassword(ChangePasswordDTO passDto) throws Exception {
		UserLogin session = (UserLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(session == null) {
			throw new CommonException(RC_INVALID_SESSION, "Invalid session, please sign in first");
		}
		
		log.debug("User [{}] make a password change", session.getUsername());
		
		Optional<MasterUserDTO> userOp = service.findByUsername(session.getUsername());
		if(!userOp.isPresent()) {
			throw new CommonException(RC_DATA_NOT_FOUND, "Username not registered");
		}
		
		MasterUserDTO userDto = userOp.get();
		if(!encoder.matches(passDto.getOldPassword(), userDto.getPassword())) {
			throw new CommonException(RC_INVALID_PARAMETER, "Invalid current password");
		} else if(!passDto.getNewPassword().equals(passDto.getConfirmPassword())){
			throw new CommonException(RC_INVALID_PARAMETER, "New password didn't match");
		} else {
			userDto.setPassword(encoder.encode(passDto.getNewPassword()));
		}
		
		service.update(userDto);
		return ResponseEntity.ok(DefaultResponseBuilder.builder().setCode("00").setMessage("Update password success").build());
	}
	
}
