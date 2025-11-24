package com.api.persistence.domain.system;

import com.api.common.domain.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SysPostVO extends BaseEntity {
  private Long postId;
  private String postCode;
  private String postName;
  private Integer postSort;
  private String status;

  // ----------------------------------------------------------------------
  // Static factory methods: Entity -> VO
  // ----------------------------------------------------------------------

  /** Convert a SysPost entity to SysPostVO. */
  public static SysPostVO fromEntity(SysPost entity) {
    if (entity == null) {
      return null;
    }

    return SysPostVO.builder()
        .postId(entity.getPostId())
        .postCode(entity.getPostCode())
        .postName(entity.getPostName())
        .postSort(entity.getPostSort())
        .status(entity.getStatus())
        .build();
  }

  /** Convert a list of SysPost entities to a list of SysPostVO. */
  public static List<SysPostVO> fromEntities(List<SysPost> entities) {
    if (entities == null) {
      return List.of();
    }
    return entities.stream().map(SysPostVO::fromEntity).toList();
  }
}
