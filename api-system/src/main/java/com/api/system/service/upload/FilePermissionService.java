package com.api.system.service.upload;

import com.api.common.domain.upload.SysFileObject;
import com.api.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePermissionService {

  public boolean canRead(SysFileObject fileObject, Long userId) {
    if (fileObject == null || userId == null) {
      return false;
    }
    return fileObject.getOwnerUserId().equals(userId) || isAdmin(userId);
  }

  public boolean canDelete(SysFileObject fileObject, Long userId) {
    if (fileObject == null || userId == null) {
      return false;
    }
    return fileObject.getOwnerUserId().equals(userId) || isAdmin(userId);
  }

  private boolean isAdmin(Long userId) {
    try {
      return SecurityUtils.isAdmin(userId);
    } catch (Throwable ignore) {
      return false;
    }
  }
}
