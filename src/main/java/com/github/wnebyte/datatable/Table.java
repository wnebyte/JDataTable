package com.github.wnebyte.datatable;

import java.util.*;
import com.github.wnebyte.datatable.util.Strings;

public class Table<T> extends AbstractTable<T> {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    static String charSequence(char c, int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    static String whitespace(int len) {
        return charSequence(' ', len);
    }

    static <T> void fill(List<? super T> list, T t, int len) {
        list.clear();
        for (int i = 0; i < len; i++) {
            list.add(t);
        }
    }

    static void pad(List<Cell> c, int len) {
        for (int i = 0; i < len; i++) {
            c.add(new Cell(Strings.EMPTY));
        }
    }

    public static class Cell {

        public static final Alignment DEFAULT_ALIGNMENT = Alignment.LEFT;

        private final String text;

        private final Alignment alignment;

        public Cell(String text) {
            this(text, DEFAULT_ALIGNMENT);
        }

        public Cell(String text, Alignment alignment) {
            this.alignment = alignment;
            if (alignment == Alignment.CENTER) {
                this.text = (text.length() % 2 == 0) ? text : Strings.WHITESPACE.concat(text);
            } else {
                this.text = text;
            }
        }

        int getLength() {
            int len = text.length();
            return (len % 2 == 0) ? len : len + 1;
        }

        int getLeftPadding(int max) {
            switch (alignment) {
                case LEFT:
                    return 0;
                case RIGHT:
                    return max - text.length();
                case CENTER:
                    return (max - text.length()) >> 1;
            }

            return 0;
        }

        int getRightPadding(int max) {
            switch (alignment) {
                case LEFT:
                    return max - text.length();
                case RIGHT:
                    return 0;
                case CENTER:
                    return (max - text.length()) >> 1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Cell[text: %s, alignment: %s]", text, alignment
            );
        }
    }

    public static class Column<T> {

        public static final Alignment DEFAULT_ALIGNMENT = Alignment.LEFT;

        public static final int DEFAULT_MINIMUM_SIZE = 0;

        private final Formatter<T> fun;

        private final Alignment alignment;

        private int minimumSize;

        public Column(Formatter<T> fun) {
            this(fun, DEFAULT_ALIGNMENT);
        }

        public Column(Formatter<T> fun, Alignment alignment) {
            this.fun = fun;
            this.alignment = alignment;
            this.minimumSize = DEFAULT_MINIMUM_SIZE;
        }

        @Override
        public String toString() {
            return String.format(
                    "Column[fun: %s, alignment: %s, minimumSize: %d]", fun, alignment, minimumSize
            );
        }
    }

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    public char SEPARATOR_CHAR = '|';

    public char OUTLINE_CHAR = '-';

    public char CORNER_CHAR = '+';

    private final List<Cell> headers;

    private final List<Column<T>> columns;

    private final List<T> rows;

    private final List<String> content;

    private Cell[][] cells;

    private boolean autoGrowColumnSize = false;

    private int[] maxCols;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Table() {
        this.headers = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.content = new ArrayList<>();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    private Cell[][] populateCells() {
        Cell[][] cells = new Cell[rows.size() + (headers.isEmpty() ? 0 : 1)][columns.size()];

        // transf headers
        if (!headers.isEmpty()) {
            if (headers.size() < columns.size()) {
                pad(headers, columns.size() - headers.size());
            }
            // iterate over columns for when columns.size() < headers.size();
            for (int col = 0; col < columns.size(); col++) {
                Cell cell = headers.get(col);
                cells[0][col] = cell;
            }
        }

        // transf data
        for (int row = 0; row < rows.size(); row++) {
            T data = rows.get(row);
            for (int col = 0; col < columns.size(); col++) {
                Column<T> column = columns.get(col);
                cells[row + 1][col] = new Cell(column.fun.apply(data), column.alignment);
            }
        }

        return cells;
    }

    @Override
    public void sync() {
        cells = populateCells();
        content.clear();

        if (autoGrowColumnSize)
            setAutoGrowColumnSize();

        for (Cell[] value : cells) {
            StringBuilder s = new StringBuilder();

            for (int col = 0; col < value.length; col++) {
                Cell cell = value[col];
                int max = getMaxColLength(col);
                s.append(SEPARATOR_CHAR)
                        .append(Strings.WHITESPACE)
                        .append(whitespace(cell.getLeftPadding(max)))
                        .append(cell.text)
                        .append(Strings.WHITESPACE)
                        .append(whitespace(cell.getRightPadding(max)))
                        .append((col == value.length - 1) ? SEPARATOR_CHAR : "");
            }

            content.add(s.toString());
        }

        List<String> box = new ArrayList<>(content.size());
        String str = createCorneredRowSeparator();
        fill(box, str, content.size());
        int j = 0;
        for (String s : box) {
            content.add(j, s);
            j = j + 2;
        }
        content.add(str);
    }

    private String createCorneredRowSeparator() {
        Cell[] value = cells[0];
        StringBuilder s = new StringBuilder();

        for (int col = 0; col < value.length; col++) {
            int max = getMaxColLength(col);
            s.append(CORNER_CHAR)
                    .append(OUTLINE_CHAR)
                    .append(charSequence(OUTLINE_CHAR, max))
                    .append(OUTLINE_CHAR)
                    .append((col == value.length - 1) ? CORNER_CHAR : "");
        }

        return s.toString();
    }

    private int getMaxColLength(int col) {
        int max = 0;

        for (Cell[] value : cells) {
            Cell cell = value[col];
            Column<T> column = columns.get(col);
            int len = Math.max(cell.getLength(), column.minimumSize);
            max = Math.max(len, max);
        }

        return max;
    }

    private int getMaxColLength() {
        int max = 0;

        for (int col = 0; col < cells[0].length; col++) {
            int tmp = getMaxColLength(col);
            max = Math.max(tmp, max);
        }

        return max;
    }

    private void setAutoGrowColumnSize() {
        int max = getMaxColLength();

        for (int i = 0; i < columns.size(); i++) {
            setMinimumColumnSize(i, max);
        }
    }

    @Override
    public void setMinimumColumnSize(int col, int value) {
        if (col <= columns.size() - 1) {
            Column<T> column = columns.get(col);
            if (0 < value) {
                value = (value % 2 == 0) ? value : value + 1;
                column.minimumSize = value;
            }
        }
    }

    @Override
    public void setAutoGrowColumnSize(boolean value) {
        this.autoGrowColumnSize = value;
    }

    @Override
    public void addHeader(String header) {
        headers.add(new Cell(header));
    }

    @Override
    public void addHeader(String header, Alignment alignment) {
        headers.add(new Cell(header, alignment));
    }

    @Override
    public void setHeader(int index, String header) {
        headers.set(index, new Cell(header));
    }

    @Override
    public void setHeader(int index, String header, Alignment alignment) {
        headers.set(index, new Cell(header, alignment));
    }

    @Override
    public void addColumn(Formatter<T> fun) {
        columns.add(new Column<>(fun));
    }

    @Override
    public void addColumn(Formatter<T> fun, Alignment alignment) {
        columns.add(new Column<>(fun, alignment));
    }

    @Override
    public void setColumn(int index, Formatter<T> fun) {
        columns.set(index, new Column<>(fun));
    }

    @Override
    public void setColumn(int index, Formatter<T> fun, Alignment alignment) {
        columns.set(index, new Column<>(fun, alignment));
    }

    @Override
    public void addRow(T data) {
        rows.add(data);
    }

    @Override
    public void setRow(int index, T data) {
        rows.set(index, data);
    }

    @Override
    public void addAllRows(Collection<T> data) {
        rows.addAll(data);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int i = 0;
        for (String str : content) {
            s.append(str);
            if (i++ < content.size() - 1)
                s.append(Strings.LS);
        }
        return s.toString();
    }
}
