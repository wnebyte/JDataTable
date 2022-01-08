package com.github.wnebyte.datatable;

import java.util.*;
import com.github.wnebyte.datatable.util.Strings;

public class Table<T> extends AbstractTable<T> {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    static String charSequenceOf(char c, int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    static String whitespace(int len) {
        return charSequenceOf(' ', len);
    }

    static <T> void fill(List<? super T> c, T t, int len) {
        c.clear();
        for (int i = 0; i < len; i++) {
            c.add(t);
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
            this(fun, alignment, DEFAULT_MINIMUM_SIZE);
        }

        public Column(Formatter<T> fun, int minimumSize) {
            this(fun, DEFAULT_ALIGNMENT, minimumSize);
        }

        public Column(Formatter<T> fun, Alignment alignment, int minimumSize) {
            this.fun = fun;
            this.alignment = alignment;
            setMinimumSize(minimumSize);
        }

        void setMinimumSize(int size) {
            if (0 < size) {
                this.minimumSize = (size % 2 == 0) ? size : size + 1;
            } else {
                this.minimumSize = 0;
            }
        }

        int getMinimumSize() {
            return minimumSize;
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
    #      STATIC FIELDS      #
    ###########################
    */

    public static final char DEFAULT_SEPARATOR_CHAR = '|';

    public static final char DEFAULT_BOX_CHAR = '-';

    public static final char DEFAULT_CORNER_CHAR = '+';

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final List<Cell> headers;

    private final List<Column<T>> columns;

    private final List<T> rows;

    private final List<String> content;

    private Cell[][] cells;

    private int[] maxCols;

    private boolean autoGrowColumnSize;

    private char separatorChar;

    private char boxChar;

    private char cornerChar;

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
        this.autoGrowColumnSize = false;
        this.separatorChar = DEFAULT_SEPARATOR_CHAR;
        this.boxChar = DEFAULT_BOX_CHAR;
        this.cornerChar = DEFAULT_CORNER_CHAR;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    private Cell[][] populateCells() {
        Cell[][] cells = new Cell[rows.size() + (headers.isEmpty() ? 0 : 1)][columns.size()];

        // transform headers
        if (!headers.isEmpty()) {
            if (headers.size() < columns.size()) {
                pad(headers, columns.size() - headers.size());
            }
            // iterate over columns for when there are more headers than columns
            for (int col = 0; col < columns.size(); col++) {
                Cell cell = headers.get(col);
                cells[0][col] = cell;
            }
        }

        // transform data
        for (int row = 0; row < rows.size(); row++) {
            T data = rows.get(row);
            for (int col = 0; col < columns.size(); col++) {
                Column<T> column = columns.get(col);
                cells[row + 1][col] = new Cell(column.fun.apply(data), column.alignment);
            }
        }

        return cells;
    }

    /**
     * @return an array of ints consisting of each Columns largest Cell.
     */
    private int[] populateMaxCols() {
        int[] maxCols = new int[columns.size()];
        for (int col = 0; col < maxCols.length; col++) {
            maxCols[col] = getMaxColLength(col);
        }
        return maxCols;
    }

    @Override
    public void sync() {
        cells = populateCells();
        content.clear();

        if (autoGrowColumnSize)
            setAutoGrowColumnSize();

        maxCols = populateMaxCols();

        for (Cell[] value : cells) {
            StringBuilder s = new StringBuilder();

            for (int col = 0; col < value.length; col++) {
                Cell cell = value[col];
                int max = maxCols[col];
                s.append(separatorChar)
                        .append(Strings.WHITESPACE)
                        .append(whitespace(cell.getLeftPadding(max)))
                        .append(cell.text)
                        .append(Strings.WHITESPACE)
                        .append(whitespace(cell.getRightPadding(max)))
                        .append((col == value.length - 1) ? separatorChar : Strings.EMPTY);
            }

            content.add(s.toString());
        }

        List<String> box = new ArrayList<>(content.size());
        String str = mkSeparator();
        fill(box, str, content.size());
        int j = 0;
        for (String s : box) {
            content.add(j, s);
            j = j + 2;
        }
        content.add(str);
    }

    private String mkSeparator() {
        Cell[] value = cells[0];
        StringBuilder s = new StringBuilder();

        for (int col = 0; col < value.length; col++) {
            int max = maxCols[col];
            s.append(cornerChar)
                    .append(boxChar)
                    .append(charSequenceOf(boxChar, max))
                    .append(boxChar)
                    .append((col == value.length - 1) ? cornerChar : Strings.EMPTY);
        }

        return s.toString();
    }

    private int getMaxColLength(int col) {
        int max = 0;

        for (Cell[] value : cells) {
            Cell cell = value[col];
            Column<T> column = columns.get(col);
            int len = Math.max(cell.getLength(), column.getMinimumSize());
            max = Math.max(len, max);
        }

        return max;
    }

    /**
     * @return the largest Cell/Column size.
     */
    private int getMaxColLength() {
        int max = 0;

        for (int col = 0; col < cells[0].length; col++) {
            int tmp = getMaxColLength(col);
            max = Math.max(tmp, max);
        }

        return max;
    }

    /**
     * Sets the minimum size for each Column to the size of the largest Column.
     */
    private void setAutoGrowColumnSize() {
        int max = getMaxColLength();

        for (int i = 0; i < columns.size(); i++) {
            setMinimumColumnSize(i, max);
        }
    }

    /**
     * Specify whether the size for each Column should grow to the size of the largest Column.
     * @param value the desired value.
     */
    @Override
    public void setAutoGrowColumnSize(boolean value) {
        this.autoGrowColumnSize = value;
    }

    /**
     * Sets the <code>minimumSize</code> for the Column at the specified <code>index</code> to the specified
     * <code>size</code>.
     * @param index the index of the Column.
     * @param size the desired size.
     */
    @Override
    public void setMinimumColumnSize(int index, int size) {
        if (index <= columns.size() - 1) {
            Column<T> column = columns.get(index);
            column.setMinimumSize(size);
        }
    }

    /**
     * Sets the <code>minimumSize</code> for all existing Columns to the specified <code>size</code>.
     * @param size the desired size.
     */
    @Override
    public void setMinimumColumnSize(int size) {
        for (int i = 0; i < columns.size(); i++) {
            setMinimumColumnSize(i, size);
        }
    }

    @Override
    public void addHeader(String header) {
        headers.add(new Cell(header));
    }

    @Override
    public void addHeader(String header, Alignment alignment) {
        headers.add(new Cell(header, alignment));
    }

    public void addHeader(Cell header) {
        headers.add(header);
    }

    @Override
    public void setHeader(int index, String header) {
        headers.set(index, new Cell(header));
    }

    @Override
    public void setHeader(int index, String header, Alignment alignment) {
        headers.set(index, new Cell(header, alignment));
    }

    public void setHeader(int index, Cell header) {
        headers.set(index, header);
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
    public void addColumn(Formatter<T> fun, int minimumSize) {
        columns.add(new Column<>(fun, minimumSize));
    }

    @Override
    public void addColumn(Formatter<T> fun, Alignment alignment, int minimumSize) {
        columns.add(new Column<T>(fun, alignment, minimumSize));
    }

    public void addColumn(Column<T> column) {
        columns.add(column);
    }

    @Override
    public void setColumn(int index, Formatter<T> fun) {
        columns.set(index, new Column<T>(fun));
    }

    @Override
    public void setColumn(int index, Formatter<T> fun, Alignment alignment) {
        columns.set(index, new Column<T>(fun, alignment));
    }

    @Override
    public void setColumn(int index, Formatter<T> fun, int minimumSize) {
        columns.set(index, new Column<T>(fun, minimumSize));
    }

    @Override
    public void setColumn(int index, Formatter<T> fun, Alignment alignment, int minimumSize) {
        columns.set(index, new Column<T>(fun, alignment, minimumSize));
    }

    public void setColumn(int index, Column<T> column) {
        columns.set(index, column);
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

    public void setCornerChar(char c) {
        this.cornerChar = c;
    }

    public void setSeparatorChar(char c) {
        this.separatorChar = c;
    }

    public void setBoxChar(char c) {
        this.boxChar = c;
    }

    public List<String> getContent() {
        return new ArrayList<>(content);
    }

    public List<Cell> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    public List<Column<T>> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<T> getRows() {
        return Collections.unmodifiableList(rows);
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
