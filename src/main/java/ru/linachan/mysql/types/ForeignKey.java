package ru.linachan.mysql.types;

import ru.linachan.mysql.MySQLType;

public class ForeignKey<T extends MySQLType<?>> implements MySQLType<Integer> {

    @Override
    public void setValue(String value) {

    }

    @Override
    public Integer getValue() {
        return null;
    }
}
