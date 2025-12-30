package com.api.system.service;

import com.api.common.constant.UserConstants;
import com.api.common.utils.StringUtils;
import com.api.framework.exception.ServiceException;
import com.api.system.domain.system.SysPost;
import com.api.system.domain.system.SysPostVO;
import com.api.system.repository.SysPostRepository;
import com.api.system.repository.SysUserPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for managing system posts.
 *
 * <p>Uses JPA for SysPost and keeps SysUserPostMapper for usage count checks.
 */
@Slf4j
@Service
public class SysPostService {

  private final SysPostRepository postRepository;
  private final SysUserPostRepository userPostRepository;

  @Autowired
  public SysPostService(
      SysPostRepository postRepository, SysUserPostRepository userPostRepository) {
    this.postRepository = postRepository;
    this.userPostRepository = userPostRepository;
  }

  /** Query post list with simple filter (code/name/status). */
  public List<SysPost> selectPostList(SysPost post) {
    if (post == null) {
      return postRepository.findAll();
    }

    // ExampleMatcher: like for name/code, exact for status
    ExampleMatcher matcher =
        ExampleMatcher.matching()
            .withIgnoreNullValues()
            .withMatcher("postName", ExampleMatcher.GenericPropertyMatchers.contains())
            .withMatcher("postCode", ExampleMatcher.GenericPropertyMatchers.contains());

    Example<SysPost> example = Example.of(post, matcher);
    return postRepository.findAll(example);
  }

  /** Get all posts (for dropdowns). */
  public List<SysPostVO> getAllPosts() {
    return SysPostVO.fromEntities(postRepository.findAll());
  }

  /** Get post by ID. */
  public SysPost selectPostById(Long postId) {
    return postRepository
        .findById(postId)
        .orElseThrow(() -> new ServiceException("Post not found: " + postId));
  }

  /** Get post ID list for a user. */
  public List<Long> selectPostListByUserId(Long userId) {
    return postRepository.findPostIdsByUserId(userId);
  }

  /** Check if post name is unique. */
  public boolean checkPostNameUnique(SysPost post) {
    Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();

    return postRepository
        .findByPostName(post.getPostName())
        .map(existing -> existing.getPostId().longValue() == postId)
        .orElse(UserConstants.UNIQUE);
  }

  /** Check if post code is unique. */
  public boolean checkPostCodeUnique(SysPost post) {
    Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();

    return postRepository
        .findByPostCode(post.getPostCode())
        .map(existing -> existing.getPostId().longValue() == postId)
        .orElse(UserConstants.UNIQUE);
  }

  /** Count how many users are bound to this post. */
  public int countUserPostById(Long postId) {
    return userPostRepository.countByPost_PostId(postId);
  }

  /** Delete a single post. */
  public int deletePostById(Long postId) {
    if (countUserPostById(postId) > 0) {
      SysPost post = selectPostById(postId);
      throw new ServiceException(
          post.getPostName() + " is assigned to users and cannot be deleted");
    }
    postRepository.deleteById(postId);
    return 1;
  }

  /** Batch delete posts. */
  public int deletePostByIds(Long[] postIds) {
    for (Long postId : postIds) {
      if (countUserPostById(postId) > 0) {
        SysPost post = selectPostById(postId);
        throw new ServiceException(
            post.getPostName() + " is assigned to users and cannot be deleted");
      }
    }
    postRepository.deleteAllById(List.of(postIds));
    return postIds.length;
  }

  /** Create a post. */
  public int insertPost(SysPost post) {
    postRepository.save(post);
    return 1;
  }

  /** Update a post. */
  public int updatePost(SysPost post) {
    postRepository.save(post);
    return 1;
  }
}
