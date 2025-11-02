package com.work.IGA.Configuration;

import com.work.IGA.Models.Users.UserSchema;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
     private final UUID id;
     private final String email;
     private final String password;
     private final List<GrantedAuthority> authorities;

    

     public  static UserDetailsImpl build(UserSchema user) {
         // map  single role to  authority;
         String roleName = user.getRole().name();
         return new UserDetailsImpl(
                 user.getId(),
                 user.getEmail(),
                 user.getPassword(),
                 List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
         );
     }


    public UUID getId(){
         return id;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return  true;
    }
}
