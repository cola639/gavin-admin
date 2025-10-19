package com.api.persistence.repository.system;

import com.api.persistence.domain.system.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysPostRepository extends JpaRepository<SysPost, Long> {
  boolean existsByPostName(String postName);

  boolean existsByPostCode(String postCode);
}
