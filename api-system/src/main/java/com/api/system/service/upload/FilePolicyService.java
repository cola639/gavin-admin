package com.api.system.service.upload;

import java.util.List;
import java.util.Locale;

import com.api.common.enums.upload.FileCategoryEnum;
import com.api.common.enums.upload.FileVisibilityEnum;
import com.api.framework.config.upload.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePolicyService {

  private final FileStorageProperties properties;

  @Value
  public static class Policy {
    String prefix;
    long maxSizeBytes;
    List<String> allowedExtensions;
    FileVisibilityEnum visibility;
    int presignExpirySeconds;
  }

  public Policy getPolicy(FileCategoryEnum category) {
    FileStorageProperties.CategoryRule rule =
        properties
            .findRule(category.name())
            .orElseThrow(
                () -> new IllegalArgumentException("Missing category rule: " + category.name()));

    return new Policy(
        normalizePrefix(rule.getPrefix()),
        rule.getMaxSizeMb() * 1024L * 1024L,
        rule.getAllowedExtensions().stream().map(s -> s.toLowerCase(Locale.ROOT).trim()).toList(),
        rule.getVisibility(),
        rule.getPresignExpirySeconds());
  }

  private String normalizePrefix(String prefix) {
    String p = prefix == null ? "" : prefix.trim();
    while (p.startsWith("/")) {
      p = p.substring(1);
    }
    while (p.endsWith("/")) {
      p = p.substring(0, p.length() - 1);
    }
    return p;
  }
}
