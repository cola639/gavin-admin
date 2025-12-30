package com.api.framework.config.upload;

import com.api.common.enums.FileVisibilityEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

  @Valid private Map<String, CategoryRule> categories = new HashMap<>();

  @Data
  public static class CategoryRule {
    @NotBlank private String prefix;

    @NotNull
    @Min(1)
    private Integer maxSizeMb = 50;

    @NotNull private List<String> allowedExtensions = new ArrayList<>();

    @NotNull private FileVisibilityEnum visibility = FileVisibilityEnum.PRIVATE;

    @NotNull
    @Min(60)
    private Integer presignExpirySeconds = 1800;
  }

  public Optional<CategoryRule> findRule(String categoryName) {
    if (categoryName == null) {
      return Optional.empty();
    }
    CategoryRule rule = categories.get(categoryName);
    if (rule != null) {
      return Optional.of(rule);
    }
    return categories.entrySet().stream()
        .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(categoryName))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}
