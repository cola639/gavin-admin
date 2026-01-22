package com.api.system.imports.user;

import com.api.common.domain.SysUser;
import com.api.system.imports.base.ImportRowWriter;
import com.api.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persist validated users. */
@Component
@RequiredArgsConstructor
public class UserImportWriter implements ImportRowWriter<SysUser> {

  private final SysUserRepository userRepository;

  @Override
  public SysUser save(SysUser user) {
    return userRepository.save(user);
  }

  @Override
  public void saveAll(Iterable<SysUser> users) {
    userRepository.saveAll(users);
  }
}
