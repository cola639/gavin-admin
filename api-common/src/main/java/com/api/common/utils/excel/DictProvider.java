package com.api.common.utils.excel;

public interface DictProvider {
  DictResult getDict(String dictType, String value);
}
