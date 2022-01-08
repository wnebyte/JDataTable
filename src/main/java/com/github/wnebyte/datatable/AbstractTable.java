package com.github.wnebyte.datatable;

import java.util.Collection;

public abstract class AbstractTable<T> {

    public abstract void addHeader(String header);

    public abstract void addHeader(String header, Alignment alignment);

    public abstract void setHeader(int index, String header);

    public abstract void setHeader(int index, String header, Alignment alignment);

    public abstract void addColumn(Formatter<T> fun);

    public abstract void addColumn(Formatter<T> fun, Alignment alignment);

    public abstract void addColumn(Formatter<T> fun, int minimumSize);

    public abstract void addColumn(Formatter<T> fun, Alignment alignment, int minimumSize);

    public abstract void setColumn(int index, Formatter<T> fun);

    public abstract void setColumn(int index, Formatter<T> fun, Alignment alignment);

    public abstract void setColumn(int index, Formatter<T> fun, int minimumSize);

    public abstract void setColumn(int index, Formatter<T> fun, Alignment alignment, int minimumSize);

    public abstract void addRow(T data);

    public abstract void setRow(int index, T data);

    public abstract void addAllRows(Collection<T> data);

    public abstract void setAutoGrowColumnSize(boolean value);

    public abstract void setMinimumColumnSize(int size);

    public abstract void setMinimumColumnSize(int index, int size);

    public abstract void sync();
}
