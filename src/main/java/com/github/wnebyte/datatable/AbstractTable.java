package com.github.wnebyte.datatable;

import java.util.Collection;

public abstract class AbstractTable<T> {

    public abstract static class Cell {

        protected abstract int getLeftPadding(int max);

        protected abstract int getRightPadding(int max);
    }

    public abstract void addHeader(String header);

    public abstract void addHeader(String header, Alignment alignment);

    public abstract void setHeader(int index, String header);

    public abstract void setHeader(int index, String header, Alignment alignment);

    public abstract void addColumn(Formatter<T> fun);

    public abstract void addColumn(Formatter<T> fun, Alignment alignment);

    public abstract void setColumn(int index, Formatter<T> fun);

    public abstract void setColumn(int index, Formatter<T> fun, Alignment alignment);

    public abstract void addRow(T data);

    public abstract void setRow(int index, T data);

    public abstract void addAllRows(Collection<T> data);

    public abstract void setAutoGrowColumnSize(boolean value);

    public abstract void setMinimumColumnSize(int col, int value);

    public abstract void sync();
}
