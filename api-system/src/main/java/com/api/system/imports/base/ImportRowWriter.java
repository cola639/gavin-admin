package com.api.system.imports.base;

/** Persist generated entities. */
public interface ImportRowWriter<E> {
  E save(E entity);

  default void saveAll(Iterable<E> entities) {
    for (E entity : entities) {
      save(entity);
    }
  }
}
