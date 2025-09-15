package com.api.system.repository;

import com.api.system.domain.SysPost;
import com.api.system.domain.SysUserPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysPostRepository extends JpaRepository<SysPost, Long> {
    boolean existsByPostName(String postName);

    boolean existsByPostCode(String postCode);
}

