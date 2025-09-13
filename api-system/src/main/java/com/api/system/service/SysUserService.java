package com.api.system.service;

import com.api.common.domain.entity.SysUser;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserRepository userRepository;

    public Page<SysUser> selectUserList(SysUser user, Map<String, Object> params, Pageable pageable) {
        Specification<SysUser> spec = SpecificationBuilder.<SysUser>builder()
                .eq("delFlag", "0")
                .eq("userId", user.getUserId())
                .like("userName", user.getUserName())
                .eq("status", user.getStatus())
                .like("phonenumber", user.getPhonenumber())
                .between("createTime", (Date) params.get("beginTime"), (Date) params.get("endTime"))
                .build();

        return userRepository.findAll(spec, pageable);
    }

    public SysUser selectUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public SysUser selectUserByUserName(String userName) {
        return userRepository.findByUserNameAndDelFlag(userName, "0").orElse(null);
    }

    public SysUser insertUser(SysUser user) {
        return userRepository.save(user);
    }

    public SysUser updateUser(SysUser user) {
        return userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}
