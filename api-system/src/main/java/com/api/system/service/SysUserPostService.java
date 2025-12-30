package com.api.system.service;

import com.api.common.domain.SysUser;
import com.api.system.domain.system.SysPost;
import com.api.system.domain.system.SysUserPost;
import com.api.system.repository.SysUserPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Service for managing user-post associations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserPostService {

  private final SysUserPostRepository repository;

  /**
   * Insert user-post associations.
   *
   * @param user user entity containing postIds
   */
  public void insertUserPost(SysUser user) {
    Long[] posts = user.getPostIds();
    if (posts != null && posts.length > 0) {
      List<SysUserPost> list = new ArrayList<>(posts.length);
      for (Long postId : posts) {
        SysUserPost up =
            SysUserPost.builder()
                .user(SysUser.builder().userId(user.getUserId()).build())
                .post(SysPost.builder().postId(postId).build())
                .build();
        list.add(up);
      }
      repository.saveAll(list);
      log.info("Inserted {} user-post associations for userId={}", list.size(), user.getUserId());
    } else {
      log.info("No posts to insert for userId={}", user.getUserId());
    }
  }

  public void deleteByUserId(Long userId) {
    log.info("Deleting user-post associations by userId: {}", userId);
    repository.deleteByUser_UserId(userId);
  }

  public void deleteByUser_UserIdIn(List<Long> ids) {
    log.info("Deleting user-post associations by userIds: {}", ids);
    repository.deleteByUser_UserIdIn(ids);
  }
}
