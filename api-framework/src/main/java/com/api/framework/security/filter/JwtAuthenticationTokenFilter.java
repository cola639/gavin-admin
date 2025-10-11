package com.api.framework.security.filter;

import java.io.IOException;

import com.api.common.domain.LoginUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** token filter to validate the token's validity */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
  private final TokenService tokenService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    LoginUser loginUser = tokenService.getLoginUser(request);
    if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
      // Validate and refresh the token
      tokenService.verifyToken(loginUser);

      // Set the authentication in the security context
      // Create an authentication token
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
      // Set details
      authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      // Set the authentication in the security context
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    // Continue the filter chain
    chain.doFilter(request, response);
  }
}
