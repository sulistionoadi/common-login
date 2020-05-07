package com.sulistionoadi.ngoprek.common.login.provider;

import java.util.ArrayList;
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
import com.sulistionoadi.ngoprek.common.dto.security.MasterUserDTO;
import com.sulistionoadi.ngoprek.common.dto.security.UserLogin;
import com.sulistionoadi.ngoprek.common.login.service.MasterUserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	private PasswordEncoder encoder;
	private MasterUserService userService;
	
	@Autowired
	public CustomAuthenticationProvider(PasswordEncoder encoder, MasterUserService userService) {
		this.encoder = encoder;
		this.userService = userService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		String rawpass = authentication.getCredentials().toString();
		//String encpass = encoder.encode(rawpass);
		
		try {
			Optional<MasterUserDTO> userOp = userService.findByUsernameFetchEager(name);
			if (!userOp.isPresent()) {
				throw new UsernameNotFoundException("Invalid Username/Password");
			} else if(!userOp.get().getIsActive()) {
				throw new BadCredentialsException("User is not active");
			} else {
				log.debug("Checking Password [{} == {}]", rawpass, userOp.get().getPassword());
				if(!encoder.matches(rawpass, userOp.get().getPassword())) {
					log.warn("Invalid password for username {}", name);
					throw new UsernameNotFoundException("Invalid Username/Password");
				}
				
				UserLogin userObj = getUserLoginInfo(userOp.get());
				return new UsernamePasswordAuthenticationToken(userObj, userObj.getPassword(), userObj.getAuthorities());
			}
		} catch (Exception e) {
			log.error("Authentication failure, cause:{}", e.getMessage(), e);
			throw new BadCredentialsException(e.getMessage());
		}
		
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
	private UserLogin getUserLoginInfo(MasterUserDTO dto) throws Exception {
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
}
