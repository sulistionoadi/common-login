package com.sulistionoadi.ngoprek.common.login.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sulistionoadi.ngoprek.common.dto.security.AccessMenuDTO;
import com.sulistionoadi.ngoprek.common.dto.security.LoginHistoryDTO;
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.dto.security.UserLogin;
import com.sulistionoadi.ngoprek.common.login.service.LoginHistoryService;
import com.sulistionoadi.ngoprek.common.login.service.MasterUserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	private PasswordEncoder encoder;
	private MasterUserService userService;
	private LoginHistoryService loginService;
	
	@Autowired
	public CustomAuthenticationProvider(PasswordEncoder encoder, MasterUserService userService,
			LoginHistoryService loginService) {
		this.encoder = encoder;
		this.userService = userService;
		this.loginService = loginService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		String rawpass = authentication.getCredentials().toString();
		//String encpass = encoder.encode(rawpass);
		
		Optional<MasterUserDTO> userOp;
		try {
			userOp = userService.findByUsernameFetchEager(name);
		} catch (Exception e) {
			saveLoginHistory(name, "Authentication failure, cause:" + e.getMessage());
			log.error("Authentication failure, cause:{}", e.getMessage(), e);
			throw new BadCredentialsException(e.getMessage());
		}
		
		if (!userOp.isPresent()) {
			throw new UsernameNotFoundException("Invalid Username/Password");
		} else if(!userOp.get().getIsActive()) {
			saveLoginHistory(name, "User is not active");
			throw new BadCredentialsException("User is not active");
		} else {
			log.debug("Checking Password [{} == {}]", rawpass, userOp.get().getPassword());
			if(!encoder.matches(rawpass, userOp.get().getPassword())) {
				log.warn("Invalid password for username {}", name);
				saveLoginHistory(name, "Invalid Password");
				throw new UsernameNotFoundException("Invalid Username/Password");
			}
			
			saveLoginHistory(name, "Login Success");
			UserLogin userObj = getUserLoginInfo(userOp.get());
			return new UsernamePasswordAuthenticationToken(userObj, userObj.getPassword(), userObj.getAuthorities());
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
	private UserLogin getUserLoginInfo(MasterUserDTO dto) {
		return UserLogin.builder()
				.username(dto.getUsername())
				.password("")
				.enabled(dto.getIsActive())
				.rolename(dto.getRole().getName())
				.menus(buildStringMenus(dto.getRole().getPermittedMenu()))
				.permittedMenu(dto.getRole().getPermittedMenu())
				.build();
	}

	public List<String> buildStringMenus(Set<AccessMenuDTO> permittedMenu) {
		List<String> availableMenus = new ArrayList<>();
		for (AccessMenuDTO menu : permittedMenu.stream()
				.filter(m -> m.getMenuCode().toUpperCase().startsWith("MNU"))
				.collect(Collectors.toList())) {
			log.debug("Add Menu : {}, hasChilds:{}", menu.getMenuCode(), !menu.getChilds().isEmpty());
			availableMenus.add(menu.getMenuCode());
			for (AccessMenuDTO child : menu.getChilds().stream()
					.filter(m -> m.getMenuCode().toUpperCase().startsWith("MNU"))
					.collect(Collectors.toList())) {
				log.debug("Add Childs : " + child.getMenuCode());
				availableMenus.add(child.getMenuCode());
			}
		}
		
		return availableMenus;
	}
	
	private void saveLoginHistory(String username, String remark) {
		LoginHistoryDTO dto = LoginHistoryDTO.builder().username(username).activityDate(new Date()).remark(remark).build();
		try {
			loginService.log(dto);
		} catch (Exception e) {
			log.error("Cannot save Login Activity History, cause:{}", e.getMessage(), e);
		}
	}
}
