package com.api.persistence.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents an authenticated user within the system. Implements Spring Security's {@link
 * UserDetails} interface to integrate with the authentication framework.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUser implements UserDetails {

  private static final long serialVersionUID = 1L;

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

  @Override
  @JsonIgnore
  public String getPassword() {
    return user != null ? user.getPassword() : null;
  }

  @Override
  public String getUsername() {
    return user != null ? user.getUserName() : null;
  }

  /**
   * Account validity checks. By default, these always return true. They can be customized for
   * advanced security.
   */
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
   * Converts user permissions into Spring Security authorities. Can be enhanced to map Set<String>
   * → SimpleGrantedAuthority.
   */
  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null; // TODO: Map permissions to authorities if needed
  }
}
