package ru.linachan.yggdrasil.common.console.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table {

    private final String[] header;
    private final List<String[]> rows = new ArrayList<>();

    public Table(String... columns) {
        header = columns;
    }

    public Table(List<String> columns) {
        header = (String[]) columns.toArray();
    }

    public Table(Map<?, ?> values, String keyHeader, String valueHeader) {
        header = new String[] {
            (keyHeader != null) ? keyHeader : "",
            (valueHeader != null) ? valueHeader : ""
        };

        for (Object key: values.keySet()) {
            addRow((String) key, (String) values.get(key));
        }
    }

    public Table(List<String> values, String fieldHeader) {
        header = new String[] { fieldHeader };

        values.forEach(this::addRow);
    }

    public void addRow(String... values) {
        if (values.length != header.length) {
            throw new IllegalArgumentException("Row size do not match header size");
        }

        rows.add(values);
    }

    public Integer[] getFieldSize() {
        final Integer[] fieldSize = new Integer[header.length];

        for (int i = 0; i < header.length; i++) {
            fieldSize[i] = header[i].length();

            for (String[] row: rows) {
                if (row[i].length() > fieldSize[i]) {
                    fieldSize[i] = row[i].length();
                }
            }
        }

        return fieldSize;
    }

    public String[][] asArray() {
        String[][] array = new String[rows.size() + 1][];

        array[0] = header;

        int arrayIndex = 1;
        for (String[] row: rows) {
            array[arrayIndex] = row;

            arrayIndex++;
        }

        return array;
    }
}
