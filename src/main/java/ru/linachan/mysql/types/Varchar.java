package ru.linachan.mysql.types;

import ru.linachan.mysql.MySQLType;

public class Varchar implements MySQLType<String> {

    @Override
    public void setValue(String value) {

    }

    @Override
    public String getValue() {
        return null;
    }
}
