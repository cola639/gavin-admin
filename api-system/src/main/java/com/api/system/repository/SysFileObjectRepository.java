package com.api.system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.api.common.domain.file.SysFileObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysFileObjectRepository extends JpaRepository<SysFileObject, Long> {

  Optional<SysFileObject> findByFileIdAndDeletedFalse(Long fileId);

  List<SysFileObject> findByFileIdInAndDeletedFalse(List<Long> fileIds);

  boolean existsByBucketAndObjectKey(String bucket, String objectKey);

  /** Pending = deleted=false AND etag is null. */
  List<SysFileObject> findByDeletedFalseAndEtagIsNullAndCreatedAtBefore(LocalDateTime time);

  List<SysFileObject> findByDeletedFalseAndEtagIsNotNullOrderByUpdatedAtAsc(Pageable pageable);
}
