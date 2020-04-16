package com.sulistionoadi.ngoprek.common.dto.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@NoArgsConstructor
public class UserLogin implements UserDetails {
	
	private static final long serialVersionUID = 1350259750744690616L;

	private String username;
	private String password;
	private Boolean enabled;
	private String rolename;
	private List<String> menus;
	private Set<AccessMenuDTO> permittedMenu = new HashSet<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
        for (AccessMenuDTO am : this.permittedMenu) {
            authorities.add(new SimpleGrantedAuthority(am.getMenuCode()));
            for (AccessMenuDTO ac : am.getChilds()) {
            	authorities.add(new SimpleGrantedAuthority(ac.getMenuCode()));
            }
        }
        return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.enabled;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.enabled;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Builder
	public UserLogin(String username, String password, Boolean enabled, String rolename,
			List<String> menus, Set<AccessMenuDTO> permittedMenu) {
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.rolename = rolename;
		this.menus = menus;
		this.permittedMenu = permittedMenu;
	}
	
	public AccessMenuDTO findAccessMenuByCode(String code) {
		for (AccessMenuDTO menu : this.permittedMenu.stream()
				.filter(m -> m.getMenuCode().toUpperCase().startsWith("MNU"))
				.collect(Collectors.toList())) {
			
			if(menu.getMenuCode().equals(code)) {
				return menu;
			} else {
				for (AccessMenuDTO child : menu.getChilds().stream()
						.filter(m -> m.getMenuCode().toUpperCase().startsWith("MNU"))
						.collect(Collectors.toList())) {
					if(child.getMenuCode().equals(code)) {
						return child;
					}
				}
			}
		}
		
		return null;
	}

}
