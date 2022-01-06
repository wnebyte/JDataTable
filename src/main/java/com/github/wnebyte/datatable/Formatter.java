package com.github.wnebyte.datatable;

import java.util.function.Function;

@FunctionalInterface
public interface Formatter<T> extends Function<T, String> {
}
