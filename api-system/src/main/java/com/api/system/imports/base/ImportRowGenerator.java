package com.api.system.imports.base;

/** Generate persistent entities from rows and validation context. */
public interface ImportRowGenerator<R, C, E> {
  E generate(R row, C context, String operator);
}
