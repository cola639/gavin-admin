package com.api.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUser implements UserDetails {

  @Serial private static final long serialVersionUID = 1L;

  /** Unique user ID */
  private Long userId;

  /** Department ID */
  private Long deptId;

  /** Unique session/token identifier */
  private String token;

  /** Login timestamp */
  private Long loginTime;

  /** Expiration timestamp */
  private Long expireTime;

  /** Client IP address */
  private String ipaddr;

  /** Geolocation of login */
  private String loginLocation;

  /** Browser type */
  private String browser;

  /** Operating system */
  private String os;

  /** Set of permissions for the user */
  private Set<String> permissions;

  /** Associated system user details */
  private SysUser user;

  private String loginType; // PASSWORD / GITHUB / GOOGLE / FACEBOOK

  private String oauth2Provider; // github / google / facebook
  private String oauth2UserId; // GitHub user id / Google user id / Facebook user id

  @Override
  @JsonIgnore
  public String getPassword() {
    return user != null ? user.getPassword() : null;
  }

  @Override
  public String getUsername() {
    return user != null ? user.getUserName() : null;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return true;
  }

  /**
   * ✅ IMPORTANT: never return null here. Convert permission strings into Spring Security
   * authorities.
   */
  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (permissions == null || permissions.isEmpty()) {
      return List.of();
    }
    return permissions.stream()
        .filter(p -> p != null && !p.isBlank())
        .map(SimpleGrantedAuthority::new)
        .toList();
  }
}
