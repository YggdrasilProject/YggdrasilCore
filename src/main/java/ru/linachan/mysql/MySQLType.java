package ru.linachan.mysql;

public interface MySQLType<T> {

    void setValue(String value);

    T getValue();
}
